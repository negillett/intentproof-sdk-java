# Contributing

Cross-repository **pins**, **`INTENTPROOF_*`** environment variables, and script naming are documented in the **[`intentproof-spec` CONTRIBUTING guide](https://github.com/IntentProof/intentproof-spec/blob/main/CONTRIBUTING.md#terminology-shared-with-sdk-repos)**.

Run the full verification suite (formatting, tests, coverage gate):

```bash
./gradlew check
```

Dependency and plugin versions are centralized in `gradle/libs.versions.toml`. Shared test helpers live under `src/testFixtures/java/`.

Tagged releases (Maven Central + GitHub Releases) are documented in [`RELEASING.md`](RELEASING.md).

## Parity with Node and Python

This library targets **wire-format and behavioral parity** with the **IntentProof** TypeScript SDK ([`intentproof-sdk-node`](https://github.com/IntentProof/intentproof-sdk-node)) and **IntentProof** Python SDK ([`intentproof-sdk-python`](https://github.com/IntentProof/intentproof-sdk-python)). Treat those repos as behavioral references (do not copy source mechanically). When you change observable behavior, keep the **root `README.md`** accurate for users, and extend the notes below if the Java SDK **intentionally** differs.

**Naming:** Public options use **camelCase** (JSON / Node alignment), e.g. `correlationId`, `captureInput`. Static entrypoints live on **`IntentProof`**; `getIntentProofClient()` is an alias for `getClient()`. Published Maven coordinates: **`io.github.intentproof:intentproof-sdk`**.

**Default `wrap` inputs:** As in Node, the default snapshot for inputs is **positional arguments only** — `Function` wrappers capture a one-element list `[arg]`; `Runnable` / `Supplier` use `[]`; use **`wrapAll`** for arbitrary positional arity. There is no implicit kwargs map unless you add a documented Java-only extension.

**Correlation:** IDs use a **thread-local** (`Correlation`), analogous to synchronous Node usage with `AsyncLocalStorage`. For async work (`CompletableFuture`, etc.), set correlation at the boundary that matches your threading model; an explicit wrap `correlationId` overrides context.

**Attributes / validation:** `WrapOptions.attributes` and default attributes on `IntentProofConfig` allow **string, number, or boolean** values. Validation failures use **`IllegalArgumentException`** with messages aligned to the Node SDK.

**Wire JSON:** Field names and omission rules follow the same contract as the other SDKs’ wire maps. Timestamps are **UTC ISO-8601 with milliseconds and `Z`** (`TimeUtil.utcIsoMs`). **`HttpExporter`** uses the default **`{"intentproof":"1","event":…}`** body with the same **three-tier** serialization fallback as Node/Python.

**Toolchain:** **Java 21** (Gradle Java toolchain; Foojay can provision the JDK if needed). Spotless + **google-java-format** (see `build.gradle.kts`).

If cross-language golden tests reveal a deliberate Java-only difference, document it in this section.

## CI and dependency graph (maintainers)

GitHub may enable **Automatic Dependency Submission** for Gradle (`dynamic/dependency-graph/auto-submission`; the Actions UI may label it **Automatic Dependency Submission**). That **dynamic** workflow runs on **every** push to `main`, including commits that **only** refresh repo-root **`conformance-certificate.json`** and **`conformance-report.json`**.

**`CI`**, **`.github/workflows/conformance-attestation.yml`** (push to **`main`**), and **`.github/workflows/dependency-submission.yml`** skip those pushes via the same **`paths-ignore`** list, so on cert-bot artifact commits you may see **only** the automatic **`submit-gradle`** job succeed. That does **not** mean full SDK CI failed—it did not run by design.

To avoid that extra check (and possible duplicate submission alongside `.github/workflows/dependency-submission.yml`), turn off automatic Gradle dependency submission in the repository or organization **code security / dependency graph** settings and keep the explicit workflow only. See GitHub’s guide to [configuring dependency submission](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/configuring-the-dependency-submission-api).

## Security

Do not open public GitHub issues for undisclosed security vulnerabilities. Use [`SECURITY.md`](SECURITY.md) (private advisory reporting) instead.
