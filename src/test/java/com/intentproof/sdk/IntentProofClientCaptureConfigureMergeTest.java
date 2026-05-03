package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class IntentProofClientCaptureConfigureMergeTest {

  @Test
  void captureHooksFallbackAndIncludeErrorStackOff() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(
            IntentProofConfig.builder().exporters(List.of(mem)).includeErrorStack(false).build());

    Function<Integer, Integer> cinFail =
        c.wrap(
            WrapOptions.builder()
                .intent("cin")
                .action("a.cin")
                .captureInput(
                    x -> {
                      throw new IllegalStateException("snap");
                    })
                .build(),
            i -> i + 1);
    assertEquals(4, cinFail.apply(3));

    Function<Integer, Integer> coutFail =
        c.wrap(
            WrapOptions.builder()
                .intent("cout")
                .action("a.cout")
                .captureOutput(
                    x -> {
                      throw new IllegalStateException("out");
                    })
                .build(),
            i -> i);
    assertEquals(2, coutFail.apply(2));

    Function<Integer, Integer> cerrCatch =
        c.wrap(
            WrapOptions.builder()
                .intent("cerr")
                .action("a.cerr")
                .captureError(
                    e -> {
                      throw new IllegalStateException("ignore");
                    })
                .build(),
            i -> {
              throw new RuntimeException("boom");
            });
    assertThrows(RuntimeException.class, () -> cerrCatch.apply(1));
    ExecutionEvent ev = mem.getEvents().get(mem.getEvents().size() - 1);
    assertNull(ev.output());
    assertNull(ev.error().stack());
  }

  @Test
  void configureRejectsNullOnExporterError() {
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of()).build());
    assertThrows(
        IllegalArgumentException.class,
        () -> c.configure(IntentProofConfig.builder().onExporterError(null).build()));
  }

  @Test
  void configureRejectsBadExporterSlot() {
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of()).build());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            c.configure(
                IntentProofConfig.builder()
                    .exporters(new ArrayList<>(java.util.Arrays.asList((Exporter) null)))
                    .build()));
  }

  @Test
  void mergeAttrsDirectlyExercisesPackagePrivateHelper() {
    assertNull(IntentProofClient.mergeAttrs(Map.of(), null));
    assertNull(IntentProofClient.mergeAttrs(null, Map.of()));
    assertEquals(Map.of("a", 1), IntentProofClient.mergeAttrs(Map.of("a", 1), Map.of()));
    assertEquals(
        Map.of("a", 1, "b", 2),
        IntentProofClient.mergeAttrs(Map.of("a", 0), Map.of("a", 1, "b", 2)));
  }

  @Test
  void emitErrorUsesPerWrapIncludeErrorStackAndSerializeLimits() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Runnable err =
        c.wrap(
            WrapOptions.builder()
                .intent("err")
                .action("a.err")
                .includeErrorStack(true)
                .maxDepth(2)
                .maxKeys(3)
                .redactKeys(List.of("secret"))
                .maxStringLength(9)
                .build(),
            (Runnable)
                () -> {
                  throw new RuntimeException("boom");
                });
    assertThrows(RuntimeException.class, err::run);
    assertNotNull(mem.getEvents().get(0).error().stack());
  }
}
