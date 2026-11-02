"""F3 W3 T6-B: GOLDEN-MUTATION check of the goldens of LiquidacionSolicitudAporteRescate (plan F3 section 4: break the code in one small way that matters, confirm that at least one test goes red,
restore).

There is no production code of T6-B yet (red before code), so the mutants are of the ORACLE: copies of the legacy source `LiquidacionSolicitudAporteRescate.java` with ONE line changed, or copies of its SQL
files (`..._SelectSolicitudes.sql`, `..._SelectSolicitudesFiltros.sql`, `..._UpdateSolicitudes.sql`, `ComunesSolicitudes_SelectModalidadPago.sql`, `ConsultarRetencion.sql`) with ONE clause changed (a LEGACY_OVERLAY /
LEGACY_OVERLAY_RES directory under the temp dir; `legacy/` is only read and NEVER written). Each mutant is compiled (Java) and run by `legacy-oracle/record_liq.sh` over the whole matrix (ORACLE_OUT = a temp
dir, ONE run per case: the goldens are never overwritten) and every case is compared with its golden (the key `_provenance` is ignored). A mutant is KILLED when at least one case differs: the matrix pins that
line. Control: the unmodified legacy re-recorded the same way must differ in NO case (the harness and the determinism are sound).

  python -X utf8 -I canary_liq.py [text of the mutation name]
Writes golden-mutation-liq/GOLDEN_MUTATION_T6B.json (mutant, killed, number of cases that differ, the first cases) and prints the summary. Needs JDK 25 (JAVA_HOME) and the local Maven repository jars the record
scripts use. The SQL mutants reuse the classes compiled by the control run; each Java mutant is compiled. Exit 1 if the control differs, a text to mutate is not in the legacy, or a mutant survives.
"""
import json
import os
import pathlib
import shutil
import subprocess
import sys
import tempfile

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
LEG = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL"
FUENTE = LEG / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo"
JAVA = FUENTE / "LiquidacionSolicitudAporteRescate.java"
SQLS = {
    "S": (FUENTE / "sql" / "LiquidacionSolicitudAporteRescate_SelectSolicitudes.sql", "cl/bcs/fondomutuo/sql"),
    "F": (FUENTE / "sql" / "LiquidacionSolicitudAporteRescate_SelectSolicitudesFiltros.sql", "cl/bcs/fondomutuo/sql"),
    "U": (FUENTE / "sql" / "LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql", "cl/bcs/fondomutuo/sql"),
    "M": (FUENTE / "sql" / "ComunesSolicitudes_SelectModalidadPago.sql", "cl/bcs/fondomutuo/sql"),
    "R": (LEG / "APV" / "JAVA" / "BcsAPVServicios" / "src" / "cl" / "bcs" / "APV" / "sql" / "ConsultarRetencion.sql", "cl/bcs/APV/sql"),
}
SALIDA = BASE / "golden-mutation-liq"
BASH = os.environ.get("BASH_EXE") or (next((c for c in ("C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe") if os.path.exists(c)), None) or "bash")   # NOT the WSL bash of System32
T = "\t"

