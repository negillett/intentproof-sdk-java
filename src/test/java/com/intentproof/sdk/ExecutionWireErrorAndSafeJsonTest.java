package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentproof.sdk.fixtures.ExecutionEventFixtures;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionWireErrorAndSafeJsonTest {

  @Test
  void executionErrorSnapshotAndWireStackBranch() {
    ExecutionErrorSnapshot withStack = new ExecutionErrorSnapshot("E", "m", "stack-here");
    assertTrue(withStack.hasStack());
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId("id");
    ev.setIntent("i");
    ev.setAction("a");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId(null);
    ev.setOutput(null);
    ExecutionError gen = new ExecutionError();
    gen.setName(withStack.name());
    gen.setMessage(withStack.message());
    gen.setStack(withStack.stack());
    ev.setError(gen);
    ev.setAttributes(null);
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
