package com.intentproof.sdk.fixtures;

import com.intentproof.sdk.ExecutionEvent;
import java.util.ArrayDeque;

/**
 * Violates {@link java.util.Queue} contract on purpose: first {@link #poll()} can return {@code
 * null} while the deque is non-empty, so tests can exercise defensive branches in {@code
 * BoundedQueueExporter}.
 */
public final class PollReturnsNullOnceDeque extends ArrayDeque<ExecutionEvent> {
  private boolean returnedNull;

  @Override
  public ExecutionEvent poll() {
    if (!returnedNull && !super.isEmpty()) {
      returnedNull = true;
      return null;
    }
    return super.poll();
  }
}
