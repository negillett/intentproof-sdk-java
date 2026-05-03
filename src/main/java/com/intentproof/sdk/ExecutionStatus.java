package com.intentproof.sdk;

/** Terminal outcome for a wrapped invocation. */
public enum ExecutionStatus {
  /** Invocation completed without a throwable error path. */
  ok,
  /** Invocation failed or completed with an error snapshot. */
  error
}
