"""Writes every FRESH input (case.json + seed.sql) under fresh/cases/: FRESH-NOR-nn (make_fresh_nor.py) and FRESH-MAN-nn (make_fresh_man.py).
The goldens/dumps are produced by run_fresh.py (legacy and new, same OracleRunner). Never run against cases/.
  py -3 make_fresh.py
"""
import pathlib

import cases_nor_lib as L
import make_fresh_man
import make_fresh_nor

if __name__ == "__main__":
    L.CASES = pathlib.Path(make_fresh_man.OUT)
    L.CASES.mkdir(parents=True, exist_ok=True)
    make_fresh_nor.generar()
    make_fresh_man.generar()
    print("fresh cases:", len(list(L.CASES.iterdir())))
