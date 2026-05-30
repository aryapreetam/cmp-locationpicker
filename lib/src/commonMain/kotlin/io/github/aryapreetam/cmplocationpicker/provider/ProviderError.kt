package io.github.aryapreetam.cmplocationpicker.provider

/**
 * Error surfaced by a provider (map or places) that might trigger fallback.
 *
 * We intentionally keep this high-level; reliable quota detection generally requires host signals.
 */
sealed interface ProviderError {
  /** Auth/billing/quota errors (often only reliably detectable with host-provided info). */
  data class QuotaOrAuth(val message: String? = null) : ProviderError

  /** Network/connectivity errors. */
  data class Network(val message: String? = null) : ProviderError

  /** Any other provider-specific failure. */
  data class Unknown(val message: String? = null) : ProviderError
}
