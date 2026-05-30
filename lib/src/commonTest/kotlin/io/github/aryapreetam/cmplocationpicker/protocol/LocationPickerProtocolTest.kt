package io.github.aryapreetam.cmplocationpicker.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationPickerProtocolTest {
  @Test
  fun parseIncoming_rejectsMissingType() {
    val res = LocationPickerProtocol.parseIncoming("{\"id\":\"1\"}")
    assertTrue(res is LocationPickerProtocol.ParseResult.Error)
    res as LocationPickerProtocol.ParseResult.Error
    assertEquals("missing type", res.reason)
    assertEquals("1", res.id)
  }

  @Test
  fun parseIncoming_acceptsReady() {
    val res = LocationPickerProtocol.parseIncoming("{\"type\":\"ready\",\"id\":\"abc\"}")
    assertTrue(res is LocationPickerProtocol.ParseResult.Success)
    val msg = (res as LocationPickerProtocol.ParseResult.Success).message
    assertTrue(msg is LocationPickerProtocol.Incoming.Ready)
    assertEquals("abc", msg.id)
  }

  @Test
  fun parseIncoming_acceptsLocationUpdate() {
    val res = LocationPickerProtocol.parseIncoming(
      "{\"type\":\"location_update\",\"id\":\"m1\",\"lat\":12.34,\"lng\":56.78}"
    )
    assertTrue(res is LocationPickerProtocol.ParseResult.Success)
    val msg = (res as LocationPickerProtocol.ParseResult.Success).message
    assertTrue(msg is LocationPickerProtocol.Incoming.LocationUpdate)
    assertEquals("m1", msg.id)
    assertEquals(12.34, msg.latLng.latitude)
    assertEquals(56.78, msg.latLng.longitude)
  }

  @Test
  fun parseIncoming_rejectsOutOfRangeLatLng() {
    val res = LocationPickerProtocol.parseIncoming(
      "{\"type\":\"location_update\",\"id\":\"m2\",\"lat\":123.0,\"lng\":56.78}"
    )
    assertTrue(res is LocationPickerProtocol.ParseResult.Error)
    res as LocationPickerProtocol.ParseResult.Error
    assertEquals("m2", res.id)
  }

  @Test
  fun parseIncoming_rejectsTooLarge() {
    val huge = "x".repeat(LocationPickerProtocol.DefaultMaxMessageChars + 1)
    val res = LocationPickerProtocol.parseIncoming(huge)
    assertTrue(res is LocationPickerProtocol.ParseResult.Error)
    res as LocationPickerProtocol.ParseResult.Error
    assertEquals("message too large", res.reason)
  }

  @Test
  fun ackJavaScript_containsId() {
    val js = LocationPickerProtocol.ackJavaScript(id = "abc", ok = true)
    assertTrue(js.contains("\"id\":\"abc\""))
    assertTrue(js.contains("\"ok\":true"))
  }
}
