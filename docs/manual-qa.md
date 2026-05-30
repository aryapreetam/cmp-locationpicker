### Manual QA checklist (iOS + WASM)

#### Why manual QA is required (v0)
`cmp-locationpicker` uses `cmp-webview` to render `htmlContent` with a JS↔Compose bridge.

Due to current interop testing limitations:

- iOS + WASM: automated Compose UI tests using `runComposeUiTest` are not suitable for WebView interop (missing `LocalInteropContainer`).
- Desktop: `evaluateJavaScript` is best-effort and does **not** provide return values; all flows must be validated via JS→Compose messages + Compose→JS acks.

This document is the v0 manual QA checklist for iOS and WASM.

#### Common setup
1. Run the `sample` app for the target platform.
2. Open the location picker dialog.
3. Ensure the map loads and the search UI is visible.

#### iOS checklist
- [ ] Dialog opens and renders the map (Leaflet tiles load).
- [ ] Search input is usable.
- [ ] Typing 3+ chars shows suggestions.
- [ ] Tapping a suggestion:
  - [ ] Recenters the map
  - [ ] Moves the marker
  - [ ] Updates the coordinate display
- [ ] Tapping on the map:
  - [ ] Moves the marker
  - [ ] Updates the coordinate display
- [ ] Closing the dialog works.
- [ ] Selecting a location updates the sample screen with the picked `LatLng`.

#### WASM (browser) checklist
- [ ] Dialog opens and renders the map.
- [ ] Search input and suggestions behave as expected.
- [ ] Map tap updates marker/coords.
- [ ] Selecting a location updates the sample screen.

#### Known constraints to keep in mind
- The JS↔Compose protocol is **ack-based**. If JS messages stop arriving, verify that Compose can execute `evaluateJavaScript` and that acks are being sent.
- IP-based center is best-effort via `https://ipapi.co/json/` and may fall back to a fixed coordinate.
