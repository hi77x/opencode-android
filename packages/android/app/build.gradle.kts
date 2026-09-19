import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
val releaseKeystore = rootProject.file("keystore/opencode-release.jks")
val releaseStorePassword =
    localProperties.getProperty("RELEASE_STORE_PASSWORD") ?: System.getenv("OPENCODE_ANDROID_STORE_PASSWORD")
val hasReleaseSigning = releaseKeystore.exists() && !releaseStorePassword.isNullOrEmpty()

android {
    namespace = "ai.opencode.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "ai.opencode.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseStorePassword
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS") ?: "opencode"
                keyPassword =
                    localProperties.getProperty("RELEASE_KEY_PASSWORD") ?: releaseStorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signed with the local release keystore when available, otherwise
            // falls back to the debug key so clean checkouts still build.
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            // The bundled opencode server executable is stored in jniLibs and must be
            // extracted to nativeLibraryDir, where Android allows execve for app code.
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
