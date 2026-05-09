# Changelog

Repository: [IntentProof Java SDK (`intentproof-sdk-java`)](https://github.com/IntentProof/intentproof-sdk-java).

All notable changes to this repository are documented here. **Maven Central** publishes **`io.github.intentproof:intentproof-sdk`** using SemVer aligned with Git tags **`vMAJOR.MINOR.PATCH`** (see [`RELEASING.md`](RELEASING.md)).

## Unreleased

- **CI / conformance split:** add **`spec-conformance.yml`** (PR + manual oracle) and **`conformance-attestation.yml`** (trusted **`main`** signing, validation, cert-bot publish-back), matching Node/Python/API; trim oracle + publish from **`ci.yml`**.

## 0.1.3 — 2026-05-08

- **Conformance pipeline hardening:** pin to `spec-v2.0.1`, keep canonical
  checks green when workflow push-back is blocked by branch protection, and
  continue uploading report/certificate artifacts for each run.
- **Cert-bot loop prevention:** skip conformance publish follow-up on cert-bot
  actor runs and ignore conformance-only root JSON updates on push to
  `main`/`master` to prevent repeated release-branch churn.
- **Dependency-check flow alignment:** match dependency submission behavior to CI
  path filters for conformance-only updates, and document GitHub automatic
  dependency submission behavior in CONTRIBUTING for maintainers.
- **CI/docs polish:** tighten workflow token permissions, simplify publish
  gating conditions, and refresh README conformance guidance.
- **Javadoc reliability:** exclude generated schema classes from Javadoc scope
  and remove a flaky third-party docs link to prevent transient external
  outages from failing CI.

## 0.1.2 — 2026-05-06

- **Spec-first generated wire model layer:** replace handwritten wire-model
  ownership with jsonschema2pojo output from pinned `intentproof-spec`
  schemas (`generated/v1`), including `ExecutionEvent`/`ExecutionWire`
  integration updates.
- **Pinning and hardening enforcement:** add immutable spec version+commit
  declarations in `gradle.properties`; enforce pin validity, no bundled
  schemas, no handwritten wire models, and generated-source drift checks via
  dedicated scripts.
- **Build/CI reliability:** add schema preparation/codegen task chain and
  deterministic task ordering around Spotless/Javadoc/JaCoCo; harden CI and
  release workflows to check out pinned spec SHA and run conformance/drift
  gates.
- **Parity and quality coverage:** expand execution-wire tests/fixtures and
  align docs (README/CONTRIBUTING) with spec-pinned development workflow.

## 0.1.1 — 2026-05-04

Git tag [`v0.1.1`](https://github.com/IntentProof/intentproof-sdk-java/releases/tag/v0.1.1); Maven **`io.github.intentproof:intentproof-sdk:0.1.1`**.

- Add this changelog.
- **Specification conformance (local):** Gradle task **`intentproofSpecConformance`** and [`scripts/spec-conformance.sh`](scripts/spec-conformance.sh) run the canonical [`intentproof-spec`](https://github.com/IntentProof/intentproof-spec) Vitest oracle against a sibling checkout (`../intentproof-spec`) or **`INTENTPROOF_SPEC_ROOT`**.
- **CI:** run the same canonical oracle on every push/PR by checking out `intentproof-spec` in [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
- **Docs & metadata:** README section for the specification repo and release-history pointer; CONTRIBUTING links to the Node and Python SDK repositories; SECURITY and POM **SCM** URLs use **`github.com/IntentProof`**; Gradle **`description`** string; GitHub Release title **IntentProof Java SDK** (see [`.github/workflows/release.yml`](.github/workflows/release.yml)).

## 0.1.0 — 2026-05-04

Git tag [`v0.1.0`](https://github.com/IntentProof/intentproof-sdk-java/releases/tag/v0.1.0); Maven **`io.github.intentproof:intentproof-sdk:0.1.0`**.

- Initial **IntentProof Java SDK**: structured **`ExecutionEvent`** emission, **`IntentProofClient`**, **`HttpExporter`** / **`MemoryExporter`** / **`BoundedQueueExporter`**, correlation and snapshot helpers, and Jackson-based JSON wire utilities (Java **21**).
- **Quality gate:** `./gradlew check` runs Spotless, unit tests, JaCoCo coverage verification (full line and instruction ratios on production code), production-only Error Prone, and Javadoc generation.
- **CI:** build and test on **Temurin 21**, plus **Trivy** filesystem scan of the assembled library JAR (runtime dependency vulnerabilities).
- **Release automation:** tag-triggered signing, publish to **Maven Central** (Sonatype), and **GitHub Release** attaching main, sources, and Javadoc JARs ([`RELEASING.md`](RELEASING.md)).
- Cross-SDK **wire-format and behavioral parity** expectations for contributors documented in [`CONTRIBUTING.md`](CONTRIBUTING.md).
