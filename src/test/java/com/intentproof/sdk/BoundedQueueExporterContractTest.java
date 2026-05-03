package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.intentproof.sdk.fixtures.PollReturnsNullOnceDeque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class BoundedQueueExporterContractTest {

  @Test
  void flushCompletesImmediatelyWhenIdle() throws Exception {
    MemoryExporter mem = new MemoryExporter();
    BoundedQueueExporter q =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder().exporter(mem).build());
    ((CompletableFuture<?>) q.flush()).get();
  }

  @Test
  void pumpBreaksWhenInternalPollReturnsNull() {
    MemoryExporter mem = new MemoryExporter();
    PollReturnsNullOnceDeque evil = new PollReturnsNullOnceDeque();
    BoundedQueueExporter q =
        new BoundedQueueExporter(
            BoundedQueueExporter.BoundedQueueExporterOptions.builder().exporter(mem).build(), evil);
    ExecutionEvent ev =
        new ExecutionEvent(
            UUID.randomUUID().toString(),
            "i",
            "a",
            List.of(),
            ExecutionStatus.ok,
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.001Z",
            1L,
            null,
            1,
            null,
            null);
    assertDoesNotThrow(() -> q.export(ev));
  }
}
