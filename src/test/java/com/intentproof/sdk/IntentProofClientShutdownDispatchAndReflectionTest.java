package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class IntentProofClientShutdownDispatchAndReflectionTest {

  @Test
  void withCorrelationNullIdThrows() {
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of()).build());
    assertThrows(IllegalArgumentException.class, () -> c.withCorrelation(null, () -> {}));
  }

  @Test
  void shutdownImporterThrowsInShutdownAndFlush() throws Exception {
    Exporter evil =
        new Exporter() {
          @Override
          public Object export(ExecutionEvent event) {
            return null;
          }

          @Override
          public Object shutdown() {
            throw new AssertionError("sd");
          }

          @Override
          public Object flush() {
            throw new IllegalStateException("fl");
          }
        };
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(evil)).build());
    c.shutdown().get();
  }

  @Test
  void shutdownCatchesThrowableFromShutdown() throws Exception {
    Exporter ok =
        new Exporter() {
          @Override
          public Object export(ExecutionEvent event) {
            return null;
          }

          @Override
          public Object shutdown() {
            return null;
          }
        };
    Exporter bad =
        new Exporter() {
          @Override
          public Object export(ExecutionEvent event) {
            return null;
          }

          @Override
          public Object shutdown() {
            throw new NoClassDefFoundError("boom");
          }
        };
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(ok, bad)).build());
    c.shutdown().get();
  }

  @Test
  void dispatchSyncExportThrowable() {
    AtomicInteger calls = new AtomicInteger();
    Exporter boom =
        event -> {
          throw new LinkageError("sync");
        };
    IntentProofClient c =
        IntentProof.createClient(
            IntentProofConfig.builder()
                .exporters(List.of(boom))
                .onExporterError((t, e) -> calls.incrementAndGet())
                .build());
    c.wrap(WrapOptions.builder().intent("sync").action("a.sync").build(), () -> {}).run();
    assertTrue(calls.get() >= 1);
  }

  @Test
  void toErrorSnapshotUsesClassNameWhenSimpleNameEmpty() throws Exception {
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of()).build());
    java.lang.reflect.Method m =
        IntentProofClient.class.getDeclaredMethod(
            "toErrorSnapshot", Throwable.class, boolean.class);
    m.setAccessible(true);
    Throwable anon =
        new Throwable() {
          @Override
          public String getMessage() {
            return null;
          }
        };
    ExecutionErrorSnapshot snap = (ExecutionErrorSnapshot) m.invoke(c, anon, Boolean.FALSE);
    assertFalse(snap.name().isEmpty());
  }

  @Test
  void dispatchCompletableFutureFailureCallsSink() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    Exporter bad =
        event -> {
          CompletableFuture<Void> cf = new CompletableFuture<>();
          cf.completeExceptionally(new IllegalStateException("x"));
          return cf;
        };
    IntentProofClient c =
        IntentProof.createClient(
            IntentProofConfig.builder()
                .exporters(List.of(bad))
                .onExporterError((t, e) -> calls.incrementAndGet())
                .build());
    c.wrap(WrapOptions.builder().intent("d").action("a.d").build(), () -> {}).run();
    c.flush().get();
    assertTrue(calls.get() >= 1);
  }

  @Test
  void threadDeathPropagatesAsError() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Runnable r =
        c.wrap(
            WrapOptions.builder().intent("td").action("a.td").build(),
            (Runnable)
                () -> {
                  throw new ThreadDeath();
                });
    assertThrows(ThreadDeath.class, r::run);
  }
}
