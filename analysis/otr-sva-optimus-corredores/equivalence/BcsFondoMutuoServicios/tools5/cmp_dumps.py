#!/usr/bin/env python3
"""Compares two dump directories case by case with the oracle's strict comparison (compare.same: whole dump, key order, without _provenance).
  py -3 cmp_dumps.py <baselineDir> <otherDir> <equivalenceDir> [casePrefix ...]
Only the cases present in <otherDir> are compared. Prints one line per different case and a summary 'DETECTED n of m cases changed' (exit 0 always: the caller reads it).
Used by the canary (baseline = dumps of the unmodified new code, other = dumps of a mutant): a mutant is caught by the oracle when at least one case changes.
"""
import glob
import os
import sys

sys.path.insert(0, sys.argv[3])
import compare  # noqa: E402


def main():
    base, other = sys.argv[1], sys.argv[2]
    prefixes = tuple(sys.argv[4:]) or ("",)
    changed, total = [], 0
    for p in sorted(glob.glob(os.path.join(other, "*.json"))):
        cid = os.path.basename(p)[:-5]
        if not cid.startswith(prefixes):
            continue
        total += 1
        b = os.path.join(base, cid + ".json")
        if not os.path.exists(b) or not compare.same(open(b, encoding="utf-8").read(), open(p, encoding="utf-8").read()):
            changed.append(cid)
    for c in changed:
        print("  changed:", c)
    print("ORACLE: %s %d of %d fresh cases changed" % ("DETECTED" if changed else "NOT DETECTED -", len(changed), total))


if __name__ == "__main__":
    main()
