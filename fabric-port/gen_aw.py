"""Generate access-widener `accessible field` lines with javap-verified descriptors.

Usage:
    python fabric-port/gen_aw.py members.txt >> review-then-append-to-extra.aw

members.txt: one `<binary.class.Name> <memberName>` pair per line (# comments ok);
inner classes use $. Emits `accessible field <cls> <name> <descriptor>` per resolved
field (never guess a descriptor by hand — the census "has private access in X" list
is the input source; see client-compile-plan.md step 1 for the workflow). Missing
members are reported on stderr. After appending to fabric-port/extra.aw, run
`python fabric-port/at2aw.py` to regenerate the shipped accesswidener.

JAVAP/JAR paths below are machine-specific (this dev box has no JDK on PATH; javap
lives under the Adobe Animate jre — see CLAUDE.md gotchas).
"""
import subprocess
import sys

JAVAP = r"C:/Program Files/Adobe/Adobe Animate 2024/jre/bin/javap.exe"
JAR = (r"C:/Users/Newpi/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/"
       r"1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2/"
       r"minecraft-merged-1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2.jar")


def parse_class(cls):
    out = subprocess.run([JAVAP, "-p", "-s", "-cp", JAR, cls], capture_output=True, text=True)
    if out.returncode != 0:
        print(f"# ERROR javap {cls}: {out.stderr.strip()[:120]}", file=sys.stderr)
        return {}
    fields = {}
    lines = out.stdout.splitlines()
    for i, line in enumerate(lines):
        s = line.strip()
        if s.endswith(";") and "(" not in s and i + 1 < len(lines) and "descriptor:" in lines[i + 1]:
            name = s[:-1].split()[-1]
            fields[name] = lines[i + 1].split("descriptor:")[1].strip()
    return fields


pairs = []
for raw in open(sys.argv[1], encoding='utf-8'):
    raw = raw.strip()
    if raw and not raw.startswith('#'):
        cls, member = raw.split()
        pairs.append((cls, member))

cache = {}
missing = []
for cls, member in pairs:
    if cls not in cache:
        cache[cls] = parse_class(cls)
    desc = cache[cls].get(member)
    if desc is None:
        missing.append((cls, member))
        continue
    print(f"accessible field {cls.replace('.', '/')} {member} {desc}")
for cls, member in missing:
    print(f"# MISSING: {cls}.{member}", file=sys.stderr)
