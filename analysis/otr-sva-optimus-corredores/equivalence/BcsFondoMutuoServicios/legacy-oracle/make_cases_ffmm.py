"""Thin entry point so that the whole ffmm front (record_ffmm.sh, sync_ffmm.py, make_cases_ffmm.py, canary_ffmm.py, shims-ffmm/) can be driven from legacy-oracle/. The generator itself lives
next to its case libraries one directory up (equivalence/BcsFondoMutuoServicios/make_cases_ffmm.py, like make_cases_cpf.py); this file only runs it.

  py -3 -I legacy-oracle/make_cases_ffmm.py
"""
import pathlib
import runpy
import sys

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
runpy.run_path(str(HERE.parent / "make_cases_ffmm.py"), run_name="__main__")
