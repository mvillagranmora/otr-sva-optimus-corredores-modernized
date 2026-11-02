"""F3 W3 T6-A (CertificarPorFiltrosSolicitudAporteRescate): copies, byte for byte, the FMM-CERPF cases (case.json, seed.sql, expected.json recorded by record_cerpf.sh) into the module test resources
src/test/resources/fondomutuo-cerpf/cases/, plus schema-cerpf.sql (the H2 stand-ins of what the select of the filters reads) and the determinism report. The schemas and bases shared with Certificar
(schema-cer.sql, seed/base-cer*.sql) are the ones of T1..T4-B under fondomutuo-cer/ and are NOT touched here. A case without golden is NOT copied (the module test would fail).
Nothing under legacy/ or src/main is touched.

  python -X utf8 -I legacy-oracle/sync_cerpf.py                       writes into modernized/.../BcsFondoMutuoServicios
  python -X utf8 -I legacy-oracle/sync_cerpf.py <module directory>    writes into ANOTHER copy of the module (validation outside the repo)
"""
import json
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MOD = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MOD / "src" / "test" / "resources" / "fondomutuo-cerpf"


def main():
    (DEST / "cases").mkdir(parents=True, exist_ok=True)
    for f in ("determinism-cerpf.txt", "schema-cerpf.sql"):
        shutil.copyfile(BASE / f, DEST / f)
    for old in (DEST / "cases").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    por_modo = {}
    for d in sorted((BASE / "cases-cerpf").glob("FMM-CERPF-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        modo = [r for r in ref if r.startswith("PF-modo:")][0]
        por_modo[modo] = por_modo.get(modo, 0) + 1
    print("FMM-CERPF cases synced:", n, por_modo)


if __name__ == "__main__":
    main()
