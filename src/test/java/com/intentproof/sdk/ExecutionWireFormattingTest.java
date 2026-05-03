package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionWireFormattingTest {

  @Test
  void omitsOutputOnErrorWhenNull() {
    ExecutionEvent ev =
        new ExecutionEvent(
            "id",
            "i",
            "a",
            List.of(),
            ExecutionStatus.error,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            "cid",
            null,
            new ExecutionErrorSnapshot("E", "m", "s"),
            null);
    Map<String, Object> m = ExecutionWire.toWireMap(ev);
    assertFalse(m.containsKey("output"));
    assertEquals("cid", m.get("correlationId"));
  }

  @Test
  void okEventIncludesAttributes() {
    ExecutionEvent ev =
        new ExecutionEvent(
            "id",
            "i",
            "a",
            List.of(),
            ExecutionStatus.ok,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            null,
            1,
            null,
            Map.of("tier", "gold"));
    assertTrue(ExecutionWire.toWireMap(ev).containsKey("attributes"));
  }

  @Test
  void executionErrorSnapshotHasStackFalse() {
    assertFalse(new ExecutionErrorSnapshot("E", "m", null).hasStack());
  }
}
