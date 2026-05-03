package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class IntentProofExporterErrorsFlushMemoryTest {

  @Test
  void syncExporterThrowAndAsyncExporterFailureCallHook() throws Exception {
    List<String> errors = new CopyOnWriteArrayList<>();
    MemoryExporter mem = new MemoryExporter();
    Exporter syncThrow =
        event -> {
          throw new IllegalStateException("sync");
        };
    Exporter asyncFail =
        event -> {
          CompletableFuture<Void> f = new CompletableFuture<>();
          f.completeExceptionally(new IllegalStateException("async"));
          return f;
        };
    IntentProofClient c =
        IntentProof.createClient(
            IntentProofConfig.builder()
                .exporters(List.of(mem, syncThrow, asyncFail))
                .onExporterError((t, e) -> errors.add(t.getMessage()))
                .build());
    Function<Integer, Integer> add =
        c.wrap(WrapOptions.builder().intent("x").action("a.x").build(), i -> i);
    add.apply(1);
    c.flush().get();
    assertTrue(errors.stream().anyMatch(m -> m.contains("sync")));
    assertTrue(errors.stream().anyMatch(m -> m.contains("async")));
  }

  @Test
  void defaultExporterErrorGoesToStderr() {
    MemoryExporter mem = new MemoryExporter();
    Exporter boom =
        event -> {
          throw new IllegalStateException("boom");
        };
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem, boom)).build());
    PrintStream old = System.err;
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    System.setErr(new PrintStream(buf));
    try {
      c.wrap(WrapOptions.builder().intent("e").action("a.e").build(), () -> {}).run();
    } finally {
      System.setErr(old);
    }
    assertTrue(buf.toString(StandardCharsets.UTF_8).contains("boom"));
  }

  @Test
  void flushAndShutdownWithCompletableFutureExporters() throws Exception {
    CompletableFuture<Void> flushFut = new CompletableFuture<>();
    Exporter fl =
        new Exporter() {
          @Override
          public Object export(ExecutionEvent event) {
            return null;
          }

          @Override
          public Object flush() {
            return flushFut;
          }
        };
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(fl)).build());
    CompletableFuture<Void> waitFlush = c.flush();
    assertFalse(waitFlush.isDone());
    flushFut.complete(null);
    waitFlush.get();

    Exporter sh =
        new Exporter() {
          @Override
          public Object export(ExecutionEvent event) {
            return null;
          }

          @Override
          public Object shutdown() {
            throw new RuntimeException("sd");
          }

          @Override
          public Object flush() {
            return CompletableFuture.completedFuture(null);
          }
        };
    IntentProofClient c2 =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(sh)).build());
    c2.shutdown().get();
  }

  @Test
  void memoryExporterRingBufferAndShutdownDefault() {
    MemoryExporter mem = new MemoryExporter(2);
    assertThrows(IllegalArgumentException.class, () -> new MemoryExporter(0));
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Runnable r = c.wrap(WrapOptions.builder().intent("m").action("a.m").build(), () -> {});
    r.run();
    r.run();
    r.run();
    assertEquals(2, mem.getEvents().size());
    assertNull(((Exporter) mem).shutdown());
    assertNull(((Exporter) mem).flush());
  }
}
