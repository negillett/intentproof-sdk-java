package com.intentproof.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.util.LinkedHashMap;
import java.util.Map;

/** Wire JSON shape shared with Python/Node SDKs. */
public final class ExecutionWire {
  /**
   * Minimal JSON body used when serialization fails inside {@link HttpExporter} (matches other
   * SDKs).
   */
  public static final String HTTP_EXPORTER_FALLBACK_BODY =
      "{\"intentproof\":\"1\",\"eventSerializeFailed\":true}";

  private static final ObjectMapper MAPPER =
      new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  private ExecutionWire() {}

  /**
   * Converts an execution event to the cross-SDK wire map (string keys, JSON-safe values).
   *
   * @param e event to serialize
   * @return mutable map suitable for JSON encoding
   */
  public static Map<String, Object> toWireMap(ExecutionEvent e) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", e.getId());
    out.put("intent", e.getIntent());
    out.put("action", e.getAction());
    out.put("inputs", inputsWire(e));
    boolean ok = e.getStatus() == IntentProofExecutionEventV1.Status.OK;
    out.put("status", ok ? "ok" : "error");
    out.put("startedAt", e.getStartedAt());
    out.put("completedAt", e.getCompletedAt());
    Number dur = e.getDurationMs();
    out.put(
        "durationMs",
        dur == null ? null : (dur.doubleValue() == dur.longValue() ? dur.longValue() : dur));
    if (e.getCorrelationId() != null) {
      out.put("correlationId", e.getCorrelationId());
    }
    if (ok || e.getOutput() != null) {
      out.put("output", e.getOutput());
    }
    if (e.getError() != null) {
      Map<String, Object> err = new LinkedHashMap<>();
      ExecutionError er = e.getError();
      err.put("name", er.getName());
      err.put("message", er.getMessage());
      if (er.getStack() != null) {
        err.put("stack", er.getStack());
      }
      if (er.getCode() != null) {
        err.put("code", er.getCode());
      }
      out.put("error", err);
    }
    Map<String, Object> attrs = attributesWire(e);
    if (attrs != null && !attrs.isEmpty()) {
      out.put("attributes", attrs);
    }
    return out;
  }

  private static Object inputsWire(ExecutionEvent e) {
    if (e.getInputs() == null) {
      return Map.of();
    }
    Map<String, Object> m = e.getInputs().getAdditionalProperties();
    return m.isEmpty() ? Map.of() : m;
  }

  private static Map<String, Object> attributesWire(ExecutionEvent e) {
    if (e.getAttributes() == null) {
      return null;
    }
    Map<String, Object> m = e.getAttributes().getAdditionalProperties();
    return m.isEmpty() ? null : m;
  }

  /**
   * Default HTTP POST body; matches Node {@code safeJsonEnvelope}.
   *
   * @param event event to wrap in the {@code intentproof} envelope
   * @return JSON string, or {@link #HTTP_EXPORTER_FALLBACK_BODY} on hard failure
   */
  public static String safeJsonEnvelope(ExecutionEvent event) {
    return safeJsonEnvelope(event, MAPPER);
  }

  /**
   * Same wire envelope as {@link #safeJsonEnvelope(ExecutionEvent)}; tests may supply a mapper that
   * fails deterministically.
   */
  static String safeJsonEnvelope(ExecutionEvent event, ObjectMapper mapper) {
    Map<String, Object> wire = toWireMap(event);
    try {
      Map<String, Object> root = new LinkedHashMap<>();
      root.put("intentproof", "1");
      root.put("event", wire);
      return mapper.writeValueAsString(root);
    } catch (JsonProcessingException first) {
      try {
        Map<String, Object> partial = new LinkedHashMap<>();
        partial.put("intentproof", "1");
        Map<String, Object> eventPartial = new LinkedHashMap<>();
        eventPartial.put("id", event.getId());
        eventPartial.put("action", event.getAction());
        eventPartial.put("intent", event.getIntent());
        boolean ok = event.getStatus() == IntentProofExecutionEventV1.Status.OK;
        eventPartial.put("status", ok ? "ok" : "error");
        if (event.getCorrelationId() != null) {
          eventPartial.put("correlationId", event.getCorrelationId());
        }
        eventPartial.put("startedAt", event.getStartedAt());
        eventPartial.put("completedAt", event.getCompletedAt());
        Number dur = event.getDurationMs();
        eventPartial.put(
            "durationMs",
            dur == null ? null : (dur.doubleValue() == dur.longValue() ? dur.longValue() : dur));
        partial.put("eventPartial", eventPartial);
        partial.put("note", "full event not JSON-serializable");
        return mapper.writeValueAsString(partial);
      } catch (JsonProcessingException second) {
        return HTTP_EXPORTER_FALLBACK_BODY;
      }
    }
  }
}
