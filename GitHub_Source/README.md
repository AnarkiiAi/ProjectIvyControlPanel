# AnarkiiControl

A compact Android TV / NVIDIA Shield control panel designed as an alternative to navigating the full settings interface from Projectivy Launcher. The app provides a remote-friendly popup-style settings panel with quick shortcuts, artwork, and Android TV navigation.

## What it does
- Launches a small settings/control panel on Android TV devices, designed for NVIDIA Shield.
- Provides quick access to device, display, network, Bluetooth, storage, applications, volume, and related settings shortcuts (actual availability depends on OS version and permissions).
- Includes an optional AccessibilityService (`RemoteKeyService`) that detects the remote's **Menu** key and opens the panel. This requires manually enabling the service in Android accessibility settings.
- Uses a landscape, leanback-friendly interface with focused tile artwork.

## Requirements
- Android Studio with Android SDK matching `app/build.gradle.kts` (compile SDK 37, min SDK 30, target SDK 37).
- Gradle wrapper included. Dependencies are declared in `gradle/libs.versions.toml` and resolved through Google/Maven Central.
- Android TV / NVIDIA Shield for intended experience. Device and OS permissions may limit shortcuts and interception of remote buttons.

## Build
Open the `GitHub_Source` folder as a project in Android Studio. Let Gradle sync, install required Android SDK components, then use **Build > Build APK(s)** or run on an Android TV device. From the terminal on Windows: `gradlew.bat assembleDebug`. On macOS/Linux: `./gradlew assembleDebug` (make executable if necessary). A fresh build has not been performed as part of this packaging exercise.

## Install
For a compiled APK, enable installation from your chosen trusted sideloading source on Android TV, transfer the APK and install. To use the remote Menu interception, enable AnarkiiControl's accessibility service in Android settings. Only enable accessibility services you trust. The separately packaged APK under `Release/` is a preserved existing build, not freshly rebuilt or signature-verified against these sources.

## Privacy and permissions
The manifest requests Internet, network state, Bluetooth, Bluetooth Connect, and background-process permissions. The optional accessibility service can observe accessibility events, including foreground app identity, and intercept remote key events. Review source and permission scope before enabling it. No claim of formal security auditing is made.

## Relationship to Projectivy
This is an independent companion/replacement control panel for the user's preferred Android TV experience. It is not an official Projectivy Launcher release or affiliated with its developers. No third-party Projectivy APKs are included in this repository.

## Repository layout
- `app/src/main`: Kotlin source, AndroidManifest, layouts, drawables and app resources.
- `app/build.gradle.kts`: Android app build configuration.
- `gradle/`, `gradlew`, `gradlew.bat`: Gradle build metadata and wrapper.
- `Release/AnarkiiControl.apk` in the separate distribution folder: existing compiled APK.

## Known limitations
The GitHub-ready copy is curated from an existing Android Studio project. No clean rebuild, device test, APK signature verification, dependency audit or feature-by-feature regression was performed during packaging. Android 15+ behavior, accessibility policy and privileged settings access may vary. Contributions and bug reports are welcome once published.

## License
No explicit open-source license has been selected. Copyright remains with the applicable authors. Choose a license deliberately before inviting redistribution or contributions.
