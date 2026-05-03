package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class IntentProofClientTest {

  @Test
  void defaultClientUsesMemoryExporter() {
    IntentProofClient c = new IntentProofClient();
    Function<Integer, Integer> add =
        c.wrap(
            WrapOptions.builder().intent("Add numbers").action("math.add").build(),
            (Integer a) -> a + 1);
    assertEquals(6, add.apply(5));
    // Cannot directly access MemoryExporter — configure explicit exporter for assertions below.
  }

  @Test
  void correlationExplicitOverridesContext() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(
            IntentProofConfig.builder()
                .exporters(List.of(mem))
                .defaultAttributes(Map.of("service", "billing-api", "env", "test"))
                .build());

    Function<Map<String, Object>, Map<String, Object>> createRefund =
        c.wrap(
            WrapOptions.builder()
                .intent("Return captured funds")
                .action("stripe.refund.create")
                .attributes(Map.of("vendor", "stripe"))
                .captureInput(
                    args -> {
                      @SuppressWarnings("unchecked")
                      Map<String, Object> m = (Map<String, Object>) args.get(0);
                      return Map.of("paymentIntentId", m.get("paymentIntentId"));
                    })
                .captureOutput(
                    out -> {
                      @SuppressWarnings("unchecked")
                      Map<String, Object> m = (Map<String, Object>) out;
                      return Map.of("refundId", m.get("id"));
                    })
                .build(),
            inp -> Map.of("id", "re_1", "amountCents", inp.get("amountCents")));

    IntentProof.runWithCorrelationId(
        "req_outer",
        () ->
            createRefund.apply(
                Map.of(
                    "paymentIntentId",
                    "pi_1",
                    "amountCents",
                    100,
                    "correlationId",
                    "should_not_win")));

    ExecutionEvent e = mem.getEvents().get(mem.getEvents().size() - 1);
    assertEquals("req_outer", e.correlationId());

    mem.clear();
    Function<Map<String, Object>, Map<String, Object>> scoped =
        c.wrap(
            WrapOptions.builder()
                .intent("Refund")
                .action("stripe.refund.create")
                .correlationId("explicit-wrap-id")
                .build(),
            inp -> inp);

    scoped.apply(Map.of());
    e = mem.getEvents().get(mem.getEvents().size() - 1);
    assertEquals("explicit-wrap-id", e.correlationId());
  }

  @Test
  void withCorrelationBlankUsesRandomUuid() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    AtomicReference<String> seen = new AtomicReference<>();
    c.withCorrelation(
        "   ",
        () -> {
          seen.set(c.getCorrelationId());
        });
    assertNotNull(seen.get());
    assertTrue(
        seen.get()
            .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
  }

  @Test
  void wrapCapturesErrorSidecar() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());

    Function<Map<String, Object>, Object> pay =
        c.wrap(
            WrapOptions.builder()
                .intent("Capture authorized funds")
                .action("stripe.payment_intent.capture")
                .captureInput(
                    args ->
                        Map.of("paymentIntentId", ((Map<?, ?>) args.get(0)).get("paymentIntentId")))
                .captureError(err -> Map.of("code", "card_declined", "retryable", false))
                .build(),
            inp -> {
              throw new RuntimeException("Your card was declined.");
            });

    assertThrows(RuntimeException.class, () -> pay.apply(Map.of("paymentIntentId", "pi_x")));

    ExecutionEvent e = mem.getEvents().get(0);
    assertEquals(ExecutionStatus.error, e.status());
    assertEquals("RuntimeException", e.error().name());
    assertEquals("Your card was declined.", e.error().message());
    assertEquals(Map.of("code", "card_declined", "retryable", false), e.output());
  }

  @Test
  void validationRejectsBlankIntent() {
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of()).build());
    assertThrows(
        IllegalArgumentException.class,
        () -> c.wrap(WrapOptions.builder().intent("").action("x.a").build(), () -> {}));
  }

  @Test
  void assertCorrelationIdRejectsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> IntentProof.runWithCorrelationId(" \t\n", () -> {}));
  }

  @Test
  void wireOmitsEmptyAttributes() {
    ExecutionEvent ev =
        new ExecutionEvent(
            "id1",
            "i",
            "a",
            List.of(),
            ExecutionStatus.ok,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            null,
            42,
            null,
            null);
    Map<String, Object> wire = ExecutionWire.toWireMap(ev);
    assertNull(wire.get("attributes"));
  }
}
