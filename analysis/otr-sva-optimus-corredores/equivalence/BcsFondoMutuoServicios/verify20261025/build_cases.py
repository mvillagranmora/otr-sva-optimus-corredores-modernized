"""verify 2026-10-25: writes equivalence/BcsFondoMutuoServicios/cases.json (development goldens) in the format of the skill's compare.py, from the files
Volcar.java wrote under verify20261025/dev/{nor,rre}/{legacy,new}/. Approvals are NOT written here: cases the module's own aprobadas.json or a case's
approvedDifference treats as approved get only a "note" naming that record, for a person to transcribe if they accept it.

  python -I build_cases.py
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
RES = WS / "modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/src/test/resources"
APROBADAS = json.loads((RES / "fondomutuo-nor/aprobadas.json").read_text(encoding="utf-8"))

# Approved by a person: recorded at the signer's request on 2026-10-25 (verify session), kept here so a re-run of this script keeps them.
FIRMA = "Approved with conditions by Mauricio Villagran Mora (Arquitecto), 2026-10-25, recorded at the signer's request. "
CLASE = ("only the exception class differs (legacy Exception, new RuntimeException, thrown by the scripted collaborator double of each harness); "
         "message and servlet decision ROLLBACK_HTTP500 identical; the caller sees only INTERNAL ERROR #code")
APROBADOS = {
    "FMM-NOR-07_aporte-usd-genera-spot": "DEV-F4: gate G2 answers 503 before any write (ProcesarIngresoOperacionSpot of Mesa is ported in F2)",
    "FMM-NOR-37_bloqueo-lote-parcial": "DEV-6-H1: the new code drops the session-local lock table the legacy leaks on a failed lock",
    "FMM-NOR-T33_tes-dev-f5-agregarlog-de-tesoreria-falla": "DEV-F5: a failing Tesoreria-side AgregarLog answers 503 + rollback instead of the legacy 200 with an error",
    "FMM-NOR-30_fallo-agregar-log": CLASE,
    "FMM-NOR-94_tesoreria-excepcion-se-propaga": CLASE,
    "FMM-NOR-T06_tes-rollback-conjunto-falla-el-log": CLASE,
    "FMM-NOR-T30_tes-rollback-conjunto-dos-aportes-falla-el-log": CLASE,
    "FMM-RRE-50_agregarlog-falla-tras-insertar": CLASE,
    "FMM-RRE-19_falta-parametro-spot": ("only the exception class differs (legacy Exception, new ParametroNoEncontradoException, product code); message and "
                                        "servlet decision ROLLBACK_HTTP500 identical; the caller sees only INTERNAL ERROR #code"),
}


def nota(familia, cid):
    if familia != "nor":
        return None
    if cid in APROBADAS and isinstance(APROBADAS[cid], dict) and "compuerta" in APROBADAS[cid]:
        a = APROBADAS[cid]
        return "module aprobadas.json lists it as %s (gate %s): %s" % (a["dev"], a["compuerta"], a["motivo"])
    if cid in APROBADAS.get("tablaLocalSoltadaEnFallo", {}):
        a = APROBADAS["tablaLocalSoltadaEnFallo"][cid]
        return "module aprobadas.json tablaLocalSoltadaEnFallo lists it as %s: %s" % (a["dev"], a["motivo"][:300])
    caso = json.loads((RES / "fondomutuo-nor/cases" / cid / "case.json").read_text(encoding="utf-8"))
    if "approvedDifference" in caso:
        a = caso["approvedDifference"]
        return "case.json carries approvedDifference %s (decision %s), provenance: %s" % (a.get("dev"), a.get("decisionServlet"), a.get("procedencia"))
    return None


def main():
    cases = []
    for familia, titulo in (("nor", "CrearSolicitudAporteRescateNormal"), ("rre", "CrearSolicitudRescateReinversion")):
        d = HERE / "dev" / familia
        for f in sorted((d / "legacy").glob("*.txt")):
            cid = f.stem
            c = {"id": cid, "input": cid, "title": "%s golden %s" % (titulo, cid),
                 "legacy": "verify20261025/dev/%s/legacy/%s.txt" % (familia, cid), "new": "verify20261025/dev/%s/new/%s.txt" % (familia, cid), "mask": []}
            n = nota(familia, cid)
            if n:
                c["note"] = n
            if cid in APROBADOS:
                c["approvedDifference"] = FIRMA + APROBADOS[cid]
            cases.append(c)
    doc = {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
           "legacy": {"label": "goldens (expected.json) RECORDED by executing the unmodified legacy cl.bcs.fondomutuo classes on in-memory H2 MODE=LEGACY with "
                               "registered doubles (legacy-oracle/record.sh, record_w1b.sh, record_rre.sh), JDK 25, UTC; canonical text by verify20261025/Volcar.java",
                      "command": "legacy-oracle/record*.sh (development); canonicalised by: java Volcar nor|rre <out>"},
           "new": {"label": "modernized BcsFondoMutuoServicios src/main through the module's test harness (CasoNormal / CasoRre, profile ABIERTAS) on the same H2 "
                            "schema, seeds and scripted collaborators, JDK 25, UTC; re-run by the verifier 2026-10-25",
                   "command": "java -cp <volcar>;<surefire test class path> Volcar nor|rre analysis/.../verify20261025/dev/<nor|rre>"},
           "note": "One case = the canonical text of one golden: salidaCruda, tablas, colaboradores (name/arguments/answer), secuenciasSolicitadas, sesion, "
                   "decisionServlet, exception class (message only for a plain Exception). FMM-MAN (Manejador) and the 3A/3B pure-closure goldens are not "
                   "included: their module tests compare a subset, not a full dump.",
           "cases": cases}
    (BASE / "cases.json").write_text(json.dumps(doc, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("cases:", len(cases))


if __name__ == "__main__":
    main()
