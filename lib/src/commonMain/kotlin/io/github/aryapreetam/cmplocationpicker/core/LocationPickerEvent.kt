package io.github.aryapreetam.cmplocationpicker.core

import io.github.aryapreetam.cmplocationpicker.model.LatLng

sealed interface LocationPickerEvent {
  data object WebViewReady : LocationPickerEvent

  data class IpCenterResolved(val latLng: LatLng) : LocationPickerEvent

  data class JsSelectedLocationUpdated(val latLng: LatLng) : LocationPickerEvent

  data class ProtocolError(val message: String) : LocationPickerEvent
}
