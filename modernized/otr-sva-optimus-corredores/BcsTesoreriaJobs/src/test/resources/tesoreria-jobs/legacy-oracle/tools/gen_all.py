"""Regenerates every case input: py -3 gen_all.py [CM|CG|PG|PD ...]  (no argument: all). Then run ../record.sh to record expected.json."""
import importlib
import sys

from gen_common import limpiar, total

GRUPOS = {
    "CM": ["gen_cm1", "gen_cm2"],
    "CG": ["gen_cg1", "gen_cg2"],
    "PG": ["gen_pg1", "gen_pg2", "gen_pg3"],
    "PD": ["gen_pd"],
}

if __name__ == "__main__":
    elegidos = sys.argv[1:] or list(GRUPOS)
    for g in elegidos:
        limpiar(g + "-")
        for m in GRUPOS[g]:
            try:
                importlib.import_module(m).generar()
            except ModuleNotFoundError as e:
                if e.name != m:
                    raise
                print("skipped (not written yet):", m)
    print("cases written:", total())
