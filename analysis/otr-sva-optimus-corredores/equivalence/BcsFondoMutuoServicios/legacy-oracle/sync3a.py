"""Copies the step-3A oracle fixtures into the module's test resources (the Tesoreria pattern: goldens travel with the module).

    py -3 -I sync3a.py

Copies cases3a/<case>/{case.json,seed.sql,expected.json} and the schema/base files that the oracle runs on into
modernized/.../BcsFondoMutuoServicios/src/test/resources/fondomutuo-3a/. Pure copy (bytes untouched; never edit the copies by hand: re-record with
record3a.sh and sync again). Fails when a case has no expected.json (a fixture that was never recorded).
"""
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-3a"

FILES = ["schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql", "seed/doubles-base.json"]


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    for f in FILES:
        (DEST / f).parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(BASE / f, DEST / f)
    n = 0
    for d in sorted((BASE / "cases3a").iterdir()):
        if not (d / "case.json").exists():
            continue
        if not (d / "expected.json").exists():
            print("MISSING golden: " + d.name)
            sys.exit(1)
        out = DEST / "cases" / d.name
        out.mkdir(parents=True, exist_ok=True)
        for name in ("case.json", "seed.sql", "expected.json"):
            if (d / name).exists():
                shutil.copyfile(d / name, out / name)
        n += 1
    print("synced %d cases and %d fixture files to %s" % (n, len(FILES), DEST))


main()
