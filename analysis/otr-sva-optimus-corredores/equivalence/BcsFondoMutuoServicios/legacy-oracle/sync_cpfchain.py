"""F3 W1 front 9, part B (the CHAIN): copies, byte for byte, the FM-CHAIN cases (case.json, seed.sql, expected.json recorded by record_cpfchain.sh) of cases-cpfchain/, schema-cpfchain.sql and
seed/base-cpfchain.sql into the module test resources src/test/resources/fondomutuo-cpfchain/ (cases under cases-cpfchain/, NOT cases-cpf/), TOGETHER with byte copies of the CPF front's
schema-cpf.sql and seed/base-cpf.sql (read only: the CPF front's own files are never written; the chain loads them first, exactly as the oracle did). The HANA-function stand-ins MAP / TO_DECIMAL
of schema-cpf.sql are implemented by src/test/java/cl/bcs/oracle/H2FnCpf.java, which sync_cpf.py already put in the module test source set (this script does not touch it; it only checks it is there).
A case without golden is NOT copied (the module test then fails on the missing case count / golden, never passes by omission) and is listed. Nothing outside src/test/resources/fondomutuo-cpfchain/
is written.

  py -3 -I legacy-oracle/sync_cpfchain.py
"""
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MODULO = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MODULO / "src" / "test" / "resources" / "fondomutuo-cpfchain"


def main():
    if not (MODULO / "src" / "test" / "java" / "cl" / "bcs" / "oracle" / "H2FnCpf.java").exists():
        raise SystemExit("src/test/java/cl/bcs/oracle/H2FnCpf.java is missing: run legacy-oracle/sync_cpf.py first (the chain loads schema-cpf.sql, whose aliases point to it)")
    (DEST / "cases-cpfchain").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    for rel in ("schema-cpf.sql", "seed/base-cpf.sql", "schema-cpfchain.sql", "seed/base-cpfchain.sql"):
        shutil.copyfile(BASE / rel, DEST / rel)
    for old in (DEST / "cases-cpfchain").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    for d in sorted((BASE / "cases-cpfchain").glob("FM-CHAIN-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases-cpfchain" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
    if (BASE / "determinism-cpfchain.txt").exists():
        shutil.copyfile(BASE / "determinism-cpfchain.txt", DEST / "determinism-cpfchain.txt")
    print("FM-CHAIN cases synced:", n)


if __name__ == "__main__":
    main()
