# Changelog

Repository: [IntentProof Java SDK (`intentproof-sdk-java`)](https://github.com/IntentProof/intentproof-sdk-java).

All notable changes to this repository are documented here. **Maven Central** publishes **`io.github.intentproof:intentproof-sdk`** using SemVer aligned with Git tags **`vMAJOR.MINOR.PATCH`** (see [`RELEASING.md`](RELEASING.md)).

## Unreleased

- Add this changelog.
- **Specification conformance (local):** Gradle task **`intentproofSpecConformance`** and [`scripts/spec-conformance.sh`](scripts/spec-conformance.sh) run the canonical [`intentproof-spec`](https://github.com/IntentProof/intentproof-spec) Vitest oracle against a sibling checkout (`../intentproof-spec`) or **`INTENTPROOF_SPEC_ROOT`**.
- **CI:** run the same canonical oracle on every push/PR by checking out `intentproof-spec` in [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
- **Docs & metadata:** README section for the specification repo and release-history pointer; CONTRIBUTING links to the Node and Python SDK repositories; SECURITY and POM **SCM** URLs aligned with **`github.com/IntentProof`**; Gradle **`description`** string; GitHub Release title uses **IntentProof Java SDK** (see [`.github/workflows/release.yml`](.github/workflows/release.yml)).

## 0.1.0 — 2026-05-04

Git tag [`v0.1.0`](https://github.com/IntentProof/intentproof-sdk-java/releases/tag/v0.1.0); Maven **`io.github.intentproof:intentproof-sdk:0.1.0`**.

- Initial **IntentProof Java SDK**: structured **`ExecutionEvent`** emission, **`IntentProofClient`**, **`HttpExporter`** / **`MemoryExporter`** / **`BoundedQueueExporter`**, correlation and snapshot helpers, and Jackson-based JSON wire utilities (Java **21**).
- **Quality gate:** `./gradlew check` runs Spotless, unit tests, JaCoCo coverage verification (full line and instruction ratios on production code), production-only Error Prone, and Javadoc generation.
- **CI:** build and test on **Temurin 21**, plus **Trivy** filesystem scan of the assembled library JAR (runtime dependency vulnerabilities).
- **Release automation:** tag-triggered signing, publish to **Maven Central** (Sonatype), and **GitHub Release** attaching main, sources, and Javadoc JARs ([`RELEASING.md`](RELEASING.md)).
- Cross-SDK **wire-format and behavioral parity** expectations for contributors documented in [`CONTRIBUTING.md`](CONTRIBUTING.md).