# (name, legacy text, replacement, occurrence 1-based; 0 = every occurrence). Java mutants of the class.
MUT_JAVA = [
    ("J01 the select does not get the company of the request", 'params.put("p_IdEmpresa", in.p_IdEmpresa);', 'params.put("p_IdEmpresa", 1L);', 1),
    ("J02 APV.Habilitado is read with the company of the request", 'getParametroConfiguracionString(conn,IdEmpresa, "PLATAFORMA","APV.Habilitado")', 'getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA","APV.Habilitado")', 1),
    ("J03 the LAST filter selects, not the first", "in.p_Filtros.stream().findFirst().get()", "in.p_Filtros.get(in.p_Filtros.size() - 1)", 1),
    ("J04 an empty FechaCertificacion is not 0001-01-01", 'aux.FechaCertificacion.equals("") ? "0001-01-01" : aux.FechaCertificacion', "aux.FechaCertificacion", 1),
    ("J05 an empty FechaLiquidacion is not 0001-01-01", 'aux.FechaLiquidacion.equals("") ? "0001-01-01" : aux.FechaLiquidacion', "aux.FechaLiquidacion", 1),
    ("J06 the omnibus filter removes rescates, not aportes", 'p.TipoSolicitud.equals("APO") && p.IdOmnibus == 0).count()', 'p.TipoSolicitud.equals("RES") && p.IdOmnibus == 0).count()', 1),
    ("J07 the omnibus flag is inverted", "if(_c.SI.equals(liquidarOmnibus)) {", "if(!_c.SI.equals(liquidarOmnibus)) {", 1),
    ("J08 the medium of the group is swapped (cargo / abono)", "p.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? p.IdMedioDePagoCargoAGF : p.IdMedioDePagoAbonoAGF",
     "p.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? p.IdMedioDePagoAbonoAGF : p.IdMedioDePagoCargoAGF", 1),
    ("J09 the currency of the group is the client's", "grupo.IdMoneda = p.IdMonedaFondo;", "grupo.IdMoneda = p.IdMoneda;", 1),
    ("J10 the date of certification of the group is the liquidation one", "grupo.FechaCertificacion = p.FechaCertificacion;", "grupo.FechaCertificacion = p.FechaLiquidacion;", 1),
    ("J11 the APV sequences do not count the solicitudes", "secuencias = aportesRescatesGrouped.size()+solicitudesLiquidar.size();", "secuencias = aportesRescatesGrouped.size();", 1),
    ("J12 the sequence of the carta is the one of the comprobante", '"BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate"', '"BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion"', 1),
    ("J13 the group sums MontoPago, not Monto", "entry.getValue().stream().map(p -> p.Monto)", "entry.getValue().stream().map(p -> p.MontoPago)", 1),
    ("J14 the type of movement of the AGF line is swapped", 'ingresoEgreso.IdTipoMovimiento = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVEGR" : "MOVING";',
     'ingresoEgreso.IdTipoMovimiento = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVING" : "MOVEGR";', 1),
    ("J15 the type of person of the AGF line is swapped", 'ingresoEgreso.IdTipoPersona = _c.SI.equals(liquidacionCtaInv) ? "CLI" : "CNT";', 'ingresoEgreso.IdTipoPersona = _c.SI.equals(liquidacionCtaInv) ? "CNT" : "CLI";', 1),
    ("J16 the portfolio of the AGF line is always the AGF's", "ingresoEgreso.IdPortafolio = _c.SI.equals(liquidacionCtaInv) ? entry.getKey().IdPortafolioAGF : 0L;", "ingresoEgreso.IdPortafolio = entry.getKey().IdPortafolioAGF;", 1),
    ("J17 the concept of the AGF line is swapped", 'ingresoEgreso.IdConcepto = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "CAAEFM" : "ABREFM";',
     'ingresoEgreso.IdConcepto = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "ABREFM" : "CAAEFM";', 1),
    ("J18 IdComprobante takes the comprobante sequence", "ingresoEgreso.IdComprobante = secuencia_carta.get(numReferencia);", "ingresoEgreso.IdComprobante = secuencia_comprobante.get(numReferencia);", 1),
    ("J19 IdOperacion takes the carta sequence", "ingresoEgreso.IdOperacion = secuencia_comprobante.get(numReferencia);", "ingresoEgreso.IdOperacion = secuencia_carta.get(numReferencia);", 1),
    ("J20 MueveCuentaInversion of the AGF line is swapped", 'ingresoEgreso.MueveCuentaInversion = _c.SI.equals(liquidacionCtaInv) ? "N" : "S";', 'ingresoEgreso.MueveCuentaInversion = _c.SI.equals(liquidacionCtaInv) ? "S" : "N";', 1),
    ("J21 a medium with no modality is MODDEP, not MODCAJ (AGF)", ":_c.MODCAJ;", ':"MODDEP";', 1),
    ("J22 the condition of liquidation of the AGF line is T1", 'ingresoEgreso.IdCondicionLiquidacion = "T0";', 'ingresoEgreso.IdCondicionLiquidacion = "T1";', 1),
    ("J23 the client's amount ignores the commission", "ingresoEgresoCliente.Monto = solicitud.Monto.subtract(solicitud.MontoComision);", "ingresoEgresoCliente.Monto = solicitud.Monto;", 1),
    ("J24 the withholding is taken on Monto, not MontoPago", "solicitud.MontoPago.multiply(retRow.PorcentajeRetencion)", "solicitud.Monto.multiply(retRow.PorcentajeRetencion)", 1),
    ("J25 a row with percentage zero applies", "retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO)!=0", "retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO)==0", 1),
    ("J26 the withholding looks for APO rows", 'retRow.CodigoRetiro.equals("RES")', 'retRow.CodigoRetiro.equals("APO")', 1),
    ("J27 the withholding is for APV-A", 'solicitud.CodigoPortafolio.equals("APV-B")', 'solicitud.CodigoPortafolio.equals("APV-A")', 1),
    ("J28 RESTRASP has no withholding", 'solicitud.IdTipoMovimientoFondoMutuo.equals("RESTRASP")', "false", 1),
    ("J29 the withholding also applies to CIN", 'solicitud.IdMedioDePago.equals("CIN") == false', 'solicitud.IdMedioDePago.equals("CIN") == true', 1),
    ("J30 a row of regimen null does not apply", "retRow.IdRegimenTributario == null ||", "retRow.IdRegimenTributario != null ||", 1),
    ("J31 the routing of the client's line is inverted", 'if(!ingresoEgresoCliente.IdTipoMovimiento.equalsIgnoreCase("MOVING"))', 'if(ingresoEgresoCliente.IdTipoMovimiento.equalsIgnoreCase("MOVING"))', 1),
    ("J32 a CIN client line goes to Tesoreria", 'formaPagoCliente.IdMedioDePago.equals("CIN") == true', 'formaPagoCliente.IdMedioDePago.equals("CIN") == false', 1),
    ("J33 carta_solicitud stores the list of the solicitud, not the complete one", "carta_solicitud.put((long) numReferencia, solicitudesLiquidar);", "carta_solicitud.put((long) numReferencia, java.util.Arrays.asList(solicitud));", 0),
    ("J34 Tesoreria is called as automatic income", 'entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "N";', 'entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";', 1),
    ("J35 the simulation is not copied to Tesoreria", "entradaCrearIngresoEgreso.p_SimularOperacion = in.p_SimularOperacion;", 'entradaCrearIngresoEgreso.p_SimularOperacion = "N";', 1),
    ("J36 a Tesoreria counter of 1 does not stop", "if(salidaIngresoEgreso.P_OUT_ERRORESCONTADOR > 0) {", "if(salidaIngresoEgreso.P_OUT_ERRORESCONTADOR > 1) {", 1),
    ("J37 the Tesoreria error keeps the earlier messages", "out.P_OUT_MENSAJES = salidaIngresoEgreso.P_OUT_MENSAJES;", "out.P_OUT_MENSAJES.addAll(salidaIngresoEgreso.P_OUT_MENSAJES);", 1),
    ("J38 the result says LIQ", "res.Estado = sol.EstadoLiquidacion;", 'res.Estado = "LIQ";', 1),
    ("J39 the result code is not the one of the solicitud", "res.Codigo = sol.Codigo;", 'res.Codigo = "UI_X";', 1),
    ("J40 the folio of every reference is the first one", "sol.FolioCartaLiquidacion = secuencia_carta.get(salida.NumeroReferencia.intValue());", "sol.FolioCartaLiquidacion = secuencia_carta.get(0);", 1),
    ("J41 an aporte of the cuenta de inversion is MOVABN", 'obj.IdTipoMovimiento = ie.IdTipoMovimiento.equals(_c.TipoSolicitudAporte) ? "MOVABN" : "MOVCAR";', 'obj.IdTipoMovimiento = ie.IdTipoMovimiento.equals("MOVEGR") ? "MOVABN" : "MOVCAR";', 1),
    ("J42 the operation of the cuenta de inversion is APORFM", 'ie.IdTipoOperacion.equals("APORFM") ? "ABPEFM" :', 'ie.IdTipoOperacion.equals("APORFM") ? "APORFM" :', 1),
    ("J43 the cuenta de inversion gets the simulation flag", 'entradaCargoAbono.p_simularOperacion  = "N";', "entradaCargoAbono.p_simularOperacion  = in.p_SimularOperacion;", 1),
    ("J44 the cuenta de inversion gets the module of the request", 'entradaCargoAbono.p_IdModulo = "FMM";', "entradaCargoAbono.p_IdModulo = in.p_IdModulo;", 1),
    ("J45 the cuenta de inversion gets no date of the system", "entradaCargoAbono.p_FechaSistema = fechaSistema;", 'entradaCargoAbono.p_FechaSistema = "";', 1),
    ("J46 the numbering of the cargos jumps by two", "i++;", "i += 2;", 1),
    ("J47 a CIN error needs counter AND message", "salidaCargoAbono.P_OUT_ERRORESCONTADOR > 0 || errores > 0", "salidaCargoAbono.P_OUT_ERRORESCONTADOR > 0 && errores > 0", 1),
    ("J48 the CIN error type is case sensitive", 'p.tipo.equalsIgnoreCase("ERROR")', 'p.tipo.equals("ERROR")', 1),
    ("J49 the CIN folio is always the first", "sol.FolioCartaLiquidacion = secuencia_carta.get(K.intValue());", "sol.FolioCartaLiquidacion = secuencia_carta.get(0);", 1),
    ("J50 the log is not called", "agregarLogService.logica(conn, entradaAgregarLog);", "// agregarLogService.logica(conn, entradaAgregarLog);", 1),
    ("J51 the log job is 2", "entradaAgregarLog.p_IdTrabajo = 1L;", "entradaAgregarLog.p_IdTrabajo = 2L;", 1),
    ("J52 the list of logs IS assigned", "AgregarLog agregarLogService = new AgregarLog();", "entradaAgregarLog.p_Log.addAll(logList);\n\t\t\tAgregarLog agregarLogService = new AgregarLog();", 1),
    ("J53 the omnibus removal does not remove", "solicitudesLiquidar.removeAll(sinOmnibus);", "// solicitudesLiquidar.removeAll(sinOmnibus);", 1),
    ("J54 the empty-filters check fires with one filter", "if(in.p_Filtros.size() == 0) {", "if(in.p_Filtros.size() == 1) {", 1),
    ("J55 movements are used only from six ids", "if(movimientos.size() > 0 && idMovimientos.size() > 0) {", "if(movimientos.size() > 5 && idMovimientos.size() > 0) {", 1),
    ("J56 the code of the empty-filters message changes", '"NO_SE_INGRESARON_FILTROS"', '"NO_SE_INGRESARON_FILTROZ"', 1),
    ("J57 the client's type of movement is swapped", 'ingresoEgresoCliente.IdTipoMovimiento = solicitud.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVING" : "MOVEGR";',
     'ingresoEgresoCliente.IdTipoMovimiento = solicitud.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVEGR" : "MOVING";', 1),
    ("J58 a client medium with no modality is MODDEP", ":_c.MODCAJ;", ':"MODDEP";', 2),
    ("J59 the second sequence is requested for one value less", 'new Long(secuencias));\n\t\tList<Long> secuencia_comprobante = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion", new Long(secuencias));',
     'new Long(secuencias));\n\t\tList<Long> secuencia_comprobante = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion", new Long(secuencias + 1));', 1),
    ("J60 the first pass reports nothing for the solicitud", "out.P_OUT_RESULTADO.add(res);", "if (false) out.P_OUT_RESULTADO.add(res);", 1),
]
# SQL mutants: (name, key of SQLS, legacy text, replacement, occurrence)
MUT_SQL = [
    ("S01 the movements select does not filter by company", "S", 'WHERE "Solicitudes"."IdEmpresa" = :p_IdEmpresa\nAND ', "WHERE ", 1),
    ("S02 the movements select takes liquidated rows too", "S", "\"Solicitudes\".\"EstadoLiquidacion\" = 'ING'", "\"Solicitudes\".\"EstadoLiquidacion\" IN ('ING','LIQ')", 1),
    ("S03 the movement type RESCNN is not selected", "S", ", 'RESCNN'", "", 1),
    ("S04 the movement type APOTRASP is not selected", "S", "'APOTRASP', ", "", 1),
    ("S05 the filters select does not filter by company", "F", '\nAND "Solicitudes"."IdEmpresa" = :p_IdEmpresa;', ";", 1),
    ("S06 the filters select ignores the date of liquidation", "F", 'AND "Solicitudes"."FechaLiquidacion" = (CASE WHEN :FechaLiquidacion <> \'\' THEN :FechaLiquidacion ELSE "Solicitudes"."FechaLiquidacion" END)', "AND 1 = 1", 1),
    ("S07 the filters select ignores the date of certification", "F", 'AND "Solicitudes"."FechaCertificacion" = (CASE WHEN :FechaCertificacion <> \'\' THEN :FechaCertificacion ELSE "Solicitudes"."FechaCertificacion" END)', "AND 1 = 1", 1),
    ("S08 the filters select ignores the issuer", "F", 'WHERE "Solicitudes"."IdEmisor" = (CASE WHEN :IdEmisor <> \'\' THEN :IdEmisor ELSE "Solicitudes"."IdEmisor" END)', "WHERE 1 = 1", 1),
    ("S09 the filters select ignores the type of request", "F", 'AND "Solicitudes"."TipoSolicitud" = (CASE WHEN :TipoSolicitud <> \'\' THEN :TipoSolicitud ELSE "Solicitudes"."TipoSolicitud" END)', "AND 1 = 1", 1),
    ("S10 the filters select ignores the series", "F", 'AND "Solicitudes"."IdSerieFondo"  = (CASE WHEN :IdSerieFondo <> \'\' THEN :IdSerieFondo ELSE "Solicitudes"."IdSerieFondo" END)', "AND 1 = 1", 1),
    ("S11 a rescate needs ACP, not CRT", "F", "'RES' AND \"Solicitudes\".\"EstadoSolicitud\" = 'CRT'", "'RES' AND \"Solicitudes\".\"EstadoSolicitud\" = 'ACP'", 1),
    ("S12 the state filter applies to rescates, not aportes", "F", "\"Solicitudes\".\"TipoSolicitud\" = 'APO') OR", "\"Solicitudes\".\"TipoSolicitud\" = 'RES') OR", 1),
    ("S13 the filters select takes liquidated rows", "F", "\"Solicitudes\".\"EstadoLiquidacion\" = 'ING'", "\"Solicitudes\".\"EstadoLiquidacion\" = 'LIQ'", 1),
    ("S14 the filters select drops RESNORM", "F", "'RESNORM',", "", 1),
    ("S15 the UPDATE leaves the solicitud ING", "U", "sol.\"EstadoLiquidacion\" = 'LIQ'", "sol.\"EstadoLiquidacion\" = 'ING'", 1),
    ("S16 the UPDATE does not write the folio", "U", 'sol."FolioCartaLiquidacion" = :FolioCartaLiquidacion', 'sol."FolioCartaLiquidacion" = 0', 1),
    ("S17 the UPDATE touches every solicitud", "U", 'WHERE sol."IdSolicitudAporteRescate" = :IdSolicitudAporteRescate', 'WHERE sol."IdSolicitudAporteRescate" > 0', 1),
    ("S18 the UPDATE does not write the registration date", "U", 'sol."FechaLiquidacionRegistro" = CURRENT_UTCTIMESTAMP,\n', "", 1),
    ("S19 the modality of a medium is its own id", "M", ',"IdModalidadDePago"', ',"IdMedioDePago" AS "IdModalidadDePago"', 1),
    ("S20 the percentage of the withholding is divided by 1000", "R", '"PorcentajeRetencion"/100 AS', '"PorcentajeRetencion"/1000 AS', 1),
    ("S21 the withholding reads no valid row", "R", "\"IdEstado\" = 'VIG'", "\"IdEstado\" = 'XXX'", 1),
]


