package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WrapOptionsSerializeOptionsTest {

  @Test
  void wrapOptionsBuildAndSetters() {
    assertThrows(IllegalStateException.class, () -> WrapOptions.builder().action("a").build());
    assertThrows(IllegalStateException.class, () -> WrapOptions.builder().intent("i").build());
    WrapOptions w =
        WrapOptions.builder()
            .intent("i")
            .action("a")
            .correlationId("c")
            .attributes(Map.of("k", 1L))
            .captureInput(x -> x)
            .captureOutput(x -> x)
            .captureError(x -> x)
            .includeErrorStack(false)
            .maxDepth(1)
            .maxKeys(2)
            .redactKeys(List.of("a"))
            .maxStringLength(3)
            .build();
    assertNotNull(w);
    assertNotNull(
        WrapOptions.builder().intent("i2").action("a2").attributes(null).redactKeys(null).build());
  }

  @Test
  void serializeOptionsToBuilderRoundTrip() {
    SerializeOptions s1 =
        SerializeOptions.builder()
            .maxDepth(2)
            .maxKeys(3)
            .redactKeys(List.of("p"))
            .maxStringLength(9)
            .build();
    SerializeOptions s2 = s1.toBuilder().build();
    assertEquals(2, s2.maxDepth());
  }

  @Test
  void serializeOptionsRedactKeysNullInBuilder() {
    assertNotNull(SerializeOptions.builder().redactKeys(null).build());
  }
}
