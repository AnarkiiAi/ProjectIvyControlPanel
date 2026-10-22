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
        minSdk = 28
        targetSdk = 37
        versionCode = 2
        versionName = "1.01"

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
