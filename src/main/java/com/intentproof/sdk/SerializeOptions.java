package com.intentproof.sdk;

import java.util.ArrayList;
import java.util.List;

/** Controls snapshot limits for wrapped inputs and outputs. */
public final class SerializeOptions {
  private final Integer maxDepth;
  private final Integer maxKeys;
  private final List<String> redactKeys;
  private final Integer maxStringLength;

  private SerializeOptions(Builder b) {
    this.maxDepth = b.maxDepth;
    this.maxKeys = b.maxKeys;
    this.redactKeys = b.redactKeys == null ? null : List.copyOf(b.redactKeys);
    this.maxStringLength = b.maxStringLength;
  }

  /**
   * @return snapshot max depth, or {@code null} for default
   */
  public Integer maxDepth() {
    return maxDepth;
  }

  /**
   * @return snapshot max keys per collection/map, or {@code null} for default
   */
  public Integer maxKeys() {
    return maxKeys;
  }

  /**
   * @return keys to redact, or {@code null} for default
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

  /**
   * @return builder pre-populated with this instance's limits
   */
  public Builder toBuilder() {
    Builder b = builder();
    b.maxDepth = this.maxDepth;
    b.maxKeys = this.maxKeys;
    if (this.redactKeys != null) {
      b.redactKeys = new ArrayList<>(this.redactKeys);
    }
    b.maxStringLength = this.maxStringLength;
    return b;
  }

  /** Fluent builder for {@link SerializeOptions}. */
  public static final class Builder {
    private Integer maxDepth;
    private Integer maxKeys;
    private List<String> redactKeys;
    private Integer maxStringLength;

    private Builder() {}

    /**
     * @param maxDepth max object walk depth, or {@code null} for library default
     * @return this builder
     */
    public Builder maxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    /**
     * @param maxKeys max entries per map or list, or {@code null} for library default
     * @return this builder
     */
    public Builder maxKeys(Integer maxKeys) {
      this.maxKeys = maxKeys;
      return this;
    }

    /**
     * @param redactKeys keys to replace with redaction tokens, or {@code null} for none
     * @return this builder
     */
    public Builder redactKeys(List<String> redactKeys) {
      this.redactKeys = redactKeys == null ? null : new ArrayList<>(redactKeys);
      return this;
    }

    /**
     * @param maxStringLength max characters per string, or {@code null} for library default
     * @return this builder
     */
    public Builder maxStringLength(Integer maxStringLength) {
      this.maxStringLength = maxStringLength;
      return this;
    }

    /**
     * @return immutable options
     */
    public SerializeOptions build() {
      return new SerializeOptions(this);
    }
  }
}
