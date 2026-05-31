"""AC-13: selector.config.json에서 Diversity@7 >= 3 및 gate_status=pass 검증."""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent
CONFIG = ROOT / "ci/selector.config.json"

if not CONFIG.exists():
    print("FAIL AC-13: ci/selector.config.json not found")
    sys.exit(1)

config = json.loads(CONFIG.read_text())
excluded = config["selector_excluded"]

if excluded:
    print("SKIP AC-13: selector_excluded=true — diversity gate is informational only")
    sys.exit(0)

gate = config["gate_status"]
div = config["diversity_at_7"]
sel_score = config["selector_mean_raw_score"]
hash_score = config["hash_k_mean_raw_score"]

errors = []
if gate != "pass":
    errors.append(f"gate_status={gate!r} (expected 'pass')")
if div < 3:
    errors.append(f"diversity_at_7={div} < 3")
if sel_score < hash_score:
    errors.append(f"selector_mean_raw_score={sel_score} < hash_k_mean_raw_score={hash_score}")

if errors:
    print("FAIL AC-13: " + "; ".join(errors))
    sys.exit(1)

print(f"PASS AC-13: gate={gate}, diversity@7={div}, sel_score={sel_score}, hash_score={hash_score}")
