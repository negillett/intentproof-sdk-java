package com.intentproof.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Default exporter: ring buffer in memory for debugging and tests (max 1000 events by default). */
public final class MemoryExporter implements Exporter {
  private final int maxEvents;
  private final List<ExecutionEvent> events = new ArrayList<>();

  /** Constructs an in-memory exporter retaining up to {@code 1000} events. */
  public MemoryExporter() {
    this(1000);
  }

  /**
   * @param maxEvents ring buffer capacity (oldest dropped when exceeded); must be {@code >= 1}
   */
  public MemoryExporter(int maxEvents) {
    if (maxEvents < 1) {
      throw new IllegalArgumentException(
          "MemoryExporter: \"maxEvents\" must be a finite number >= 1");
    }
    this.maxEvents = maxEvents;
  }

  @Override
  public Object export(ExecutionEvent event) {
    synchronized (events) {
      events.add(event);
      int overflow = events.size() - maxEvents;
      if (overflow > 0) {
        events.subList(0, overflow).clear();
      }
    }
    return null;
  }

  /**
   * Mutable snapshot for inspection — oldest first, newest last.
   *
   * @return unmodifiable copy of captured events
   */
  public List<ExecutionEvent> getEvents() {
    synchronized (events) {
      return Collections.unmodifiableList(new ArrayList<>(events));
    }
  }

  /** Removes all captured events from this exporter. */
  public void clear() {
    synchronized (events) {
      events.clear();
    }
  }

  @Override
  public Object flush() {
    return null;
  }
}
