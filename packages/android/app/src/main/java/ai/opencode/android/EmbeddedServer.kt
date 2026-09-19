package ai.opencode.android

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

/**
 * Runs the opencode server bundled inside the APK.
 *
 * The server is the unmodified upstream `opencode serve` entrypoint, compiled
 * for `bun-linux-arm64-android` and shipped as `libopencode.so` in jniLibs.
 * Android extracts jniLibs into `nativeLibraryDir`, which is the only
 * application-writable location where modern Android still permits `execve`
 * (app data files are denied by SELinux on targetSdk 29+), so the executable
 * lives there and the app spawns it as a child process.
 *
 * The server binds loopback only and serves the production bundle of
 * `packages/app` embedded in the binary at `http://127.0.0.1:<port>/`.
 */
class EmbeddedServer(private val context: Context) {
    private var process: Process? = null
    private var port: Int = 0

    private val usrDir: File get() = canonical(File(context.filesDir, "usr"))

    /**
     * `/data/user/0/...` and `/data/data/...` are the same directory, but the
     * UI stores whichever string it saw first. Canonicalizing every host path
     * keeps project records, session directories and the directory picker on
     * one representation, otherwise the session history appears empty.
     */
    private fun canonical(file: File): File = runCatching { file.canonicalFile }.getOrDefault(file)

    fun running(): Boolean = process?.isAlive == true

    fun binary(): File = File(context.applicationInfo.nativeLibraryDir, "libopencode.so")

    fun nativeLibraryDir(): File = File(context.applicationInfo.nativeLibraryDir)

    fun home(): File = canonical(File(context.filesDir, "home").apply { mkdirs() })

    fun binDir(): File = canonical(File(context.filesDir, "bin").apply { mkdirs() })

    // Lives under HOME so the web UI's project picker (which searches from
    // HOME) can find it without native directory APIs.
    fun workspaceDir(): File = canonical(File(home(), "workspace").apply { mkdirs() })

    fun logFile(): File = canonical(File(home(), "server.log"))

    /** Tools shipped next to the server binary, exposed on PATH through symlinks. */
    private val tools =
        mapOf(
            "rg" to "librg.so",
            "git" to "libgit.so",
            "busybox" to "libbusybox.so",
            // A bare Bun runtime lets the agent execute JS/TS files, and the
            // `node` alias covers the common `node script.js` invocation.
            "bun" to "libbun.so",
            "node" to "libbun.so",
        )

    fun start(): Int {
        check(!running()) { "server already running" }
        val binary = binary()
        check(binary.isFile) { "server binary missing from APK: ${binary.absolutePath}" }

        prepareFilesystem()
        prepareTools()
        installGitRuntime()
        stopStaleServers()

        port = findFreePort()
        val builder =
            ProcessBuilder(
                binary.absolutePath,
                "serve",
                "--hostname=127.0.0.1",
                "--port=$port",
            )
        builder.directory(workspaceDir())
        builder.environment().putAll(environment())
        builder.redirectErrorStream(true)
        builder.redirectOutput(logFile())

        Log.i(TAG, "starting server on 127.0.0.1:$port")
        process = builder.start()
        awaitHealthy()
        return port
    }

    fun stop() {
        val child = process ?: return
        process = null
        child.destroy()
        if (!child.waitFor(5, TimeUnit.SECONDS)) child.destroyForcibly()
    }

    private fun prepareFilesystem() {
        val home = home()
        listOf(
            "tmp",
            ".config/opencode",
            ".local/share",
            ".local/state",
            ".cache",
        ).forEach { File(home, it).mkdirs() }

        val config = File(home, ".config/opencode/opencode.json")
        if (!config.isFile) {
            config.writeText(
                """
                {
                  "${'$'}schema": "https://opencode.ai/config.json",
                  "shell": "/system/bin/sh"
                }
                """.trimIndent() + "\n",
            )
        }
    }

    /**
     * Creates `<files>/bin` symlinks that point at executables stored in
     * nativeLibraryDir. Executing through the symlink is allowed because the
     * final inode lives in the app's native library directory.
     */
    private fun prepareTools() {
        val bin = binDir()
        bin.listFiles()?.forEach { it.delete() }
        for ((name, library) in tools) {
            val target = File(nativeLibraryDir(), library)
            if (!target.isFile) continue
            val link = File(bin, name)
            runCatching { android.system.Os.symlink(target.absolutePath, link.absolutePath) }
                .onFailure { Log.w(TAG, "symlink failed for $name: ${it.message}") }
        }
    }

    /**
     * Force-stopping the app can leave the previous server child alive (it
     * keeps the default port and makes the UI origin change on next launch,
     * which loses client-side project state). Kill any same-uid server child
     * before starting a new one.
     */
    private fun stopStaleServers() {
        val myUid = android.os.Process.myUid()
        val myPid = android.os.Process.myPid()
        File("/proc").listFiles()?.forEach { dir ->
            val pid = dir.name.toIntOrNull() ?: return@forEach
            if (pid == myPid) return@forEach
            val cmdline = runCatching { File(dir, "cmdline").readText() }.getOrNull() ?: return@forEach
            if (!cmdline.contains("libopencode.so")) return@forEach
            val uid =
                runCatching {
                    File(dir, "status")
                        .readLines()
                        .firstOrNull { it.startsWith("Uid:") }
                        ?.split(Regex("\\s+"))
                        ?.getOrNull(1)
                        ?.toIntOrNull()
                }.getOrNull()
            if (uid != myUid) return@forEach
            Log.i(TAG, "stopping stale server process $pid")
            runCatching { android.os.Process.killProcess(pid) }
        }
        Thread.sleep(200)
    }

