# AnarkiiControl 1.02 release

Based on the Shield-tested v1.01 commit a851c394b03fde1d7c7c4fcb1fcf0e39e9008c35.
Package: com.anarkii.control. Version code: 3. Minimum SDK: 28 (Android 9).
Target and compile SDK: 36. Java 17 is used by the build workflow.

## Changes

- Release builds explicitly disable debugging and support a persistent release key.
- Bluetooth hardware is optional for store device filtering.
- Memory cleanup no longer claims confirmed success. On Android 14 and later the
  memory tile refreshes its reading instead of attempting to kill other apps.
- CI builds both variants, runs release lint, and checks the APK's SDK/version/debug metadata.

## Local signed build

Set ANARKII_KEYSTORE_PATH (absolute path), ANARKII_STORE_PASSWORD,
ANARKII_KEY_ALIAS and ANARKII_KEY_PASSWORD in the build process environment.
From GitHub_Source run `./gradlew assembleRelease --no-configuration-cache`.
Without these variables Gradle creates an unsigned release; partial configuration fails.
Never commit the keystore, passwords or signing configuration containing secret values.

## GitHub signed build

In repository Settings > Secrets and variables > Actions, add:

- ANARKII_KEYSTORE_BASE64: the base64-encoded release keystore
- ANARKII_STORE_PASSWORD: its password
- ANARKII_KEY_ALIAS: its alias
- ANARKII_KEY_PASSWORD: its key password

The android9-v1.01 push/manual workflow signs only after the build and checks pass.
Pull requests never receive signing secrets. Missing all secrets leaves a successful
build check with a debug download and a clearly labeled UNSIGNED APK for local
signing; incomplete secrets fail signing. A signed
release artifact is uploaded only after apksigner verification succeeds.

## Install and publish

Keep an encrypted offline backup of the key and its password for future updates.
The first release uses a different key from the debug APK, so uninstall the debug
app before installing the release. This resets app data and can require re-enabling
the accessibility service. Subsequent releases must use the same release key and
increasing version codes.

Test the signed APK on the Shield before publishing: launch, D-pad navigation,
Menu interception, settings shortcuts, app search and returning to the panel.
CI uploads build artifacts only; it does not publish a GitHub Release or Play listing.
