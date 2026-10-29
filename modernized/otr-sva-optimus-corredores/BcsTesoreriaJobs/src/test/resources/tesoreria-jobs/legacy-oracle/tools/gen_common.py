"""Shared helpers of the case generators (gen_*.py). Run: py -3 gen_all.py  (writes ../../cases/<ID>/case.json and seed.sql).

A case is the INPUT only; expected.json is recorded from the legacy by record.sh, never written here.
"""
import copy
import json
import os
import shutil

CASES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "cases")
PROGRAMA = {"nombre": "BSCL.Plataforma.BD::BAT.Programa", "orden": "\"IdPrograma\""}
_escritos = []


def merge(base, over):
    out = copy.deepcopy(base)
    for k, v in over.items():
        if isinstance(v, dict) and isinstance(out.get(k), dict):
            out[k] = merge(out[k], v)
        else:
            out[k] = v
    return out


def escribir(cid, caso, seed=None):
    d = os.path.join(CASES, cid)
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(caso, f, indent=2, ensure_ascii=False)
        f.write("\n")
    s = os.path.join(d, "seed.sql")
    if seed:
        with open(s, "w", encoding="utf-8", newline="\n") as f:
            f.write(seed.strip() + "\n")
    elif os.path.exists(s):
        os.remove(s)
    _escritos.append(cid)


def limpiar(prefijo):
    if os.path.isdir(CASES):
        for n in os.listdir(CASES):
            if n.startswith(prefijo):
                shutil.rmtree(os.path.join(CASES, n))


def total():
    return len(_escritos)
