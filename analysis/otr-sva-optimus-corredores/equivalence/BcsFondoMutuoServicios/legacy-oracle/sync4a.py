"""Copies the step-4A oracle cases of CrearSolicitudAporteRescateNormal into the module's test resources (goldens travel with the module).

    py -3 -I sync4a.py

Copies cases/FMM-NOR-*/{case.json,seed.sql,expected.json} into modernized/.../BcsFondoMutuoServicios/src/test/resources/fondomutuo-nor/cases/.
The schema and base seed are the ones step 3A already copied (fondomutuo-3a/). Pure copy (bytes untouched; never edit the copies by hand: re-record with
record.sh FMM-NOR and sync again). Fails when a case has no expected.json.
"""
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-nor" / "cases"


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    n = 0
    for d in sorted((BASE / "cases").iterdir()):
        if not d.name.startswith("FMM-NOR-") or not (d / "case.json").exists():
            continue
        if not (d / "expected.json").exists():
            print("MISSING golden: " + d.name)
            sys.exit(1)
        out = DEST / d.name
        out.mkdir(parents=True, exist_ok=True)
        for name in ("case.json", "seed.sql", "expected.json"):
            if (d / name).exists():
                shutil.copyfile(d / name, out / name)
        n += 1
    print("synced %d cases to %s" % (n, DEST))


if __name__ == "__main__":
    main()
