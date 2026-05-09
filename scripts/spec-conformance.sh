#!/usr/bin/env bash
# Resolve the IntentProof specification checkout (directory name intentproof-spec) and run its canonical conformance script (Vitest oracle).
# CI checks out the spec repo explicitly; locally use a sibling clone or INTENTPROOF_SPEC_ROOT.
set -euo pipefail

sdk_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
spec_root="${INTENTPROOF_SPEC_ROOT:-}"

if [[ -z "$spec_root" ]]; then
  sibling="${sdk_root}/../intentproof-spec"
  if [[ -f "${sibling}/package.json" && -d "${sibling}/schema" ]]; then
    spec_root="$(cd "$sibling" && pwd)"
  fi
fi

if [[ -z "$spec_root" || ! -f "${spec_root}/scripts/run-conformance.sh" ]]; then
  echo "IntentProof specification checkout (intentproof-spec) not found." >&2
  echo "  Clone https://github.com/IntentProof/intentproof-spec next to this repo (../intentproof-spec), or set INTENTPROOF_SPEC_ROOT to the spec checkout." >&2
  exit 1
fi

bash "${sdk_root}/scripts/check-consumer-spec-pin.sh" "$spec_root"

export INTENTPROOF_SDK_ID="${INTENTPROOF_SDK_ID:-java}"
export INTENTPROOF_SDK_NAME="${INTENTPROOF_SDK_NAME:-intentproof-sdk-java}"
export INTENTPROOF_SDK_LANGUAGE="${INTENTPROOF_SDK_LANGUAGE:-java}"
export INTENTPROOF_SDK_VERSION="${INTENTPROOF_SDK_VERSION:-$(grep -E '^version=' "${sdk_root}/gradle.properties" | head -1 | cut -d= -f2)}"

exec bash "${spec_root}/scripts/run-conformance.sh" "$spec_root"
