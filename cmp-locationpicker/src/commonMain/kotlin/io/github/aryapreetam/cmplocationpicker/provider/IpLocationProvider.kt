package io.github.aryapreetam.cmplocationpicker.provider

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

interface IpLocationProvider {
  /** Best-effort IP-based location lookup with a deterministic fallback. */
  suspend fun getBestEffortLatLng(): LatLng
}

class DefaultIpLocationProvider(
  private val endpoint: String = DefaultEndpoint,
  private val fallback: LatLng = DefaultFallback,
  private val client: HttpClient = HttpClient()
) : IpLocationProvider {
  override suspend fun getBestEffortLatLng(): LatLng {
    return runCatching {
      val text = fetchText(endpoint) ?: return fallback
      parseIpApiJsonResponse(text) ?: fallback
    }.getOrElse { fallback }
  }

  private suspend fun fetchText(url: String): String? {
    val response = client.get(url) {
      // Some free geo-IP providers are sensitive to missing/unknown user agents.
      header(HttpHeaders.UserAgent, DefaultUserAgent)
      header(HttpHeaders.Accept, "text/plain, application/json")
    }
    if (!response.status.isSuccess()) return null
    return response.bodyAsText().trim()
  }

  private fun parseIpApiJsonResponse(text: String): LatLng? = parseIpApiJson(text, json)

  companion object {
    private val json = Json { ignoreUnknownKeys = true }

    /** Default v0 endpoint: returns JSON that includes `latitude` and `longitude`. */
    const val DefaultEndpoint: String = "https://ipapi.co/json/"

    const val DefaultUserAgent: String = "cmp-locationpicker/0.0.1"

    // Best-effort default: center of India.
    val DefaultFallback: LatLng = LatLng(latitude = 20.5937, longitude = 78.9629)
  }
}

/** Parses `ipapi.co/json/` response. */
internal fun parseIpApiJson(text: String, json: Json = Json { ignoreUnknownKeys = true }): LatLng? {
  val root = runCatching { json.parseToJsonElement(text) }.getOrNull() as? JsonObject ?: return null
  val lat = root["latitude"]?.jsonPrimitive?.doubleOrNull ?: return null
  val lng = root["longitude"]?.jsonPrimitive?.doubleOrNull ?: return null
  return runCatching { LatLng(latitude = lat, longitude = lng) }.getOrNull()
}
