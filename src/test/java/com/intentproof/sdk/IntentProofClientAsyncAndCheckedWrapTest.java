package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.intentproof.sdk.fixtures.SneakyThrows;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class IntentProofClientAsyncAndCheckedWrapTest {

  static final class OddThrowable extends Throwable {}

  static final class OddChecked extends Exception {}

  private static void throwOddChecked() {
    throw SneakyThrows.sneakyThrow(new OddChecked());
  }

  @Test
  void wrapCompletableFutureSuccessAndFailurePaths() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());

    Function<Integer, CompletableFuture<Integer>> ok =
        c.wrap(
            WrapOptions.builder().intent("async ok").action("async.ok").build(),
            i -> CompletableFuture.completedFuture(i + 10));
    assertEquals(11, ok.apply(1).get());

    Function<Integer, CompletableFuture<Integer>> bad =
        c.wrap(
            WrapOptions.builder().intent("async err").action("async.err").build(),
            i -> {
              CompletableFuture<Integer> cf = new CompletableFuture<>();
              cf.completeExceptionally(new OddThrowable());
              return cf;
            });
    java.util.concurrent.CompletionException joinEx =
        assertThrows(java.util.concurrent.CompletionException.class, () -> bad.apply(1).join());
    assertInstanceOf(OddThrowable.class, joinEx.getCause());
    ExecutionEvent last = mem.getEvents().get(mem.getEvents().size() - 1);
    assertEquals(ExecutionStatus.error, last.status());
  }

  @Test
  void wrapSyncBodyThrowsPlainCheckedThrowableWrappedAsRuntimeException() {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Supplier<Integer> wrapped =
        c.wrap(
            WrapOptions.builder().intent("checked").action("sync.checked").build(),
            () -> {
              throwOddChecked();
              return 0;
            });
    RuntimeException rex = assertThrows(RuntimeException.class, wrapped::get);
    assertInstanceOf(OddChecked.class, rex.getCause());
    ExecutionEvent last = mem.getEvents().get(mem.getEvents().size() - 1);
    assertEquals(ExecutionStatus.error, last.status());
  }
}
