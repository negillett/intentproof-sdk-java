package com.intentproof.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Configuration with partial-update semantics for {@link
 * IntentProofClient#configure(IntentProofConfig)}.
 */
public final class IntentProofConfig {
  private final boolean exportersSet;
  private final List<Exporter> exporters;
  private final boolean onExporterErrorSet;
  private final BiConsumer<Throwable, ExecutionEvent> onExporterError;
  private final boolean defaultAttributesSet;
  private final Map<String, Object> defaultAttributes;
  private final boolean includeErrorStackSet;
  private final boolean includeErrorStack;

  private IntentProofConfig(Builder b) {
    this.exportersSet = b.exportersSet;
    this.exporters = b.exporters;
    this.onExporterErrorSet = b.onExporterErrorSet;
    this.onExporterError = b.onExporterError;
    this.defaultAttributesSet = b.defaultAttributesSet;
    this.defaultAttributes = b.defaultAttributes;
    this.includeErrorStackSet = b.includeErrorStackSet;
    this.includeErrorStack = b.includeErrorStack;
  }

  /**
   * @return {@code true} if {@link Builder#exporters(java.util.List)} was set on the builder
   */
  public boolean exportersSet() {
    return exportersSet;
  }

  /**
   * @return configured exporters list when set, otherwise builder default
   */
  public List<Exporter> exporters() {
    return exporters;
  }

  /**
   * @return {@code true} if {@link Builder#onExporterError(java.util.function.BiConsumer)} was set
   *     on the builder
   */
  public boolean onExporterErrorSet() {
    return onExporterErrorSet;
  }

  /**
   * @return exporter error handler when set
   */
  public BiConsumer<Throwable, ExecutionEvent> onExporterError() {
    return onExporterError;
  }

  /**
   * @return {@code true} if {@link Builder#defaultAttributes(java.util.Map)} was set on the builder
   */
  public boolean defaultAttributesSet() {
    return defaultAttributesSet;
  }

  /**
   * @return default attributes merged into wrapped calls when set
   */
  public Map<String, Object> defaultAttributes() {
    return defaultAttributes;
  }

  /**
   * @return {@code true} if {@link Builder#includeErrorStack(boolean)} was set on the builder
   */
  public boolean includeErrorStackSet() {
    return includeErrorStackSet;
  }

  /**
   * @return default stack capture policy when set
   */
  public boolean includeErrorStack() {
    return includeErrorStack;
  }

  /**
   * @return new mutable builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link IntentProofConfig}. */
  public static final class Builder {
    private boolean exportersSet;
    private List<Exporter> exporters;

    private boolean onExporterErrorSet;
    private BiConsumer<Throwable, ExecutionEvent> onExporterError;

    private boolean defaultAttributesSet;
    private Map<String, Object> defaultAttributes;

    private boolean includeErrorStackSet;
    private boolean includeErrorStack;

    private Builder() {}

    /**
     * Sets the exporter chain (replaces prior value when merged via {@link
     * IntentProofClient#configure}).
     *
     * @param exporters non-null exporter list
     * @return this builder
     */
    public Builder exporters(List<Exporter> exporters) {
      this.exportersSet = true;
      this.exporters = new ArrayList<>(Objects.requireNonNull(exporters, "exporters"));
      return this;
    }

    /**
     * Sets the callback invoked when an exporter throws or completes exceptionally.
     *
     * @param onExporterError non-null handler
     * @return this builder
     */
    public Builder onExporterError(BiConsumer<Throwable, ExecutionEvent> onExporterError) {
      this.onExporterErrorSet = true;
      this.onExporterError = onExporterError;
      return this;
    }

    /**
     * Sets default JSON-safe attributes merged into every wrapped execution.
     *
     * @param defaultAttributes non-null attribute map
     * @return this builder
     */
    public Builder defaultAttributes(Map<String, Object> defaultAttributes) {
      this.defaultAttributesSet = true;
      this.defaultAttributes = Map.copyOf(Objects.requireNonNull(defaultAttributes));
      return this;
    }

    /**
     * Sets whether stack traces are captured by default when errors are recorded.
     *
     * @param includeErrorStack {@code true} to include stacks unless overridden per wrap
     * @return this builder
     */
    public Builder includeErrorStack(boolean includeErrorStack) {
      this.includeErrorStackSet = true;
      this.includeErrorStack = includeErrorStack;
      return this;
    }

    /**
     * @return immutable configuration snapshot
     */
    public IntentProofConfig build() {
      return new IntentProofConfig(this);
    }
  }
}
