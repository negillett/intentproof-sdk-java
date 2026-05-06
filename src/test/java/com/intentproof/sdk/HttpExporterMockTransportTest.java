package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.intentproof.sdk.fixtures.ExecutionEventFixtures;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HttpExporterMockTransportTest {

  @Test
  void runAsyncFailureNotifiesOnError() throws Exception {
    HttpClient client = mock(HttpClient.class);
    Mockito.when(client.send(any(HttpRequest.class), any())).thenThrow(new LinkageError("wire"));
    @SuppressWarnings("unchecked")
    BiConsumer<Throwable, ExecutionEvent> onErr = mock(BiConsumer.class);
    HttpExporter ex =
        new HttpExporter(
            HttpExporter.HttpExporterOptions.builder()
                .url("http://127.0.0.1:9/nope")
                .awaitEach(true)
                .onError(onErr)
                .build(),
            client);
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId("id");
    ev.setIntent("i");
    ev.setAction("a");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.OK);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId(null);
    ev.setOutput(1);
    ev.setError(null);
    ev.setAttributes(null);
    ex.export(ev);
    verify(onErr, timeout(5000)).accept(any(), any());
    ((CompletableFuture<?>) ex.flush()).get();
  }

  @Test
  void sendInterruptedNotifiesOnError() throws Exception {
    HttpClient client = mock(HttpClient.class);
    Mockito.when(client.send(any(HttpRequest.class), any()))
        .thenThrow(new InterruptedException("cancelled"));
    @SuppressWarnings("unchecked")
    BiConsumer<Throwable, ExecutionEvent> onErr = mock(BiConsumer.class);
    HttpExporter ex =
        new HttpExporter(
            HttpExporter.HttpExporterOptions.builder()
                .url("http://127.0.0.1:9/nope")
                .awaitEach(true)
                .onError(onErr)
                .build(),
            client);
    ex.export(ExecutionEventFixtures.syntheticOk());
    verify(onErr, timeout(5000)).accept(any(InterruptedException.class), any());
    ((CompletableFuture<?>) ex.flush()).get();
  }

  @Test
  void optionsHeadersNullAccepted() {
    HttpExporter.HttpExporterOptions o =
        HttpExporter.HttpExporterOptions.builder().url("http://x/x").headers(null).build();
    assertNotNull(new HttpExporter(o));
  }
}
