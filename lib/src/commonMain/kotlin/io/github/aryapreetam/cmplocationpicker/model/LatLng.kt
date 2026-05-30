package io.github.aryapreetam.cmplocationpicker.model

/** Simple latitude/longitude coordinate in degrees. */
data class LatLng(
  val latitude: Double,
  val longitude: Double
) {
  init {
    require(latitude in -90.0..90.0) { "latitude out of range: $latitude" }
    require(longitude in -180.0..180.0) { "longitude out of range: $longitude" }
  }
}
