package io.github.aryapreetam.cmplocationpicker.core

import io.github.aryapreetam.cmpwebview.WebViewController
import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.protocol.LocationPickerProtocol
import io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Thin controller that wires async effects (IP lookup, acks, Compose→JS commands)
 * around a pure reducer.
 */
class LocationPickerController(
  private val scope: CoroutineScope,
  private val ipLocationProvider: IpLocationProvider,
  private val webViewController: WebViewController
) {
  private val _state = MutableStateFlow(LocationPickerState())
  val state: StateFlow<LocationPickerState> = _state.asStateFlow()

  fun startIpCenterLookup() {
    scope.launch {
      val latLng = ipLocationProvider.getBestEffortLatLng()
      dispatch(LocationPickerEvent.IpCenterResolved(latLng))
      // Best-effort: tell JS to center the map. We don't rely on return values.
      // This may be Unsupported if the WebView isn't attached yet; we'll also re-attempt on `ready`.
      webViewController.evaluateJavaScript(
        "window.CmpLocationPickerBridge && window.CmpLocationPickerBridge.setCenter(${latLng.latitude}, ${latLng.longitude});"
      )
    }
  }

  fun onJsMessage(raw: String) {
    val parsed = LocationPickerProtocol.parseIncoming(raw)
    when (parsed) {
      is LocationPickerProtocol.ParseResult.Success -> {
        when (val msg = parsed.message) {
          is LocationPickerProtocol.Incoming.Ready -> {
            dispatch(LocationPickerEvent.WebViewReady)
            // If IP center already resolved before the WebView was attached/ready, apply it now.
            state.value.ipCenter?.let { ipCenter ->
              scope.launch {
                webViewController.evaluateJavaScript(
                  "window.CmpLocationPickerBridge && window.CmpLocationPickerBridge.setCenter(${ipCenter.latitude}, ${ipCenter.longitude});"
                )
              }
            }
            ack(msg.id, ok = true, error = null)
          }

          is LocationPickerProtocol.Incoming.LocationUpdate -> {
            dispatch(LocationPickerEvent.JsSelectedLocationUpdated(msg.latLng))
            ack(msg.id, ok = true, error = null)
          }
        }
      }

      is LocationPickerProtocol.ParseResult.Error -> {
        dispatch(LocationPickerEvent.ProtocolError(parsed.reason))
        val id = parsed.id
        if (id != null) ack(id, ok = false, error = parsed.reason)
      }
    }
  }

  private fun ack(id: String, ok: Boolean, error: String?) {
    scope.launch {
      webViewController.evaluateJavaScript(LocationPickerProtocol.ackJavaScript(id, ok, error))
    }
  }

  private fun dispatch(event: LocationPickerEvent) {
    _state.value = LocationPickerReducer.reduce(_state.value, event)
  }
}
