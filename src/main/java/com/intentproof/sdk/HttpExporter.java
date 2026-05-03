package com.intentproof.sdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * POST {@link ExecutionEvent} records as JSON using the JVM HttpClient.
 *
 * <p>Exporter methods may return {@link java.util.concurrent.CompletableFuture} instances;
 * in-flight work is tracked in a set keyed by identity (not {@link CompletableFuture#equals}).
 */
@SuppressWarnings({"FutureReturnValueIgnored", "CollectionUndefinedEquality"})
public final class HttpExporter implements Exporter {
  private final URI uri;
  private final String method;
  private final Map<String, String> headers;
  private final HttpExporterBody bodySerializer;
  private final boolean awaitEach;
  private final Duration timeout;
  private final BiConsumer<Throwable, ExecutionEvent> onError;
  private final HttpClient httpClient;

  private volatile boolean closed;
  private final java.util.Set<CompletableFuture<Void>> inFlight =
      java.util.concurrent.ConcurrentHashMap.newKeySet();

  /**
   * Constructs an exporter using the default JVM {@link HttpClient}.
   *
   * @param options URL, headers, body serializer, and behavior flags
   */
  public HttpExporter(HttpExporterOptions options) {
    this(options, HttpClient.newBuilder().build());
  }

  /**
   * Same as {@link #HttpExporter(HttpExporterOptions)} with an injectable {@link HttpClient} (for
   * tests).
   *
   * @param options HTTP target and behavior
   * @param httpClient client used for {@code send}
   */
  HttpExporter(HttpExporterOptions options, HttpClient httpClient) {
    Objects.requireNonNull(options, "options");
    String url = options.url();
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "HttpExporter: \"url\" must be a non-empty string (trimmed length is 0)");
    }
    this.uri = URI.create(url);
    String m = options.method() == null ? "POST" : options.method().trim();
    this.method = m.isEmpty() ? "POST" : m;
    Map<String, String> h = new LinkedHashMap<>();
    h.put("content-type", "application/json");
    if (options.headers() != null) {
      for (Map.Entry<String, String> e : options.headers().entrySet()) {
        if (e.getKey() != null && e.getValue() != null) {
          h.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
        }
      }
    }
    this.headers = Map.copyOf(h);
    this.bodySerializer = options.body() != null ? options.body() : ExecutionWire::safeJsonEnvelope;
    this.awaitEach = options.awaitEach();
    Integer timeoutMs = options.timeoutMs();
    if (timeoutMs != null) {
      if (timeoutMs <= 0) {
        throw new IllegalArgumentException(
            "HttpExporter: \"timeoutMs\" must be a finite number > 0 when set");
      }
      this.timeout = Duration.ofMillis(timeoutMs.longValue());
    } else {
      this.timeout = null;
    }
    this.onError = options.onError();
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
  }

  @Override
  public Object export(ExecutionEvent event) {
    if (closed) {
      if (onError != null) {
        onError.accept(new IllegalStateException("HttpExporter has been shut down"), event);
      }
      return null;
    }

    String payload;
    try {
      payload = bodySerializer.serialize(event);
    } catch (RuntimeException e) {
      if (onError != null) {
        onError.accept(e, event);
      }
      payload = ExecutionWire.safeJsonEnvelope(event);
    }
    final String bodyToSend = payload;

    CompletableFuture<Void> run = CompletableFuture.runAsync(() -> postSilently(bodyToSend, event));

    inFlight.add(run);
    run.whenComplete(
        (ok, err) -> {
          inFlight.remove(run);
          if (err != null && onError != null) {
            onError.accept(err, event);
          }
        });

    if (awaitEach) {
      return run;
    }
    return null;
  }

  private void postSilently(String payload, ExecutionEvent event) {
    try {
      HttpRequest.Builder rb =
          HttpRequest.newBuilder()
              .uri(uri)
              .method(method, HttpRequest.BodyPublishers.ofString(payload));
      for (Map.Entry<String, String> e : headers.entrySet()) {
        rb.header(e.getKey(), e.getValue());
      }
      if (timeout != null) {
        rb.timeout(timeout);
      }
      HttpRequest req = rb.build();
      HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() < 200 || res.statusCode() >= 300) {
        if (onError != null) {
          onError.accept(
              new IllegalStateException(
                  "HTTP " + res.statusCode() + ": " + responseBodySnippetForError(res.body())),
              event);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      if (onError != null) {
        onError.accept(e, event);
      }
    } catch (Exception e) {
      if (onError != null) {
        onError.accept(e, event);
      }
    }
  }

  private static final int MAX_ERROR_RESPONSE_SNIPPET = 1024;

  /**
   * Keeps non-2xx error messages bounded; full bodies are not required for exporter diagnostics.
   */
  private static String responseBodySnippetForError(String body) {
    if (body == null || body.isEmpty()) {
      return "";
    }
    if (body.length() <= MAX_ERROR_RESPONSE_SNIPPET) {
      return body;
    }
    return body.substring(0, MAX_ERROR_RESPONSE_SNIPPET) + "... (truncated)";
  }

  @Override
  public Object flush() {
    if (inFlight.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    CompletableFuture<?>[] arr = inFlight.toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(arr);
  }

  @Override
  public Object shutdown() {
    closed = true;
    return flush();
  }

  /** Serializes an {@link ExecutionEvent} to the HTTP request body string. */
  @FunctionalInterface
  public interface HttpExporterBody {
    /**
     * @param event event to POST
     * @return JSON (or other) body for the HTTP request
     */
    String serialize(ExecutionEvent event);
  }

  /** Immutable options for {@link HttpExporter}. */
  public static final class HttpExporterOptions {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final HttpExporterBody body;
    private final boolean awaitEach;
    private final Integer timeoutMs;
    private final BiConsumer<Throwable, ExecutionEvent> onError;

    private HttpExporterOptions(Builder b) {
      this.url = b.url;
      this.method = b.method;
      this.headers = b.headers;
      this.body = b.body;
      this.awaitEach = b.awaitEach;
      this.timeoutMs = b.timeoutMs;
      this.onError = b.onError;
    }

    /**
     * @return target URL string
     */
    public String url() {
      return url;
    }

    /**
     * @return HTTP method (uppercased by caller conventions), never empty
     */
    public String method() {
      return method;
    }

    /**
     * @return lowercase header map including {@code content-type}
     */
    public Map<String, String> headers() {
      return headers;
    }

    /**
     * @return body serializer, or {@code null} meaning {@link ExecutionWire#safeJsonEnvelope}
     */
    public HttpExporterBody body() {
      return body;
    }

    /**
     * @return {@code true} when each export should return the in-flight future
     */
    public boolean awaitEach() {
      return awaitEach;
    }

    /**
     * @return per-request timeout in milliseconds, or {@code null} for no timeout
     */
    public Integer timeoutMs() {
      return timeoutMs;
    }

    /**
     * @return error callback, or {@code null}
     */
    public BiConsumer<Throwable, ExecutionEvent> onError() {
      return onError;
    }

    /**
     * @return new options builder
     */
    public static Builder builder() {
      return new Builder();
    }

    /** Fluent builder for {@link HttpExporterOptions}. */
    public static final class Builder {
      private String url;
      private String method;
      private Map<String, String> headers;
      private HttpExporterBody body;
      private boolean awaitEach;
      private Integer timeoutMs;
      private BiConsumer<Throwable, ExecutionEvent> onError;

      /**
       * @param url absolute HTTP(S) URL
       * @return this builder
       */
      public Builder url(String url) {
        this.url = url;
        return this;
      }

      /**
       * @param method HTTP verb (defaults to POST when blank)
       * @return this builder
       */
      public Builder method(String method) {
        this.method = method;
        return this;
      }

      /**
       * @param headers extra headers merged with defaults (keys lowercased); {@code null} clears
       * @return this builder
       */
      public Builder headers(Map<String, String> headers) {
        this.headers = headers == null ? null : Map.copyOf(headers);
        return this;
      }

      /**
       * @param body custom serializer, or {@code null} for default JSON envelope
       * @return this builder
       */
      public Builder body(HttpExporterBody body) {
        this.body = body;
        return this;
      }

      /**
       * @param awaitEach when {@code true}, {@link Exporter#export} returns the async future
       * @return this builder
       */
      public Builder awaitEach(boolean awaitEach) {
        this.awaitEach = awaitEach;
        return this;
      }

      /**
       * @param timeoutMs positive timeout in milliseconds for each request
       * @return this builder
       */
      public Builder timeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
      }

      /**
       * @param onError invoked for serialization failures, non-2xx responses, and transport errors
       * @return this builder
       */
      public Builder onError(BiConsumer<Throwable, ExecutionEvent> onError) {
        this.onError = onError;
        return this;
      }

      /**
       * @return immutable options
       */
      public HttpExporterOptions build() {
        return new HttpExporterOptions(this);
      }
    }
  }
}