def comparable(texto):
    j = json.loads(texto)
    j.pop("_provenance", None)
    return j


def grabar(extra_env, destino, work):
    env = dict(os.environ)
    env.update({"PYTHON_CMD": "python -X utf8", "ORACLE_WORK": work.as_posix(), "ORACLE_OUT": destino.as_posix(), "ORACLE_REPORT": (destino / "informe.txt").as_posix(), "ORACLE_REPEAT": "1",
                "M2_REPO": os.environ.get("M2_REPO", (pathlib.Path.home() / ".m2" / "repository").as_posix())})
    env.update(extra_env)
    destino.mkdir(parents=True, exist_ok=True)
    r = subprocess.run([BASH, "legacy-oracle/record_liq.sh", "FMM-LIQ"], env=env, capture_output=True, text=True, encoding="utf-8", cwd=str(BASE), stdin=subprocess.DEVNULL)
    if r.returncode != 0:
        print(r.stdout[-1500:])
        print(r.stderr[-1500:])
        raise SystemExit("record_liq.sh failed (%s)" % r.returncode)


def comparar(destino):
    distintos = []
    for d in sorted((BASE / "cases-liq").glob("FMM-LIQ-*")):
        g = comparable((d / "expected.json").read_text(encoding="utf-8"))
        n = destino / (d.name + ".json")
        if not n.exists():
            distintos.append(d.name + " (not recorded)")
            continue
        if comparable(n.read_text(encoding="utf-8")) != g:
            distintos.append(d.name)
    return distintos


