package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App() {
  var showDialog by remember { mutableStateOf(false) }
  var picked by remember { mutableStateOf<LatLng?>(null) }

  MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("cmp-locationpicker sample") },
          actions = {
            if (picked != null) {
              TextButton(onClick = { picked = null }) { Text("Clear") }
            }
          }
        )
      }
    ) { padding ->
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(onClick = { showDialog = true }) {
            Text("Pick location")
          }

          Text(
            text = picked?.let { "Picked: ${it.latitude}, ${it.longitude}" } ?: "No location selected"
          )
        }
      }
    }

    if (showDialog) {
      LocationPickerDialog(
        onDismiss = { showDialog = false },
        onLocationPicked = { picked = it },
        strings = LocationPickerStrings(
          title = "Pick location",
          closeContentDescription = "Close",
          selectButton = "Select",
          loadingMap = "Loading map…",
          searchPlaceholder = "Search location…",
          noResults = "No results",
          searchError = "Search error"
        ),
        options = LocationPickerOptions(
          searchLanguageTag = "en",
          countryCodes = "in"
        )
      )
    }
  }
}