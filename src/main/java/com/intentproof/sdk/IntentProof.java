package com.intentproof.sdk;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Module entrypoint: default client instance, factory, correlation scope, and validation helpers
 * (see {@link IntentProofClient} for wrap/flush/shutdown).
 */
public final class IntentProof {
  private static final Object LOCK = new Object();
  private static volatile IntentProofClient defaultClient;

  /**
   * Build identity for this artifact (from the JAR manifest when published, otherwise a dev
   * fallback).
   */
  public static final String VERSION = VersionHolder.implementationVersionOrDev();

  private IntentProof() {}

  /**
   * Default singleton (lazy), same as {@link #getClient()}.
   *
   * @return shared {@link IntentProofClient} instance
   */
  public static IntentProofClient client() {
    return getClient();
  }

  /**
   * Returns the process-wide default client, creating it on first use.
   *
   * @return shared {@link IntentProofClient} instance
   */
  public static IntentProofClient getClient() {
    IntentProofClient c = defaultClient;
    if (c == null) {
      synchronized (LOCK) {
        c = defaultClient;
        if (c == null) {
          c = new IntentProofClient();
          defaultClient = c;
        }
      }
    }
    return c;
  }

  /**
   * Node SDK compatible alias for {@link #getClient()}.
   *
   * @return shared {@link IntentProofClient} instance
   */
  public static IntentProofClient getIntentProofClient() {
    return getClient();
  }

  /**
   * Creates a new client from the given configuration (does not affect the default singleton).
   *
   * @param config initial client configuration
   * @return new {@link IntentProofClient}
   */
  public static IntentProofClient createClient(IntentProofConfig config) {
    return new IntentProofClient(config);
  }

  /**
   * Reads the correlation id from thread-local context, if any.
   *
   * @return current correlation id, or {@code null}
   */
  public static String getCorrelationId() {
    return Correlation.get();
  }

  /**
   * Runs {@code fn} with {@code correlationId} installed in thread-local context.
   *
   * @param correlationId correlation id to assert and install
   * @param fn work to run under that id
   */
  public static void runWithCorrelationId(String correlationId, Runnable fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertCorrelationId(correlationId);
    Correlation.runWith(correlationId, fn);
  }

  /**
   * Runs {@code fn} with {@code correlationId} installed in thread-local context and returns its
   * result.
   *
   * @param <T> result type
   * @param correlationId correlation id to assert and install
   * @param fn supplier to run under that id
   * @return value from {@code fn}
   */
  public static <T> T runWithCorrelationId(String correlationId, Supplier<T> fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertCorrelationId(correlationId);
    return Correlation.runWith(correlationId, fn);
  }

  /**
   * Validates a correlation id string (throws if invalid).
   *
   * @param id candidate correlation id
   */
  public static void assertCorrelationId(String id) {
    Validation.assertCorrelationId(id);
  }

  /**
   * Validates {@link WrapOptions} shape (throws if invalid).
   *
   * @param options options to validate
   */
  public static void assertWrapOptionsShape(WrapOptions options) {
    Validation.assertWrapOptionsShape(options);
  }
}
