"""AC-20: AndroidManifest.xml intent-filter 정적 검사.

규칙:
- android.intent.action.VIEW (deep link) 없어야 함
- android.intent.action.MAIN + android.intent.category.LAUNCHER 정확히 1개
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).parent.parent
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
NS = "http://schemas.android.com/apk/res/android"

tree = ET.parse(MANIFEST)
manifest_root = tree.getroot()

errors = []
main_launcher_count = 0

for activity in manifest_root.findall(".//activity"):
    for intent_filter in activity.findall("intent-filter"):
        actions = {e.get(f"{{{NS}}}name") for e in intent_filter.findall("action")}
        categories = {e.get(f"{{{NS}}}name") for e in intent_filter.findall("category")}

        if "android.intent.action.VIEW" in actions:
            name = activity.get(f"{{{NS}}}name", "unknown")
            errors.append(f"deep link intent-filter found in <activity android:name={name!r}>")

        if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories:
            main_launcher_count += 1

if main_launcher_count != 1:
    errors.append(f"expected exactly 1 MAIN/LAUNCHER intent-filter, found {main_launcher_count}")

if errors:
    print("FAIL AC-20:")
    for e in errors:
        print(f"  {e}")
    sys.exit(1)

print("PASS AC-20: AndroidManifest.xml intent-filter structure is correct")
