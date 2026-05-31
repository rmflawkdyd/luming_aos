"""AC-11: selector_excluded flag <-> selector.tflite 존재 양방향 게이트."""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent
CONFIG = ROOT / "ci/selector.config.json"
TFLITE = ROOT / "app/src/main/assets/selector.tflite"

if not CONFIG.exists():
    print("FAIL AC-11: ci/selector.config.json not found")
    sys.exit(1)

config = json.loads(CONFIG.read_text())
excluded = config["selector_excluded"]
tflite_exists = TFLITE.exists()

if excluded and tflite_exists:
    print("FAIL AC-11: selector_excluded=true but selector.tflite is present in assets — remove the artifact or re-run eval_diversity.py")
    sys.exit(1)

if not excluded and not tflite_exists:
    print("FAIL AC-11: selector_excluded=false but selector.tflite is missing from assets — add the artifact or set selector_excluded=true")
    sys.exit(1)

print(f"PASS AC-11: selector_excluded={excluded}, selector.tflite present={tflite_exists}")
