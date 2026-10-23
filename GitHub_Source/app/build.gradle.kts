
plugins {
    alias(libs.plugins.android.application)
}

val signingVariables = listOf(
    "ANARKII_KEYSTORE_PATH", "ANARKII_STORE_PASSWORD",
    "ANARKII_KEY_ALIAS", "ANARKII_KEY_PASSWORD"
)
val releaseSigning = signingVariables.associateWith { providers.environmentVariable(it).orNull }
val hasReleaseSigning = releaseSigning.values.all { !it.isNullOrBlank() }
check(releaseSigning.values.all { it.isNullOrBlank() } || hasReleaseSigning) {
    "Release signing requires all four ANARKII signing environment variables."
}

android {
    namespace = "com.anarkii.control"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anarkii.control"
        minSdk = 28
        targetSdk = 36
        versionCode = 3
        versionName = "1.02"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseSigning.getValue("ANARKII_KEYSTORE_PATH")!!)
                storePassword = releaseSigning.getValue("ANARKII_STORE_PASSWORD")
                keyAlias = releaseSigning.getValue("ANARKII_KEY_ALIAS")
                keyPassword = releaseSigning.getValue("ANARKII_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.glide)
}
