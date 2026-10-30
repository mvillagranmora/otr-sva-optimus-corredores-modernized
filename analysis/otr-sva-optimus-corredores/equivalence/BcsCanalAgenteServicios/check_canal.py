"""BcsCanalAgenteServicios oracle: consistency of the recorded goldens (run after legacy-oracle/record.sh, before sync_canal.py). Reads only; exit 1 on any inconsistency.
  * every case has its golden, with the provenance of the oracle, and the determinism report says 0 unstable cases;
  * every id a case cites (B-CA / D-CAN) is in reglas_can.py and every id of the catalog is cited (the ones in SIN_CASO are documentation or text pins);
  * every collaborator the golden recorded was scripted by the case (no "SIN RESPUESTA CONFIGURADA"), no exception is an IllegalStateException of the oracle itself;
  * the cases that tag a legacy defect really show it in the golden (spot checks, see ESPERADOS).

  py -3 -I check_canal.py
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import reglas_can  # noqa: E402

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"


def _exc(g):
    return g.get("excepcion", "").split(":")[0]


# defect id -> (name fragment of a case that must show it, what the golden must say)
ESPERADOS = {
    "D-CAN-01": ("portafolio-inexistente-lanza-nullpointer", lambda g: _exc(g) == "NullPointerException" and len(g["sentencias"]) == 1 and not g["colaboradores"]),
    "D-CAN-02": ("un-movimiento-con-comprobante-completo", lambda g: "P_OUT_ADVERTENCIASCONTADOR" in g["salida"] and g["salida"]["P_OUT_ADVERTENCIASCONTADOR"] is None),
    "D-CAN-03": ("un-movimiento-con-comprobante-completo", lambda g: g["salida"]["P_SALDOEFECTIVOINICIAL"] is None and g["salida"]["P_SALDOEFECTIVOFINAL"] is None),
    "D-CAN-04": ("errores-de-la-cartola-con-lista-vacia", lambda g: g["salida"]["P_OUT_ERRORESCONTADOR"] == 0 and g["salida"]["P_OUT_ERRORESDETALLE"] == []),
    "D-CAN-05": ("falla-el-select-de-comprobantes", lambda g: _exc(g) == "SQLException" and len(list(g["tablas"].values())[0]) == 2),
    "D-CAN-06": ("id-de-movimiento-repetido", lambda g: [m["FolioComprobante"] for m in g["salida"]["P_OUT_RESULTADO"]] == ["7002", "7002"]),
    "D-CAN-08": ("cartola-con-lista-nula", lambda g: _exc(g) == "SQLException" and len(g["sentencias"]) == 1),
    "D-CAN-12": ("filas-ajenas-en-la-tabla-de-trabajo", lambda g: list(g["tablas"].values())[0] == [] and len(list(g["tablasIniciales"].values())[0]) == 1),
}


def main():
    malos, n, citados = [], 0, set()
    familias = {}
    for d in sorted(CASES.glob("CAN-*")):
        n += 1
        familias[d.name.split("-")[1]] = familias.get(d.name.split("-")[1], 0) + 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        c = json.loads((d / "case.json").read_text(encoding="utf-8"))
        if "Recorded by legacy-oracle/runner/OracleRunnerCanalAgente.java executing the " not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the oracle" % d.name)
        for r in c["ref"]:
            citados.add(r)
            if r not in reglas_can.REGLAS and r not in reglas_can.DEFECTOS:
                malos.append("%s: cites %s which is not in reglas_can.py" % (d.name, r))
        for col in g["colaboradores"]:
            if col.get("respuesta") == "SIN RESPUESTA CONFIGURADA":
                malos.append("%s: collaborator %s.%s had no scripted answer" % (d.name, col["colaborador"], col["metodo"]))
        ex = g.get("excepcion", "")
        if ex.startswith("IllegalStateException") or ex.startswith("IllegalArgumentException"):
            malos.append("%s: the oracle itself failed: %s" % (d.name, ex.split("\n")[0][:160]))
        if "salida" not in g and "excepcion" not in g:
            malos.append("%s: neither output nor exception" % d.name)
        for ref, (fragmento, prueba) in ESPERADOS.items():
            if fragmento in d.name:
                try:
                    ok = prueba(g)
                except Exception as err:  # noqa: BLE001
                    ok = False
                    malos.append("%s: expectation of %s raised %r" % (d.name, ref, err))
                if not ok:
                    malos.append("%s: the golden does not show the defect %s" % (d.name, ref))
                if ref not in c["ref"]:
                    malos.append("%s: shows %s but does not cite it" % (d.name, ref))
    for ref, (fragmento, _) in ESPERADOS.items():
        if not any(fragmento in d.name for d in CASES.glob("CAN-*")):
            malos.append("no case for the expectation of %s (%s)" % (ref, fragmento))
    for r in list(reglas_can.REGLAS) + list(reglas_can.DEFECTOS):
        if r not in citados and r not in reglas_can.SIN_CASO:
            malos.append("%s is in reglas_can.py but no case cites it" % r)
    rep = BASE / "determinism.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera or "unstable: 0" not in primera or "repeats per case: 20" not in primera:
            malos.append("determinism report: some case is unstable or not 20 runs: " + primera)
    else:
        malos.append("no determinism.txt (run record.sh with ORACLE_REPEAT=20)")
    print("cases checked:", n, familias)
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
