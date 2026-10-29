"""Canary of the goldens of FMM-CPF (ConsultarCustodiaPortafolioFondoMutuo), F3 W1 front 8.

The new code does not exist when the goldens are recorded, so it cannot be mutated: as FSA / SFM / DET (canary_sfm_det.py) and F2 plan section 7 ("mutation of goldens"), each mutation is a
ONE-LINE change of a COPY of the legacy source (LEGACY_OVERLAY, a temporary directory: nothing under legacy/ is touched) or of one of its four SQL (LEGACY_OVERLAY_RES, a temporary directory
with cl/bcs/fondomutuo/sql/<file>, put BEFORE the legacy tree on the class path). The oracle is re-run on it with ORACLE_OUT (the goldens are NOT rewritten), one repetition, and the dumps are
compared with the recorded goldens. A mutation that changes no dump means the case set does not pin that behaviour (an EQUIVALENT mutant, or a hole to report).

  py -3 -I legacy-oracle/canary_cpf.py [java|sql] [text of the mutation name]      (needs JAVA_HOME / bash like the record scripts; prints one line per mutation and a total)

The "expected equivalent" mutants are listed with the reason (the property is not observable at the boundary of the class): they are reported, not counted as holes.
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
CLASE = "ConsultarCustodiaPortafolioFondoMutuo.java"
SQL = "ConsultarCustodiaPortafolioFondoMutuo_%s.sql"

# (kind, file, old text, new text, what a rewrite could plausibly get wrong, equivalent-mutant reason or ""[, n = which occurrence of the old text, default 1])
MUTACIONES = [
    ("java", CLASE, "Boolean esFechaConsultaValida = _c.FechaDefecto.equals(fechaConsulta) || fechaConsulta == null;", "Boolean esFechaConsultaValida = _c.FechaDefecto.equals(fechaConsulta);",
     "a null date is not the default date (FM-CPF-02)", ""),
    ("java", CLASE, "String fechaConsulta = _c.FechaDefecto;\n", "String fechaConsulta = _c.FechaDefecto;\n\t\tfechaSistema = _c.FechaDefecto;\n",
     "the default date is the date of the system (FM-CPF-02)", ""),
    ("java", CLASE, "if(_c.SI.equals(filtraSoloSolicitudesNoAutomaticas)) {", "if(_c.SI.equalsIgnoreCase(filtraSoloSolicitudesNoAutomaticas)) {", "the parameter is case insensitive (FM-CPF-03)", ""),
    ("java", CLASE, "parametros.put(\"IngresoAutomatico\", _c.NO);", "parametros.put(\"IngresoAutomatico\", _c.SI);", "automatic flag sent inverted (FM-CPF-03)", ""),
    ("java", CLASE, "solicitud->solicitud.CantidadAporte.compareTo(BigDecimal.ZERO) == 1", "solicitud->solicitud.CantidadAporte.compareTo(BigDecimal.ZERO) != -1", "aportes of 0 quotas are valued (FM-CPF-04)", ""),
    ("java", CLASE, "solicitud->solicitud.CantidadRescate.compareTo(BigDecimal.ZERO) == 1", "solicitud->solicitud.CantidadRescate.compareTo(BigDecimal.ZERO) != -1", "rescates of 0 quotas are valued (FM-CPF-04)", ""),
    ("java", CLASE, "private final Long FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO = -1L;", "private final Long FACTOR_PARA_BUSQUEDA_RESCATE_VALORIZADO = 1L;", "the id of a rescate valuation is negative (FM-CPF-04)", ""),
    ("java", CLASE, "AtomicLong idInterno = new AtomicLong(1L);\n\t\tsolicitudes.parallelStream()", "AtomicLong idInterno = new AtomicLong(0L);\n\t\tsolicitudes.parallelStream()", "the ids of the solicitudes start at 2, not 1 (D-F3A-04)", ""),
    ("java", CLASE, "solicitud.CantidadMontoAporte = _c.CalculoPorMonto.equals(solicitud.CalculoPorMontoCuota) ? solicitud.MontoMonedaSistemaAporte : aporte.MontoMonedaSistema;",
     "solicitud.CantidadMontoAporte = _c.CalculoPorMonto.equals(solicitud.CalculoPorMontoCuota) ? aporte.MontoMonedaSistema : solicitud.MontoMonedaSistemaAporte;",
     "the amount of an aporte by amount comes from the valuation (FM-CPF-04)", ""),
    ("java", CLASE, "solicitud.CantidadMontoRescate = rescate.MontoMonedaSistema;", "solicitud.CantidadMontoRescate = rescate.Monto;", "the amount of a rescate comes from Monto (FM-CPF-04)", ""),
    ("java", CLASE, "instrumento.SolicitudesTransito.CantidadRescate = solicitud.CantidadRescate.add(instrumento.SolicitudesTransito.CantidadRescate, MathContext.DECIMAL128);",
     "instrumento.SolicitudesTransito.CantidadRescate = instrumento.SolicitudesTransito.CantidadRescate;", "the rescates of a second row are not added (FM-CPF-07)", ""),
    ("java", CLASE, "instrumento.SolicitudesTransito.CantidadMontoAporte = solicitud.CantidadMontoAporte.add(instrumento.SolicitudesTransito.CantidadMontoAporte, MathContext.DECIMAL128);",
     "instrumento.SolicitudesTransito.CantidadMontoAporte = solicitud.CantidadMontoAporte;", "the amount of the last row replaces the sum (FM-CPF-07)", ""),
    ("java", CLASE, "instrumento.Custodia = custodia;", "if(instrumento.Custodia == null) instrumento.Custodia = custodia;", "the FIRST custody of a key wins instead of the last (FM-CPF-07)", ""),
    ("java", CLASE, "salida.IdBoveda = \"COR\";", "salida.IdBoveda = \"DCV\";", "the vault of a saldo without custody (FM-CPF-08)", ""),
    ("java", CLASE, "salida.PPPCustodia = saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia != null ? saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia : BigDecimal.ZERO;",
     "salida.PPPCustodia = saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia;", "a null PPPCustodia stays null instead of 0 (FM-CPF-08)", ""),
    ("java", CLASE, "salida.NombreAdministradora = salidaTemp.IdEmisor;", "salida.NombreAdministradora = salidaTemp.NombreFondo;", "the administrator name of the output (D-F3A-06)", ""),
    ("java", CLASE, "salida.NombreAdministradora = serie.IdEmisor;", "salida.NombreAdministradora = serie.NombreFondo;", "the administrator name of the PRELIMINARY row (dead write: the output row recomputes it from IdEmisor)",
     "the preliminary NombreAdministradora is overwritten by obtenerSalidaCustodia (CPF:1101): dead write"),
    ("java", CLASE, "salida.Nemotecnico = serie.IdSerieFondo;", "salida.Nemotecnico = serie.CodigoSerie;", "the nemotecnico (D-F3A-06)", ""),
    ("java", CLASE, "salida.RestringeRescate = serie.RestringeRescate; ", "salida.RestringeRescate = \"N\"; ", "RestringeRescate is copied from the series (FM-CPF-08)", ""),
    ("java", CLASE, "salida.CantidadTransSolicitudesRescate = salida.CantidadCustodia.add(salida.CantidadTransSolicitudesAporte, MathContext.DECIMAL128);",
     "salida.CantidadTransSolicitudesRescate = salida.CantidadCustodia;", "total rescate = custody only, without the aportes in transit (FM-CPF-09)", ""),
    ("java", CLASE, "if(rescateTotales.get(key) != null) {", "if(rescateTotales.get(key) == null) {", "total rescate flag inverted (FM-CPF-05)", ""),
    ("java", CLASE, "String key = saldoPortafolioRegimenInstrumento.IdPortafolio + \"_\" + saldoPortafolioRegimenInstrumento.IdInstrumento;",
     "String key = saldoPortafolioRegimenInstrumento.IdCliente + \"_\" + saldoPortafolioRegimenInstrumento.IdInstrumento;", "the key of the total rescate (FM-CPF-05)", ""),
    ("java", CLASE, "(_c.SI.equals(regimen.RescateConMandato) || _c.SI.equals(regimen.AceptaMandatoEnReinversion)) ? \"S\" : \"N\";",
     "(_c.SI.equals(regimen.RescateConMandato) && _c.SI.equals(regimen.AceptaMandatoEnReinversion)) ? \"S\" : \"N\";", "MLT needs both mandates (FM-CPF-10)", ""),
    ("java", CLASE, "salida.MLT = (_c.SI.equals(regimen.RescateConMandato) || _c.SI.equals(regimen.AceptaMandatoEnReinversion)) ? \"S\" : \"N\";\n\t\t\t\t\t\t\tbreak;",
     "salida.MLT = (_c.SI.equals(regimen.RescateConMandato) || _c.SI.equals(regimen.AceptaMandatoEnReinversion)) ? \"S\" : \"N\";", "the LAST regimen decides instead of the first (D-F3A-08)", ""),
    ("java", CLASE, "if(_c.SI.equals(entrada.p_BuscaMLT)) {", "if(_c.SI.equalsIgnoreCase(entrada.p_BuscaMLT)) {", "BuscaMLT is case insensitive (FM-CPF-10)", ""),
    ("java", CLASE, "salida.MLT = \"N\";", "salida.MLT = \"S\";", "MLT without search is 'S' (FM-CPF-10)", ""),
    ("java", CLASE, "calculoMontoCuota.IdInterno = salida.IdInterno * -2L;", "calculoMontoCuota.IdInterno = salida.IdInterno;", "the guarantee is asked and looked up with the same id (D-F3A-03: MontoGarantia would be real)", ""),
    ("java", CLASE, "filter(salida->BigDecimal.ZERO.compareTo(salida.CantidadGarantia) != 0)", "filter(salida->BigDecimal.ZERO.compareTo(salida.CantidadGarantia) < 0)", "a negative guarantee is not valued (FM-CPF-12)", ""),
    ("java", CLASE, "filter(salida->BigDecimal.ZERO.compareTo(salida.CantidadTransSolicitudesRescate) != 0)", "filter(salida->BigDecimal.ZERO.compareTo(salida.CantidadTransSolicitudesRescate) < 0)",
     "a negative rescate in transit is not valued (FM-CPF-12)", ""),
    ("java", CLASE, "salida.MontoTransSolicitudesRescate = resultadoRescate.MontoMonedaSistema;", "salida.MontoTransSolicitudesRescate = resultadoRescate.Monto;", "the rescate in transit amount (FM-CPF-12)", ""),
    ("java", CLASE, "calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudAporte;", "calculoMontoCuota.TipoSolicitud =  _c.TipoSolicitudRescate;",
     "the rescate in transit is valued as RES instead of APO (D-F3A-05: the type is not observable at the boundary: CalcularMontoNumeroCuota does not use it for a fund in its own currency)",
     "TipoSolicitud only selects the observed-dollar rate of a deferred solicitud and CPF never defers (IdMonedaInversion = IdMonedaFondo): not observable", 4),
    ("java", CLASE, "if(\"S\".equals(calculaValorCuotaDiaSiguiente)) {", "if(\"N\".equals(calculaValorCuotaDiaSiguiente)) {", "the next-day parameter is inverted (FM-CPF-13)", ""),
    ("java", CLASE, "calculoEntrada.Fecha = fechaValorCuotaDiaSiguiente != null ? fechaValorCuotaDiaSiguiente : entrada.p_FechaConsulta;", "calculoEntrada.Fecha = entrada.p_FechaConsulta;",
     "the next working day is ignored (FM-CPF-13)", ""),
    ("java", CLASE, "inObtenerDiaSiguiente.p_diaHabil = _c.SI;", "inObtenerDiaSiguiente.p_diaHabil = _c.NO;", "the next day does not skip non working days (FM-CPF-13)",
     "for ONE day, p_diaHabil 'N' + p_soloCaeDiaHabil 'S' adds a calendar day and then rolls forward to the next working day: the same date as skipping (not observable with numeroDias = 1)"),
    ("java", CLASE, "fechaConsulta.numeroDias = 1L;", "fechaConsulta.numeroDias = 2L;", "the next day is 2 days ahead (FM-CPF-13)", ""),
    ("java", CLASE, "Map<String, String> fechasValorCuotaDiaSiguiente = obtenerDiasSiguiente(con, entrada.p_IdEmpresa, salidaPreliminar);",
     "Map<String, String> fechasValorCuotaDiaSiguiente = \"S\".equals(calculaValorCuotaDiaSiguiente) ? obtenerDiasSiguiente(con, entrada.p_IdEmpresa, salidaPreliminar) : new HashMap<String,String>();",
     "the next days are computed only when the parameter is 'S' (D-F3A-07: the company is looked up even with 'N')", ""),
    ("java", CLASE, "calculoEntrada.IdMonedaDestino = salida.IdMonedaSistema;", "calculoEntrada.IdMonedaDestino = salida.IdMonedaFondo;", "the destination currency of the paridad, 'S' branch (FM-CPF-13)", ""),
    ("java", CLASE, "calculoEntrada.MontoOrigen =  salida.ValorCuota;", "calculoEntrada.MontoOrigen =  BigDecimal.ONE;", "the paridad amount, 'S' branch (FM-CPF-13)", ""),
    ("java", CLASE, "salida.ValorCuotaMonedaSistema = esValorCuotaMonedaSistemaValido(valorCuotaMonedaSistema) ? valorCuotaMonedaSistema.MontoDestino : BigDecimal.ZERO;",
     "salida.ValorCuotaMonedaSistema = esValorCuotaMonedaSistemaValido(valorCuotaMonedaSistema) ? valorCuotaMonedaSistema.MontoDestino : BigDecimal.ONE;", "no paridad answer gives 1 instead of 0 (FM-CPF-13)", ""),
    ("java", CLASE, "salida.MontoGarantiaMonedaSistema = MonedaFormato.formatear(con, salida.CantidadGarantia.multiply(salida.ValorCuotaMonedaSistema), MonedaFormato.MONTO, monedaSistema).valor;",
     "salida.MontoGarantiaMonedaSistema = MonedaFormato.formatear(con, salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema), MonedaFormato.MONTO, monedaSistema).valor;",
     "the guarantee amount uses the custody quantity (FM-CPF-13)", ""),
    ("java", CLASE, "salida.MontoMonedaSistema = salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema);", "salida.MontoMonedaSistema = salida.CantidadCustodia;",
     "the unformatted fallback of the system amount (FM-CPF-13)", ""),
    ("java", CLASE, "String key = solicitud.IdCliente + \"_\" + solicitud.IdPortafolio + \"_\" + solicitud.IdInstrumento + \"_\" + solicitud.IdRegimenFondoMutuo;",
     "String key = solicitud.IdCliente + \"_\" + solicitud.IdPortafolio + \"_\" + solicitud.IdInstrumento;", "the regimen is not part of the saldo key (FM-CPF-07)", ""),
    ("java", CLASE, "salida.IdInterno = idInterno.getAndIncrement();", "salida.IdInterno = idInterno.incrementAndGet();", "the internal ids of the saldos start at 2 (D-F3A-04)", ""),
    ("java", CLASE, "salida.DecimalesCuota = serie.DecimalesCuota;", "salida.DecimalesCuota = serie.DecimalesValorCuota;", "DecimalesCuota copied from the wrong column (FM-CPF-08)", ""),
    ("java", CLASE, "salida.IdMonedaSistema = idMonedaSistema;", "salida.IdMonedaSistema = \"USD\";", "the system currency of the paridad (FM-CPF-13)", ""),
    # SQL mutations
    ("sql", SQL % "SelectSolicitudes", "\"Solicitudes\".\"Estado\" IN ('ING', 'PEN')", "\"Solicitudes\".\"Estado\" IN ('ING')", "PEN solicitudes are not in transit (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "AND \"Solicitudes\".\"TipoSolicitud\" IN ('APO','RES')", "AND \"Solicitudes\".\"TipoSolicitud\" IN ('APO')", "rescates are not in transit (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "OR \"Solicitudes\".\"IdRegimenTributario\"='BF'", "", "BF is not exempt from the automatic filter (FM-CPF-03, QD-F3A-02)", ""),
    ("sql", SQL % "SelectSolicitudes", "Spot.\"Estado\" != 'MOD'", "Spot.\"Estado\" = 'MOD'", "the Spot join keeps the modified operations (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "AND \"Solicitudes\".\"CalculoPorMontoCuota\" != 'T'", "AND \"Solicitudes\".\"CalculoPorMontoCuota\" = 'T'", "total rescates are in the transit universe (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "AND \"Solicitudes\".\"IdEmpresa\" = :IdEmpresa", "AND 1 = 1", "the company filter (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "MAP(MatrizAgrupador.\"IdProductoAgrupador\",NULL,0,MatrizAgrupador.\"IdProductoAgrupador\") = :IdProductoAgrupador", "MatrizAgrupador.\"IdProductoAgrupador\" = :IdProductoAgrupador",
     "a portfolio without product does not match product 0 (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "AND (:IdFamiliaFondo ='' OR \"SerieFondo\".\"IdFamiliaFondo\" = :IdFamiliaFondo)", "AND 1 = 1", "the family filter (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "WHEN COALESCE(Spot.\"IdOperacionSpotOrigen\",0) > 0 THEN Spot.\"MontoMonedaSecundaria\"", "WHEN COALESCE(Spot.\"IdOperacionSpotOrigen\",0) > 0 THEN \"Solicitudes\".\"MontoMonedaSistema\"",
     "the Spot amount of an aporte (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudes", "CASE WHEN COALESCE(\"PORT\".\"IdMatrizAgrupadorAPV\",0) != 0 THEN COALESCE(\"MAT\".\"EsADC\",'N') ELSE COALESCE(MAT_NOAPV.\"EsADC\",'N') END AS \"EsADC\"",
     "COALESCE(MAT_NOAPV.\"EsADC\",'N') AS \"EsADC\"", "ADC of an APV portfolio comes from the NOAPV matrix (FM-CPF-03)", ""),
    ("sql", SQL % "SelectSolicitudesTotales", "AND \"Solicitudes\".\"CalculoPorMontoCuota\" = 'T'", "AND \"Solicitudes\".\"CalculoPorMontoCuota\" != 'T'", "total rescates are the non 'T' ones (FM-CPF-05)", ""),
    ("sql", SQL % "SelectSolicitudesTotales", "AND (:FiltraIngresoAutomatico = 'N' OR \"IngresoAutomatico\" = :IngresoAutomatico)", "AND (:FiltraIngresoAutomatico = 'N' OR \"IngresoAutomatico\" = :IngresoAutomatico OR \"Solicitudes\".\"IdRegimenTributario\"='BF')",
     "the totals SQL also exempts BF (FM-CPF-05, QD-F3A-02)", ""),
    ("sql", SQL % "SelectSolicitudesTotales", "AND (:IdCliente = 0 OR \"Solicitudes\".\"IdCliente\" = :IdCliente)", "AND 1 = 1", "the client filter of the totals (FM-CPF-05)", ""),
    ("sql", SQL % "SelectRegimen", "AND Regimen.\"IdEstado\"='ACT'", "", "inactive regimes are considered (FM-CPF-10)", ""),
    ("sql", SQL % "SelectSeries", "AND \"DctoIdentidad\".\"Predeterminado\" ='S'", "AND \"DctoIdentidad\".\"Predeterminado\" ='N'", "the default identity document of the issuer (FM-CPF-08)", ""),
    ("sql", SQL % "SelectSeries", "\"CTINS\".\"Nombre\" AS \"NombreClasificacionTipoInstrumento\"", "\"TINS\".\"Nombre\" AS \"NombreClasificacionTipoInstrumento\"", "the classification name column (FM-CPF-08)", ""),
    ("sql", SQL % "SelectSeries", ",\"SerieDeFondo\".\"RestringeRescate\"", ",'N' AS \"RestringeRescate\"", "RestringeRescate of the series (FM-CPF-08)", ""),
]


def reemplazar(texto, viejo, nuevo, n):
    """Replaces the n-th occurrence (1 = the first) of `viejo`."""
    pos = -1
    for _ in range(n):
        pos = texto.index(viejo, pos + 1)
    return texto[:pos] + nuevo + texto[pos + len(viejo):]


def comparable(j):
    j = dict(j)
    j.pop("_provenance", None)
    e = j.get("excepcion")
    if e and ":" in e and e.split(":")[0] != "Exception":
        j["excepcion"] = e.split(":")[0]
    return j


def correr(overlay, overlay_res, salida):
    env = dict(os.environ, ORACLE_OUT=salida.as_posix(), ORACLE_REPEAT="1", ORACLE_REPORT=(salida / "report.txt").as_posix(), ORACLE_WORK=(salida / "work").as_posix(), PYTHONUTF8="1")
    if overlay is not None:
        env["LEGACY_OVERLAY"] = overlay.as_posix()
    if overlay_res is not None:
        env["LEGACY_OVERLAY_RES"] = overlay_res.as_posix()
        # a SQL mutation needs no compilation: it reuses the classes of a previous run when ORACLE_REUSE_CLASSES points at its work dir
        if os.environ.get("ORACLE_REUSE_CLASSES"):
            env["ORACLE_WORK"] = os.environ["ORACLE_REUSE_CLASSES"]
            env["ORACLE_SKIP_COMPILE"] = "1"
    return subprocess.run([BASH, (HERE / "record_cpf.sh").as_posix(), "FMM-CPF"], env=env, capture_output=True, text=True, timeout=3000)


def main():
    tipo = sys.argv[1] if len(sys.argv) > 1 and sys.argv[1] in ("java", "sql") else None
    filtro = sys.argv[2] if len(sys.argv) > 2 else (sys.argv[1] if len(sys.argv) > 1 and tipo is None else None)
    total = cambian = 0
    equivalentes = []
    # CANARY_RANGE="a:b": runs only the mutations with index a <= i < b (to run several copies of this script in parallel over disjoint ranges; a SQL mutation reuses the compiled
    # classes of ORACLE_REUSE_CLASSES, so every parallel copy needs its OWN copy of that directory)
    rango = os.environ.get("CANARY_RANGE")
    for indice, mutacion in enumerate(MUTACIONES):
        if rango and not (int(rango.split(":")[0]) <= indice < int(rango.split(":")[1])):
            continue
        kind, archivo, viejo, nuevo, que, equiv = mutacion[:6]
        n = mutacion[6] if len(mutacion) > 6 else 1
        if tipo and kind != tipo:
            continue
        if filtro and filtro not in que:
            continue
        origen = (FMP / archivo) if kind == "java" else (FMP / "sql" / archivo)
        texto = origen.read_bytes().decode("utf-8", errors="replace").replace("\r\n", "\n")
        if viejo not in texto:
            print("MUTATION NOT APPLICABLE (text not found):", archivo, viejo[:70].replace("\n", " "))
            continue
        total += 1
        with tempfile.TemporaryDirectory() as t:
            t = pathlib.Path(t)
            (t / "out").mkdir()
            (t / "work").mkdir()
            if kind == "java":
                (t / "overlay").mkdir()
                (t / "overlay" / archivo).write_text(texto.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
                r = correr(t / "overlay", None, t / "out")
            else:
                res = t / "res" / "cl" / "bcs" / "fondomutuo" / "sql"
                res.mkdir(parents=True)
                (res / archivo).write_text(texto.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
                r = correr(None, t / "res", t / "out")
            if r.returncode not in (0, 4):
                print("RUN FAILED (%s %s): %s" % (kind, archivo, (r.stdout[-300:] + r.stderr[-300:]).replace("\n", " ")))
                continue
            distintos = []
            for d in sorted((BASE / "cases-cpf").glob("FMM-CPF-*")):
                dump = t / "out" / (d.name + ".json")
                gold = d / "expected.json"
                if not gold.exists() or not dump.exists():
                    distintos.append(d.name + " (missing)")
                    continue
                if comparable(json.loads(dump.read_text(encoding="utf-8"))) != comparable(json.loads(gold.read_text(encoding="utf-8"))):
                    distintos.append(d.name)
            if distintos:
                cambian += 1
            elif equiv:
                equivalentes.append(que)
            print("%-4s %-70s -> %3d cases change%s" % (kind, que[:70], len(distintos), "" if distintos else ("   <== EQUIVALENT (%s)" % equiv if equiv else "   <== NOT PINNED")), flush=True)
    print("mutations: %d, changing at least one golden: %d, expected equivalent (reported): %d" % (total, cambian, len(equivalentes)))


if __name__ == "__main__":
    main()
