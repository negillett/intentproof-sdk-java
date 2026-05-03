package com.intentproof.sdk;

import java.util.concurrent.CompletionStage;

final class RuntimeUtil {
  private RuntimeUtil() {}

  static boolean isCompletionStage(Object x) {
    return x instanceof CompletionStage<?>;
  }

  @SuppressWarnings("unchecked")
  static <T> CompletionStage<T> asCompletionStage(Object x) {
    return (CompletionStage<T>) x;
  }
}
