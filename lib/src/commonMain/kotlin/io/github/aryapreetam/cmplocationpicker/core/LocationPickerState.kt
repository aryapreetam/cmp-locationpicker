package io.github.aryapreetam.cmplocationpicker.core

import io.github.aryapreetam.cmplocationpicker.model.LatLng

data class LocationPickerState(
  val webViewReady: Boolean = false,
  val ipCenter: LatLng? = null,
  val selectedLocation: LatLng? = null,
  val lastProtocolError: String? = null
) {
  val isLoading: Boolean get() = !webViewReady
}
