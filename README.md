## **Logs narrate; IntentProof gives you proof.**

[![CI](https://github.com/IntentProof/intentproof-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/IntentProof/intentproof-sdk-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.intentproof/intentproof-sdk)](https://central.sonatype.com/search?q=g:io.github.intentproof+intentproof-sdk)
<a href="https://github.com/IntentProof/intentproof-sdk-java/raw/main/conformance-certificate.json" target="_blank" rel="noopener noreferrer"><img src="https://img.shields.io/badge/conformance_certificate-view-0366d6" alt="Conformance Certificate" /></a>

**IntentProof** is **auditable execution records** for actions that must be defensible—**intent** tied to what actually ran.

**Wrap** the calls that matter; each invocation emits one **verifiable** **`ExecutionEvent`**, structured so intent and outcome can be **reconciled** with reality—not only observed.

Observability captures what happened. **IntentProof** tells you whether it matched what was **meant to happen**.

Every **`ExecutionEvent`** contains:

- **`intent`**: what this invocation was meant to prove
- **`action`**: the stable operation id for this step
- **`status`**: success or error
- **`inputs`** and **`output`**: what the runtime saw going in and coming out

## Why this matters

Modern systems—especially AI agents—do not only compute; they act:
issuing refunds, sending emails, updating databases.

When something goes wrong, logs tell you what ran.
They don't tell you:

- what was supposed to happen
- whether all steps completed
- whether systems ended up in a consistent state

**IntentProof** exists to bridge that gap.

It records intent alongside execution so systems can be verified, not just observed.

### Picture this:

It's 4:47 on a Friday. A customer insists the critical action never happened. Support sees scattered traces; engineering sees green checks; finance asks for **one** clean chain: what was **supposed** to occur, what **did** occur, and whether the outcome is **complete**.

Ordinary telemetry shows that *something ran*. It rarely ships an **auditable story** you can hand to someone who doesn't read your codebase. **IntentProof** exists for when the question stops being "what was logged?" and starts being **"prove it."**

## Requirements

- **Java** 21 or newer (LTS)

## Install

**Coordinates:** `io.github.intentproof:intentproof-sdk`.

Replace **`x.y.z`** with the library version you intend to pin.

**Maven**

```xml
<dependency>
  <groupId>io.github.intentproof</groupId>
  <artifactId>intentproof-sdk</artifactId>
  <version>x.y.z</version>
</dependency>
```

**Gradle**

```kotlin
dependencies {
    implementation("io.github.intentproof:intentproof-sdk:x.y.z")
}
```

## Quick start

```java
import com.intentproof.sdk.IntentProof;
import com.intentproof.sdk.WrapOptions;

var client = IntentProof.client();

var refund = client.wrap(
    WrapOptions.builder()
        .intent("Initiate refund")
        .action("stripe.refunds.create")
        .build(),
    this::stripeRefundsCreate);
```

Each refund call emits one **`ExecutionEvent`** with the **`intent`** and **`action`** you chose, the **`inputs`** and **`output`** (or **`error`** + **`status: "error"`**), and timing fields—an execution record you can inspect, export, or verify later.

## Reference

Detailed tables for the client API, emitted events, configuration, and related exports.

### `IntentProofClient` API

| Member | Description |
| ------ | ----------- |
| **`IntentProofClient()`** / **`IntentProofClient(IntentProofConfig config)`** | Creates a client. Default exporters: a single **`MemoryExporter`** if you omit **`config.exporters()`**. |
| **`configure(IntentProofConfig config)`** | Re-applies **`IntentProofConfig`** fields (exporters, error hook, defaults, stack policy). |
| **`wrap(WrapOptions options, …)`** | Returns a handler (e.g. **`Function`**) that records one **`ExecutionEvent`** per invocation. Options must satisfy validation (`intent` / `action` non-empty strings, etc.). |
| **`flush()`** | Waits for **`flush()`** on every **`Exporter`** that implements it, typically in parallel. |
| **`shutdown()`** | For each **`Exporter`**, calls **`shutdown()`** if implemented, otherwise **`flush()`** if implemented. |
| **`getCorrelationId()`** | Returns the correlation ID from **thread-local or scoped context**, if any. |
| **`withCorrelation(Runnable fn)`** | Runs **`fn`** with a **fresh UUID** as correlation ID for nested wraps. |
| **`withCorrelation(String correlationId, Runnable fn)`** | Runs **`fn`** with **`correlationId`** trimmed; blank / whitespace-only **`correlationId`** falls back to a UUID. |

#### Static helpers (same module as the client)

These use the same correlation context as **`IntentProofClient`** instances:

| API | Description |
| --- | ----------- |
| **`IntentProof.createClient(IntentProofConfig config)`** | New isolated client (tests, workers, multi-tenant). |
| **`IntentProof.getClient()`** | Lazy singleton used by **`IntentProof.client()`**. |
| **`IntentProof.client()`** | Default singleton instance. |
| **`IntentProof.getCorrelationId()`** | Same behavior as the instance method. |
| **`IntentProof.runWithCorrelationId(String id, Runnable fn)`** | Requires a **non-empty** correlation ID after strip; throws if invalid. |
| **`IntentProof.assertCorrelationId(String id)`** | Runtime validation for correlation ID shape. |
| **`IntentProof.assertWrapOptionsShape(WrapOptions options)`** | Runtime validation for **`WrapOptions`**. |

### `ExecutionEvent` fields

| Field | Description |
| ----- | ----------- |
| **`id`** | Unique event id (UUID). |
| **`correlationId`** | Request or trace correlation ID when present—usually from context or **`WrapOptions`**. |
| **`intent`** | Human-readable label for what this invocation is meant to prove (outcome, policy goal, or domain). |
| **`action`** | Stable operation id for this step (often dotted or namespaced). |
| **`inputs`** | JSON-safe snapshot of call arguments (default) or **`captureInput`** result. |
| **`output`** | JSON-safe return value or **`captureOutput`** result on success. When **`status`** is **`"error"`**, set only if **`captureError`** returned a value. |
| **`error`** | On failure: **`name`**, **`message`**, and optional **`stack`** (see **`includeErrorStack`**). |
| **`status`** | **`"ok"`** if the wrapped call completed normally; **`"error"`** if it threw. |
| **`startedAt`** | Start time (ISO 8601). |
| **`completedAt`** | Completion time (ISO 8601). |
| **`durationMs`** | Wall time between start and completion, in milliseconds. |
| **`attributes`** | Optional plain map (string / number / boolean values only), merged from client defaults and wrap options. |

### `WrapOptions` and `IntentProofConfig`

#### `WrapOptions` (passed to **`wrap`**)

| Field | Description |
| ----- | ----------- |
| **`intent`**, **`action`** | Required, non-empty after trim. |
| **`correlationId`** | Optional; when set, non-empty after strip. Otherwise the active correlation ID from context is used, if any. |
| **`attributes`** | Per-invocation dimensions merged over **`defaultAttributes`**. |
| **`captureInput`**, **`captureOutput`**, **`captureError`** | Optional hooks to replace default **`snapshot`** behavior for inputs, success output, or error-side extra **`output`**. |
| **`includeErrorStack`** | When `false`, omit **`error.stack`** for this wrap (overrides client default). |
| **`maxDepth`**, **`maxKeys`**, **`redactKeys`**, **`maxStringLength`** | Forwarded to **`snapshot`** for inputs and outputs (see **`SerializeOptions`**). |

#### `IntentProofConfig` (constructor / **`configure`**)

| Field | Description |
| ----- | ----------- |
| **`exporters`** | Ordered list of **`Exporter`** instances; each receives every **`ExecutionEvent`**. |
| **`onExporterError`** | Called when any exporter’s **`export`** throws or completes exceptionally. Defaults to logging / stderr. |
| **`defaultAttributes`** | Merged into every event’s **`attributes`** (wrap-specific attributes win on key collision). |
| **`includeErrorStack`** | Default `true`; set `false` in production if stacks must not leave the trust zone. |

### Related exports

- **`MemoryExporter`**, **`HttpExporter`**, **`BoundedQueueExporter`** — Delivery implementations; each implements **`Exporter`**.
- **`snapshot`** — Same JSON-safe serializer the client uses internally, if you build custom tooling.
- **`VERSION`** — Package version string (for example from **`Package.getImplementationVersion()`** or the JAR manifest’s **`Implementation-Version`**).

---

## Examples

### 1 — Refund and customer receipt

Support approves **order `ORD-1042`**. Your service creates the **Stripe refund**, then emails the customer a receipt. **`runWithCorrelationId`** ties both calls to **`req_refund_ord_1042`**. Each **`wrap`** defines its own **`intent`** (the outcome you are proving for that step) and **`action`** (how it is done); **`correlationId`** is what stitches them together.

**`captureInput`** / **`captureOutput`** trim each record to the fields you want in proof (refund id, amounts, message id)—not full vendor payloads.

JSON on the wire uses **camelCase**; Java **`WrapOptions`** uses the same camelCase names (e.g. **`captureInput`**).

```java
import com.intentproof.sdk.IntentProof;
import static com.intentproof.sdk.IntentProof.runWithCorrelationId;

import java.util.HashMap;
import java.util.Map;

var client = IntentProof.client();

var createRefund = client.wrap(
    WrapOptions.builder()
        .intent("Return captured funds to the customer's original card network")
        .action("stripe.refund.create")
        .attributes(Map.of("vendor", "stripe", "step", "refund_money"))
        .captureInput(
            args -> {
              @SuppressWarnings("unchecked")
              var map = (Map<String, Object>) args.get(0);
              var out = new HashMap<String, Object>();
              out.put("paymentIntentId", map.get("paymentIntentId"));
              out.put("amountCents", map.get("amountCents"));
              out.put("reason", map.getOrDefault("reason", null));
              return out;
            })
        .captureOutput(result -> {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) result;
            return Map.of(
                    "refundId", map.get("id"),
                    "status", map.get("status"),
                    "amountCents", map.get("amountCents"));
        })
        .build(),
    inp -> {
      @SuppressWarnings("unchecked")
      var map = (Map<String, Object>) inp;
      return Map.of(
          "id", "re_3SAMPLEabcdefghijklmnop",
          "status", "succeeded",
          "amountCents", map.get("amountCents"));
    });

var sendRefundReceipt = client.wrap(
    WrapOptions.builder()
        .intent("Deliver a customer-visible refund confirmation for the ledger entry")
        .action("email.customer.refund_receipt")
        .attributes(Map.of("channel", "email", "step", "notify_customer"))
        .captureInput(
            args -> {
              @SuppressWarnings("unchecked")
              var map = (Map<String, Object>) args.get(0);
              return Map.of(
                  "customerId", map.get("customerId"),
                  "orderId", map.get("orderId"),
                  "refundId", map.get("refundId"),
                  "amountCents", map.get("amountCents"));
            })
        .captureOutput(result -> {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) result;
            return Map.of(
                    "messageId", map.get("messageId"),
                    "status", map.get("status"));
        })
        .build(),
    p -> Map.of("messageId", "msg_49401_sample", "status", "queued"));

runWithCorrelationId("req_refund_ord_1042", () -> {
  var refundInput =
      Map.<String, Object>of(
          "paymentIntentId", "pi_3SAMPLEabcdefghijklmnop",
          "amountCents", 4999,
          "reason", "requested_by_customer");
  var refund = createRefund.apply(refundInput);

  var receiptInput =
      Map.<String, Object>of(
          "customerId", "cus_SAMPLEabcdefghijkl",
          "orderId", "ORD-1042",
          "refundId", refund.get("id"),
          "amountCents", refund.get("amountCents"));
  sendRefundReceipt.apply(receiptInput);
});
```

Emitted **`ExecutionEvent`** values (same **`correlationId`** on each; distinct **`intent`** per step; **`id`** / timestamps omitted):

```json
[
  {
    "correlationId": "req_refund_ord_1042",
    "intent": "Return captured funds to the customer's original card network",
    "action": "stripe.refund.create",
    "inputs": {
      "paymentIntentId": "pi_3SAMPLEabcdefghijklmnop",
      "amountCents": 4999,
      "reason": "requested_by_customer"
    },
    "status": "ok",
    "output": {
      "refundId": "re_3SAMPLEabcdefghijklmnop",
      "status": "succeeded",
      "amountCents": 4999
    },
    "attributes": {
      "service": "billing-api",
      "env": "test",
      "vendor": "stripe",
      "step": "refund_money"
    }
  },
  {
    "correlationId": "req_refund_ord_1042",
    "intent": "Deliver a customer-visible refund confirmation for the ledger entry",
    "action": "email.customer.refund_receipt",
    "inputs": {
      "customerId": "cus_SAMPLEabcdefghijkl",
      "orderId": "ORD-1042",
      "refundId": "re_3SAMPLEabcdefghijklmnop",
      "amountCents": 4999
    },
    "status": "ok",
    "output": { "messageId": "msg_49401_sample", "status": "queued" },
    "attributes": {
      "service": "billing-api",
      "env": "test",
      "channel": "email",
      "step": "notify_customer"
    }
  }
]
```

### 2 — Payment failure with operator metadata (`captureError`)

When a capture **throws**, the record still carries **`status: "error"`** and **`error.message`** for proof of failure. **`captureError`** adds a small, JSON-safe **`output`** for dashboards (e.g. decline code) without pretending the business call succeeded.

```java
var capturePayment =
    client.wrap(
        WrapOptions.builder()
            .intent("Capture authorized funds")
            .action("stripe.payment_intent.capture")
            .captureInput(
                args ->
                    Map.of(
                        "paymentIntentId",
                        ((Map<?, ?>) args.get(0)).get("paymentIntentId")))
            .captureError(err -> Map.of("code", "card_declined", "retryable", false))
            .build(),
        inp -> {
          throw new RuntimeException("Your card was declined.");
        });

try {
  capturePayment.apply(Map.of("paymentIntentId", "pi_3SAMPLEabcdefghijklmnop"));
} catch (RuntimeException ignored) {
  // card declined — expected
}
```

```json
{
  "intent": "Capture authorized funds",
  "action": "stripe.payment_intent.capture",
  "inputs": { "paymentIntentId": "pi_3SAMPLEabcdefghijklmnop" },
  "status": "error",
  "error": {
    "name": "RuntimeException",
    "message": "Your card was declined."
  },
  "output": { "code": "card_declined", "retryable": false }
}
```

### 3 — Proof delivery over HTTP (same **`ExecutionEvent`** shape)

**`HttpExporter`** POSTs the same **`ExecutionEvent`** your verifiers see in memory—here alongside **`MemoryExporter`** so tests can assert the wire without a real collector. The request omits ambient credentials; the body is **`{ "intentproof": "1", "event": … }`** (see exporter implementation). For authenticated collectors, pass **`headers`** (e.g. **`Authorization`**, API keys) — see the Security section above.

```java
var runProbe =
    client.wrap(
        WrapOptions.builder().intent("HTTP test").action("test.http").build(),
        () -> 42);
runProbe.get();
```

```json
{
  "intent": "HTTP test",
  "action": "test.http",
  "inputs": [],
  "status": "ok",
  "output": 42
}
```

---

## Security

For **vulnerability reporting**, see **`SECURITY.md`**.

Every **`ExecutionEvent`** you emit is data you may ship off-process. Treat them like audit-grade execution records: they can include PII, secrets, stack traces, and business identifiers depending on your **`snapshot`** / **`capture*`** hooks.

- **Minimize payload:** Use **`redactKeys`**, **`maxDepth`** / **`maxKeys`** / **`maxStringLength`**, and narrow **`captureInput`** / **`captureOutput`** / **`captureError`** so proof records contain only what verifiers need.
- **Stacks:** Set **`includeErrorStack`** to `false` on the client (or per wrap) when traces must not leave your trust zone.
- **HTTP ingest:** Keep collector **`url`** and any redirect behavior under **trusted configuration** (avoid SSRF if URLs were ever influenced by untrusted input). Prefer **HTTPS** and **short-lived credentials** end-to-end.
- **`HttpExporter` auth:** Pass credentials in **`headers`** (for example **`Authorization: Bearer …`**, **`x-api-key`**, or whatever your collector expects). The SDK does **not** log header values; use short-lived tokens and scope them to ingest only.
- **Runtime surface:** On the **JVM**, treat the ingest endpoint and headers with the same care you would for any outbound credential.
- **Delivery semantics:** Exporter failures invoke **`onExporterError`** and do **not** roll back the wrapped callable’s side effects—design compensating controls if you need strict “delivered exactly once” guarantees.

Custom **`body`** serializers: if **`body(event)`** throws, **`HttpExporter`** notifies **`onError`** and falls back to the same **JSON envelope** path as the default serializer (full event, then a partial envelope, then a minimal `eventSerializeFailed` payload) so **`export`** still completes and the configured HTTP client runs when possible.

---

## Canonical specification (`intentproof-spec`)

**Shared pins and terminology** (`INTENTPROOF_SPEC_ROOT`, **`intentproofSpecCommit`**, script names) are documented in the **`intentproof-spec`** repository (`CONTRIBUTING.md`, Terminology).

**`intentproof-spec`** holds normative schemas, golden **`execution_event_cases.jsonl`**, and the canonical **`spec-conformance.sh`** toolchain.

- **Version pin:** **`intentproofSpecVersion`** and **`intentproofSpecCommit`** in **`gradle.properties`** match **`spec.json`** and the spec **`HEAD`** checkout; **`scripts/check-consumer-spec-pin.sh`** delegates to **`intentproof-spec`** **`scripts/check-consumer-spec-pins.sh`** before conformance.

- **CI:** `.github/workflows/ci.yml` runs hardening, **`./gradlew check`**, and the **Trivy** JAR scan against a pinned **`intentproof-spec`** checkout.
- **Spec conformance (PR):** `.github/workflows/spec-conformance.yml` runs the same Gradle gates plus **`scripts/spec-conformance.sh`** (canonical oracle + replay) and uploads a **`conformance-report.json`** artifact (committed spec public key path; no signing secrets on PRs).
- **Trusted attestation (`main`):** `.github/workflows/conformance-attestation.yml` follows the **`intentproof-api`** pattern: signed oracle output, **`npm run validate:conformance-certificate`** in the spec checkout, combined **`conformance-artifacts`** upload, and cert-bot publish of root **`conformance-certificate.json`** / **`conformance-report.json`** when they change.

- **Local:** clone **`intentproof-spec`** **next to** this repository (`../intentproof-spec`), then:

  ```bash
  ./gradlew intentproofSpecConformance
  ```

  Or set **`INTENTPROOF_SPEC_ROOT`** and run **`bash scripts/spec-conformance.sh`**.

- **Generated POJOs:** drift check with **`bash scripts/verify-generated-pojos.sh`** (after **`INTENTPROOF_SPEC_ROOT`** is set).

- **No handwritten wire models:** **`scripts/check-no-handwritten-model-types.sh`** delegates to **`intentproof-spec`** (also covered by Gradle **`check`** / hardening flows).

---

## Project development

Layout: **Gradle** (`settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`). The project targets **Java 21** (Gradle Java toolchain). CI runs **`./gradlew check`** on **Temurin 21**. Release history: **`CHANGELOG.md`**.

Contributing (formatting, tests, coverage, cross-SDK parity, **shared `intentproof-spec` terminology**): see **`CONTRIBUTING.md`**.

Build locally:

```bash
./gradlew check                 # CI-style: Spotless, tests, JaCoCo coverage gate
./gradlew spotlessApply check   # apply Google Java Format, then same as above
```

The build uses Gradle’s **Foojay toolchain resolver** so Gradle can provision a matching JDK when your machine only has a JRE or a newer JDK without **`javac`** on **`PATH`**.

Publishing uses the Gradle **`maven-publish`** plugin plus Sonatype Central staging (**`io.github.gradle-nexus.publish-plugin`**) for **`io.github.intentproof:intentproof-sdk`**. Tag-driven releases run **`./gradlew check`**, publish signed artifacts, and attach build outputs per **`RELEASING.md`**.

---

## License

Apache-2.0 (see **`LICENSE`** at the repository root).
