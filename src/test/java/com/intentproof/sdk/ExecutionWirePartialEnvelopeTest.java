package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExecutionWirePartialEnvelopeTest {

  @Test
  void safeJsonEnvelopePartialIncludesCorrelationWhenPrimarySerializationFails() {
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId(UUID.randomUUID().toString());
    ev.setIntent("intent");
    ev.setAction("action");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.OK);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId("corr-partial");
    ev.setOutput(1);
    ev.setError(null);
    ev.setAttributes(null);
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
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId(UUID.randomUUID().toString());
    ev.setIntent("intent");
    ev.setAction("action");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId(null);
    ev.setOutput(null);
    ExecutionError err = new ExecutionError();
    err.setName("E");
    err.setMessage("m");
    err.setStack(null);
    ev.setError(err);
    ev.setAttributes(null);
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
