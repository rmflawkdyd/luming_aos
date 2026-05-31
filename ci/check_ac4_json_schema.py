"""AC-4: activities.v1.json JSON Schema 유효성 검사."""
import json
import sys
from pathlib import Path

try:
    import jsonschema
except ImportError:
    print("ERROR: jsonschema not installed — run: pip install jsonschema")
    sys.exit(2)

ROOT = Path(__file__).parent.parent
SCHEMA_FILE = ROOT / "ci/schema.activities.v1.json"
DATA_FILE = ROOT / "app/src/main/assets/activities.v1.json"

schema = json.loads(SCHEMA_FILE.read_text())
data = json.loads(DATA_FILE.read_text())

try:
    jsonschema.validate(instance=data, schema=schema)
    activity_count = len(data.get("activities", []))
    print(f"PASS AC-4: activities.v1.json is valid ({activity_count} activities)")
except jsonschema.ValidationError as e:
    path = " -> ".join(str(p) for p in e.absolute_path) or "(root)"
    print(f"FAIL AC-4: {path}: {e.message}")
    sys.exit(1)
except jsonschema.SchemaError as e:
    print(f"FAIL AC-4: schema itself is invalid: {e.message}")
    sys.exit(1)
