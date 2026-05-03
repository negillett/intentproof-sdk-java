package com.intentproof.sdk;

import java.util.Objects;

/** Serializable error surface for {@link ExecutionEvent}. */
public final class ExecutionErrorSnapshot {
  private final String name;
  private final String message;
  private final String stack;

  /**
   * @param name exception type name
   * @param message exception message text
   * @param stack stack trace text, or {@code null} when stacks are omitted
   */
  public ExecutionErrorSnapshot(String name, String message, String stack) {
    this.name = Objects.requireNonNull(name, "name");
    this.message = Objects.requireNonNull(message, "message");
    this.stack = stack;
  }

  /**
   * @return exception type name
   */
  public String name() {
    return name;
  }

  /**
   * @return exception message text
   */
  public String message() {
    return message;
  }

  /**
   * Stack trace text when capture policy includes stacks.
   *
   * @return stack text, or {@code null} when stacks are omitted
   */
  public String stack() {
    return stack;
  }

  /**
   * @return {@code true} when {@link #stack()} is non-null
   */
  public boolean hasStack() {
    return stack != null;
  }
}
