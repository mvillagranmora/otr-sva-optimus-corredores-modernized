"""F3 W3 T4-A: copies, byte for byte, the FMM-CSR cases (case.json, seed.sql, expected.json recorded by record_csr.sh), schema-csr.sql, seed/base-csr.sql and determinism-csr.txt into the module
test resources src/test/resources/fondomutuo-csr/. A case without golden is NOT copied (the module test would fail). A golden over 1 MB (the two lots of 2.000 aportes, 13-17 MB of pretty
JSON) is stored GZIPPED as expected.json.gz, both here (the plain file is removed after zipping: the recorder rewrites it) and in the test resources; CasoCsr reads either form.

  py -3 -I legacy-oracle/sync_csr.py
"""
import gzip
import pathlib
import shutil

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
DEST = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-csr"
LIMITE = 1024 * 1024


def comprimir(origen: pathlib.Path, destino: pathlib.Path):
    with open(origen, "rb") as f, open(destino, "wb") as salida, gzip.GzipFile(filename="", mode="wb", fileobj=salida, mtime=0, compresslevel=9) as g:   # mtime 0: reproducible bytes
        shutil.copyfileobj(f, g)


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "schema-csr.sql", DEST / "schema-csr.sql")
    shutil.copyfile(BASE / "seed" / "base-csr.sql", DEST / "seed" / "base-csr.sql")
    shutil.copyfile(BASE / "determinism-csr.txt", DEST / "determinism-csr.txt")
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = grandes = 0
    for d in sorted((BASE / "cases-csr").glob("FMM-CSR-*")):
        plano = d / "expected.json"
        gz = d / "expected.json.gz"
        if plano.exists() and plano.stat().st_size > LIMITE:
            comprimir(plano, gz)
            plano.unlink()
        if not plano.exists() and not gz.exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json", "expected.json.gz"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        if gz.exists():
            grandes += 1
        n += 1
    print("FMM-CSR cases synced: %d (%d with a gzipped golden)" % (n, grandes))


if __name__ == "__main__":
    main()
