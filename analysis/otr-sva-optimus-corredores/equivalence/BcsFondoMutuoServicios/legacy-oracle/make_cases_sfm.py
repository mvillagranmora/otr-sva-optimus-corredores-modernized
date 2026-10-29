"""Thin entry point so that the whole sfm front (record_sfm.sh, sync_sfm.py, make_cases_sfm.py, shims-sfm/) can be driven from legacy-oracle/. The generator itself lives next to its
case libraries one directory up (equivalence/BcsFondoMutuoServicios/make_cases_sfm.py, like make_cases_fsa.py); this file only runs it.

  py -3 -I legacy-oracle/make_cases_sfm.py
"""
import pathlib
import runpy
import sys

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
runpy.run_path(str(HERE.parent / "make_cases_sfm.py"), run_name="__main__")
