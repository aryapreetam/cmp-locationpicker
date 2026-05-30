package io.github.aryapreetam.cmplocationpicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.aryapreetam.cmplocationpicker.core.LocationPickerController
import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.provider.DefaultIpLocationProvider
import io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider
import io.github.aryapreetam.cmplocationpicker.provider.MapProvider
import io.github.aryapreetam.cmplocationpicker.provider.osm.LeafletOsmMapProvider
import io.github.aryapreetam.cmpwebview.WebView
import io.github.aryapreetam.cmpwebview.rememberWebViewController

@Composable
fun LocationPickerDialog(
  onDismiss: () -> Unit,
  onLocationPicked: (LatLng) -> Unit,
  modifier: Modifier = Modifier,
  strings: LocationPickerStrings = LocationPickerStrings(),
  options: LocationPickerOptions = LocationPickerOptions(),
  mapProvider: MapProvider = LeafletOsmMapProvider(),
  ipLocationProvider: IpLocationProvider = DefaultIpLocationProvider(),
  fallbackCenter: LatLng = DefaultIpLocationProvider.DefaultFallback
) {
  val scope = rememberCoroutineScope()
  val webViewController = rememberWebViewController()
  val controller = remember(scope, ipLocationProvider, webViewController) {
    LocationPickerController(
      scope = scope,
      ipLocationProvider = ipLocationProvider,
      webViewController = webViewController
    )
  }

  val state by controller.state.collectAsState()

  val html = remember(fallbackCenter, strings, options, mapProvider) {
    mapProvider.createHtml(
      fallbackCenter = fallbackCenter,
      strings = strings,
      options = options
    )
  }

  LaunchedEffect(Unit) {
    controller.startIpCenterLookup()
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    val containerShape = RoundedCornerShape(16.dp)

    Column(
      modifier = modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.9f)
        .clip(containerShape)
        .background(Color.White)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onDismiss),
          contentAlignment = Alignment.Center
        ) {
          BasicText(
            text = "X",
            modifier = Modifier.padding(4.dp),
            style = TextStyle(fontSize = 20.sp)
          )
        }

        Spacer(Modifier.width(8.dp))

        BasicText(
          text = strings.title,
          modifier = Modifier.weight(1f),
          style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        )

        Spacer(Modifier.width(8.dp))

        val canPick = state.selectedLocation != null
        BasicText(
          text = strings.selectButton,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = canPick) {
              state.selectedLocation?.let(onLocationPicked)
              onDismiss()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (canPick) Color(0xFF1565C0) else Color(0xFF9E9E9E)
          )
        )
      }

      Spacer(Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFE0E0E0)))

      Box(modifier = Modifier.fillMaxSize()) {
        WebView(
          htmlContent = html,
          modifier = Modifier.fillMaxSize(),
          controller = webViewController,
          onScriptResult = { msg -> controller.onJsMessage(msg) }
        )

        if (state.isLoading) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.White.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
          ) {
            BasicText(
              text = strings.loadingMap,
              style = TextStyle(fontSize = 14.sp, color = Color.Black)
            )
          }
        }
      }
    }
  }
}
