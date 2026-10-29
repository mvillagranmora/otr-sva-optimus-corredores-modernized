"""F3 W1 front 2: copies, byte for byte, the FMM-FMR cases (case.json, seed.sql, expected.json recorded by record_fmr.sh) from cases-fmr/ and seed/base-fmr.sql
into the module test resources src/test/resources/fondomutuo-fmr/. A case without golden is NOT copied (the module test then fails on the missing case count /
golden, never passes by omission) and is listed.

  py -3 -I legacy-oracle/sync_fmr.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-fmr"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-fmr.sql", DEST / "seed" / "base-fmr.sql")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-fmr").glob("FMM-FMR-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-fmr.txt").exists():
        shutil.copyfile(BASE / "determinism-fmr.txt", DEST / "determinism-fmr.txt")
    print("FMM-FMR cases synced:", n)


if __name__ == "__main__":
    main()
