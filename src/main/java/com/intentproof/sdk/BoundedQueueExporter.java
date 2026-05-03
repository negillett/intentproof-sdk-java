package com.intentproof.sdk;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

/**
 * Bounded concurrency + backlog for async-heavy exporters.
 *
 * <p>{@link Exporter#export}, {@link Exporter#flush}, and {@link Exporter#shutdown} may return
 * {@link java.util.concurrent.CompletableFuture} instances; callers are responsible for joining or
 * handling errors from those futures.
 */
@SuppressWarnings("FutureReturnValueIgnored")
public final class BoundedQueueExporter implements Exporter {
  private final Exporter inner;
  private final int maxConcurrent;
  private final int maxQueue;
  private final QueueOverflowStrategy strategy;
  private final BiConsumer<ExecutionEvent, String> onDrop;
  private final BiConsumer<Throwable, ExecutionEvent> onInnerError;

  private final Queue<ExecutionEvent> queue;
  private volatile boolean accepting = true;
  private int active;
  private final ConcurrentLinkedQueue<CompletableFuture<Void>> idleWaiters =
      new ConcurrentLinkedQueue<>();

  /**
   * Wraps {@code options.exporter()} with bounded concurrency and queueing.
   *
   * @param options queue and inner exporter configuration
   */
  public BoundedQueueExporter(BoundedQueueExporterOptions options) {
    this(options, new ArrayDeque<>());
  }

  /**
   * Same as {@link #BoundedQueueExporter(BoundedQueueExporterOptions)} with an injectable queue
   * (for tests).
   *
   * @param options queue configuration
   * @param queue backing queue instance
   */
  BoundedQueueExporter(BoundedQueueExporterOptions options, Queue<ExecutionEvent> queue) {
    Objects.requireNonNull(options, "options");
    Objects.requireNonNull(queue, "queue");
    Exporter innerEx = options.exporter();
    if (innerEx == null) {
      throw new IllegalArgumentException(
          "BoundedQueueExporter: \"exporter\" must be an object with an export() method");
    }
    this.inner = innerEx;
    int rawMc = options.maxConcurrent() != null ? options.maxConcurrent() : 4;
    this.maxConcurrent = Math.max(1, rawMc);
    int rawQ = options.maxQueue() != null ? options.maxQueue() : 1000;
    if (rawQ == 0) {
      this.maxQueue = 0;
    } else if (rawQ < 0) {
      this.maxQueue = 1000;
    } else {
      this.maxQueue = rawQ;
    }
    QueueOverflowStrategy st =
        options.strategy() != null ? options.strategy() : QueueOverflowStrategy.DROP_NEWEST;
    this.strategy = st;
    this.onDrop = options.onDrop();
    this.onInnerError = options.onInnerError();
    this.queue = queue;
  }

  @Override
  public Object export(ExecutionEvent event) {
    if (!accepting) {
      if (onDrop != null) {
        onDrop.accept(event, "shutdown");
      }
      return null;
    }

    long cap = maxQueue <= 0 ? Long.MAX_VALUE : maxQueue;

    synchronized (queue) {
      if (queue.size() >= cap) {
        if (strategy == QueueOverflowStrategy.DROP_OLDEST) {
          ExecutionEvent dropped = queue.poll();
          if (dropped != null && onDrop != null) {
            onDrop.accept(dropped, "queue-overflow-drop-oldest");
          }
          queue.add(event);
        } else {
          if (onDrop != null) {
            onDrop.accept(event, "queue-overflow-drop-newest");
          }
        }
        pumpLocked();
        return null;
      }
      queue.add(event);
      pumpLocked();
    }
    return null;
  }

  private void pumpLocked() {
    while (active < maxConcurrent && !queue.isEmpty()) {
      ExecutionEvent next = queue.poll();
      if (next == null) {
        break;
      }
      active++;
      runInner(next)
          .whenComplete(
              (ok, err) -> {
                synchronized (queue) {
                  active--;
                  pumpLocked();
                  resolveIdleWaitersIfNeededLocked();
                }
              });
    }
  }

