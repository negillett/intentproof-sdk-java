package com.intentproof.sdk;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Runtime validation helpers aligned with the Node SDK messages (adapted to Java types). */
public final class Validation {
  private Validation() {}

  /**
   * Validates a correlation id (non-blank string).
   *
   * @param id candidate id (must be a non-blank {@link String})
   */
  public static void assertCorrelationId(Object id) {
    if (!(id instanceof String s)) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"correlationId\" must be a string, got " + describe(id));
    }
    if (s.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"correlationId\" must be a non-empty string (trimmed length is 0)");
    }
  }

  /**
   * Validates {@link WrapOptions} field types and non-blank intent and action strings.
   *
   * @param options options instance to validate
   */
  public static void assertWrapOptionsShape(WrapOptions options) {
    Objects.requireNonNull(options, "options");
    assertWrapOptionsShape(
        options.intent(),
        options.action(),
        options.correlationId(),
        options.attributes(),
        options.includeErrorStack(),
        options.maxDepth(),
        options.maxKeys(),
        options.maxStringLength(),
        options.redactKeys());
  }

  /** Same checks as {@link #assertWrapOptionsShape(WrapOptions)}; package-private for tests. */
  static void assertWrapOptionsShape(
      Object intent,
      Object action,
      String correlationId,
      Map<String, Object> attributes,
      Object includeErrorStack,
      Object maxDepth,
      Object maxKeys,
      Object maxStringLength,
      Object redactKeys) {
    if (!(intent instanceof String intentStr)) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"intent\" must be a string, got " + describe(intent));
    }
    if (intentStr.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"intent\" must be a non-empty string (trimmed length is 0)");
    }
    if (!(action instanceof String actionStr)) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"action\" must be a string, got " + describe(action));
    }
    if (actionStr.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"action\" must be a non-empty string (trimmed length is 0)");
    }

    if (correlationId != null) {
      if (correlationId.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "IntentProofClient: \"correlationId\" must be a non-empty string when provided "
                + "(trimmed length is 0)");
      }
    }

    if (attributes != null) {
      assertAttributesRecord("WrapOptions.attributes", attributes);
    }

    if (includeErrorStack != null && !(includeErrorStack instanceof Boolean)) {
      throw new IllegalArgumentException(
          "IntentProofClient: \"includeErrorStack\" must be a boolean when provided, got "
              + describe(includeErrorStack));
    }

    if (maxDepth != null && !(maxDepth instanceof Integer)) {
      throw new IllegalArgumentException(
          "IntentProofClient: maxDepth must be Integer when provided");
    }
    if (maxKeys != null && !(maxKeys instanceof Integer)) {
      throw new IllegalArgumentException(
          "IntentProofClient: maxKeys must be Integer when provided");
    }
    if (maxStringLength != null && !(maxStringLength instanceof Integer)) {
      throw new IllegalArgumentException(
          "IntentProofClient: maxStringLength must be Integer when provided");
    }
    if (redactKeys != null && !(redactKeys instanceof List<?>)) {
      throw new IllegalArgumentException(
          "IntentProofClient: redactKeys must be a List when provided");
    }
  }

  /**
   * Ensures {@code value} is a map whose values are JSON-safe primitives only.
   *
   * @param label field name for error messages
   * @param value attribute map to validate
   */
  public static void assertAttributesRecord(String label, Map<String, Object> value) {
    Objects.requireNonNull(label, "label");
    if (value == null) {
      throw new IllegalArgumentException(
          "IntentProofClient: " + label + " must be a plain object, got null");
    }
    for (Map.Entry<String, Object> e : value.entrySet()) {
      String key = e.getKey();
      Object v = e.getValue();
      if (!isAllowedAttrValue(v)) {
        throw new IllegalArgumentException(
            "IntentProofClient: "
                + label
                + "[\""
                + key
                + "\"] must be a string, number, or boolean, got "
                + describe(v));
      }
    }
  }

  static boolean isAllowedAttrValue(Object v) {
    if (v instanceof String || v instanceof Boolean) {
      return true;
    }
    return v instanceof Number;
  }

  static String describe(Object value) {
    if (value == null) {
      return "null";
    }
    return value.getClass().getSimpleName().isEmpty()
        ? value.getClass().getName().toLowerCase(Locale.ROOT)
        : value.getClass().getSimpleName().toLowerCase(Locale.ROOT);
  }

  static void assertExporterAtIndex(Object ex, int index) {
    if (ex == null || !(ex instanceof Exporter)) {
      throw new IllegalArgumentException(
          "IntentProofClient: exporters[" + index + "] must be an object with an export() method");
    }
  }
}