    /**
     * Installs the bundled git runtime (Termux aarch64 packages, shipped as
     * `assets/git-runtime.zip`) into app-private storage.
     *
     * Android denies `execve` on app data, so the host never runs these ELF
     * files directly: `files/bin/git` is a shell wrapper that starts them via
     * `/system/bin/linker64`, and every ELF helper in `libexec/git-core` is
     * renamed to `<name>.bin` behind a matching wrapper script.
     */
    private fun installGitRuntime() {
        val usr = File(context.filesDir, "usr")
        val marker = File(usr, ".runtime-version")
        val current = marker.isFile && marker.readText().trim() == GIT_RUNTIME_VERSION

        if (!current) {
            usr.deleteRecursively()
            usr.mkdirs()
            context.assets.open("git-runtime.zip").use { raw ->
                ZipInputStream(BufferedInputStream(raw)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val out = File(usr, entry.name)
                        if (entry.isDirectory) {
                            out.mkdirs()
                            continue
                        }
                        out.parentFile?.mkdirs()
                        FileOutputStream(out).use { zip.copyTo(it) }
                        zip.closeEntry()
                    }
                }
            }
            marker.writeText(GIT_RUNTIME_VERSION)
            writeGitConfig()
            Log.i(TAG, "installed git runtime $GIT_RUNTIME_VERSION")
        }

        // Git helpers are ELF binaries; app data cannot be exec'd, so the two
        // remote helpers are symlinked to their nativeLibraryDir copies.
        linkHelper(usr, "git-remote-http", "libgit-remote-http.so")
        linkHelper(usr, "git-remote-https", "libgit-remote-http.so")
    }

    private fun linkHelper(usr: File, name: String, library: String) {
        val target = File(nativeLibraryDir(), library)
        if (!target.isFile) return
        val link = File(usr, "libexec/git-core/$name")
        link.delete()
        runCatching { android.system.Os.symlink(target.absolutePath, link.absolutePath) }
            .onFailure { Log.w(TAG, "helper symlink failed for $name: ${it.message}") }
    }

    private fun writeGitConfig() {
        val config = File(home(), ".gitconfig")
        if (config.isFile) return
        config.writeText(
            """
            [user]
            	name = OpenCode
            	email = opencode@localhost
            """.trimIndent() + "\n",
        )
    }

    private fun environment(): Map<String, String> {
        val home = home()
        return mapOf(
            "HOME" to home.absolutePath,
            "TMPDIR" to File(home, "tmp").absolutePath,
            "SHELL" to SYSTEM_SHELL,
            "PATH" to "${nativeLibraryDir().absolutePath}:${binDir().absolutePath}:/system/bin:/system/xbin",
            // The bundled git resolves its shared libraries from the native
            // library directory plus the extracted Termux runtime.
            "LD_LIBRARY_PATH" to "${nativeLibraryDir().absolutePath}:${File(usrDir, "lib").absolutePath}",
            "GIT_EXEC_PATH" to File(usrDir, "libexec/git-core").absolutePath,
            "GIT_TEMPLATE_DIR" to File(usrDir, "share/git-core/templates").absolutePath,
            "GIT_SSL_CAINFO" to File(usrDir, "etc/ssl/certs/ca-bundle.crt").absolutePath,
            "GIT_CONFIG_NOSYSTEM" to "1",
            "GIT_ATTR_NOSYSTEM" to "1",
            "XDG_CONFIG_HOME" to File(home, ".config").absolutePath,
            "XDG_DATA_HOME" to File(home, ".local/share").absolutePath,
            "XDG_CACHE_HOME" to File(home, ".cache").absolutePath,
            "XDG_STATE_HOME" to File(home, ".local/state").absolutePath,
            // The fff native search library has no Android build; the ripgrep
            // fallback is always available (bundled as librg.so).
            "OPENCODE_DISABLE_FFF" to "true",
            "NO_PROXY" to "127.0.0.1,localhost,::1",
            "no_proxy" to "127.0.0.1,localhost,::1",
        )
    }

    private fun awaitHealthy() {
        val deadline = System.currentTimeMillis() + START_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (!running()) throw IllegalStateException("server exited during startup:\n${logTail()}")
            if (healthy()) return
            Thread.sleep(250)
        }
        stop()
        throw IllegalStateException("server did not become healthy in ${START_TIMEOUT_MS}ms:\n${logTail()}")
    }

    private fun healthy(): Boolean =
        try {
            val connection = URL("http://127.0.0.1:$port/api/health").openConnection() as HttpURLConnection
            connection.connectTimeout = 2_000
            connection.readTimeout = 2_000
            try {
                connection.responseCode == 200
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            false
        }

    fun logTail(lines: Int = 60): String =
        try {
            logFile().readLines().takeLast(lines).joinToString("\n")
        } catch (_: Exception) {
            "no server log"
        }

    private fun findFreePort(): Int =
        try {
            ServerSocket(DEFAULT_PORT).use { it.localPort }
        } catch (_: Exception) {
            ServerSocket(0).use { it.localPort }
        }

    companion object {
        private const val TAG = "OpenCodeServer"
        private const val START_TIMEOUT_MS = 90_000L
        private const val SYSTEM_SHELL = "/system/bin/sh"
        private const val DEFAULT_PORT = 4096
        private const val GIT_RUNTIME_VERSION = "termux-git-2.55.0"
    }
}
