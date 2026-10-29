"""Thin entry point of the chain front (record_cpfchain.sh, sync_cpfchain.py, make_cases_cpfchain.py, canary_cpfchain.py): runs equivalence/BcsFondoMutuoServicios/make_cases_cpfchain.py.

  py -3 -I legacy-oracle/make_cases_cpfchain.py
"""
import pathlib
import runpy
import sys

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
runpy.run_path(str(HERE.parent / "make_cases_cpfchain.py"), run_name="__main__")
