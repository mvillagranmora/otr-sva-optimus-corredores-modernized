"""F3 W1 front 9: copies, byte for byte, the FM-CPFF cases (case.json, seed.sql, expected.json recorded by record_ffmm.sh) of cases-ffmm/, schema-ffmm.sql and seed/base-ffmm.sql
into the module test resources src/test/resources/fondomutuo-ffmm/ (cases under cases-ffmm/, NOT cases/), and the HANA-function stand-in runner-ffmm/cl/bcs/oracle/H2FnFfmm.java
into the test source set (src/test/java/cl/bcs/oracle/H2FnFfmm.java: schema-ffmm.sql aliases MAP to it, in the oracle and in the new code's H2). A case without golden is NOT copied
(the module test then fails on the missing case count / golden, never passes by omission) and is listed. Nothing outside those two destinations is written.

  py -3 -I legacy-oracle/sync_ffmm.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MODULO = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MODULO / "src" / "test" / "resources" / "fondomutuo-ffmm"


def main():
    (DEST / "cases-ffmm").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-ffmm.sql", DEST / "seed" / "base-ffmm.sql")
    shutil.copyfile(BASE / "schema-ffmm.sql", DEST / "schema-ffmm.sql")
    fn = MODULO / "src" / "test" / "java" / "cl" / "bcs" / "oracle" / "H2FnFfmm.java"
    fn.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(HERE / "runner-ffmm" / "cl" / "bcs" / "oracle" / "H2FnFfmm.java", fn)
    for old in (DEST / "cases-ffmm").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-ffmm").glob("FM-CPFF-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases-ffmm" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-ffmm.txt").exists():
        shutil.copyfile(BASE / "determinism-ffmm.txt", DEST / "determinism-ffmm.txt")
    print("FM-CPFF cases synced:", n)


if __name__ == "__main__":
    main()
