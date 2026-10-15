plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.anarkii.control"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.anarkii.control"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
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