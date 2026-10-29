"""Canary of the goldens of FMM-SFM (ConsultarSolicitudesFFMM + WS) and FMM-DET (ConsultarDetalleSolicitud + its delegate ConsultarSolicitudAporteRescateUI), 2026-10-26.

The new code did not exist when this was written, so it cannot be mutated: as FSA (README of consultas/fecha) and F2 plan section 7 ("mutation of goldens"), each mutation is a ONE-LINE
change of a COPY of the legacy source (LEGACY_OVERLAY, a temporary directory: nothing under legacy/ is touched), the oracle is re-run on it with ORACLE_OUT (the goldens are not
rewritten) and the dumps are compared with the recorded goldens. A mutation that changes no dump means the case set does not pin that behaviour (an EQUIVALENT mutant or a hole).

  py -3 -I legacy-oracle/canary_sfm_det.py [sfm|det]          (needs JAVA_HOME / bash like the record scripts; prints one line per mutation and a total)
"""
import json
import os
import pathlib
import subprocess
import sys
import tempfile

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
FMP = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo"
_GIT = "C:/Program Files/Git/usr/bin/bash.exe"
BASH = os.environ.get("BASH_EXE") or (_GIT if os.path.exists(_GIT) else "bash")   # on Windows a bare "bash" may be the WSL launcher, which does not understand C:/ paths

# (script, file, old text, new text, what a rewrite could plausibly get wrong)
MUTACIONES = [
    ("sfm", "ConsultarSolicitudesFFMM.java", "if(enProceso.compareTo(0L)>0) {", "if(enProceso.compareTo(1L)>0) {", "EN PROCESO needs two rows instead of one"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "sol.HoraSolicitud = Fecha.toStringTime(Fecha.toLocalTime(Fecha.toTime(sol.HoraSolicitud)));",
     "sol.HoraSolicitud = Fecha.toStringTime(Fecha.toTime(sol.HoraSolicitud));", "the zone offset is not applied to the hour"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "montoProceso.MontoCuotasComprometidas = montoProceso.MontoCuotasComprometidas.add(solicitud.MontoCuotasOrigen, MathContext.DECIMAL128);",
     "montoProceso.MontoCuotasComprometidas = montoProceso.MontoCuotasComprometidas.add(solicitud.MontoCuotasDestino, MathContext.DECIMAL128);", "committed units of a non-REV from the destination"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "filter(solicitud->!\"\".equals(solicitud.Estado))", "filter(solicitud->true)", "rows with an empty global state are kept"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "String filtra = campo.equals(0L) ? _c.NO : _c.SI;", "String filtra = campo.equals(1L) ? _c.NO : _c.SI;", "client 0 is a filter and 1 is not"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "if(_c.SI.equals(filtraADC)) {", "if(_c.SI.equalsIgnoreCase(filtraADC)) {", "the ADC parameter is case insensitive"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "(!aporteRescate.FolioContrato.equals(0L) && aporteRescate.FolioContrato.equals(solicitud.FolioContrato))",
     "(aporteRescate.FolioContrato.equals(solicitud.FolioContrato))", "FolioContrato 0 also associates rows"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "if(_c.TipoOperacionReinversionWS.equals(solicitud.TipoSolicitud)) {", "if(_c.TipoOperacionReinversionWS.equalsIgnoreCase(solicitud.TipoSolicitud)) {",
     "'rev' in lower case is a reinvestment"),
    ("sfm", "ConsultarSolicitudesFFMM.java", "sol.FechaIngreso = sol.FechaSolicitud.concat(\" \"+ sol.HoraSolicitud);", "sol.FechaIngreso = sol.FechaSolicitud.concat(\"T\"+ sol.HoraSolicitud);",
     "the entry date separator"),
    ("sfm", "ConsultarSolicitudesFFMMWS.java", "sol.HoraSolicitud = fechaHora.toLocalTime().toString();", "sol.HoraSolicitud = fechaHora.toLocalTime().withSecond(0).toString();",
     "the WS hour loses its seconds"),
    ("sfm", "ConsultarSolicitudesFFMMWS.java", "sol.FechaSolicitud = fechaHora.toLocalDate().toString();", "sol.FechaSolicitud = sol.FechaSolicitud;", "the WS keeps the view date instead of the local one"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "_c.EstadoIngresado.equals(EstadoSolicitud) || _c.EstadoPendiente.equals(EstadoSolicitud)", "_c.EstadoIngresado.equals(EstadoSolicitud)",
     "PEN solicitudes are not valued"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "(_c.APV_AN.equals(solicitud.IdRegimenTributarioAPV) || _c.APV_B.equals(solicitud.IdRegimenTributarioAPV))",
     "(_c.APV_AN.equals(solicitud.IdRegimenTributarioAPV))", "APV-B does not retain"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "porcentajeRetencion.divide(new BigDecimal(\"100\"), MathContext.DECIMAL128)", "porcentajeRetencion.divide(new BigDecimal(\"10\"), MathContext.DECIMAL128)",
     "the percentage divisor"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "!_c.CuentaInversion.equals(solicitud.IdMedioDePago)", "true", "the investment account payment also retains"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "Boolean esRescateNormal = _c.RescateFondoMutuo.equals(solicitud.IdTipoMovimientoFondoMutuo);",
     "Boolean esRescateNormal = _c.RescateFondoMutuo.equalsIgnoreCase(solicitud.IdTipoMovimientoFondoMutuo);", "RESNORM is case insensitive"),
    ("det", "ConsultarSolicitudAporteRescateUI.java", "solicitud.Monto = resultadoValorizacion.Monto;", "solicitud.Monto = resultadoValorizacion.MontoDestino;", "the valued amount"),
    ("det", "ConsultarDetalleSolicitud.java", "entrada.p_SolicitudesAporteRescate == null || entrada.p_SolicitudesAporteRescate == 0", "entrada.p_SolicitudesAporteRescate == null",
     "id 0 is not rejected"),
    ("det", "ConsultarDetalleSolicitud.java", "resultado.Monto = solicitud.Monto;", "resultado.Monto = solicitud.MontoMonedaSistema;", "the output amount comes from the system amount"),
    ("det", "ConsultarDetalleSolicitud.java", "resultado.NombreEstadoSpot = solicitud.NombreEstadoSpot;", "resultado.NombreEstadoSpot = solicitud.NombreEstadoConfirmacionSpot;", "Spot state copied from the wrong field"),
]


