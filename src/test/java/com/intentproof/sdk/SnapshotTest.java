package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SnapshotTest {

  @Test
  void redactsKeysCaseInsensitive() {
    SerializeOptions opts =
        SerializeOptions.builder().redactKeys(java.util.List.of("", "password", "TOKEN")).build();
    @SuppressWarnings("unchecked")
    Map<String, Object> out =
        (Map<String, Object>)
            Snapshot.snapshot(Map.of("password", "secret", "token", "t", "ok", 1), opts);
    assertEquals("[REDACTED]", out.get("password"));
    assertEquals("[REDACTED]", out.get("token"));
    assertEquals(1L, out.get("ok"));
  }

  @Test
  void truncatesStringsAndUsesLimits() {
    Object truncated =
        Snapshot.snapshot("abcdefghij", SerializeOptions.builder().maxStringLength(4).build());
    assertTrue(truncated.toString().contains("abcd"));
    assertTrue(truncated.toString().contains("truncated"));

    Map<String, Object> wide = new LinkedHashMap<>();
    for (int i = 0; i < 5; i++) {
      wide.put("k" + i, i);
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> sw =
        (Map<String, Object>)
            Snapshot.snapshot(wide, SerializeOptions.builder().maxKeys(3).build());
    assertTrue(sw.containsKey("…"));
    assertTrue(sw.get("…").toString().contains("more keys"));
  }

  @Test
  void cyclesBecomeCircularToken() {
    Map<String, Object> circular = new LinkedHashMap<>();
    circular.put("a", 1);
    circular.put("self", circular);
    @SuppressWarnings("unchecked")
    Map<String, Object> out = (Map<String, Object>) Snapshot.snapshot(circular);
    assertEquals(1L, out.get("a"));
    assertEquals("[Circular]", out.get("self"));
  }
}
