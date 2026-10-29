#!/usr/bin/env python3
"""Copies, byte for byte, the ConsultarFlujoCustodia oracle material into the module tests of
modernized/otr-sva-optimus-corredores/BcsCustodiaServicios:

  src/test/resources/custodia-flujo/   schema-flujo.sql, schema-session.sql, seed/base.sql, unit-goldens/item-fuente.json,
                                       cases/<id>/{case.json, seed.sql, expected.json}
  src/test/java/cl/bcs/custodia/flujo/h2/   the SINGLE-SOURCE harness classes of legacy-oracle/h2/ (dialect bridge, HANA functions, MatrizUso
                                            witness, case script, case runner + dump), so legacy and new are measured by the same code.

A case without expected.json (no golden recorded from the legacy) is NOT copied and makes the script fail: a comparison that cannot run is a
failure, never a skip. Writes only under the two destination directories above (old content there is replaced).

  py -3 -I legacy-oracle/sync.py
"""
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MOD = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsCustodiaServicios"
RES = MOD / "src" / "test" / "resources" / "custodia-flujo"
JAV = MOD / "src" / "test" / "java" / "cl" / "bcs" / "custodia" / "flujo" / "h2"


def main():
    sin_golden = [d.name for d in sorted((BASE / "cases").iterdir()) if d.is_dir() and not (d / "expected.json").exists()]
    if sin_golden:
        print("cases without a golden (run legacy-oracle/record.sh first):", sin_golden)
        sys.exit(2)
    if RES.exists():
        shutil.rmtree(RES)
    (RES / "seed").mkdir(parents=True)
    (RES / "unit-goldens").mkdir()
    (RES / "cases").mkdir()
    for f in ("schema-flujo.sql", "schema-session.sql"):
        shutil.copyfile(BASE / f, RES / f)
    shutil.copyfile(BASE / "seed" / "base.sql", RES / "seed" / "base.sql")
    shutil.copyfile(BASE / "unit-goldens" / "item-fuente.json", RES / "unit-goldens" / "item-fuente.json")
    n = 0
    for d in sorted((BASE / "cases").iterdir()):
        if not d.is_dir():
            continue
        t = RES / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    JAV.mkdir(parents=True, exist_ok=True)
    for old in JAV.glob("*.java"):
        old.unlink()
    k = 0
    for src in sorted((HERE / "h2" / "cl" / "bcs" / "custodia" / "flujo" / "h2").glob("*.java")):
        shutil.copyfile(src, JAV / src.name)
        k += 1
    print("goldens synced:", n, "| harness classes synced:", k)


if __name__ == "__main__":
    main()
