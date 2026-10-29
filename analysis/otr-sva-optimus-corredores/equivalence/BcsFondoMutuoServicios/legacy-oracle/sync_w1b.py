"""F2 W1B: copies the layer-2 fixtures into the module's test resources (pure byte copy; never edit the copies):
  schema-tesoreria-fm.sql, seed/base-tesoreria-fm.sql  -> src/test/resources/fondomutuo-3a/ (next to the base schema the harness loads)
  cases/FMM-NOR-T*/{case.json,seed.sql,expected.json}  -> src/test/resources/fondomutuo-nor/cases/ (a case without expected.json FAILS the sync)

    py -3 -I sync_w1b.py
"""
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
RES = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources"


def main():
    for rel in ("schema-tesoreria-fm.sql", "seed/base-tesoreria-fm.sql"):
        dst = RES / "fondomutuo-3a" / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(BASE / rel, dst)
    n = 0
    for d in sorted((BASE / "cases").iterdir()):
        if not d.name.startswith("FMM-NOR-T") or not (d / "case.json").exists():
            continue
        if not (d / "expected.json").exists():
            print("MISSING golden: " + d.name)
            sys.exit(1)
        out = RES / "fondomutuo-nor" / "cases" / d.name
        out.mkdir(parents=True, exist_ok=True)
        for name in ("case.json", "seed.sql", "expected.json"):
            if (d / name).exists():
                shutil.copyfile(d / name, out / name)
        n += 1
    print("synced schema + %d layer-2 cases" % n)


if __name__ == "__main__":
    main()
