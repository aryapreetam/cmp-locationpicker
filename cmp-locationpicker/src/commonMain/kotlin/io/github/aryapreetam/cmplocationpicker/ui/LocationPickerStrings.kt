package io.github.aryapreetam.cmplocationpicker.ui

/**
 * UI strings for the library.
 *
 * Defaults are English. Apps should pass localized strings.
 */
data class LocationPickerStrings(
  val title: String = "Pick location",
  val closeContentDescription: String = "Close",
  val selectButton: String = "Select",
  val loadingMap: String = "Loading map…",
  val searchPlaceholder: String = "Search location…",
  val noResults: String = "No results",
  val searchError: String = "Search error"
)
