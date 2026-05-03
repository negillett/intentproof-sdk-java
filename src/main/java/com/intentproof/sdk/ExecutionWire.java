package com.intentproof.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    out.put("id", e.id());
    out.put("intent", e.intent());
    out.put("action", e.action());
    out.put("inputs", e.inputs());
    out.put("status", e.status() == ExecutionStatus.ok ? "ok" : "error");
    out.put("startedAt", e.startedAt());
    out.put("completedAt", e.completedAt());
    out.put("durationMs", e.durationMs());
    if (e.correlationId() != null) {
      out.put("correlationId", e.correlationId());
    }
    if (e.status() == ExecutionStatus.ok || e.output() != null) {
      out.put("output", e.output());
    }
    if (e.error() != null) {
      Map<String, Object> err = new LinkedHashMap<>();
      err.put("name", e.error().name());
      err.put("message", e.error().message());
      if (e.error().stack() != null) {
        err.put("stack", e.error().stack());
      }
      out.put("error", err);
    }
    if (e.attributes() != null && !e.attributes().isEmpty()) {
      out.put("attributes", e.attributes());
    }
    return out;
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
        eventPartial.put("id", event.id());
        eventPartial.put("action", event.action());
        eventPartial.put("intent", event.intent());
        eventPartial.put("status", event.status() == ExecutionStatus.ok ? "ok" : "error");
        if (event.correlationId() != null) {
          eventPartial.put("correlationId", event.correlationId());
        }
        eventPartial.put("startedAt", event.startedAt());
        eventPartial.put("completedAt", event.completedAt());
        eventPartial.put("durationMs", event.durationMs());
        partial.put("eventPartial", eventPartial);
        partial.put("note", "full event not JSON-serializable");
        return mapper.writeValueAsString(partial);
      } catch (JsonProcessingException second) {
        return HTTP_EXPORTER_FALLBACK_BODY;
      }
    }
  }
}
