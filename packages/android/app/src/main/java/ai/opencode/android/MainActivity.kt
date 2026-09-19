package ai.opencode.android

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Hosts the existing opencode web UI.
 *
 * The activity starts the bundled opencode server (see [EmbeddedServer]) and
 * then loads its origin in a WebView. `packages/app` is embedded in the server
 * binary at build time and served from `http://127.0.0.1:<port>/`, so the
 * frontend is used exactly as shipped - no UI code is duplicated here.
 */
class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var overlay: TextView
    private var server: EmbeddedServer? = null
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var serverOnPrivateStorage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        overlay =
            TextView(this).apply {
                text = getString(R.string.starting)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.BLACK)
                gravity = Gravity.CENTER
            }
        root.addView(
            overlay,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )

        webView = WebView(this)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            // packages/app ships a real mobile viewport; honoring it gives the
            // same responsive layout a phone browser would get.
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
        }
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    Log.i(TAG, "${message.message()} @${message.sourceId()}:${message.lineNumber()}")
                    return true
                }

                override fun onShowFileChooser(
                    view: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = filePathCallback
                    return try {
                        val intent = fileChooserParams.createIntent()
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                        true
                    } catch (error: Exception) {
                        fileChooserCallback = null
                        Log.w(TAG, "file chooser failed: ${error.message}")
                        false
                    }
                }
            }
        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url
                    if (url.host == "127.0.0.1" || url.host == "localhost") return false
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, url)) }
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    injectMobileStyles()
                }
            }
        root.addView(
            webView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        setContentView(root)

        val embedded = EmbeddedServer(applicationContext)
        server = embedded
        requestSharedStorageIfNeeded()
        startServer(embedded)
    }

    private fun sharedStorageReady(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }

    /**
     * opencode stores sessions under HOME. Shared storage (`/sdcard/OpenCode`)
     * survives uninstalls; without the permission the host falls back to
     * app-private storage, which is wiped on uninstall.
     */
    private fun requestSharedStorageIfNeeded() {
        if (sharedStorageReady()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName"),
                    ),
                )
            }.onFailure {
                runCatching { startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) }
            }
            return
        }
        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST)
    }

    private fun startServer(embedded: EmbeddedServer) {
        overlay.visibility = View.VISIBLE
        overlay.text = getString(R.string.starting)
        Thread {
            runCatching { embedded.start() }
                .onSuccess { port ->
                    serverOnPrivateStorage = !sharedStorageReady()
                    Log.i(TAG, "server ready on port $port (shared=${!serverOnPrivateStorage})")
                    runOnUiThread {
                        overlay.visibility = View.GONE
                        webView.loadUrl("http://127.0.0.1:$port/")
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "server failed to start", error)
                    runOnUiThread {
                        overlay.visibility = View.VISIBLE
                        overlay.text = getString(R.string.start_failed, error.message ?: "unknown error")
                    }
                }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        val embedded = server ?: return
        if (!serverOnPrivateStorage || !sharedStorageReady()) return
        // Shared storage was granted while the server ran on private storage:
        // restart so sessions move to /sdcard/OpenCode and survive reinstalls.
        Log.i(TAG, "shared storage granted, restarting server")
        serverOnPrivateStorage = false
        Thread {
            embedded.stop()
            runCatching { embedded.start() }
                .onSuccess { port ->
                    runOnUiThread { webView.loadUrl("http://127.0.0.1:$port/") }
                }
                .onFailure { error -> Log.e(TAG, "server restart failed", error) }
        }.start()
    }

    /**
     * `packages/app` is used exactly as shipped and is not modified by this
     * host. Its dialogs assume a desktop-width window, so the WebView adds a
     * small stylesheet on narrow screens: dialogs go full-screen and the
     * settings tab list becomes a scrollable strip above the panel.
     */
    private fun injectMobileStyles() {
        val css =
            """
            (function () {
              var id = 'oc-android-mobile-css';
              var existing = document.getElementById(id);
              if (existing) existing.remove();
              var style = document.createElement('style');
              style.id = id;
              style.textContent = ${org.json.JSONObject.quote(MOBILE_CSS)};
              document.head.appendChild(style);
            })();
            """.trimIndent()
        webView.evaluateJavascript(css, null)
        injectStateMigration()
        injectUsagePanel()
    }

    /**
     * `/data/user/0/...` and `/data/data/...` are the same directory. Older UI
     * state stored the former while the server canonicalizes to the latter,
     * which made the session history look empty (sessions are grouped by
     * directory). Rewrite stored paths once and reload if anything changed.
     */
    private fun injectStateMigration() {
        val js =
            """
            (function () {
              try {
                var from = '/data/user/0/ai.opencode.android';
                var to = '/data/data/ai.opencode.android';
                var changes = [];
                for (var i = 0; i < localStorage.length; i++) {
                  var key = localStorage.key(i);
                  if (!key) continue;
                  var value = localStorage.getItem(key);
                  var nextKey = key.split(from).join(to);
                  var nextValue = (value || '').split(from).join(to);
                  if (nextKey !== key || nextValue !== value) changes.push([key, nextKey, nextValue]);
                }
                for (var j = 0; j < changes.length; j++) {
                  localStorage.removeItem(changes[j][0]);
                  localStorage.setItem(changes[j][1], changes[j][2]);
                }
                if (changes.length > 0) location.reload();
              } catch (error) {}
            })();
            """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    /**
     * Adds the Android host's context-usage affordance. The panel itself lives
     * in `assets/usage-panel.js`; the app's own `context.*` translations are
     * synced into `assets/usage-i18n.json` at build time.
     */
    private fun injectUsagePanel() {
        val script =
            runCatching {
                val panel = assets.open("usage-panel.js").bufferedReader().use { it.readText() }
                val i18n = assets.open("usage-i18n.json").bufferedReader().use { it.readText() }
                panel.replace("__USAGE_I18N__", i18n)
            }.getOrNull() ?: return
        webView.evaluateJavascript(script, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Keep the server process alive when the UI has no history to pop.
        if (webView.canGoBack()) webView.goBack() else moveTaskToBack(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            val callback = fileChooserCallback
            fileChooserCallback = null
            if (callback != null) {
                val result =
                    if (resultCode == RESULT_OK && data != null) {
                        WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                    } else {
                        null
                    }
                callback.onReceiveValue(result)
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        server?.stop()
        server = null
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OpenCodeAndroid"
        private const val FILE_CHOOSER_REQUEST = 1001
        private const val STORAGE_REQUEST = 1002

        private val MOBILE_CSS =
            """
            @media (max-width: 760px) {
              [data-slot="dialog-content"] {
                max-width: 100vw !important;
                width: 100vw !important;
                height: 100dvh !important;
                max-height: 100dvh !important;
                border-radius: 0 !important;
                left: 0 !important;
                top: 0 !important;
                transform: none !important;
                display: flex !important;
                flex-direction: column !important;
                overflow: hidden !important;
              }
              .settings-v2 {
                flex: 1 1 auto !important;
                min-height: 0 !important;
                height: auto !important;
                flex-direction: column !important;
              }
              .settings-v2 > [data-slot="tabs-v2-list"] {
                width: 100% !important;
                flex: 0 0 auto !important;
                height: auto !important;
                max-height: 40vh !important;
                align-self: flex-start !important;
                overflow-x: auto !important;
                overflow-y: hidden !important;
                border-right: 0 !important;
                border-bottom: 1px solid var(--border-weak-base, rgba(255,255,255,0.1)) !important;
              }
              .settings-v2 > [data-slot="tabs-v2-list"] > div {
                flex-direction: row !important;
                justify-content: flex-start !important;
                align-items: center !important;
                gap: 6px !important;
                height: auto !important;
              }
              .settings-v2 > [data-slot="tabs-v2-list"] > div > div {
                flex-direction: row !important;
                gap: 6px !important;
                padding-top: 0 !important;
              }
              .settings-v2 > [data-slot="tabs-v2-list"] > div > div > div {
                flex-direction: row !important;
                gap: 6px !important;
              }
              .settings-v2 > [data-slot="tabs-v2-list"] > div > div:last-child {
                display: none !important;
              }
              .settings-v2 > [data-slot="tabs-v2-content"] {
                width: 100% !important;
                flex: 1 1 auto !important;
                min-height: 0 !important;
                overflow-y: auto !important;
              }
              /* The main session tab row scrolls instead of squeezing labels. */
              [data-slot="tabs-list"] {
                overflow-x: auto !important;
                overflow-y: hidden !important;
              }
              [data-slot="tabs-list"] > * {
                flex: 0 0 auto !important;
                width: auto !important;
                min-width: 96px !important;
                max-width: none !important;
              }
              [data-slot="tabs-list"] [data-slot="tabs-trigger"] {
                width: 100% !important;
                white-space: nowrap !important;
                padding-left: 10px !important;
                padding-right: 10px !important;
              }
              /* Open session tabs in the titlebar keep a readable width and
                 scroll, so the close button does not sit on the title. */
              /* Session tabs: size the sortable wrappers (the inner item is
                 stretched to the wrapper) so tabs never overlap and the strip
                 scrolls horizontally. */
              [data-slot="titlebar-tabs-scroll"] > div > div[class*="w-56"] {
                width: 160px !important;
                min-width: 148px !important;
                max-width: 200px !important;
                flex-shrink: 0 !important;
                margin-right: 6px !important;
              }
              [data-slot="titlebar-tab-item"] {
                width: 100% !important;
                min-width: 0 !important;
                max-width: none !important;
                border: 1px solid var(--border-weak-base, rgba(255, 255, 255, 0.1)) !important;
              }
              [data-slot="titlebar-tabs-scroll"] {
                overflow-x: auto !important;
              }
              /* Home: keep Settings/Help pinned and make the session list the
                 scrollable canvas instead of stretching an empty page. */
              div[class*="max-w-[1080px]"][class*="grid-rows-"] {
                height: 100% !important;
                min-height: 0 !important;
              }
              div[class*="max-w-[1080px]"] > section {
                min-height: 0 !important;
                overflow-y: auto !important;
                overscroll-behavior: contain;
              }
              form[class*="prompt-input"] {
                min-height: 132px !important;
              }
              form[class*="prompt-input"] > div:first-child {
                min-height: 96px !important;
              }
              form[class*="prompt-input"] [contenteditable="true"] {
                min-height: 96px !important;
                max-height: 50vh !important;
                line-height: 1.5 !important;
                padding-bottom: 14px !important;
              }
              /* Toolbar: the model name truncates instead of colliding with
                 the variant selector and the send button. */
              form[class*="prompt-input"] > div[class*="h-11"] {
                gap: 8px !important;
              }
              form[class*="prompt-input"] > div[class*="h-11"] > * {
                min-width: 0 !important;
              }
              form[class*="prompt-input"] button[data-action="prompt-model"] {
                flex: 0 1 auto !important;
                max-width: 132px !important;
                overflow: hidden !important;
              }
              form[class*="prompt-input"] button[data-action="prompt-model"] * {
                min-width: 0 !important;
              }
              form[class*="prompt-input"] button[data-action="prompt-model"] span {
                overflow: hidden !important;
                text-overflow: ellipsis !important;
                white-space: nowrap !important;
              }
              form[class*="prompt-input"] button[aria-label="Choose model variant"] {
                flex: 0 0 auto !important;
                margin-right: 2px !important;
              }
              form[class*="prompt-input"] button[aria-label="Send"] {
                flex: 0 0 auto !important;
              }
            }
            """.trimIndent()
    }
}