  private CompletableFuture<Void> runInner(ExecutionEvent event) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            Object r = inner.export(event);
            if (r instanceof CompletableFuture<?> cf) {
              cf.join();
            }
          } catch (Throwable e) {
            if (onInnerError != null) {
              onInnerError.accept(e, event);
            }
          }
        });
  }

  private void resolveIdleWaitersIfNeededLocked() {
    if (queue.isEmpty() && active == 0) {
      CompletableFuture<Void> w;
      while ((w = idleWaiters.poll()) != null) {
        w.complete(null);
      }
    }
  }

  @Override
  public Object flush() {
    CompletableFuture<Void> done = new CompletableFuture<>();
    synchronized (queue) {
      if (queue.isEmpty() && active == 0) {
        done.complete(null);
      } else {
        idleWaiters.add(done);
      }
    }
    return done;
  }

  @Override
  public Object shutdown() {
    accepting = false;
    return flush();
  }

  /** Immutable options for {@link BoundedQueueExporter}. */
  public static final class BoundedQueueExporterOptions {
    private final Exporter exporter;
    private final Integer maxConcurrent;
    private final Integer maxQueue;
    private final QueueOverflowStrategy strategy;
    private final BiConsumer<ExecutionEvent, String> onDrop;
    private final BiConsumer<Throwable, ExecutionEvent> onInnerError;

    private BoundedQueueExporterOptions(Builder b) {
      this.exporter = b.exporter;
      this.maxConcurrent = b.maxConcurrent;
      this.maxQueue = b.maxQueue;
      this.strategy = b.strategy;
      this.onDrop = b.onDrop;
      this.onInnerError = b.onInnerError;
    }

    /**
     * @return wrapped inner exporter
     */
    public Exporter exporter() {
      return exporter;
    }

    /**
     * @return configured max concurrent inner exports, or {@code null} for default
     */
    public Integer maxConcurrent() {
      return maxConcurrent;
    }

    /**
     * @return configured max queued events, or {@code null} for default
     */
    public Integer maxQueue() {
      return maxQueue;
    }

    /**
     * @return overflow strategy when the queue is full, or {@code null} for default
     */
    public QueueOverflowStrategy strategy() {
      return strategy;
    }

    /**
     * @return callback when an event is dropped, or {@code null}
     */
    public BiConsumer<ExecutionEvent, String> onDrop() {
      return onDrop;
    }

    /**
     * @return callback when the inner exporter throws, or {@code null}
     */
    public BiConsumer<Throwable, ExecutionEvent> onInnerError() {
      return onInnerError;
    }

    /**
     * @return new options builder
     */
    public static Builder builder() {
      return new Builder();
    }

    /** Fluent builder for {@link BoundedQueueExporterOptions}. */
    public static final class Builder {
      private Exporter exporter;
      private Integer maxConcurrent;
      private Integer maxQueue;
      private QueueOverflowStrategy strategy;
      private BiConsumer<ExecutionEvent, String> onDrop;
      private BiConsumer<Throwable, ExecutionEvent> onInnerError;

      /**
       * @param exporter inner exporter to wrap (required)
       * @return this builder
       */
      public Builder exporter(Exporter exporter) {
        this.exporter = exporter;
        return this;
      }

      /**
       * @param maxConcurrent maximum concurrent exports to the inner exporter (minimum {@code 1}
       *     applied)
       * @return this builder
       */
      public Builder maxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
        return this;
      }

      /**
       * @param maxQueue maximum queued events before overflow handling ({@code 0} means unbounded)
       * @return this builder
       */
      public Builder maxQueue(int maxQueue) {
        this.maxQueue = maxQueue;
        return this;
      }

      /**
       * @param strategy how to handle overflow when the configured max queue size is exceeded
       * @return this builder
       */
      public Builder strategy(QueueOverflowStrategy strategy) {
        this.strategy = strategy;
        return this;
      }

      /**
       * @param onDrop invoked with the dropped event and a reason string
       * @return this builder
       */
      public Builder onDrop(BiConsumer<ExecutionEvent, String> onDrop) {
        this.onDrop = onDrop;
        return this;
      }

      /**
       * @param onInnerError invoked when the inner {@link Exporter#export} throws
       * @return this builder
       */
      public Builder onInnerError(BiConsumer<Throwable, ExecutionEvent> onInnerError) {
        this.onInnerError = onInnerError;
        return this;
      }

      /**
       * @return immutable options
       */
      public BoundedQueueExporterOptions build() {
        return new BoundedQueueExporterOptions(this);
      }
    }
  }
}
