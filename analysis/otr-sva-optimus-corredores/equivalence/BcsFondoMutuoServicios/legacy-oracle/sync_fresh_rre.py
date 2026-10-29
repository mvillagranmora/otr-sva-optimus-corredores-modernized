"""F2 W1 front 2: copies the fresh inputs (fresh-rre/cases/FRESH-RRE-nn) with the dump of the UNMODIFIED legacy (fresh-rre/legacy/<id>.json, recorded by
record_rre.sh with ORACLE_CASES / ORACLE_OUT) as expected.json into src/test/resources/fondomutuo-rre/frescas/ (module test RreFrescasTest). Bytes as is.

  py -3 -I legacy-oracle/sync_fresh_rre.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-rre" / "frescas"


def main():
    if DEST.exists():
        shutil.rmtree(DEST)
    DEST.mkdir(parents=True)
    n = 0
    for d in sorted((BASE / "fresh-rre" / "cases").iterdir()):
        dump = BASE / "fresh-rre" / "legacy" / (d.name + ".json")
        if not dump.exists():
            print("no legacy dump, skipped:", d.name)
            continue
        t = DEST / d.name
        t.mkdir()
        shutil.copyfile(d / "case.json", t / "case.json")
        shutil.copyfile(d / "seed.sql", t / "seed.sql")
        shutil.copyfile(dump, t / "expected.json")
        n += 1
    print("FRESH-RRE synced:", n)


if __name__ == "__main__":
    main()
