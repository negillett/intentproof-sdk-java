package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intentproof.sdk.fixtures.ExecutionEventFixtures;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class HttpExporterIntegrationTest {

  @Test
  void postsEventAwaitEachAndHandlesFailures() throws Exception {
    CopyOnWriteArrayList<String> bodies = new CopyOnWriteArrayList<>();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/ingest",
        exchange -> {
          bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          byte[] ok = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, ok.length);
          exchange.getResponseBody().write(ok);
          exchange.close();
        });
    server.setExecutor(null);
    server.start();
    int port = server.getAddress().getPort();
    try {
      MemoryExporter mem = new MemoryExporter();
      java.util.HashMap<String, String> hdr = new java.util.HashMap<>();
      hdr.put("X-Test", "v");
      HttpExporter http =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/ingest")
                  .method("  ")
                  .headers(hdr)
                  .awaitEach(true)
                  .timeoutMs(5000)
                  .onError((t, e) -> {})
                  .build());
      IntentProofClient c =
          IntentProof.createClient(
              IntentProofConfig.builder().exporters(List.of(mem, http)).build());
      Runnable run =
          c.wrap(WrapOptions.builder().intent("http").action("test.http").build(), () -> {});
      run.run();
      c.flush().get();
      assertEquals(1, bodies.size());
      assertTrue(bodies.get(0).contains("intentproof"));

      HttpExporter badUrl =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:1/nope")
                  .awaitEach(true)
                  .onError((t, e) -> {})
                  .build());
      badUrl.export(mem.getEvents().get(0));
      ((CompletableFuture<?>) badUrl.flush()).get();

      assertThrows(
          IllegalArgumentException.class,
          () -> new HttpExporter(HttpExporter.HttpExporterOptions.builder().url("  ").build()));
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new HttpExporter(
                  HttpExporter.HttpExporterOptions.builder()
                      .url("http://127.0.0.1:9/x")
                      .timeoutMs(0)
                      .build()));

      HttpExporter bodyThrow =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/ingest")
                  .body(
                      e -> {
                        throw new IllegalStateException("ser");
                      })
                  .onError((t, e) -> {})
                  .build());
      bodyThrow.export(mem.getEvents().get(0));
      ((CompletableFuture<?>) bodyThrow.flush()).get();

      HttpExporter closed =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/ingest")
                  .onError((t, e) -> {})
                  .build());
      ((CompletableFuture<?>) closed.shutdown()).get();
      closed.export(mem.getEvents().get(0));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void non2xxNotifiesOnError() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    AtomicReference<String> saw = new AtomicReference<>();
    server.createContext(
        "/bad",
        exchange -> {
          exchange.sendResponseHeaders(500, 0);
          exchange.close();
        });
    server.setExecutor(null);
    server.start();
    int port = server.getAddress().getPort();
    try {
      HttpExporter http =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/bad")
                  .method("PUT")
                  .awaitEach(true)
                  .onError((t, e) -> saw.set(t.getMessage()))
                  .build());
      MemoryExporter mem = new MemoryExporter();
      IntentProofClient c =
          IntentProof.createClient(IntentProofConfig.builder().exporters(List.of(mem)).build());
      ExecutionEvent ev =
          mem.getEvents().isEmpty() ? ExecutionEventFixtures.syntheticOk() : mem.getEvents().get(0);
      http.export(ev);
      ((CompletableFuture<?>) http.flush()).get();
      assertNotNull(saw.get());
      assertTrue(saw.get().contains("500"));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void non2xxIncludesShortResponseBodyInError() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    AtomicReference<String> saw = new AtomicReference<>();
    server.createContext(
        "/short",
        exchange -> {
          byte[] body = "service unavailable".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(503, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.setExecutor(null);
    server.start();
    int port = server.getAddress().getPort();
    try {
      HttpExporter http =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/short")
                  .awaitEach(true)
                  .onError((t, e) -> saw.set(t.getMessage()))
                  .build());
      http.export(ExecutionEventFixtures.syntheticOk());
      ((CompletableFuture<?>) http.flush()).get();
      assertEquals("HTTP 503: service unavailable", saw.get());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void non2xxErrorMessageTruncatesLargeResponseBody() throws Exception {
    String huge = "x".repeat(5000);
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    AtomicReference<String> saw = new AtomicReference<>();
    server.createContext(
        "/huge",
        exchange -> {
          byte[] body = huge.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(502, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.setExecutor(null);
    server.start();
    int port = server.getAddress().getPort();
    try {
      HttpExporter http =
          new HttpExporter(
              HttpExporter.HttpExporterOptions.builder()
                  .url("http://127.0.0.1:" + port + "/huge")
                  .awaitEach(true)
                  .onError((t, e) -> saw.set(t.getMessage()))
                  .build());
      http.export(ExecutionEventFixtures.syntheticOk());
      ((CompletableFuture<?>) http.flush()).get();
      String msg = saw.get();
      assertNotNull(msg);
      assertTrue(msg.contains("502"));
      assertTrue(msg.contains("truncated"), msg);
      assertTrue(msg.length() < 2500, "message should stay bounded");
    } finally {
      server.stop(0);
    }
  }
}
