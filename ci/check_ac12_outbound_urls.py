"""AC-12: Android Kotlin 소스에서 api.open-meteo.com 외 outbound URL 정적 검사."""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent
KOTLIN_SRC = ROOT / "app/src/main/java"
ALLOWED_HOSTS = {"api.open-meteo.com"}

URL_RE = re.compile(r'https?://([a-zA-Z0-9._-]+)')

failures = []
for kt_file in KOTLIN_SRC.rglob("*.kt"):
    for lineno, line in enumerate(kt_file.read_text().splitlines(), 1):
        for match in URL_RE.finditer(line):
            host = match.group(1).rstrip(".")
            if host not in ALLOWED_HOSTS:
                rel = kt_file.relative_to(ROOT)
                failures.append(f"  {rel}:{lineno}: {match.group(0)}")

if failures:
    print("FAIL AC-12: disallowed outbound URLs found:")
    for f in failures:
        print(f)
    sys.exit(1)

print("PASS AC-12: all outbound URLs are api.open-meteo.com only")
