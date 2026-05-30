package io.github.aryapreetam.cmplocationpicker.protocol

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/** JS↔Compose protocol for `cmp-locationpicker` (v0). */
object LocationPickerProtocol {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  /**
   * Upper bound to avoid accidental huge payloads from JS.
   *
   * Note: we validate by UTF-16 char length (good enough for our JSON messages).
   */
  const val DefaultMaxMessageChars: Int = 16_384

  sealed interface Incoming {
    val id: String

    data class Ready(override val id: String) : Incoming

    data class LocationUpdate(
      override val id: String,
      val latLng: LatLng
    ) : Incoming
  }

  sealed interface ParseResult {
    data class Success(val message: Incoming) : ParseResult

    /**
     * Parsing/validation error.
     * If [id] is non-null, Compose should ack with `ok=false`.
     */
    data class Error(val reason: String, val id: String? = null) : ParseResult
  }

  fun parseIncoming(raw: String, maxChars: Int = DefaultMaxMessageChars): ParseResult {
    if (raw.length > maxChars) return ParseResult.Error("message too large", id = null)

    val root = try {
      json.parseToJsonElement(raw)
    } catch (_: Throwable) {
      return ParseResult.Error("invalid json", id = null)
    }

    val obj = root as? JsonObject ?: return ParseResult.Error("expected json object", id = null)
    val id = obj.stringOrNull("id")
    val type = obj.stringOrNull("type") ?: return ParseResult.Error("missing type", id = id)

    return when (type) {
      "ready" -> {
        val idValue = id ?: return ParseResult.Error("missing id", id = null)
        ParseResult.Success(Incoming.Ready(idValue))
      }

      "location_update" -> {
        val idValue = id ?: return ParseResult.Error("missing id", id = null)
        val lat = obj.doubleOrNull("lat")
        val lng = obj.doubleOrNull("lng")
        val latValue = lat ?: return ParseResult.Error("missing lat", id = idValue)
        val lngValue = lng ?: return ParseResult.Error("missing lng", id = idValue)
        val latLng = try {
          LatLng(latitude = latValue, longitude = lngValue)
        } catch (t: Throwable) {
          return ParseResult.Error(t.message ?: "invalid lat/lng", id = idValue)
        }
        ParseResult.Success(Incoming.LocationUpdate(id = idValue, latLng = latLng))
      }

      else -> ParseResult.Error("unknown type: $type", id = id)
    }
  }

  /**
   * Compose→JS ack script. JS must define `window.CmpLocationPickerBridge.onAck(ackJson)`.
   *
   * We intentionally don't rely on return values.
   */
  fun ackJavaScript(id: String, ok: Boolean, error: String? = null): String {
    val safeId = id.escapeForJsonString()
    val safeError = error?.escapeForJsonString()
    val errorPart = if (safeError == null) "null" else "\"$safeError\""
    val ackJson = "{\"type\":\"ack\",\"id\":\"$safeId\",\"ok\":$ok,\"error\":$errorPart}"
    return "window.CmpLocationPickerBridge && window.CmpLocationPickerBridge.onAck($ackJson);"
  }
}

private fun JsonObject.stringOrNull(key: String): String? {
  val el = this[key] ?: return null
  val prim = el as? JsonPrimitive ?: return null
  return if (prim.isString) prim.content else null
}

private fun JsonObject.doubleOrNull(key: String): Double? {
  val el: JsonElement = this[key] ?: return null
  return el.jsonPrimitive.doubleOrNull
}

private fun String.escapeForJsonString(): String {
  // Minimal escape for JSON string literal content.
  return buildString(length) {
    for (c in this@escapeForJsonString) {
      when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
      }
    }
  }
}
