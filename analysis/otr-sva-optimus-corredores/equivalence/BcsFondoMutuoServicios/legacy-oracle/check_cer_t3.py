"""F3 W3 T3: consistency of the recorded goldens of cases-cer-t3/ with the tag each case declares (run after record_cer_t3.sh, before sync_cer_t3.py):
  * `T3-costura:completo`            the legacy ran to the end: NO exception in the golden;
  * `T3-costura:excepcion`           the legacy threw INSIDE F-C..F-H: an exception, and the last collaborator call is one of F-C..F-H (or none after the T1 prefix);
  * `T3-costura:excepcion-posterior` the legacy finished F-C..F-H and threw later (a later front): an exception, and at least one collaborator of F-C..F-H was called.
Also: every case has its golden, the provenance line, and appears stable in the determinism report. Exit 1 on any inconsistency. Reads only; never writes.

  py -3 -I legacy-oracle/check_cer_t3.py
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent
T3 = {"SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "ConsultarFlujoCustodia", "ConsultarEmpresa", "CalcularParidad", "EntregarPreciosTasas",
      "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot"}
POSTERIORES = {"CrearMovimientoComprobante", "CrearComprobante", "CrearSolicitudAporteReinversion", "NotificarEstadoOrdenFondoMutuo", "NotificarEstadoOrdenesOWP", "AgregarLog"}


def main():
    malos, n = [], 0
    resumen = {}
    for d in sorted((BASE / "cases-cer-t3").glob("FMM-CER-*")):
        n += 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        tag = [r for r in ref if r.startswith("T3-costura:")][0]
        resumen[tag] = resumen.get(tag, 0) + 1
        if "unmodified legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate" not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the oracle" % d.name)
        if not any(c["colaborador"] in ("CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo", "SaldoFlujoCaja") for c in g["colaboradores"]):
            malos.append("%s: the legacy never reached F-C / F-D (F-A early exit): the harness would never reach the seam" % d.name)
        tiene = "excepcion" in g
        ultimo = g["colaboradores"][-1]["colaborador"] if g["colaboradores"] else ""
        if tag == "T3-costura:completo" and tiene:
            malos.append("%s: tagged completo but the legacy threw %s" % (d.name, g["excepcion"][:120]))
        if tag == "T3-costura:excepcion" and not tiene:
            malos.append("%s: tagged excepcion but the legacy ended normally" % d.name)
        if tag == "T3-costura:excepcion" and tiene and g["excepcion"].startswith("SQLException"):
            malos.append("%s: tagged excepcion but the exception is the UPDATE (a later front): use excepcion-posterior" % d.name)
        if tag == "T3-costura:excepcion-posterior" and not tiene:
            malos.append("%s: tagged excepcion-posterior but the legacy ended normally" % d.name)
        if tag == "T3-costura:excepcion-posterior" and ultimo not in T3 and ultimo not in POSTERIORES:
            malos.append("%s: excepcion-posterior but the last collaborator called is %s (F-E / F-F / F-H never ran?)" % (d.name, ultimo))
    rep = BASE / "determinism-cer-t3.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera:
            malos.append("determinism report: some case is unstable: " + primera)
    print("cases checked:", n, resumen)
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
