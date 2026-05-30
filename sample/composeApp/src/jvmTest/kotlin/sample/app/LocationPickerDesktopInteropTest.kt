package sample.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import io.github.aryapreetam.cmpwebview.WebView
import io.github.aryapreetam.cmpwebview.rememberWebViewController
import io.github.aryapreetam.cmplocationpicker.core.LocationPickerController
import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationPickerDesktopInteropTest {
  @Test
  fun desktopHarness_ackChain_updatesLocationTwice() {
    // This test requires a desktop UI environment (Swing).
    // It is intentionally opt-in to avoid failing in headless/CI environments.
    if (System.getenv("CMP_LOCATIONPICKER_RUN_DESKTOP_IT") != "true") return

    val loadError = AtomicReference<String?>(null)
    val finalLatLng = AtomicReference<LatLng?>(null)
    val latch = CountDownLatch(1)

    lateinit var frame: JFrame

    SwingUtilities.invokeAndWait {
      frame = JFrame("cmp-locationpicker desktop interop test")
      val panel = ComposePanel()
      frame.contentPane.add(panel)
      frame.setSize(900, 700)
      frame.setLocationRelativeTo(null)
      frame.isVisible = true

      panel.setContent {
        val webController = rememberWebViewController()
        val scope = rememberCoroutineScope()
        val ipProvider = remember {
          object : IpLocationProvider {
            override suspend fun getBestEffortLatLng(): LatLng = LatLng(0.0, 0.0)
          }
        }

        val controller = remember(scope, webController) {
          LocationPickerController(
            scope = scope,
            ipLocationProvider = ipProvider,
            webViewController = webController
          )
        }

        val state by controller.state.collectAsState()

        LaunchedEffect(Unit) {
          controller.startIpCenterLookup()
        }

        SideEffect {
          val sel = state.selectedLocation
          if (sel?.latitude == 3.0 && sel.longitude == 4.0) {
            finalLatLng.compareAndSet(null, sel)
            latch.countDown()
          }
        }

        Box(Modifier.fillMaxSize()) {
          WebView(
            htmlContent = CONTROLLED_HTML,
            modifier = Modifier.fillMaxSize(),
            controller = webController,
            onScriptResult = { msg -> controller.onJsMessage(msg) },
            onLoadError = { loadError.set(it) }
          )

          Text("selected=${state.selectedLocation?.latitude},${state.selectedLocation?.longitude}")
        }
      }
    }

    val ok = latch.await(60, TimeUnit.SECONDS)
    SwingUtilities.invokeAndWait { frame.dispose() }

    assertEquals(true, ok, "Timed out waiting for ack-based location update")
    assertNull(loadError.get(), "WebView load error: ${loadError.get()}")
    assertEquals(3.0, finalLatLng.get()?.latitude)
    assertEquals(4.0, finalLatLng.get()?.longitude)
  }
}

private val CONTROLLED_HTML: String = """
<!DOCTYPE html>
<html>
  <head>
    <meta charset=\"UTF-8\" />
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
    <title>cmp-locationpicker desktop interop test</title>
  </head>
  <body>
    <p>cmp-locationpicker desktop interop test page</p>
    <script>
      window.addEventListener('ComposeWebViewBridgeReady', function () {
        window.CmpLocationPickerBridge = {
          onAck: function (ack) {
            try {
              if (ack && ack.type === 'ack' && ack.id === 'u1' && ack.ok) {
                window.ComposeWebViewBridge.postMessage(JSON.stringify({
                  type: 'location_update',
                  id: 'u2',
                  lat: 3.0,
                  lng: 4.0
                }));
              }
            } catch (e) {}
          },
          setCenter: function (lat, lng) {}
        };
        window.ComposeWebViewBridge.postMessage(JSON.stringify({ type: 'ready', id: 'r1' }));
        window.ComposeWebViewBridge.postMessage(JSON.stringify({ type: 'location_update', id: 'u1', lat: 1.0, lng: 2.0 }));
      });
    </script>
  </body>
</html>
""".trimIndent()
