#!/usr/bin/env python3
"""Writes the INPUTS of every APV family A case (case.json + seed.sql). Usage: python mkcases.py <apv-a resources dir>.
The goldens (expected.json) are recorded afterwards by legacy-oracle/record.sh, never written here."""
import importlib
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'casegen'))
import lib  # noqa: E402

MODULOS = ['c_simples', 'c_valida', 'c_estado', 'c_grabar', 'c_modificar', 'c_planactivo', 'c_crear', 'c_revocacion', 'c_recaudacion', 'c_consultas',
           'c_autorizacion']
for m in MODULOS:
    importlib.import_module(m)

raiz = sys.argv[1]
esperados = lib.escribir(raiz)
print('cases written:', len(lib.CASOS))
