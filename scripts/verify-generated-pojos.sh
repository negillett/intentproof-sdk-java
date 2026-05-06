#!/usr/bin/env bash
# Regenerate jsonschema2pojo sources under src/main/java/.../generated/v1 and fail on drift.
set -euo pipefail
repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo"

if [[ -z "${INTENTPROOF_SPEC_ROOT:-}" ]]; then
  echo "verify-generated-pojos: set INTENTPROOF_SPEC_ROOT to an intentproof-spec checkout" >&2
  exit 1
fi

./gradlew --no-daemon intentproofGenerateSchemaSources -PintentproofSpecRoot="${INTENTPROOF_SPEC_ROOT}"

git diff --exit-code -- src/main/java/com/intentproof/sdk/generated/v1
if [[ -n "$(git ls-files --others --exclude-standard -- src/main/java/com/intentproof/sdk/generated/v1)" ]]; then
  echo "verify-generated-pojos: untracked files under generated/v1 after generation" >&2
  git ls-files --others --exclude-standard -- src/main/java/com/intentproof/sdk/generated/v1 >&2
  exit 1
fi

echo "OK: generated Java POJOs match intentproof-spec"