def comparable(j):
    j = dict(j)
    j.pop("_provenance", None)
    e = j.get("excepcion")
    if e and ":" in e and e.split(":")[0] != "Exception":
        j["excepcion"] = e.split(":")[0]
    return j


def correr(script, overlay, salida):
    env = dict(os.environ, LEGACY_OVERLAY=overlay.as_posix(), ORACLE_OUT=salida.as_posix(), ORACLE_REPEAT="1", ORACLE_REPORT=(salida / "report.txt").as_posix(),
               ORACLE_WORK=(salida / "work").as_posix(), PYTHONUTF8="1")
    r = subprocess.run([BASH, (HERE / ("record_%s.sh" % script)).as_posix(), "FMM-"], env=env, capture_output=True, text=True, timeout=1500)
    return r


def main():
    filtro = sys.argv[1] if len(sys.argv) > 1 else None
    total = cambian = 0
    for script, archivo, viejo, nuevo, que in MUTACIONES:
        if filtro and script != filtro:
            continue
        texto = (FMP / archivo).read_text(encoding="utf-8")
        if viejo not in texto:
            print("MUTATION NOT APPLICABLE (text not found):", archivo, viejo[:60])
            continue
        total += 1
        with tempfile.TemporaryDirectory() as t:
            t = pathlib.Path(t)
            (t / "overlay").mkdir()
            (t / "out").mkdir()
            (t / "overlay" / archivo).write_text(texto.replace(viejo, nuevo), encoding="utf-8", newline="")
            r = correr(script, t / "overlay", t / "out")
            if r.returncode not in (0, 4):
                print("RUN FAILED (%s %s): %s" % (script, archivo, r.stdout[-300:] + r.stderr[-300:]))
                continue
            cases = BASE / ("cases-sfm" if script == "sfm" else "cases-det")
            distintos = []
            for d in sorted(cases.glob("FMM-*")):
                dump = t / "out" / (d.name + ".json")
                gold = d / "expected.json"
                if not gold.exists() or not dump.exists():
                    distintos.append(d.name + " (missing)")
                    continue
                if comparable(json.loads(dump.read_text(encoding="utf-8"))) != comparable(json.loads(gold.read_text(encoding="utf-8"))):
                    distintos.append(d.name)
            if distintos:
                cambian += 1
            print("%-3s %-42s %-60s -> %3d cases change%s" % (script, archivo, que, len(distintos), "" if distintos else "   <== EQUIVALENT OR NOT PINNED"))
    print("mutations: %d, changing at least one golden: %d" % (total, cambian))


if __name__ == "__main__":
    main()
