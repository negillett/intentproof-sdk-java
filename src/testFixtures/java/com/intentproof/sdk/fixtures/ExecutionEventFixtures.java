package com.intentproof.sdk.fixtures;

import com.intentproof.sdk.ExecutionEvent;
import com.intentproof.sdk.ExecutionStatus;
import java.util.List;
import java.util.UUID;

/** Shared {@link ExecutionEvent} instances for HTTP and wire tests. */
public final class ExecutionEventFixtures {
  private ExecutionEventFixtures() {}

  public static ExecutionEvent syntheticOk() {
    return new ExecutionEvent(
        UUID.randomUUID().toString(),
        "i",
        "a",
        List.of(),
        ExecutionStatus.ok,
        "2026-01-01T00:00:00.000Z",
        "2026-01-01T00:00:00.001Z",
        1L,
        "corr-test",
        1,
        null,
        null);
  }
}
