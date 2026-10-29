#!/usr/bin/env python3
"""Copies the Manejador cases (FMM-MAN-*) of the oracle (inputs + goldens recorded from the legacy) to the module test resources, byte for byte:
src/test/resources/fondomutuo-man/cases/<case>/{case.json,seed.sql,expected.json}. A case without a golden is an ERROR (never hand-written).
  py -3 -I sync_man.py
"""
import glob
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.dirname(HERE)
WS = os.path.abspath(os.path.join(BASE, "..", "..", "..", ".."))
DEST = os.path.join(WS, "modernized", "otr-sva-optimus-corredores", "BcsFondoMutuoServicios", "src", "test", "resources", "fondomutuo-man", "cases")

n = 0
for d in sorted(glob.glob(os.path.join(BASE, "cases", "FMM-MAN-*"))):
    for f in ("case.json", "seed.sql", "expected.json"):
        if not os.path.exists(os.path.join(d, f)):
            sys.exit("missing %s in %s (record it with record.sh, do not write it)" % (f, d))
    out = os.path.join(DEST, os.path.basename(d))
    os.makedirs(out, exist_ok=True)
    for f in ("case.json", "seed.sql", "expected.json"):
        shutil.copyfile(os.path.join(d, f), os.path.join(out, f))
    n += 1
print(n, "cases copied to", DEST)
