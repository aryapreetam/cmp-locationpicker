package io.github.aryapreetam.cmplocationpicker.provider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IpLocationProviderTest {
  @Test
  fun parseIpApiJson_parsesJson() {
    val res = parseIpApiJson("{\"latitude\":12.34,\"longitude\":56.78}")
    assertNotNull(res)
    assertEquals(12.34, res.latitude)
    assertEquals(56.78, res.longitude)
  }

  @Test
  fun parseIpApiJson_rejectsInvalidJson() {
    assertNull(parseIpApiJson("oops"))
    assertNull(parseIpApiJson("{\"latitude\":12.34}"))
    assertNull(parseIpApiJson("{\"longitude\":56.78}"))
  }
}
