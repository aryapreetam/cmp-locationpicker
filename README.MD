<h1 align="center">CMP Location Picker</h1>

<p align="center">
  <a href="https://github.com/aryapreetam/cmp-locationpicker/actions/workflows/release.yml">
    <img src="https://github.com/aryapreetam/cmp-locationpicker/actions/workflows/release.yml/badge.svg" alt="Release workflow status">
  </a>
  <a href="https://mvnrepository.com/artifact/io.github.aryapreetam/cmp-locationpicker">
    <img src="https://img.shields.io/maven-central/v/io.github.aryapreetam/cmp-locationpicker?label=Maven%20Central&color=blue" alt="Maven Central Version">
  </a>
  <a href="https://github.com/aryapreetam/cmp-locationpicker/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="MIT License">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-v1.10.1-4285F4?logo=jetpackcompose" alt="Compose Multiplatform Version">
  <img src="https://img.shields.io/badge/Kotlin-v2.3.21-blueviolet?logo=kotlin" alt="Kotlin Version">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/iOS-000000?logo=apple&logoColor=white" alt="iOS">
  <img src="https://img.shields.io/badge/Desktop/JVM-0078D6?logo=desktop&logoColor=white" alt="Desktop">
  <img src="https://img.shields.io/badge/Web(wasm)-4285F4?logo=google-chrome&logoColor=white" alt="Web">
</p>


Location picker dialog for **Compose Multiplatform** (Android / iOS / Desktop / Web) built on top of `cmp-webview`.

## Install

Add the dependency (replace version as needed):

```kotlin
implementation("io.github.aryapreetam:cmp-locationpicker:0.0.3")
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
  noResults = "No results",
  searchError = "Search error",
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
- `https://{s}.basemaps.cartocdn.com/...` (map tiles)
- `https://nominatim.openstreetmap.org/search` (search suggestions)

If your app must not contact these services, provide your own `IpLocationProvider` and/or `MapProvider`.

### Attribution

If you ship the default v0 provider in production, make sure you comply with attribution requirements:

- Map tile usage (Carto Voyager / OpenStreetMap) typically requires displaying an attribution such as `© OpenStreetMap contributors` and `© CARTO`.
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

### 🚀 How to Run the Sample App

For instructions on how to install and run the pre-built executables or build and run the sample application from source code, please refer to the **[How to Run the Sample App](docs/run-sample-app.md)** guide.

---

## 🧪 Manual QA (iOS + WASM)

Automated WebView interop UI tests are currently not feasible on iOS/WASM (Compose interop container limitations).
Use the checklist in **[Manual QA Checklist](docs/manual-qa.md)**.

## 🔧 Development Notes (Composite Build)

During development, this repo is typically used as a composite build alongside a local checkout of `cmp-webview`.
See `settings.gradle.kts` for the `includeBuild("../cmp-webview")` + dependency substitution setup.

---

## License

MIT License © 2025 aryapreetam. See [LICENSE](./LICENSE) for details.
