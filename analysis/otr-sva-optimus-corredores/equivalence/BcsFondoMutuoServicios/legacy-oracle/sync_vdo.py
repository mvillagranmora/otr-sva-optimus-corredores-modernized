"""F3 W1 front 4: copies, byte for byte, the FMM-VDO cases (case.json, seed.sql, expected.json recorded by record_vdo.sh) from cases-vdo/ and seed/base-vdo.sql
into the module test resources src/test/resources/fondomutuo-vdo/. A case without golden is NOT copied (the module test then fails on the missing case count /
golden, never passes by omission) and is listed.

  py -3 -I legacy-oracle/sync_vdo.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-vdo"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-vdo.sql", DEST / "seed" / "base-vdo.sql")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-vdo").glob("FMM-VDO-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-vdo.txt").exists():
        shutil.copyfile(BASE / "determinism-vdo.txt", DEST / "determinism-vdo.txt")
    print("FMM-VDO cases synced:", n)


if __name__ == "__main__":
    main()
