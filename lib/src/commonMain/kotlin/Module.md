# Module cmp-locationpicker

`cmp-locationpicker` provides a simple, cross-platform location picker dialog for **Compose Multiplatform**.

v0 implementation:

- Returns `LatLng` only (no structured address yet)
- Default stack: Leaflet + OpenStreetMap tiles + Nominatim search
- Runs inside `cmp-webview` using controlled `htmlContent`
- JS↔Compose protocol is ack-based (do not rely on JavaScript return values)

## Installation

Add the dependency to your `commonMain` source set:

```kotlin
commonMain.dependencies {
  implementation("io.github.aryapreetam:cmp-locationpicker:VERSION")
}
```

Replace `VERSION` with the latest release version.

## Quick start

```kotlin
@Composable
fun Example(
  show: Boolean,
  onDismiss: () -> Unit,
  onPicked: (io.github.aryapreetam.cmplocationpicker.model.LatLng) -> Unit
) {
  if (!show) return
  io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog(
    onDismiss = onDismiss,
    onLocationPicked = onPicked
  )
}
```

## Configuration

Most callers customize:

- `io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings` (UI strings)
- `io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions` (Nominatim options like `countryCodes` and `searchLanguageTag`)

Advanced customization:

- `io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider` (override/disable IP-based initial center)
- `io.github.aryapreetam.cmplocationpicker.provider.MapProvider` (custom HTML/assets/provider stack)

## Privacy & network notes (important)

The default v0 provider may contact third parties:

- `https://ipapi.co/json/` (best-effort initial center)
- `https://unpkg.com/...` (Leaflet assets)
- `https://{s}.tile.openstreetmap.org/...` (map tiles)
- `https://nominatim.openstreetmap.org/search` (search suggestions)

If your app must not contact these services, provide your own `IpLocationProvider` and/or `MapProvider`.

## Entry point

- `io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog`
