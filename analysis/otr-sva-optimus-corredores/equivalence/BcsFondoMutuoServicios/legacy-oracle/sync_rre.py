"""F2 W1 front 2: copies, byte for byte, the FMM-RRE cases (case.json, seed.sql, expected.json recorded by record_rre.sh) and schema-rre.sql +
seed/base-rre.sql into the module test resources src/test/resources/fondomutuo-rre/. A case without golden is NOT copied (the module test would fail).

  py -3 -I legacy-oracle/sync_rre.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-rre"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "schema-rre.sql", DEST / "schema-rre.sql")
    shutil.copyfile(BASE / "seed" / "base-rre.sql", DEST / "seed" / "base-rre.sql")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases").glob("FMM-RRE-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    print("FMM-RRE cases synced:", n)


if __name__ == "__main__":
    main()
