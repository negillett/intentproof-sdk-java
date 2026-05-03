package com.intentproof.sdk.fixtures;

/**
 * Unchecked rethrow for tests (same idea as {@code lombok.Lombok#sneakyThrow}) so checked
 * exceptions can escape {@link java.util.function.Supplier#get()} without an extra dependency.
 */
public final class SneakyThrows {
  private SneakyThrows() {}

  public static RuntimeException sneakyThrow(Throwable t) {
    if (t == null) {
      throw new NullPointerException("t");
    }
    return sneakyThrow0(t);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
    throw (T) t;
  }
}
