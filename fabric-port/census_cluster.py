"""Cluster a -PportClient (or any) javac census log by package, file, and error kind.

Usage:
    python fabric-port/census_cluster.py <log> [--pkg /client/key/]

Census workflow (see fabric-port/design/client-compile-plan.md):
    ./gradlew compileJava -PportClient --console=plain > census.log 2>&1
    python fabric-port/census_cluster.py census.log
With --pkg, prints the deduped error messages + a 2-line context snippet for one
subpackage (substring match on the /-normalized path) instead of the histograms.
"""
import collections
import re
import sys

log = sys.argv[1]
pkg = sys.argv[3] if len(sys.argv) > 3 and sys.argv[2] == '--pkg' else None
lines = open(log, encoding='utf-8', errors='replace').read().splitlines()

if pkg:
    shown = 0
    for i, line in enumerate(lines):
        norm = line.replace('\\', '/')
        if ': error:' not in line or pkg not in norm:
            continue
        print(line.strip()[:160])
        for j in range(i + 1, min(i + 4, len(lines))):
            if ': error:' in lines[j]:
                break
            s = lines[j].strip()
            if s and not s.startswith('^'):
                print('    ' + s[:120])
        shown += 1
        if shown >= 40:
            print('... (truncated at 40)')
            break
    sys.exit(0)

errs = [l for l in lines if ': error:' in l]
print('total error lines:', len(errs))
bypkg = collections.Counter()
byfile = collections.Counter()
bykind = collections.Counter()
for l in errs:
    norm = l.replace('\\', '/')
    m = re.search(r'mekanism/(client|common|api)/((?:[\w]+/)*)([A-Z]\w*)\.java', norm)
    if m:
        sub = m.group(2).rstrip('/')
        second = '/'.join(sub.split('/')[:2]) if sub else '(root)'
        bypkg[m.group(1) + '/' + second] += 1
        byfile[m.group(1) + '/' + (sub + '/' if sub else '') + m.group(3)] += 1
    msg = l.split(': error:', 1)[1].strip()
    msg = re.sub(r'symbol:\s+', '', msg)
    msg = re.sub(r'(class|method|variable|package) [\w.$<>,\[\]? ]+', r'\1 X', msg)
    bykind[msg[:100]] += 1

print('--- by package (top 25):')
for k, v in bypkg.most_common(25):
    print(f'{v:6} {k}')
print('--- by file (top 20):')
for k, v in byfile.most_common(20):
    print(f'{v:6} {k}')
print('--- by error kind (top 20):')
for k, v in bykind.most_common(20):
    print(f'{v:6} {k}')
