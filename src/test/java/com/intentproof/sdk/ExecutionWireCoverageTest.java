package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentproof.sdk.generated.v1.Attributes;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Targets JaCoCo gaps in {@link ExecutionWire} branches not exercised elsewhere. */
class ExecutionWireCoverageTest {

  private static ExecutionEvent minimalOk() {
    ExecutionEvent e = new ExecutionEvent();
    e.setId("id");
    e.setIntent("i");
    e.setAction("a");
    e.setInputs(new Inputs());
    e.setStatus(IntentProofExecutionEventV1.Status.OK);
    e.setStartedAt("2026-01-01T00:00:00.000Z");
    e.setCompletedAt("2026-01-01T00:00:00.001Z");
    e.setDurationMs(1.0);
    e.setCorrelationId(null);
    e.setOutput(null);
    e.setError(null);
    e.setAttributes(null);
    return e;
  }

  @Test
  void durationMsKeepsFractionalNumbersOnWire() {
    ExecutionEvent e = minimalOk();
    e.setDurationMs(1.25);
    assertEquals(1.25, ExecutionWire.toWireMap(e).get("durationMs"));
  }

  @Test
  void durationMsNullOnWire() {
    ExecutionEvent e = minimalOk();
    e.setDurationMs(null);
    assertEquals(null, ExecutionWire.toWireMap(e).get("durationMs"));
  }

  @Test
  void errorStatusOmitsOutputWhenNull() {
    ExecutionEvent e = minimalOk();
    e.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    e.setOutput(null);
    ExecutionError er = new ExecutionError();
    er.setName("E");
    er.setMessage("m");
    e.setError(er);
    assertFalse(ExecutionWire.toWireMap(e).containsKey("output"));
  }

  @Test
  void nonEmptyAttributesIncludedOnWire() {
    ExecutionEvent e = minimalOk();
    Attributes a = new Attributes();
    a.getAdditionalProperties().put("k", "v");
    e.setAttributes(a);
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) ExecutionWire.toWireMap(e).get("attributes");
    assertEquals("v", attrs.get("k"));
  }

  @Test
  void errorBlockIncludesOptionalCode() {
    ExecutionEvent e = minimalOk();
    e.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    ExecutionError er = new ExecutionError();
    er.setName("E");
    er.setMessage("m");
    er.setCode("ERR");
    e.setError(er);
    @SuppressWarnings("unchecked")
    Map<String, Object> err = (Map<String, Object>) ExecutionWire.toWireMap(e).get("error");
    assertEquals("ERR", err.get("code"));
  }

  @Test
  void inputsNullMappedToEmptyObjectOnWire() {
    ExecutionEvent e = minimalOk();
    e.setInputs(null);
    assertTrue(((Map<?, ?>) ExecutionWire.toWireMap(e).get("inputs")).isEmpty());
  }

  @Test
  void attributesPresentButEmptyOmitted() {
    ExecutionEvent e = minimalOk();
    com.intentproof.sdk.generated.v1.Attributes a =
        new com.intentproof.sdk.generated.v1.Attributes();
    e.setAttributes(a);
    assertFalse(ExecutionWire.toWireMap(e).containsKey("attributes"));
  }

  @Test
  void partialEnvelopeUsesFractionalDurationMs() throws Exception {
    ExecutionEvent e = minimalOk();
    e.setDurationMs(2.5);
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
    String json = ExecutionWire.safeJsonEnvelope(e, once);
    assertTrue(json.contains("\"durationMs\":2.5"), json);
  }

  @Test
  void partialEnvelopeHandlesNullDurationMs() throws Exception {
    ExecutionEvent e = minimalOk();
    e.setDurationMs(null);
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
    String json = ExecutionWire.safeJsonEnvelope(e, once);
    assertTrue(json.contains("\"durationMs\":null"), json);
  }

  @Test
  void inputsFromCapturedNullViaReflection() throws Exception {
    IntentProofClient c = new IntentProofClient();
    Method m = IntentProofClient.class.getDeclaredMethod("inputsFromCaptured", Object.class);
    m.setAccessible(true);
    Object r = m.invoke(c, (Object) null);
    assertEquals(0, ((Inputs) r).getAdditionalProperties().size());
  }
}
