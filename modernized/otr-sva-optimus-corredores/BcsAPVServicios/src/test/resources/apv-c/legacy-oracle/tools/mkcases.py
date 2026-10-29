#!/usr/bin/env python3
"""Writes the INPUTS of every APV family C case (case.json + seed.sql). Usage: python mkcases.py <apv-c resources dir>.
The goldens (expected.json) are recorded afterwards by legacy-oracle/record.sh, never written here."""
import importlib
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'casegen'))
import lib  # noqa: E402

MODULOS = [m[:-3] for m in sorted(os.listdir(os.path.join(os.path.dirname(__file__), 'casegen'))) if m.startswith('c_') and m.endswith('.py')]
for m in MODULOS:
    importlib.import_module(m)

raiz = sys.argv[1]
esperados = lib.escribir(raiz)
print('cases written:', len(lib.CASOS), 'modules:', ', '.join(MODULOS))
