package sample.app

import kotlin.test.Test
import kotlin.test.assertTrue
import io.github.aryapreetam.cmplocationpicker.protocol.LocationPickerProtocol

class SampleUITest {
  @Test
  fun protocolSmokeTest() {
    val res = LocationPickerProtocol.parseIncoming("{\"type\":\"ready\",\"id\":\"1\"}")
    assertTrue(res is LocationPickerProtocol.ParseResult.Success)
  }
}