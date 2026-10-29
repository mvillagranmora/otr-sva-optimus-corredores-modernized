#!/usr/bin/env python3
"""Step 3B case generator (inputs only: case.json + seed.sql). The goldens (expected.json) are ALWAYS recorded from the legacy by
legacy-oracle/record3b.sh, never written here. Cases for ValidaMontoInversion, ValidaPatrimonioEmisor, ServicioCumple..., ConsultarFlujoCustodiaFondoMutuo,
CrearSolicitudAGF and CrearSolicitudRescateComisionNN. Run: py -3 -I make_cases3b.py   (rewrites cases3b/<id>/{case.json,seed.sql})."""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import cases3b_lib as L  # noqa: E402
import cases3b_a as A    # noqa: E402
import cases3b_b as B    # noqa: E402
import cases3b_c as C    # noqa: E402
import cases3b_d as D    # noqa: E402
import cases3b_e as E    # noqa: E402
import cases3b_f as F    # noqa: E402

if __name__ == "__main__":
    base = os.path.join(os.path.dirname(os.path.abspath(__file__)), "cases3b")
    n = 0
    for mod in (A, B, C, D, E, F):
        for case in mod.cases():
            L.write(base, case)
            n += 1
    print("cases written:", n)
