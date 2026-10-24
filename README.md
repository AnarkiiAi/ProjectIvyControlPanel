# AnarkiiControl

A compact, glass-inspired control panel for Android TV and NVIDIA Shield. Open common settings from a remote-friendly panel, designed to work alongside Projectivy Launcher.

## Download v1.02

[Download the signed Android 9+ APK](https://github.com/AnarkiiAi/ProjectIvyControlPanel/releases/download/v1.02/AnarkiiControl-v1.02-Android9-release.apk) · [Release notes and checksum](https://github.com/AnarkiiAi/ProjectIvyControlPanel/releases/tag/v1.02)

The owner tested this exact signed v1.02 build on NVIDIA Shield and confirmed it works. Other devices and firmware versions have not been verified.

| Requirement | Value |
| --- | --- |
| Device | Android TV / NVIDIA Shield |
| Minimum Android | Android 9 (API 28) |
| Target SDK | 36 |
| Package | `com.anarkii.control` |
| Version | 1.02 (versionCode 3) |

## Install

* You will need to select “allow apps from untrusted sources” from settings>apps>security and restrictions if you have issues with installation. 

1. Download `AnarkiiControl-v1.02-Android9-release.apk` from the release assets and transfer it to your shield.
2. Allow installation from the sideloading app you use (I used File Manager+), then install the APK. Select Install, it will stage the app, hit install. Select done when finished. 
3. Launch AnarkiiControl from your TV launcher, or from All Apps.
4. To open it with the remote's **Menu** button, enable AnarkiiControl in Android's accessibility settings. The service intercepts Menu while enabled.

**Coming from v1.01 debug?** Uninstall the debug app first: the release uses a new signing certificate and cannot update it in place. Uninstalling removes app data; re-enable accessibility after installation. An older build signed with another key may also require uninstalling first. Future releases will use the same release key.

## Features and v1.02 changes

- Remote-friendly shortcuts for device, display, network, Bluetooth, storage, apps and volume settings; availability depends on your firmware.
- Optional accessibility service for the Menu-button shortcut.
- Signed release build with debugging disabled, retaining Android 9 support.
- Bluetooth hardware is optional; a touchscreen is not required.
- Memory refresh no longer claims confirmed cleanup. On Android 14+, it refreshes the memory reading instead of trying to stop other apps.

## Source and build

The [v1.02 source](https://github.com/AnarkiiAi/ProjectIvyControlPanel/tree/v1.02/GitHub_Source) is the source corresponding to this release. Open `GitHub_Source` in Android Studio, using Java 17 and Android SDK 36. Run `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`). See the [release signing guide](https://github.com/AnarkiiAi/ProjectIvyControlPanel/blob/android9-v1.01/RELEASE.md) for signed builds.

GitHub's debug/release builds, release lint and APK metadata checks passed. The published APK's signature and alignment were verified locally. Non-blocking lint warnings remain; passing checks is not a formal security audit.

## Permissions and compatibility

The optional accessibility service observes foreground-app accessibility events and intercepts remote Menu key events. Enable it only if you want that feature. The manifest also requests Internet, network state, Bluetooth and background-process permissions. Settings shortcuts and remote behavior can vary by device. Android TV/Leanback and landscape display are required.

AnarkiiControl is independent of Projectivy Launcher and is not affiliated with its developers. No explicit open-source license has been selected; copyright remains with the applicable authors.
