package com.intentproof.sdk;

import java.util.Locale;

/** Policy for {@link BoundedQueueExporter} when the backlog reaches {@code maxQueue}. */
public enum QueueOverflowStrategy {
  /** Drop the event being offered when the queue is full. */
  DROP_NEWEST,
  /** Remove the oldest queued event to make room for the new one. */
  DROP_OLDEST;

  /**
   * @return stable kebab-case name for cross-runtime configuration
   */
  public String wireName() {
    return name().toLowerCase(Locale.ROOT).replace('_', '-');
  }

  /**
   * Parses a wire-format overflow strategy name.
   *
   * @param s wire name such as {@code drop-newest}, or {@code null} for default
   * @return matching strategy
   * @throws IllegalArgumentException if {@code s} is unknown
   */
  public static QueueOverflowStrategy fromWire(String s) {
    if (s == null) {
      return DROP_NEWEST;
    }
    return switch (s) {
      case "drop-newest" -> DROP_NEWEST;
      case "drop-oldest" -> DROP_OLDEST;
      default -> throw new IllegalArgumentException("Unknown queue overflow strategy: " + s);
    };
  }
}
