package io.github.aryapreetam.cmplocationpicker.provider

/**
 * Places/autocomplete provider.
 *
 * In v0 (Leaflet+Nominatim HTML) this runs inside the page and does not surface as Kotlin calls,
 * but we keep the abstraction so future providers (e.g., Google Places) can be swapped in.
 */
interface PlacesProvider
