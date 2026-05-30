<h1 align="center">CMP Location Picker - Compose Multiplatform Location Picker Library</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-v1.10.1-4285F4?logo=jetpackcompose" alt="Compose Multiplatform Version">
  <img src="https://img.shields.io/badge/Kotlin-v2.3.21-blueviolet?logo=kotlin" alt="Kotlin Version">
</p>

<p align="center">
  <a href="https://github.com/aryapreetam/cmp-locationpicker/actions/workflows/release.yml">
    <img src="https://github.com/aryapreetam/cmp-locationpicker/actions/workflows/release.yml/badge.svg" alt="Release workflow status">
  </a>
</p>

Location picker dialog for **Compose Multiplatform** (Android / iOS / Desktop / Web) built on top of `cmp-webview`.

v0 implementation:

- Leaflet + OpenStreetMap tiles
- Nominatim search + autocomplete suggestions
- Returns `LatLng` only (no structured address yet)

## Install

Add the dependency (replace version as needed):

```kotlin
implementation("io.github.aryapreetam:cmp-locationpicker:0.0.2")
```

Note: the library UI itself uses only `compose.foundation`/`compose.ui` (no `compose.material3` dependency in `:cmp-locationpicker`).

## Usage

```kotlin
@Composable
fun Example(show: Boolean, onDismiss: () -> Unit, onPicked: (io.github.aryapreetam.cmplocationpicker.model.LatLng) -> Unit) {
  if (!show) return

  io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog(
    onDismiss = onDismiss,
    onLocationPicked = onPicked
  )
}
```

## Configuration

### Strings

The library ships with English defaults. Apps should pass localized strings:

```kotlin
strings = io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings(
  title = "Pick location",
  selectButton = "Select",
  closeContentDescription = "Close",
  loadingMap = "Loading map…",
  searchPlaceholder = "Search location…",
)
```

### Options (Leaflet/Nominatim)

```kotlin
options = io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions(
  searchLanguageTag = "en",
  countryCodes = "in",
  suggestionsLimit = 5
)
```

### Override network behavior

On dialog start, v0 performs a best-effort IP-based lookup (default endpoint: `https://ipapi.co/json/`).
The default provider expects a JSON response containing `latitude` and `longitude`.
You can override or disable it:

```kotlin
ipLocationProvider = object : io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider {
  override suspend fun getBestEffortLatLng(): io.github.aryapreetam.cmplocationpicker.model.LatLng =
    io.github.aryapreetam.cmplocationpicker.model.LatLng(20.5937, 78.9629)
}
```

You can also provide a custom `MapProvider` if you want different HTML/assets or to avoid remote CDNs.

## Privacy & network notes (important)

The default v0 implementation may perform network requests to third parties:

- `https://ipapi.co/json/` (best-effort initial center)
- `https://unpkg.com/...` (Leaflet assets)
- `https://{s}.tile.openstreetmap.org/...` (map tiles)
- `https://nominatim.openstreetmap.org/search` (search suggestions)

If your app must not contact these services, provide your own `IpLocationProvider` and/or `MapProvider`.

### Attribution

If you ship the default v0 provider in production, make sure you comply with attribution requirements:

- OpenStreetMap tile usage typically requires displaying an attribution such as `© OpenStreetMap contributors`.
- Leaflet is BSD-2-Clause licensed; keep license notices as required by your distribution.

## Testing

From a checkout that includes the composite builds:

- Unit tests (pure core + protocol):
  - `./gradlew :cmp-locationpicker:testDebugUnitTest`
- Desktop/JVM tests (sample):
  - `./gradlew :sample:composeApp:jvmTest`
- Android instrumentation (sample):
  - `./gradlew :sample:androidApp:connectedAndroidTest`

The Desktop WebView interop test is opt-in because it requires a real desktop UI environment:

```bash
CMP_LOCATIONPICKER_RUN_DESKTOP_IT=true ./gradlew :sample:composeApp:jvmTest
```

## 📦 Download Sample App Executables

Download and try the sample app on your platform without building from source:

