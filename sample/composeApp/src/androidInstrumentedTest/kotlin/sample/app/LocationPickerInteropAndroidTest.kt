package sample.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider
import io.github.aryapreetam.cmplocationpicker.provider.MapProvider
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class LocationPickerInteropAndroidTest {

  @get:Rule
  val rule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun ackBasedProtocol_allowsSecondMessageAfterAck() {
    val loadError = AtomicReference<String?>(null)
    val picked = AtomicReference<LatLng?>(null)

    rule.setContent {
      TestHarness(
        loadError = { loadError.set(it) },
        onPicked = { picked.set(it) }
      )
    }

    // Wait for dialog to become ready (loading overlay hidden).
    rule.waitUntil(timeoutMillis = 30_000) {
      loadError.get() != null || runCatching {
        rule.onNodeWithText("Loading").fetchSemanticsNode(); false
      }.getOrDefault(true)
    }

    // Trigger pick.
    rule.onNodeWithText("Select").performClick()

    // JS sends (1,2), Compose acks it, then JS sends (3,4) after ack. We expect to pick (3,4).
    rule.waitUntil(timeoutMillis = 30_000) {
      loadError.get() != null || picked.get() != null
    }

    assertNull("WebView load error: ${loadError.get()}", loadError.get())
    // At minimum, ensure a location was picked.
    // Specific coordinates are controlled by HTML (final should be 3,4).
    val value = picked.get()
    requireNotNull(value) { "Expected to pick a location" }
    if (value.latitude != 3.0 || value.longitude != 4.0) {
      throw AssertionError("Expected (3.0,4.0) but was (${value.latitude},${value.longitude})")
    }
  }
}

@Composable
private fun TestHarness(
  loadError: (String) -> Unit,
  onPicked: (LatLng) -> Unit
) {
  var showDialog by remember { mutableStateOf(true) }

  // A controlled provider that does not load external network resources.
  val mapProvider = remember {
    object : MapProvider {
      override fun createHtml(
        fallbackCenter: LatLng,
        strings: io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings,
        options: io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions
      ): String = CONTROLLED_HTML

      override fun setCenterJavaScript(latLng: LatLng): String = "window.CmpLocationPickerBridge && window.CmpLocationPickerBridge.setCenter(${latLng.latitude}, ${latLng.longitude});"
    }
  }

  val ipProvider = remember {
    object : IpLocationProvider {
      override suspend fun getBestEffortLatLng(): LatLng = LatLng(0.0, 0.0)
    }
  }

  Box(Modifier.fillMaxSize()) {
    Text(text = "picked=${if (showDialog) "<dialog>" else "<dismissed>"}")

    if (showDialog) {
      LocationPickerDialog(
        onDismiss = { showDialog = false },
        onLocationPicked = {
          onPicked(it)
        },
        strings = LocationPickerStrings(
          title = "Test",
          selectButton = "Select",
          loadingMap = "Loading"
        ),
        mapProvider = mapProvider,
        ipLocationProvider = ipProvider,
        fallbackCenter = LatLng(0.0, 0.0)
      )
    }
  }
}

private val CONTROLLED_HTML: String = """
<!DOCTYPE html>
<html>
  <head>
    <meta charset=\"UTF-8\" />
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
    <title>cmp-locationpicker interop test</title>
  </head>
  <body>
    <p>cmp-locationpicker interop test page</p>
    <script>
      window.addEventListener('ComposeWebViewBridgeReady', function () {
        // Provide the API that Compose calls for ack + best-effort commands.
        window.CmpLocationPickerBridge = {
          onAck: function (ack) {
            try {
              if (ack && ack.type === 'ack' && ack.id === 'u1' && ack.ok) {
                // Prove that Compose→JS ack executed by only sending the second update after ack.
                window.ComposeWebViewBridge.postMessage(JSON.stringify({
                  type: 'location_update',
                  id: 'u2',
                  lat: 3.0,
                  lng: 4.0
                }));
              }
            } catch (e) {}
          },
          setCenter: function (lat, lng) {
            // no-op for tests
          }
        };

        window.ComposeWebViewBridge.postMessage(JSON.stringify({ type: 'ready', id: 'r1' }));
        window.ComposeWebViewBridge.postMessage(JSON.stringify({
          type: 'location_update',
          id: 'u1',
          lat: 1.0,
          lng: 2.0
        }));
      });
    </script>
  </body>
</html>
""".trimIndent()
