package com.intentproof.sdk.fixtures;

import com.intentproof.sdk.ExecutionEvent;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.util.UUID;

/** Shared {@link ExecutionEvent} instances for HTTP and wire tests. */
public final class ExecutionEventFixtures {
  private ExecutionEventFixtures() {}

  public static ExecutionEvent syntheticOk() {
    ExecutionEvent e = new ExecutionEvent();
    e.setId(UUID.randomUUID().toString());
    e.setIntent("i");
    e.setAction("a");
    e.setInputs(new Inputs());
    e.setStatus(IntentProofExecutionEventV1.Status.OK);
    e.setStartedAt("2026-01-01T00:00:00.000Z");
    e.setCompletedAt("2026-01-01T00:00:00.001Z");
    e.setDurationMs(1.0);
    e.setCorrelationId("corr-test");
    e.setOutput(1);
    e.setError(null);
    e.setAttributes(null);
    return e;
  }
}
