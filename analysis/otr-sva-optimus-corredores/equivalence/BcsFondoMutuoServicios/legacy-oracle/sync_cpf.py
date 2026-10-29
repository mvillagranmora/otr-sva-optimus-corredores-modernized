"""F3 W1 front 8: copies, byte for byte, the FMM-CPF cases (case.json, seed.sql, expected.json recorded by record_cpf.sh) of cases-cpf/, schema-cpf.sql and seed/base-cpf.sql
into the module test resources src/test/resources/fondomutuo-cpf/ (cases under cases-cpf/, NOT cases/), and the HANA-function stand-ins runner-cpf/cl/bcs/oracle/H2FnCpf.java
into the test source set (src/test/java/cl/bcs/oracle/H2FnCpf.java: schema-cpf.sql aliases MAP / TO_DECIMAL to it, in the oracle and in the new code's H2). A case without
golden is NOT copied (the module test then fails on the missing case count / golden, never passes by omission) and is listed. Nothing outside those two destinations is written.

  py -3 -I legacy-oracle/sync_cpf.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MODULO = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MODULO / "src" / "test" / "resources" / "fondomutuo-cpf"


def main():
    (DEST / "cases-cpf").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-cpf.sql", DEST / "seed" / "base-cpf.sql")
    shutil.copyfile(BASE / "schema-cpf.sql", DEST / "schema-cpf.sql")
    fn = MODULO / "src" / "test" / "java" / "cl" / "bcs" / "oracle" / "H2FnCpf.java"
    fn.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(HERE / "runner-cpf" / "cl" / "bcs" / "oracle" / "H2FnCpf.java", fn)
    for old in (DEST / "cases-cpf").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-cpf").glob("FMM-CPF-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases-cpf" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-cpf.txt").exists():
        shutil.copyfile(BASE / "determinism-cpf.txt", DEST / "determinism-cpf.txt")
    print("FMM-CPF cases synced:", n)


if __name__ == "__main__":
    main()
