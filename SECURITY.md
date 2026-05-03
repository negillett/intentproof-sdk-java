# Security policy

## Supported versions

Security updates are applied to the **latest minor release line** on the default branch. Use the newest published version from [Maven Central](https://central.sonatype.com/search?q=g:io.github.intentproof+intentproof-sdk) or [GitHub Releases](https://github.com/IntentProof/intentproof-sdk-java/releases).

## Reporting a vulnerability

Please **do not** file undisclosed security issues as public GitHub issues.

Use **[GitHub private vulnerability reporting](https://github.com/IntentProof/intentproof-sdk-java/security/advisories/new)** for this repository (or your organization’s equivalent process if the repo is forked).

Include: affected version or commit, reproduction steps, and impact assessment if you can.

## Scope

This policy covers the **`intentproof-sdk-java`** library and its build. Consumer applications (how you configure **`HttpExporter`**, credentials, PII in events) remain your responsibility; see the **Security** section in [`README.md`](README.md).
