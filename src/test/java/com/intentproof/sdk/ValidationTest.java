package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidationTest {

  @Test
  void describeExporterAndAttributes() {
    assertEquals("null", Validation.describe(null));
    assertEquals("int[]", Validation.describe(new int[0]));
    assertNotNull(Validation.describe(new Object() {}));
    assertEquals("integer", Validation.describe(Integer.valueOf(1)));

    assertThrows(IllegalArgumentException.class, () -> Validation.assertCorrelationId(42));
    assertThrows(IllegalArgumentException.class, () -> Validation.assertCorrelationId("  "));

    assertThrows(IllegalArgumentException.class, () -> Validation.assertExporterAtIndex(null, 0));

    assertThrows(IllegalArgumentException.class, () -> Validation.assertExporterAtIndex("nope", 0));

    assertThrows(
        IllegalArgumentException.class, () -> Validation.assertAttributesRecord("z", null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertAttributesRecord("z", Map.of("k", Map.of())));
  }

  @Test
  void assertWrapOptionsShapeRejectsRuntimeShapeViolations() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape(1, "a", null, null, null, null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            Validation.assertWrapOptionsShape("  ", "a", null, null, null, null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", 2, null, null, null, null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            Validation.assertWrapOptionsShape(
                "i", " \t", null, null, null, null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", "a", " ", null, null, null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", "a", null, null, "x", null, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", "a", null, null, null, 1L, null, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", "a", null, null, null, null, 1L, null, null));

    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.assertWrapOptionsShape("i", "a", null, null, null, null, null, 1L, null));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            Validation.assertWrapOptionsShape(
                "i", "a", null, null, null, null, null, null, Map.of("k", "v")));
  }
}
