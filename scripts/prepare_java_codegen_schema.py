#!/usr/bin/env python3
"""
Prepare execution_event JSON Schema for jsonschema2pojo (same relaxations as Python SDK codegen).

- JsonValue recursion flattened for Java POJO generation.
- ``output`` widened to schema ``true`` for portability.
- Conditional ``allOf`` rules omitted (runtime validation remains normative in intentproof-spec).
"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path


def _resolve_spec_root() -> Path:
    env = os.environ.get("INTENTPROOF_SPEC_ROOT", "").strip()
    if env:
        p = Path(env)
        if (p / "spec.json").is_file():
            return p.resolve()
    here = Path(__file__).resolve().parents[1]
    sib = here.parent / "intentproof-spec"
    if (sib / "spec.json").is_file():
        return sib.resolve()
    print(
        "prepare_java_codegen_schema: set INTENTPROOF_SPEC_ROOT or clone intentproof-spec beside this repo",
        file=sys.stderr,
    )
    sys.exit(1)


def _patch_json_value_for_codegen(schema: dict) -> None:
    schema.setdefault("$defs", {})["JsonValue"] = {
        "anyOf": [
            {"type": "null"},
            {"type": "boolean"},
            {"type": "number"},
            {"type": "string"},
            {"type": "array", "items": True},
            {"type": "object", "additionalProperties": True},
        ]
    }


def _simplify_output(schema: dict) -> None:
    if "properties" in schema and "output" in schema["properties"]:
        schema["properties"]["output"] = True


def main() -> None:
    out_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else None
    if out_dir is None:
        print("usage: prepare_java_codegen_schema.py <output-dir>", file=sys.stderr)
        sys.exit(2)
    out_dir.mkdir(parents=True, exist_ok=True)

    spec_root = _resolve_spec_root()
    ee_path = spec_root / "schema" / "execution_event.v1.schema.json"
    ee = json.loads(ee_path.read_text(encoding="utf-8"))
    _patch_json_value_for_codegen(ee)
    _simplify_output(ee)
    ee.pop("allOf", None)
    ee["javaType"] = "com.intentproof.sdk.generated.v1.IntentProofExecutionEventV1"

    dest = out_dir / "execution_event.v1.schema.json"
    dest.write_text(json.dumps(ee, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"wrote {dest}", file=sys.stderr)


if __name__ == "__main__":
    main()