def mutar(fuente, viejo, nuevo, ocurrencia):
    if fuente.count(viejo) < max(ocurrencia, 1):
        return None
    if ocurrencia == 0:
        return fuente.replace(viejo, nuevo)
    partes = fuente.split(viejo)
    return viejo.join(partes[:ocurrencia]) + nuevo + viejo.join(partes[ocurrencia:])


def leer(p):
    return p.read_bytes().decode("utf-8").replace("\r\n", "\n")


def main():
    filtro = sys.argv[1] if len(sys.argv) > 1 and sys.argv[1] != "--seco" else ""
    seco = "--seco" in sys.argv
    java = leer(JAVA)
    SALIDA.mkdir(exist_ok=True)
    resultado = {"mutants": [], "controls": []}
    malos = []
    if seco:
        for nombre, viejo, nuevo, oc in MUT_JAVA:
            if mutar(java, viejo, nuevo, oc) is None:
                malos.append("%s: the text to mutate is not in the legacy source" % nombre)
        for nombre, k, viejo, nuevo, oc in MUT_SQL:
            if mutar(leer(SQLS[k][0]), viejo, nuevo, oc) is None:
                malos.append("%s: the text to mutate is not in the legacy SQL" % nombre)
        for m in malos:
            print("!!", m)
        print("dry run: %d java + %d sql mutants, %d problems" % (len(MUT_JAVA), len(MUT_SQL), len(malos)))
        sys.exit(1 if malos else 0)
    temp = pathlib.Path(tempfile.mkdtemp(prefix="canary_t6b_"))
    work_sql = temp / "work_sql"        # compiled by the control run, reused by the SQL mutants (ORACLE_SKIP_COMPILE)
    work_sql.mkdir()
    if not filtro or filtro == "C1":
        destino = temp / "control"
        grabar({}, destino, work_sql)
        d = comparar(destino)
        resultado["controls"].append({"name": "C1 unmodified legacy re-recorded (1 run per case)", "differs": len(d), "cases": d[:5]})
        if d:
            malos.append("control C1 differs in %d cases" % len(d))
        print("C1 unmodified: %d cases differ (expected 0)" % len(d))
    for nombre, k, viejo, nuevo, ocurrencia in MUT_SQL:
        if filtro and filtro not in nombre:
            continue
        ruta, paquete = SQLS[k]
        mutado = mutar(leer(ruta), viejo, nuevo, ocurrencia)
        if mutado is None:
            malos.append("%s: the text to mutate is not in the legacy SQL" % nombre)
            continue
        overlay = temp / "overlay_res"
        if overlay.exists():
            shutil.rmtree(overlay)
        (overlay / paquete).mkdir(parents=True)
        (overlay / paquete / ruta.name).write_text(mutado, encoding="utf-8", newline="")
        destino = temp / ("m_" + nombre.split(" ")[0])
        env = {"LEGACY_OVERLAY_RES": overlay.as_posix()}
        trabajo = work_sql
        if (work_sql / "classes").exists():
            env["ORACLE_SKIP_COMPILE"] = "1"
        else:
            trabajo = temp / "work_filtro"
            trabajo.mkdir(exist_ok=True)
        grabar(env, destino, trabajo)
        d = comparar(destino)
        resultado["mutants"].append({"name": nombre, "killed": len(d) > 0, "cases_that_differ": len(d), "first_cases": d[:5]})
        print("%s: %s (%d cases differ)" % (nombre, "KILLED" if d else "SURVIVED", len(d)))
        if not d:
            malos.append("%s survived: no case pins that clause of the SQL" % nombre)
    for nombre, viejo, nuevo, ocurrencia in MUT_JAVA:
        if filtro and filtro not in nombre:
            continue
        mutado = mutar(java, viejo, nuevo, ocurrencia)
        if mutado is None:
            malos.append("%s: the text to mutate is not in the legacy source" % nombre)
            continue
        overlay = temp / "overlay"
        if overlay.exists():
            shutil.rmtree(overlay)
        overlay.mkdir()
        (overlay / JAVA.name).write_text(mutado, encoding="utf-8", newline="")
        destino = temp / ("m_" + nombre.split(" ")[0])
        trabajo = temp / "work_java"
        trabajo.mkdir(exist_ok=True)
        try:
            grabar({"LEGACY_OVERLAY": overlay.as_posix()}, destino, trabajo)
        except SystemExit as e:
            malos.append("%s: the mutant does not compile or run (%s)" % (nombre, e))
            continue
        d = comparar(destino)
        resultado["mutants"].append({"name": nombre, "killed": len(d) > 0, "cases_that_differ": len(d), "first_cases": d[:5]})
        print("%s: %s (%d cases differ)" % (nombre, "KILLED" if d else "SURVIVED", len(d)))
        if not d:
            malos.append("%s survived: no case pins that line of the class" % nombre)
    ruta_json = SALIDA / "GOLDEN_MUTATION_T6B.json"
    if not filtro:
        ruta_json.write_text(json.dumps(resultado, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    elif ruta_json.exists():
        # a filtered run (a mutant re-run after a case was added, or the control) updates its own entries of the existing report
        previo = json.loads(ruta_json.read_text(encoding="utf-8"))
        for clave in ("mutants", "controls"):
            por_nombre = {m["name"]: m for m in previo[clave]}
            for m in resultado[clave]:
                por_nombre[m["name"]] = m
            previo[clave] = list(por_nombre.values())
        ruta_json.write_text(json.dumps(previo, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    shutil.rmtree(temp, ignore_errors=True)
    for m in malos:
        print("!!", m)
    print("mutants killed: %d of %d" % (sum(1 for m in resultado["mutants"] if m["killed"]), len(resultado["mutants"])))
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
