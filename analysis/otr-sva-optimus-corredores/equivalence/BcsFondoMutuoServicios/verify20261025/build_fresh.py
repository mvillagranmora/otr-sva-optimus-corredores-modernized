"""verify 2026-10-25: writes equivalence/BcsFondoMutuoServicios/fresh-cases.json from verify20261025/fresh/out/{legacy,new}/VF-RRE-nn.txt
(legacy: record_rre.sh with ORACLE_CASES/ORACLE_OUT on verify20261025/fresh/cases; new: java Volcar rre-dir). No mask, no approval.

  python -I build_fresh.py
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent


def main():
    cases = []
    for d in sorted((HERE / "fresh" / "cases").iterdir()):
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        cid = d.name
        cases.append({"id": cid + "-out", "input": cid, "title": "CrearSolicitudRescateReinversion, " + ref,
                      "legacy": "verify20261025/fresh/out/legacy/%s.txt" % cid, "new": "verify20261025/fresh/out/new/%s.txt" % cid, "mask": []})
    doc = {"system": "otr-sva-optimus-corredores / BcsFondoMutuoServicios RescateReinversion (verify 2026-10-25)",
           "legacy": {"label": "unmodified legacy CrearSolicitudRescateReinversion compiled from legacy/ into scratch, OracleRunner on H2 MODE=LEGACY, JDK 25, UTC",
                      "command": "ORACLE_CASES=verify20261025/fresh/cases ORACLE_OUT=verify20261025/fresh/legacy-dumps legacy-oracle/record_rre.sh VF-RRE"},
           "new": {"label": "modernized src/main through CasoRre (profile ABIERTAS) on the same schema, seeds and doubles",
                   "command": "java Volcar rre-dir verify20261025/fresh/out verify20261025/fresh/cases verify20261025/fresh/legacy-dumps"},
           "cases": cases}
    (BASE / "fresh-cases.json").write_text(json.dumps(doc, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("fresh cases:", len(cases))


if __name__ == "__main__":
    main()
