package com.intentproof.sdk;

import java.util.concurrent.CompletableFuture;

/**
 * Receives each emitted {@link ExecutionEvent}. Implementations must not throw from {@link
 * #export}; asynchronous failures should be reported via {@link CompletableFuture} or internal
 * handling.
 */
@FunctionalInterface
public interface Exporter {
  /**
   * Delivers one event. Must not throw synchronously; async implementations may return a {@link
   * java.util.concurrent.CompletionStage} (for example {@link CompletableFuture}) to signal
   * completion or failure.
   *
   * @param event the emission to handle
   * @return {@code null}, or a future / stage the client may await when draining exporters
   */
  Object export(ExecutionEvent event);

  /**
   * Wait until async side-effects are idle (optional).
   *
   * @return {@code null}, or a {@link java.util.concurrent.CompletionStage} the client may await
   */
  default Object flush() {
    return null;
  }

  /**
   * Stop accepting work and drain (optional). When not specialized, {@link IntentProofClient} falls
   * back to {@link #flush()}.
   *
   * @return {@code null}, or a {@link java.util.concurrent.CompletionStage} the client may await
   */
  default Object shutdown() {
    return null;
  }
}
