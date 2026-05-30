package io.github.aryapreetam.cmplocationpicker.provider

/**
 * Hook for deciding whether to fallback from a primary provider (e.g., Google) to OSM/Leaflet.
 */
fun interface ProviderFallbackPolicy {
  fun shouldFallback(error: ProviderError): Boolean

  companion object {
    /** Conservative default: only fallback for obvious quota/auth errors. */
    val Default: ProviderFallbackPolicy = ProviderFallbackPolicy { err ->
      err is ProviderError.QuotaOrAuth
    }
  }
}
