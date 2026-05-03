package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class IntentProofClientAsyncAwaitStagesTest {

  @Test
  void awaitStageSuccessEmitsOk() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Function<Integer, CompletableFuture<Integer>> f =
        c.wrap(
            WrapOptions.builder().intent("ok").action("async.ok").build(),
            i -> CompletableFuture.supplyAsync(() -> i + 1));
    assertEquals(2, f.apply(1).get());
    assertEquals(ExecutionStatus.ok, mem.getEvents().get(0).status());
  }

  @Test
  void awaitStageAlreadyCompletedFuture() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
    Function<Integer, CompletableFuture<Integer>> f =
        c.wrap(
            WrapOptions.builder().intent("sync cf").action("async.sync").build(),
            i -> CompletableFuture.completedFuture(i + 5));
    assertEquals(8, f.apply(3).get());
    assertEquals(ExecutionStatus.ok, mem.getEvents().get(0).status());
  }
}
