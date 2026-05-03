package com.intentproof.sdk;

import java.util.UUID;
import java.util.function.Supplier;

/** Thread-local correlation scope (synchronous code paths). */
final class Correlation {
  private static final ThreadLocal<String> STORE = new ThreadLocal<>();

  private Correlation() {}

  static String get() {
    return STORE.get();
  }

  static <T> T runWith(String correlationId, Supplier<T> fn) {
    String prev = STORE.get();
    STORE.set(correlationId);
    try {
      return fn.get();
    } finally {
      if (prev == null) {
        STORE.remove();
      } else {
        STORE.set(prev);
      }
    }
  }

  static void runWith(String correlationId, Runnable fn) {
    String prev = STORE.get();
    STORE.set(correlationId);
    try {
      fn.run();
    } finally {
      if (prev == null) {
        STORE.remove();
      } else {
        STORE.set(prev);
      }
    }
  }

  static String randomId() {
    return UUID.randomUUID().toString();
  }
}
