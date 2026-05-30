package io.github.aryapreetam.cmplocationpicker.provider.osm

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings
import kotlin.test.Test
import kotlin.test.assertTrue

class LeafletOsmMapProviderTest {
  @Test
  fun html_waitsForComposeWebViewBridgeReadyEvent() {
    val html = LeafletOsmMapProvider().createHtml(
      fallbackCenter = LatLng(0.0, 0.0),
      strings = LocationPickerStrings(),
      options = LocationPickerOptions()
    )

    assertTrue(
      html.contains("ComposeWebViewBridgeReady"),
      "Expected HTML to reference ComposeWebViewBridgeReady so ready/messages aren't lost before bridge init"
    )
  }
}
