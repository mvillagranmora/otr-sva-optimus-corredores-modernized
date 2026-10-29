"""F3 W1 front 4: copies, byte for byte, the FMM-DET cases (case.json, seed.sql, expected.json recorded by record_det.sh) of cases-det/, schema-det.sql and
seed/base-det.sql into the module test resources src/test/resources/fondomutuo-det/, and the HANA-function stand-ins runner-det/cl/bcs/oracle/H2FnDet.java into the
test source set (src/test/java/cl/bcs/oracle/H2FnDet.java: schema-det.sql aliases MAP / TO_DECIMAL to it, in the oracle and in the new code's H2). A case without
golden is NOT copied (the module test then fails on the missing case count / golden, never passes by omission) and is listed.

  py -3 -I legacy-oracle/sync_det.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MODULO = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MODULO / "src" / "test" / "resources" / "fondomutuo-det"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-det.sql", DEST / "seed" / "base-det.sql")
    shutil.copyfile(BASE / "schema-det.sql", DEST / "schema-det.sql")
    fn = MODULO / "src" / "test" / "java" / "cl" / "bcs" / "oracle" / "H2FnDet.java"
    fn.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(HERE / "runner-det" / "cl" / "bcs" / "oracle" / "H2FnDet.java", fn)
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-det").glob("FMM-DET-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-det.txt").exists():
        shutil.copyfile(BASE / "determinism-det.txt", DEST / "determinism-det.txt")
    print("FMM-DET cases synced:", n)


if __name__ == "__main__":
    main()
