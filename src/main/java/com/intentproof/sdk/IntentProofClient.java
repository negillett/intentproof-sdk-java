package com.intentproof.sdk;

import com.intentproof.sdk.generated.v1.Attributes;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wraps callables to emit one {@link ExecutionEvent} per invocation. Correlation is read from
 * thread-local context set by {@link #withCorrelation(Runnable)}.
 */
public final class IntentProofClient {
  private final Object lock = new Object();
  private List<Exporter> exporters;
  private BiConsumer<Throwable, ExecutionEvent> onExporterError;
  private Map<String, Object> defaultAttributes;
  private boolean defaultIncludeErrorStack;

  /** Constructs a client with in-memory exporter and default options. */
  public IntentProofClient() {
    this.exporters = new ArrayList<>();
    this.exporters.add(new MemoryExporter());
    this.onExporterError = IntentProofClient::defaultOnExporterError;
    this.defaultAttributes = new LinkedHashMap<>();
    this.defaultIncludeErrorStack = true;
  }

  /**
   * Constructs a client and applies {@code config} via {@link #configure(IntentProofConfig)}.
   *
   * @param config initial configuration (partial updates supported)
   */
  public IntentProofClient(IntentProofConfig config) {
    this();
    configure(config);
  }

  /**
   * Merges {@code config} into this client (only fields explicitly set on the builder are applied).
   *
   * @param config partial configuration
   */
  public void configure(IntentProofConfig config) {
    Objects.requireNonNull(config, "config");
    synchronized (lock) {
      if (config.exportersSet()) {
        List<Exporter> next = config.exporters();
        for (int i = 0; i < next.size(); i++) {
          Validation.assertExporterAtIndex(next.get(i), i);
        }
        this.exporters = new ArrayList<>(next);
      }
      if (config.onExporterErrorSet()) {
        if (config.onExporterError() == null) {
          throw new IllegalArgumentException(
              "IntentProofClient: onExporterError must be a function, got null");
        }
        this.onExporterError = config.onExporterError();
      }
      if (config.defaultAttributesSet()) {
        Validation.assertAttributesRecord("defaultAttributes", config.defaultAttributes());
        this.defaultAttributes = new LinkedHashMap<>(config.defaultAttributes());
      }
      if (config.includeErrorStackSet()) {
        this.defaultIncludeErrorStack = config.includeErrorStack();
      }
    }
  }

  /**
   * Waits for all exporters that return {@link CompletableFuture} from {@link Exporter#flush()} to
   * complete.
   *
   * @return future that completes when pending exporter flushes finish
   */
  public CompletableFuture<Void> flush() {
    List<CompletableFuture<?>> futs = new ArrayList<>();
    List<Exporter> copy;
    synchronized (lock) {
      copy = new ArrayList<>(exporters);
    }
    for (Exporter ex : copy) {
      Object r = ex.flush();
      if (r instanceof CompletableFuture<?> cf) {
        futs.add(cf);
      }
    }
    if (futs.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    return CompletableFuture.allOf(futs.toArray(CompletableFuture[]::new));
  }

  /**
   * Shuts down exporters (best-effort flush then shutdown) and aggregates any returned futures.
   *
   * @return future that completes when shutdown-related work finishes
   */
  public CompletableFuture<Void> shutdown() {
    List<CompletableFuture<?>> futs = new ArrayList<>();
    List<Exporter> copy;
    synchronized (lock) {
      copy = new ArrayList<>(exporters);
    }
    for (Exporter ex : copy) {
      Object r;
      try {
        r = ex.shutdown();
      } catch (Throwable ignored) {
        r = null;
      }
      if (!(r instanceof CompletableFuture<?>)) {
        try {
          r = ex.flush();
        } catch (Throwable ignored) {
          r = null;
        }
      }
      if (r instanceof CompletableFuture<?> cf) {
        futs.add(cf);
      }
    }
    if (futs.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    return CompletableFuture.allOf(futs.toArray(CompletableFuture[]::new));
  }

  /**
   * Reads the correlation id from thread-local context for this thread.
   *
   * @return current correlation id, or {@code null}
   */
  public String getCorrelationId() {
    return Correlation.get();
  }

  /**
   * Runs {@code fn} under a freshly generated correlation id.
   *
   * @param fn work to run
   */
  public void withCorrelation(Runnable fn) {
    Objects.requireNonNull(fn, "fn");
    Correlation.runWith(Correlation.randomId(), fn);
  }

  /**
   * Runs {@code fn} under {@code correlationId} (blank ids are replaced with a random id).
   *
   * @param correlationId correlation id to install, or blank for random
   * @param fn work to run
   */
  public void withCorrelation(String correlationId, Runnable fn) {
    Objects.requireNonNull(fn, "fn");
    if (correlationId == null) {
      throw new IllegalArgumentException(
          "IntentProofClient: withCorrelation: correlation id must be a string");
    }
    if (correlationId.trim().isEmpty()) {
      Correlation.runWith(Correlation.randomId(), fn);
    } else {
      Correlation.runWith(correlationId, fn);
    }
  }

  /**
   * Wraps a supplier so each invocation emits an {@link ExecutionEvent}.
   *
   * @param <R> result type
   * @param options wrap metadata and capture policy
   * @param fn underlying supplier
   * @return wrapped supplier
   */
  public <R> Supplier<R> wrap(WrapOptions options, Supplier<R> fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertWrapOptionsShape(options);
    return () -> wrapInvoke(options, new Object[0], fn);
  }

  /**
   * Wraps a runnable so each invocation emits an {@link ExecutionEvent}.
   *
   * @param options wrap metadata and capture policy
   * @param fn underlying runnable
   * @return wrapped runnable
   */
  public Runnable wrap(WrapOptions options, Runnable fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertWrapOptionsShape(options);
    return () -> {
      wrapInvoke(
          options,
          new Object[0],
          () -> {
            fn.run();
            return null;
          });
    };
  }

  /**
   * Wraps a unary function so each invocation emits an {@link ExecutionEvent}.
   *
   * @param <T> argument type
   * @param <R> result type
   * @param options wrap metadata and capture policy
   * @param fn underlying function
   * @return wrapped function
   */
  public <T, R> Function<T, R> wrap(WrapOptions options, Function<T, R> fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertWrapOptionsShape(options);
    return t -> wrapInvoke(options, new Object[] {t}, () -> fn.apply(t));
  }

  /**
   * Wraps a binary function so each invocation emits an {@link ExecutionEvent}.
   *
   * @param <T> first argument type
   * @param <U> second argument type
   * @param <R> result type
   * @param options wrap metadata and capture policy
   * @param fn underlying bifunction
   * @return wrapped bifunction
   */
  public <T, U, R> BiFunction<T, U, R> wrap(WrapOptions options, BiFunction<T, U, R> fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertWrapOptionsShape(options);
    return (t, u) -> wrapInvoke(options, new Object[] {t, u}, () -> fn.apply(t, u));
  }

  /**
   * Lowest-level wrapper: positional arguments only for default snapshot shape (Node parity).
   *
   * @param <R> result type
   * @param options wrap metadata and capture policy
   * @param fn function receiving the captured argument array (may be {@code null}, treated as
   *     empty)
   * @return wrapped function over argument arrays
   */
  public <R> Function<Object[], R> wrapAll(WrapOptions options, Function<Object[], R> fn) {
    Objects.requireNonNull(fn, "fn");
    Validation.assertWrapOptionsShape(options);
    return args -> wrapInvoke(options, args == null ? new Object[0] : args, () -> fn.apply(args));
  }

  private <R> R wrapInvoke(WrapOptions options, Object[] args, Supplier<R> body) {
    Objects.requireNonNull(body, "fn");
    Map<String, Object> defaultsCopy;
    synchronized (lock) {
      defaultsCopy = new LinkedHashMap<>(defaultAttributes);
    }
    Instant started = TimeUtil.now();
    String startedAt = TimeUtil.utcIsoMs(started);
    SerializeOptions serOpts = mergeSerializeOptions(options);

    List<Object> argList = java.util.Arrays.asList(args);
    Object inputs;
    if (options.captureInput() != null) {
      try {
        inputs = options.captureInput().apply(argList);
      } catch (RuntimeException ex) {
        inputs = Snapshot.snapshot(args, serOpts);
      }
    } else {
      inputs = Snapshot.snapshot(args, serOpts);
    }

    String correlationId =
        options.correlationId() != null ? options.correlationId() : Correlation.get();
    Map<String, Object> attrs = mergeAttrs(defaultsCopy, options.attributes());
    String intent = options.intent().trim();
    String action = options.action().trim();
    String id = java.util.UUID.randomUUID().toString();

    try {
      R out = body.get();
      if (RuntimeUtil.isCompletionStage(out)) {
        @SuppressWarnings("unchecked")
        CompletionStage<R> stage = RuntimeUtil.asCompletionStage(out);
        return awaitStage(
            stage,
            options,
            serOpts,
            started,
            startedAt,
            id,
            correlationId,
            inputs,
            attrs,
            intent,
            action);
      }
      emitOk(
          options,
          serOpts,
          started,
          startedAt,
          id,
          correlationId,
          inputs,
          attrs,
          intent,
          action,
          out);
      return out;
    } catch (Throwable e) {
      emitError(options, started, startedAt, id, correlationId, inputs, attrs, intent, action, e);
      throw asThrowableToPropagate(e);
    }
  }

  /**
   * Returns a {@link RuntimeException} to rethrow, or throws an {@link Error} directly. Used from
   * {@code catch} so JaCoCo sees a {@code throw} (not {@code return} after a call that never
   * returns normally).
   */
  private static RuntimeException asThrowableToPropagate(Throwable e) {
    if (e instanceof RuntimeException re) {
      return re;
    }
    if (e instanceof Error err) {
      throw err;
    }
    return new RuntimeException(e);
  }

  @SuppressWarnings("unchecked")
  private <R> R awaitStage(
      CompletionStage<R> stage,
      WrapOptions options,
      SerializeOptions serOpts,
      Instant started,
      String startedAt,
      String id,
      String correlationId,
      Object inputs,
      Map<String, Object> attrs,
      String intent,
      String action) {

    CompletableFuture<R> result = new CompletableFuture<>();
    var unused =
        stage.whenComplete(
            (value, err) -> {
              if (err != null) {
                emitError(
                    options,
                    started,
                    startedAt,
                    id,
                    correlationId,
                    inputs,
                    attrs,
                    intent,
                    action,
                    err);
                result.completeExceptionally(err);
              } else {
                emitOk(
                    options,
                    serOpts,
                    started,
                    startedAt,
                    id,
                    correlationId,
                    inputs,
                    attrs,
                    intent,
                    action,
                    value);
                result.complete(value);
              }
            });
    return (R) result;
  }

  private void emitOk(
      WrapOptions options,
      SerializeOptions serOpts,
      Instant started,
      String startedAt,
      String id,
      String correlationId,
      Object inputs,
      Map<String, Object> attrs,
      String intent,
      String action,
      Object result) {
    Instant completed = TimeUtil.now();
    String completedAt = TimeUtil.utcIsoMs(completed);
    long durationMs = completed.toEpochMilli() - started.toEpochMilli();
    Object output;
    if (options.captureOutput() != null) {
      try {
        output = options.captureOutput().apply(result);
      } catch (RuntimeException ex) {
        output = Snapshot.snapshot(result, serOpts);
      }
    } else {
      output = Snapshot.snapshot(result, serOpts);
    }
    ExecutionEvent event = new ExecutionEvent();
    event.setId(id);
    event.setIntent(intent);
    event.setAction(action);
    event.setInputs(inputsFromCaptured(inputs));
    event.setStatus(IntentProofExecutionEventV1.Status.OK);
    event.setStartedAt(startedAt);
    event.setCompletedAt(completedAt);
    event.setDurationMs((double) durationMs);
    event.setCorrelationId(correlationId);
    event.setOutput(output);
    event.setError(null);
    event.setAttributes(attributesFromMap(attrs));
    dispatch(event);
  }

  private void emitError(
      WrapOptions options,
      Instant started,
      String startedAt,
      String id,
      String correlationId,
      Object inputs,
      Map<String, Object> attrs,
      String intent,
      String action,
      Throwable error) {
    Instant completed = TimeUtil.now();
    String completedAt = TimeUtil.utcIsoMs(completed);
    long durationMs = completed.toEpochMilli() - started.toEpochMilli();
    boolean includeStack =
        options.includeErrorStack() != null
            ? options.includeErrorStack()
            : defaultIncludeErrorStack;
    ExecutionErrorSnapshot errSnap = toErrorSnapshot(error, includeStack);
    Object output = null;
    if (options.captureError() != null) {
      try {
        output = options.captureError().apply(error);
      } catch (RuntimeException ignored) {
        output = null;
      }
    }
    ExecutionEvent event = new ExecutionEvent();
    event.setId(id);
    event.setIntent(intent);
    event.setAction(action);
    event.setInputs(inputsFromCaptured(inputs));
    event.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    event.setStartedAt(startedAt);
    event.setCompletedAt(completedAt);
    event.setDurationMs((double) durationMs);
    event.setCorrelationId(correlationId);
    event.setOutput(output);
    ExecutionError genErr = new ExecutionError();
    genErr.setName(errSnap.name());
    genErr.setMessage(errSnap.message());
    genErr.setStack(errSnap.stack());
    event.setError(genErr);
    event.setAttributes(attributesFromMap(attrs));
    dispatch(event);
  }

  private void dispatch(ExecutionEvent event) {
    BiConsumer<Throwable, ExecutionEvent> sink;
    List<Exporter> copy;
    synchronized (lock) {
      sink = this.onExporterError;
      copy = new ArrayList<>(exporters);
    }
    for (Exporter ex : copy) {
      try {
        Object r = ex.export(event);
        if (r instanceof CompletableFuture<?> cf) {
          var unused =
              cf.whenComplete(
                  (ok, err) -> {
                    if (err != null) {
                      sink.accept(err, event);
                    }
                  });
        }
      } catch (Throwable e) {
        sink.accept(e, event);
      }
    }
  }

  private ExecutionErrorSnapshot toErrorSnapshot(Throwable e, boolean includeStack) {
    String name = e.getClass().getSimpleName();
    if (name.isEmpty()) {
      name = e.getClass().getName();
    }
    String msg = e.getMessage() != null ? e.getMessage() : "";
    String stack = null;
    if (includeStack) {
      java.io.StringWriter sw = new java.io.StringWriter();
      e.printStackTrace(new java.io.PrintWriter(sw));
      stack = sw.toString();
    }
    return new ExecutionErrorSnapshot(name, msg, stack);
  }

  private SerializeOptions mergeSerializeOptions(WrapOptions wrap) {
    SerializeOptions.Builder b = SerializeOptions.builder();
    if (wrap.maxDepth() != null) {
      b.maxDepth(wrap.maxDepth());
    }
    if (wrap.maxKeys() != null) {
      b.maxKeys(wrap.maxKeys());
    }
    if (wrap.redactKeys() != null) {
      b.redactKeys(new ArrayList<>(wrap.redactKeys()));
    }
    if (wrap.maxStringLength() != null) {
      b.maxStringLength(wrap.maxStringLength());
    }
    return b.build();
  }

  static Map<String, Object> mergeAttrs(Map<String, Object> defaults, Map<String, Object> wrap) {
    if (wrap == null || wrap.isEmpty()) {
      if (defaults == null || defaults.isEmpty()) {
        return null;
      }
      return Map.copyOf(defaults);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    if (defaults != null) {
      out.putAll(defaults);
    }
    out.putAll(wrap);
    return Map.copyOf(out);
  }

  private static void defaultOnExporterError(Throwable error, ExecutionEvent event) {
    String message = String.valueOf(error.getMessage());
    String eventId = String.valueOf(event.getId());
    System.err.println("[intentproof] exporter error: " + message + " (eventId=" + eventId + ")");
  }

  private static Inputs inputsFromCaptured(Object inputs) {
    Inputs in = new Inputs();
    if (inputs == null) {
      return in;
    }
    if (inputs instanceof Map<?, ?> m) {
      for (Map.Entry<?, ?> e : m.entrySet()) {
        in.setAdditionalProperty(String.valueOf(e.getKey()), e.getValue());
      }
      return in;
    }
    in.setAdditionalProperty("captured", inputs);
    return in;
  }

  private static Attributes attributesFromMap(Map<String, Object> attrs) {
    if (attrs == null || attrs.isEmpty()) {
      return null;
    }
    Attributes a = new Attributes();
    for (Map.Entry<String, Object> e : attrs.entrySet()) {
      a.setAdditionalProperty(e.getKey(), e.getValue());
    }
    return a;
  }
}