| Platform | Download Link |
|----------|--------------|
| 🍏 **macOS (Intel)** | [![Download](https://img.shields.io/badge/Download--Mac--x64--DMG-blue?style=for-the-badge)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-desktop-x64.dmg) |
| 🍎 **macOS (Apple Silicon)** | [![Download](https://img.shields.io/badge/Download--Mac--arm64--DMG-blue?style=for-the-badge)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-desktop-arm64.dmg) |
| 🪟 **Windows** | [![Download](https://img.shields.io/badge/Download--Windows--MSI-0078D4?style=for-the-badge&logo=windows)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-windows.msi) |
| 🐧 **Linux** | [![Download](https://img.shields.io/badge/Download--Linux--DEB-E95420?style=for-the-badge&logo=ubuntu)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-linux.deb) |
| 🤖 **Android** | [![Download](https://img.shields.io/badge/Download--Android--APK-green?style=for-the-badge&logo=android)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-android-unsigned.apk) |
| 🌐 **Web (Wasm)** | [![Download](https://img.shields.io/badge/Download--Web--Wasm--zip-purple?style=for-the-badge)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-wasm.zip) |
|  **iOS Simulator** | [![Download](https://img.shields.io/badge/Download--iOS--Simulator--zip-orange?style=for-the-badge&logo=apple)](https://github.com/aryapreetam/cmp-locationpicker/releases/latest/download/sample-app-ios-simulator.zip) |

### How to Run the Sample App

#### 🤖 Android

**Option 1: Drag and Drop**

- Download the APK file
- Open an Android emulator or connect a physical device
- Drag the APK onto the emulator window

**Option 2: ADB Install**

```bash
adb install sample-app-android-unsigned.apk
```

#### 🌐 Web (Wasm)

- Download and unzip `sample-app-wasm.zip`
- Open `index.html` in a web browser

**Note:** You can also try the live demo without downloading: [Try Live Demo](https://aryapreetam.github.io/cmp-locationpicker/demo/)

#### 🍏 iOS Simulator

- Download `sample-app-ios-simulator.zip` from the [latest release](https://github.com/aryapreetam/cmp-locationpicker/releases)
- Unzip to get `sample-app-ios-simulator.app`
- Open your iOS Simulator in Xcode
- Drag the `.app` onto the Simulator window OR run:
  ```sh
  xcrun simctl install booted /path/to/sample-app-ios-simulator.app
  ```
- The sample app will now appear and can be launched directly from the Simulator!

#### 🍎 macOS

- Download the DMG for your Mac architecture (Intel or Apple Silicon)
- Open (mount) the DMG and drag the app to your Applications folder (or Desktop)
- **When you try to open the app for the first time, macOS Gatekeeper will block it since it is an open-source build signed ad-hoc (not using a paid Apple Developer account). Follow these 6 sequential steps to allow running the app:**

  <table width="100%">
    <tr>
      <td align="center" valign="top"><strong>1. Block Alert Dialog</strong><br/><br/><img src="readme_images/mac_app_run1.png" alt="mac_app_run1" width="220"/></td>
      <td align="center" valign="top"><strong>2. Open System Settings</strong><br/><br/><img src="readme_images/mac_app_run2.png" alt="mac_app_run2" width="220"/></td>
      <td align="center" valign="top"><strong>3. Privacy & Security Section</strong><br/><br/><img src="readme_images/mac_app_run3.png" alt="mac_app_run3" width="220"/></td>
    </tr>
    <tr>
      <td align="center" valign="top"><strong>4. Click "Open Anyway"</strong><br/><br/><img src="readme_images/mac_app_run4.png" alt="mac_app_run4" width="220"/></td>
      <td align="center" valign="top"><strong>5. Authenticate Security Dialog</strong><br/><br/><img src="readme_images/mac_app_run5.png" alt="mac_app_run5" width="220"/></td>
      <td align="center" valign="top"><strong>6. Click "Open" to Launch</strong><br/><br/><img src="readme_images/mac_app_run6.png" alt="mac_app_run6" width="220"/></td>
    </tr>
  </table>

- To allow running the app:
     1. Double-click the app. If blocked, open macOS **System Settings** (or System Preferences).
     2. Navigate to **Privacy & Security** and scroll down to the **Security** section.
     3. You will see a notice stating: *"sample was blocked from use because it is not from an identified developer"*.
     4. Click **"Open Anyway"** and authenticate with your user password or Touch ID.
     5. Re-open the app and click **"Open"** on the confirmation dialog. The app will now launch safely!

> This is a normal security step for all open-source and CI-generated Mac executables. Apps are signed ad-hoc for internal/dev use, not with a public Apple developer ID.

**Tip:** Unsure about your Mac's type? Click the Apple logo → "About This Mac". If it says Intel, download x64; if it says M1, M2, or M3, download arm64.

#### 🪟 Windows

- Download and run the MSI installer
- Follow the installation wizard
- Launch the app from the Start menu

#### 🐧 Linux

- Download the `.deb` file
- Double-click the file to open it in your software manager and click "Install", OR run:
  ```bash
  sudo dpkg -i sample-app-linux.deb
  ```
- Launch the application from your desktop launcher or run `sample` in the terminal!

## Manual QA (iOS + WASM)

Automated WebView interop UI tests are currently not feasible on iOS/WASM (Compose interop container limitations).
Use the checklist in `docs/manual-qa.md`.

## Development notes (composite build)

During development, this repo is typically used as a composite build alongside a local checkout of `cmp-webview`.
See `settings.gradle.kts` for the `includeBuild("../cmp-webview")` + dependency substitution setup.

## License

MIT. See `LICENSE`.
