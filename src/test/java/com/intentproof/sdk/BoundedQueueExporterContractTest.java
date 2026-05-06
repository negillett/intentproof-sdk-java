package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.intentproof.sdk.fixtures.PollReturnsNullOnceDeque;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
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
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId(UUID.randomUUID().toString());
    ev.setIntent("i");
    ev.setAction("a");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.OK);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId(null);
    ev.setOutput(1);
    ev.setError(null);
    ev.setAttributes(null);
    assertDoesNotThrow(() -> q.export(ev));
  }
}
