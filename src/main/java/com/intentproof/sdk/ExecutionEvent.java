package com.intentproof.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * One execution record per wrapped invocation — stable fields for verifiers and ingest pipelines.
 */
public final class ExecutionEvent {
  private final String id;
  private final String intent;
  private final String action;
  private final Object inputs;
  private final ExecutionStatus status;
  private final String startedAt;
  private final String completedAt;
  private final long durationMs;
  private final String correlationId;
  private final Object output;
  private final ExecutionErrorSnapshot error;
  private final Map<String, Object> attributes;

  /**
   * @param id unique event id
   * @param intent human-readable proof intent
   * @param action stable operation identifier
   * @param inputs captured invocation inputs (JSON-safe snapshot shape)
   * @param status terminal status
   * @param startedAt UTC ISO-8601 start timestamp
   * @param completedAt UTC ISO-8601 completion timestamp
   * @param durationMs elapsed milliseconds
   * @param correlationId correlation id when present, else {@code null}
   * @param output captured return value when successful
   * @param error error snapshot when {@link ExecutionStatus#error}, else {@code null}
   * @param attributes optional JSON-safe attribute map
   */
  public ExecutionEvent(
      String id,
      String intent,
      String action,
      Object inputs,
      ExecutionStatus status,
      String startedAt,
      String completedAt,
      long durationMs,
      String correlationId,
      Object output,
      ExecutionErrorSnapshot error,
      Map<String, Object> attributes) {
    this.id = Objects.requireNonNull(id, "id");
    this.intent = Objects.requireNonNull(intent, "intent");
    this.action = Objects.requireNonNull(action, "action");
    this.inputs = inputs;
    this.status = Objects.requireNonNull(status, "status");
    this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
    this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
    this.durationMs = durationMs;
    this.correlationId = correlationId;
    this.output = output;
    this.error = error;
    this.attributes = attributes;
  }

  /**
   * @return unique event id
   */
  public String id() {
    return id;
  }

  /**
   * @return proof intent label
   */
  public String intent() {
    return intent;
  }

  /**
   * @return stable operation identifier
   */
  public String action() {
    return action;
  }

  /**
   * @return captured invocation inputs
   */
  public Object inputs() {
    return inputs;
  }

  /**
   * @return terminal invocation status
   */
  public ExecutionStatus status() {
    return status;
  }

  /**
   * @return UTC ISO-8601 start timestamp with {@code Z}
   */
  public String startedAt() {
    return startedAt;
  }

  /**
   * @return UTC ISO-8601 completion timestamp with {@code Z}
   */
  public String completedAt() {
    return completedAt;
  }

  /**
   * @return elapsed time between start and completion, in milliseconds
   */
  public long durationMs() {
    return durationMs;
  }

  /**
   * Correlation id when present — usually from context or {@link WrapOptions}.
   *
   * @return correlation id, or {@code null}
   */
  public String correlationId() {
    return correlationId;
  }

  /**
   * @return captured return value for successful invocations
   */
  public Object output() {
    return output;
  }

  /**
   * @return error snapshot for failed invocations, or {@code null}
   */
  public ExecutionErrorSnapshot error() {
    return error;
  }

  /**
   * JSON-safe attribute map (string / number / boolean values).
   *
   * @return attributes map, or {@code null}
   */
  public Map<String, Object> attributes() {
    return attributes;
  }
}
