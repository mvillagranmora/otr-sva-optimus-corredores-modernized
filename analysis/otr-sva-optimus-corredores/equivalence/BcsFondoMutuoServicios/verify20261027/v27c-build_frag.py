"""verify 2026-10-27 (v27c): writes v27c-frag-dev.json and v27c-frag-fresh.json (lists of cases, paths RELATIVE TO EQ) from the texts Volcar27c wrote, plus the temporary
check files v27c-check-dev.json / v27c-check-fresh.json (same cases, paths relative to verify20261027/ so that compare.py, which refuses paths outside its cases folder, can run).
No approval is written: cases with a known difference only get a "note". Run: python -I v27c-build_frag.py
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
WS = EQ.parents[3]
RES = WS / "modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/src/test/resources"
TIT = {"det": "ConsultarDetalleSolicitud (+ delegada ConsultarSolicitudAporteRescateUI)", "cpf": "ConsultarCustodiaPortafolioFondoMutuo"}

NOTA_KIT = ("DESVIO-KIT-1 NOT relaxed here: the legacy raises NullPointerException (ConfiguracionSistema of a null / unknown company) and the new code the kit's "
            "ParametroNoEncontradoException (the module harness writes it as a plain 'Exception' with the message 'Parametro <name> en modulo <module> no encontrado'); the exception line is the "
            "only difference, output, tables, collaborator calls, session and servlet decision (ROLLBACK_HTTP500) are identical. %s")
NOTA_DET = "The module's CasoDet.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION accepts either class for this slug; the verifier does not (DET: listed in the module README as 'to be approved by the owner')."
NOTA_CPF = "The module's CasoCpf comment says the owner DECIDED to keep the kit behaviour; no signed record was given to this verification, so it is reported as a difference, not approved."
NOTA_IDS = "CPF only: the ids of the solicitudes (D-F3A-04) are canonicalised on BOTH sides with the module test's own CpfEquivalenciaTest.conIdsCanonicos (renaming by order of first appearance); the P_OUT_TBL order is compared as written."


def dev(front):
    cases = []
    for f in sorted((HERE / "dev" / front / "legacy").glob("*.txt")):
        cid = f.stem
        c = {"id": cid, "input": cid, "title": "%s development golden %s" % (TIT[front], cid),
             "legacy": "verify20261027/dev/%s/legacy/%s.txt" % (front, cid), "new": "verify20261027/dev/%s/new/%s.txt" % (front, cid), "mask": []}
        slug = cid.split("_", 1)[1]
        notas = []
        if slug in ("empresa-nula-con-id", "empresa-sin-configuracion", "empresa-ausente", "empresa-nula") and (HERE / "dev" / front / "legacy" / (cid + ".exc")).exists():
            if (HERE / "dev" / front / "legacy" / (cid + ".exc")).read_text(encoding="utf-8").startswith("NullPointerException"):
                notas.append(NOTA_KIT % (NOTA_DET if front == "det" else NOTA_CPF))
        if front == "cpf":
            notas.append(NOTA_IDS)
        if notas:
            c["note"] = " ".join(notas)[:1500]
        cases.append(c)
    return cases


def fresh(front):
    cases = []
    for d in sorted((HERE / "fresh" / front / "cases").iterdir()):
        if not (HERE / "fresh" / front / "out" / "legacy" / (d.name + ".txt")).exists():
            continue
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        c = {"id": d.name, "input": d.name, "title": "%s fresh input: %s" % (TIT[front], ref),
             "legacy": "verify20261027/fresh/%s/out/legacy/%s.txt" % (front, d.name), "new": "verify20261027/fresh/%s/out/new/%s.txt" % (front, d.name), "mask": []}
        if front == "cpf":
            c["note"] = NOTA_IDS
        cases.append(c)
    return cases


def doc(cases, kind):
    return {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
            "legacy": {"label": ("goldens of the module's test resources recorded from the unmodified legacy (legacy-oracle/record_det.sh, record_cpf.sh)" if kind == "dev" else
                                 "unmodified legacy re-run by the verifier with record_det.sh / record_cpf.sh (ORACLE_CASES / ORACLE_OUT / ORACLE_REPORT under verify20261027/fresh), JDK 25, UTC, 20 repetitions")
                      + "; canonical text by verify20261027/Volcar27c.java",
                      "command": "record_<det|cpf>.sh VF-<DET|CPF> ; java Volcar27c %s <det|cpf> ..." % ("dev" if kind == "dev" else "fresh")},
            "new": {"label": "modernized BcsFondoMutuoServicios target/classes through the module's own harness (CasoDet / CasoCpf, profile ABIERTA), same H2 schema, seeds and scripted collaborators, JDK 25, UTC",
                    "command": "java -cp tmp/vc;<surefire.test.class.path> Volcar27c %s <det|cpf> ..." % ("dev" if kind == "dev" else "fresh")},
            "cases": cases}


def main():
    for kind, fn in (("dev", dev), ("fresh", fresh)):
        cases = []
        for front in ("det", "cpf"):
            cases += fn(front)
        (HERE / ("v27c-frag-%s.json" % kind)).write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        chk = json.loads(json.dumps(doc(cases, kind)))
        for c in chk["cases"]:
            c["legacy"] = c["legacy"].replace("verify20261027/", "", 1)
            c["new"] = c["new"].replace("verify20261027/", "", 1)
        (HERE / ("v27c-check-%s.json" % kind)).write_text(json.dumps(chk, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        print(kind, len(cases))


if __name__ == "__main__":
    main()
