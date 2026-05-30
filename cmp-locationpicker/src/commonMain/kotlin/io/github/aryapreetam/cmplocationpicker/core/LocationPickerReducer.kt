package io.github.aryapreetam.cmplocationpicker.core

object LocationPickerReducer {
  fun reduce(state: LocationPickerState, event: LocationPickerEvent): LocationPickerState {
    return when (event) {
      LocationPickerEvent.WebViewReady -> state.copy(webViewReady = true, lastProtocolError = null)

      is LocationPickerEvent.IpCenterResolved -> state.copy(ipCenter = event.latLng)

      is LocationPickerEvent.JsSelectedLocationUpdated -> state.copy(
        selectedLocation = event.latLng,
        lastProtocolError = null
      )

      is LocationPickerEvent.ProtocolError -> state.copy(lastProtocolError = event.message)
    }
  }
}
