"""F3 W1 front 3: copies, byte for byte, the FMM-SFM cases (case.json, seed.sql, expected.json recorded by record_sfm.sh) of cases-sfm/, schema-sfm.sql and
seed/base-sfm.sql into the module test resources src/test/resources/fondomutuo-sfm/. A case without golden is NOT copied (the module test then fails on the missing
case count / golden, never passes by omission) and is listed.

  py -3 -I legacy-oracle/sync_sfm.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-sfm"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-sfm.sql", DEST / "seed" / "base-sfm.sql")
    shutil.copyfile(BASE / "schema-sfm.sql", DEST / "schema-sfm.sql")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-sfm").glob("FMM-SFM-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-sfm.txt").exists():
        shutil.copyfile(BASE / "determinism-sfm.txt", DEST / "determinism-sfm.txt")
    print("FMM-SFM cases synced:", n)


if __name__ == "__main__":
    main()
