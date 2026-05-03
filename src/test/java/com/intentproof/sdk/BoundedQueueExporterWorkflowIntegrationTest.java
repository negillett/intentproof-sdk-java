package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

class BoundedQueueExporterWorkflowIntegrationTest {

  @Test
  void dropNewestAndOldestAndFlush() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    List<String> drops = new CopyOnWriteArrayList<>();
    java.util.concurrent.CountDownLatch blockInner = new java.util.concurrent.CountDownLatch(1);
    Exporter slow =
        event -> {
          try {
            blockInner.await();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          return mem.export(event);
        };
    BoundedQueueExporter qNew =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder()
                .exporter(slow)
                .maxConcurrent(1)
                .maxQueue(1)
                .strategy(QueueOverflowStrategy.DROP_NEWEST)
                .onDrop((e, reason) -> drops.add(reason))
                .build());
    IntentProofClient c1 =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(qNew)).build());
    Runnable w = c1.wrap(WrapOptions.builder().intent("q").action("a.q").build(), () -> {});
    w.run();
    w.run();
    w.run();
    blockInner.countDown();
    ((CompletableFuture<?>) qNew.flush()).get();
    assertTrue(drops.stream().anyMatch(r -> r.contains("drop-newest")));

    mem.clear();
    drops.clear();
    java.util.concurrent.CountDownLatch blockOld = new java.util.concurrent.CountDownLatch(1);
    Exporter slowOld =
        event -> {
          try {
            blockOld.await();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          return mem.export(event);
        };
    BoundedQueueExporter qOld =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder()
                .exporter(slowOld)
                .maxConcurrent(1)
                .maxQueue(1)
                .strategy(QueueOverflowStrategy.DROP_OLDEST)
                .onDrop((e, reason) -> drops.add(reason))
                .build());
    IntentProofClient c2 =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(qOld)).build());
    Runnable w2 = c2.wrap(WrapOptions.builder().intent("q2").action("a.q2").build(), () -> {});
    w2.run();
    w2.run();
    w2.run();
    blockOld.countDown();
    ((CompletableFuture<?>) qOld.flush()).get();
    assertTrue(drops.stream().anyMatch(r -> r.contains("drop-oldest")));

    new BoundedQueueExporter(
        BoundedQueueExporter.BoundedQueueExporterOptions.builder()
            .exporter(mem)
            .maxQueue(0)
            .build());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BoundedQueueExporter(
                BoundedQueueExporter.BoundedQueueExporterOptions.builder().build()));

    BoundedQueueExporter qNeg =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder()
                .exporter(mem)
                .maxQueue(-9)
                .build());
    IntentProofClient cNeg =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(qNeg)).build());
    cNeg.wrap(WrapOptions.builder().intent("neg").action("a.neg").build(), () -> {}).run();
    ((CompletableFuture<?>) qNeg.flush()).get();

    List<String> innerErr = new CopyOnWriteArrayList<>();
    Exporter innerBoom =
        event -> {
          throw new IllegalStateException("inner");
        };
    BoundedQueueExporter qInner =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder()
                .exporter(innerBoom)
                .onInnerError((t, e) -> innerErr.add(t.getMessage()))
                .build());
    IntentProofClient c3 =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(qInner)).build());
    c3.wrap(WrapOptions.builder().intent("inner").action("a.inner").build(), () -> {}).run();
    ((CompletableFuture<?>) qInner.flush()).get();
    assertTrue(innerErr.stream().anyMatch(m -> m.contains("inner")));

    List<String> shutdownDrops = new CopyOnWriteArrayList<>();
    BoundedQueueExporter qSd =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder()
                .exporter(mem)
                .maxQueue(0)
                .onDrop((e, r) -> shutdownDrops.add(r))
                .build());
    IntentProofClient c4 =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(qSd)).build());
    c4.wrap(WrapOptions.builder().intent("sd").action("a.sd").build(), () -> {}).run();
    qSd.shutdown();
    c4.wrap(WrapOptions.builder().intent("after").action("a.after").build(), () -> {}).run();
    assertTrue(shutdownDrops.stream().anyMatch(r -> r.contains("shutdown")));
  }

  @Test
  void innerReturnsCompletableFuture() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    Exporter inner =
        event ->
            CompletableFuture.runAsync(
                () -> {
                  mem.export(event);
                });
    BoundedQueueExporter q =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder().exporter(inner).build());
    IntentProofClient c =
        IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(q)).build());
    c.wrap(WrapOptions.builder().intent("cf").action("a.cf").build(), () -> {}).run();
    ((CompletableFuture<?>) q.flush()).get();
    assertEquals(1, mem.getEvents().size());
  }
}
