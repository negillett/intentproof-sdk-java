package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExecutionWirePartialEnvelopeTest {

  @Test
  void safeJsonEnvelopePartialIncludesCorrelationWhenPrimarySerializationFails() {
    ExecutionEvent ev =
        new ExecutionEvent(
            UUID.randomUUID().toString(),
            "intent",
            "action",
            List.of(),
            ExecutionStatus.ok,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            "corr-partial",
            1,
            null,
            null);
    ObjectMapper once =
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
    String json = ExecutionWire.safeJsonEnvelope(ev, once);
    assertTrue(json.contains("corr-partial"));
  }

  @Test
  void safeJsonEnvelopePartialUsesErrorStatusWhenEventFailed() {
    ExecutionEvent ev =
        new ExecutionEvent(
            UUID.randomUUID().toString(),
            "intent",
            "action",
            List.of(),
            ExecutionStatus.error,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            null,
            null,
            new ExecutionErrorSnapshot("E", "m", null),
            null);
    ObjectMapper once =
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
    String json = ExecutionWire.safeJsonEnvelope(ev, once);
    assertTrue(json.contains("\"status\":\"error\""), json);
  }
}
