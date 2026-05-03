package com.intentproof.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Options for {@link IntentProofClient#wrap(WrapOptions, java.util.function.Function)}. */
public final class WrapOptions {
  private final String intent;
  private final String action;
  private final String correlationId;
  private final Map<String, Object> attributes;
  private final Function<List<Object>, Object> captureInput;
  private final Function<Object, Object> captureOutput;
  private final Function<Object, Object> captureError;
  private final Boolean includeErrorStack;
  private final Integer maxDepth;
  private final Integer maxKeys;
  private final List<String> redactKeys;
  private final Integer maxStringLength;

  private WrapOptions(Builder b) {
    this.intent = b.intent;
    this.action = b.action;
    this.correlationId = b.correlationId;
    this.attributes = b.attributes == null ? null : Map.copyOf(b.attributes);
    this.captureInput = b.captureInput;
    this.captureOutput = b.captureOutput;
    this.captureError = b.captureError;
    this.includeErrorStack = b.includeErrorStack;
    this.maxDepth = b.maxDepth;
    this.maxKeys = b.maxKeys;
    this.redactKeys = b.redactKeys == null ? null : List.copyOf(b.redactKeys);
    this.maxStringLength = b.maxStringLength;
  }

  /**
   * @return proof intent label (required when building)
   */
  public String intent() {
    return intent;
  }

  /**
   * @return stable operation identifier (required when building)
   */
  public String action() {
    return action;
  }

  /**
   * @return explicit correlation id override, or {@code null} to use thread context
   */
  public String correlationId() {
    return correlationId;
  }

  /**
   * @return extra JSON-safe attributes merged into the event, or {@code null}
   */
  public Map<String, Object> attributes() {
    return attributes;
  }

  /**
   * @return custom input capture function, or {@code null} for default snapshot
   */
  public Function<List<Object>, Object> captureInput() {
    return captureInput;
  }

  /**
   * @return custom successful-result capture function, or {@code null} for default snapshot
   */
  public Function<Object, Object> captureOutput() {
    return captureOutput;
  }

  /**
   * @return custom error capture function, or {@code null} for default handling
   */
  public Function<Object, Object> captureError() {
    return captureError;
  }

  /**
   * @return per-wrap stack capture override, or {@code null} for client default
   */
  public Boolean includeErrorStack() {
    return includeErrorStack;
  }

  /**
   * @return snapshot max object depth limit, or {@code null} for default
   */
  public Integer maxDepth() {
    return maxDepth;
  }

  /**
   * @return snapshot max map/list keys limit, or {@code null} for default
   */
  public Integer maxKeys() {
    return maxKeys;
  }

  /**
   * @return keys to redact in snapshots, or {@code null} for default
   */
  public List<String> redactKeys() {
    return redactKeys;
  }

  /**
   * @return max string length in snapshots, or {@code null} for default
   */
  public Integer maxStringLength() {
    return maxStringLength;
  }

  /**
   * @return new mutable builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link WrapOptions}. */
  public static final class Builder {
    private String intent;
    private String action;
    private String correlationId;
    private Map<String, Object> attributes;
    private Function<List<Object>, Object> captureInput;
    private Function<Object, Object> captureOutput;
    private Function<Object, Object> captureError;
    private Boolean includeErrorStack;
    private Integer maxDepth;
    private Integer maxKeys;
    private List<String> redactKeys;
    private Integer maxStringLength;

    private Builder() {}

    /**
     * @param intent non-blank proof intent
     * @return this builder
     */
    public Builder intent(String intent) {
      this.intent = intent;
      return this;
    }

    /**
     * @param action non-blank stable action id
     * @return this builder
     */
    public Builder action(String action) {
      this.action = action;
      return this;
    }

    /**
     * @param correlationId correlation override, or {@code null} / blank to use context
     * @return this builder
     */
    public Builder correlationId(String correlationId) {
      this.correlationId = correlationId;
      return this;
    }

    /**
     * @param attributes JSON-safe attribute map, or {@code null} for none
     * @return this builder
     */
    public Builder attributes(Map<String, Object> attributes) {
      this.attributes = attributes == null ? null : new HashMap<>(attributes);
      return this;
    }

    /**
     * @param captureInput maps positional args to stored inputs, or {@code null} for default
     * @return this builder
     */
    public Builder captureInput(Function<List<Object>, Object> captureInput) {
      this.captureInput = captureInput;
      return this;
    }

    /**
     * @param captureOutput maps successful return value to stored output, or {@code null} for
     *     default
     * @return this builder
     */
    public Builder captureOutput(Function<Object, Object> captureOutput) {
      this.captureOutput = captureOutput;
      return this;
    }

    /**
     * @param captureError maps thrown error to stored output field, or {@code null} for default
     * @return this builder
     */
    public Builder captureError(Function<Object, Object> captureError) {
      this.captureError = captureError;
      return this;
    }

    /**
     * @param includeErrorStack {@code true}/{@code false} override, or {@code null} for client
     *     default
     * @return this builder
     */
    public Builder includeErrorStack(Boolean includeErrorStack) {
      this.includeErrorStack = includeErrorStack;
      return this;
    }

    /**
     * @param maxDepth snapshot recursion depth limit, or {@code null} for default
     * @return this builder
     */
    public Builder maxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    /**
     * @param maxKeys snapshot max keys per object/list, or {@code null} for default
     * @return this builder
     */
    public Builder maxKeys(Integer maxKeys) {
      this.maxKeys = maxKeys;
      return this;
    }

    /**
     * @param redactKeys map keys to replace with {@code [REDACTED]}, or {@code null} for default
     * @return this builder
     */
    public Builder redactKeys(List<String> redactKeys) {
      this.redactKeys = redactKeys == null ? null : new ArrayList<>(redactKeys);
      return this;
    }

    /**
     * @param maxStringLength max characters retained per string in snapshots, or {@code null} for
     *     default
     * @return this builder
     */
    public Builder maxStringLength(Integer maxStringLength) {
      this.maxStringLength = maxStringLength;
      return this;
    }

    /**
     * @return immutable options (requires non-blank {@code intent} and {@code action})
     */
    public WrapOptions build() {
      if (intent == null) {
        throw new IllegalStateException("WrapOptions: intent is required");
      }
      if (action == null) {
        throw new IllegalStateException("WrapOptions: action is required");
      }
      return new WrapOptions(this);
    }
  }
}
