#!/usr/bin/env python3
"""Writes the INPUTS of every apv-jobs case (case.json + seed.sql). Usage: python mkcases.py <apv-jobs resources dir>.
The goldens (expected.json) are recorded afterwards by legacy-oracle/record.sh, never written here."""
import importlib
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'casegen'))
import lib  # noqa: E402

for m in ['c_carga', 'c_ordenbf', 'c_fm', 'c_fm2', 'c_rv', 'c_rv2']:
    if os.path.exists(os.path.join(os.path.dirname(__file__), 'casegen', m + '.py')):
        importlib.import_module(m)

print('cases written:', lib.escribir(sys.argv[1]))
