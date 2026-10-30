"""BcsReplica oracle: copies, byte for byte, what the JUnit tests of the module need into src/test/resources/replica-caracterizacion/ and the three shared harness sources into src/test/java/cl/bcs/replica/oraculo/:
cases (case.json, seed.sql, expected.json recorded by record.sh), schema-replica.sql, determinism.txt, sql-manifest.txt. A case without golden is NOT copied (the module test would fail). Nothing under legacy/
or src/main is touched.

  py -3 -I sync_replica.py                       writes into modernized/.../BcsReplica
  py -3 -I sync_replica.py <module directory>    writes into ANOTHER copy of the module (validation outside the repo)
"""
import json
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import reglas_rep  # noqa: E402

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
MOD = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else WS / "modernized" / "otr-sva-optimus-corredores" / "BcsReplica"
RES = MOD / "src" / "test" / "resources" / "replica-caracterizacion"
HARNESS = BASE / "legacy-oracle" / "harness" / "cl" / "bcs" / "replica" / "oraculo"
JAVA = MOD / "src" / "test" / "java" / "cl" / "bcs" / "replica" / "oraculo"


def main():
    (RES / "cases").mkdir(parents=True, exist_ok=True)
    JAVA.mkdir(parents=True, exist_ok=True)
    for f in ("schema-replica.sql", "determinism.txt", "sql-manifest.txt"):
        shutil.copyfile(BASE / f, RES / f)
    reglas = {"reglas": {k: v[0] for k, v in reglas_rep.REGLAS.items()}, "defectos": {k: v[0] for k, v in reglas_rep.DEFECTOS.items()}}
    (RES / "reglas.json").write_text(json.dumps(reglas, indent=2, ensure_ascii=False) + chr(10), encoding="utf-8", newline=chr(10))
    for f in HARNESS.glob("*.java"):
        shutil.copyfile(f, JAVA / f.name)
    for old in (RES / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases").glob("REP-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = RES / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    print("BcsReplica cases synced:", n, "->", RES)


if __name__ == "__main__":
    main()
