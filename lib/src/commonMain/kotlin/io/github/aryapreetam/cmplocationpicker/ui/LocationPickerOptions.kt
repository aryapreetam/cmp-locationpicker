package io.github.aryapreetam.cmplocationpicker.ui

/**
 * Configuration for v0 Leaflet + Nominatim implementation.
 */
data class LocationPickerOptions(
  /**
   * Language tag forwarded to Nominatim as `accept-language` (best effort).
   * Examples: `en`, `hi`, `en-IN`.
   */
  val searchLanguageTag: String? = null,

  /**
   * Optional Nominatim country codes filter (comma-separated ISO 3166-1alpha2), e.g. `in`.
   * If null, no country filtering is applied.
   */
  val countryCodes: String? = "in",

  /** Max number of suggestions fetched from Nominatim. */
  val suggestionsLimit: Int = 5
)
