package com.intentproof.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intentproof.sdk.generated.v1.Attributes;
import com.intentproof.sdk.generated.v1.ExecutionError;
import com.intentproof.sdk.generated.v1.Inputs;
import com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionWireFormattingTest {

  @Test
  void omitsOutputOnErrorWhenNull() {
    ExecutionEvent ev = new ExecutionEvent();
    ev.setId("id");
    ev.setIntent("i");
    ev.setAction("a");
    ev.setInputs(new Inputs());
    ev.setStatus(IntentProofExecutionEventV1.Status.ERROR);
    ev.setStartedAt("2026-01-01T00:00:00.000Z");
    ev.setCompletedAt("2026-01-01T00:00:00.001Z");
    ev.setDurationMs(1.0);
    ev.setCorrelationId("cid");
    ev.setOutput(null);
    ExecutionError err = new ExecutionError();
    err.setName("E");
    err.setMessage("m");
    err.setStack("s");
    ev.setError(err);
    ev.setAttributes(null);
    Map<String, Object> m = ExecutionWire.toWireMap(ev);
    assertFalse(m.containsKey("output"));
    assertEquals("cid", m.get("correlationId"));
  }

  @Test
  void okEventIncludesAttributes() {
    Attributes attrs = new Attributes();
    attrs.setAdditionalProperty("tier", "gold");
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
    ev.setAttributes(attrs);
    assertTrue(ExecutionWire.toWireMap(ev).containsKey("attributes"));
  }

  @Test
  void executionErrorSnapshotHasStackFalse() {
    assertFalse(new ExecutionErrorSnapshot("E", "m", null).hasStack());
  }
}
