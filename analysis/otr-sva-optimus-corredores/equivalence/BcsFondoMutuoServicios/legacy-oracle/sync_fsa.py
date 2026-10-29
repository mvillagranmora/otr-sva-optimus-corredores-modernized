"""F3 W1 front 1: copies, byte for byte, the FMM-FSA cases (case.json, seed.sql, expected.json recorded by record_fsa.sh) and seed/base-fsa.sql into the module
test resources src/test/resources/fondomutuo-fsa/. A case without golden is NOT copied (the module test then fails on the missing case count / golden, never
passes by omission) and is listed.

  py -3 -I legacy-oracle/sync_fsa.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-fsa"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-fsa.sql", DEST / "seed" / "base-fsa.sql")
    shutil.copyfile(BASE / "schema-fsa.sql", DEST / "schema-fsa.sql")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases").glob("FMM-FSA-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-fsa.txt").exists():
        shutil.copyfile(BASE / "determinism-fsa.txt", DEST / "determinism-fsa.txt")
    print("FMM-FSA cases synced:", n)


if __name__ == "__main__":
    main()
