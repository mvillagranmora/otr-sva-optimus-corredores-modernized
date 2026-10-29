"""Thin entry point so that the whole cpf front (record_cpf.sh, sync_cpf.py, make_cases_cpf.py, canary_cpf.py, shims-cpf/) can be driven from legacy-oracle/. The generator itself lives
next to its case libraries one directory up (equivalence/BcsFondoMutuoServicios/make_cases_cpf.py, like make_cases_det.py); this file only runs it.

  py -3 -I legacy-oracle/make_cases_cpf.py
"""
import pathlib
import runpy
import sys

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
runpy.run_path(str(HERE.parent / "make_cases_cpf.py"), run_name="__main__")
