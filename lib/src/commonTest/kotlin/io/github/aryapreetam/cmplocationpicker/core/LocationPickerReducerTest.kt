package io.github.aryapreetam.cmplocationpicker.core

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocationPickerReducerTest {
  @Test
  fun webViewReady_clearsLoading() {
    val initial = LocationPickerState(webViewReady = false)
    val next = LocationPickerReducer.reduce(initial, LocationPickerEvent.WebViewReady)
    assertTrue(next.webViewReady)
    assertFalse(next.isLoading)
  }

  @Test
  fun jsLocationUpdate_setsSelectedLocation() {
    val initial = LocationPickerState(webViewReady = true)
    val next = LocationPickerReducer.reduce(
      initial,
      LocationPickerEvent.JsSelectedLocationUpdated(LatLng(1.0, 2.0))
    )
    assertEquals(1.0, next.selectedLocation?.latitude)
    assertEquals(2.0, next.selectedLocation?.longitude)
  }
}
