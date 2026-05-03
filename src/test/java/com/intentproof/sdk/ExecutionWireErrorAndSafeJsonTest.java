package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentproof.sdk.fixtures.ExecutionEventFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionWireErrorAndSafeJsonTest {

  @Test
  void executionErrorSnapshotAndWireStackBranch() {
    ExecutionErrorSnapshot withStack = new ExecutionErrorSnapshot("E", "m", "stack-here");
    assertTrue(withStack.hasStack());
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
            null,
            null,
            withStack,
            null);
    Map<String, Object> wire = ExecutionWire.toWireMap(ev);
    @SuppressWarnings("unchecked")
    Map<String, Object> err = (Map<String, Object>) wire.get("error");
    assertEquals("stack-here", err.get("stack"));
  }

  @Test
  void safeJsonEnvelopeFallbackTiers() {
    ExecutionEvent ok = ExecutionEventFixtures.syntheticOk();
    ObjectMapper failOnce =
        new ObjectMapper() {
          private boolean failed;

          @Override
          public String writeValueAsString(Object value) throws JsonProcessingException {
            if (!failed) {
              failed = true;
              throw new JsonProcessingException("first") {};
            }
            return super.writeValueAsString(value);
          }
        };
    String partialOk = ExecutionWire.safeJsonEnvelope(ok, failOnce);
    assertTrue(partialOk.contains("eventPartial"));

    ObjectMapper failTwice =
        new ObjectMapper() {
          @Override
          public String writeValueAsString(Object value) throws JsonProcessingException {
            throw new JsonProcessingException("always") {};
          }
        };
    assertEquals(
        ExecutionWire.HTTP_EXPORTER_FALLBACK_BODY, ExecutionWire.safeJsonEnvelope(ok, failTwice));
  }
}
