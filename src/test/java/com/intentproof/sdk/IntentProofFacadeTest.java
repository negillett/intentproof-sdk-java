package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class IntentProofFacadeTest {

  @Test
  void singletonFacadeAndVersion() {
    assertNotNull(IntentProof.VERSION);
    assertFalse(IntentProof.VERSION.isBlank());
    assertNotNull(IntentProof.client());
    assertEquals(IntentProof.getClient(), IntentProof.getIntentProofClient());
    assertEquals(IntentProof.client(), IntentProof.getIntentProofClient());
  }

  @Test
  void assertHelpers() {
    IntentProof.assertCorrelationId("x");
    IntentProof.assertWrapOptionsShape(WrapOptions.builder().intent("a").action("b").build());
  }
}
