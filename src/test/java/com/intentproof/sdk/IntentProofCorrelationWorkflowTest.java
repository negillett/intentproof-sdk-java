package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class IntentProofCorrelationWorkflowTest {

  @Test
  void runWithCorrelationIdSupplierReturnsValue() {
    String v =
        IntentProof.runWithCorrelationId(
            "corr-supplier", () -> "ok-" + IntentProof.getCorrelationId());
    assertEquals("ok-corr-supplier", v);
  }

  @Test
  void nestedCorrelationRestoresOuterId() {
    AtomicReference<String> insideInner = new AtomicReference<>();
    AtomicReference<String> afterInner = new AtomicReference<>();
    IntentProof.runWithCorrelationId(
        "outer",
        () -> {
          IntentProof.runWithCorrelationId(
              "inner", () -> insideInner.set(IntentProof.getCorrelationId()));
          afterInner.set(IntentProof.getCorrelationId());
        });
    assertEquals("inner", insideInner.get());
    assertEquals("outer", afterInner.get());
  }

  @Test
  void clientWithCorrelationFreshUuidAndExplicitId() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    AtomicReference<String> seen = new AtomicReference<>();
    c.withCorrelation(() -> seen.set(c.getCorrelationId()));
    assertNotNull(seen.get());
    c.withCorrelation("fixed-id", () -> assertEquals("fixed-id", c.getCorrelationId()));
  }

  @Test
  void supplierNestedRestoresContext() {
    String got =
        IntentProof.runWithCorrelationId(
            "outer",
            () ->
                IntentProof.runWithCorrelationId(
                    "inner", () -> "cid=" + IntentProof.getCorrelationId()));
    assertEquals("cid=inner", got);
    assertNull(IntentProof.getCorrelationId());
  }
}
