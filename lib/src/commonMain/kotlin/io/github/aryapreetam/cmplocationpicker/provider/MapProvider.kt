package io.github.aryapreetam.cmplocationpicker.provider

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings

/**
 * Map rendering provider. In v0 this is Leaflet + OSM tiles.
 */
interface MapProvider {
  /** Create controlled HTML for rendering inside `cmp-webview` via `htmlContent`. */
  fun createHtml(
    fallbackCenter: LatLng,
    strings: LocationPickerStrings,
    options: LocationPickerOptions
  ): String

  /** Best-effort JS snippet to recenter the map after load. */
  fun setCenterJavaScript(latLng: LatLng): String
}
