"""F3 W3 T1 canary of the goldens and of the tests of CertificarSolicitudAporteRescate fronts F-A and F-B (plan F3 section 4 "mutation of goldens"; the instruction: break the code in one
small way that matters, confirm that at least one test goes red, restore). Two modes; nothing under legacy/ or under the real module is ever touched:

  py -3 -I canary_cer.py legacy [text of the mutation name]
      one-line mutations of a COPY of the legacy CertificarSolicitudAporteRescate.java are run through record_cer.sh with LEGACY_OVERLAY and ORACLE_OUT in a temporary directory (the
      goldens are NOT rewritten) and every dump is compared with its golden (the _provenance line aside). A mutation that changes no golden is an EQUIVALENT mutant or a HOLE of the
      case set. Prints "<mutation>: <n> golden(s) changed" and the first cases that changed.

  py -3 -I canary_cer.py nuevo <copy of the module outside the repo> [text of the mutation name]
      one-line mutations of a candidate implementation of the production package cl.bcs.fondomutuo.certificacion inside a COPY of the module (the real module is never touched; Maven is
      never run on it): after each mutation `mvn -o test -Dtest=CerEquivalenciaTest,CerReglasTest,CerCompuertaTest` runs in the copy and the mutation must make at least one test fail.
      The old / new texts below are those of the throwaway validation implementation described in API_ASSUMED.md; against another implementation a text that is not found is reported as
      NOT APPLICABLE (adapt the text, do not read it as a pass).
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
CER = LEG / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo" / "CertificarSolicitudAporteRescate.java"
BASH = shutil.which("bash") or "C:/Program Files/Git/usr/bin/bash.exe"
MVN = shutil.which("mvn") or "mvn"
T = "\t"

# (name, file, old text, new text)
LEGACY = [
    ("FM-CER-06 el estado aceptado ya no sirve", CER, "if(!_c.EstadoIngresado.equals(sol.EstadoSolicitud) && !_c.EstadoAceptado.equals(sol.EstadoSolicitud)){", "if(!_c.EstadoIngresado.equals(sol.EstadoSolicitud)){"),
    ("FM-CER-07 la fecha se compara con la FechaValorCuota", CER, "if(!Fecha.stringToSqlDate(sol.FechaCertificacion).equals(entrada.p_FechaCertificacion)){", "if(!Fecha.stringToSqlDate(sol.FechaValorCuota).equals(entrada.p_FechaCertificacion)){"),
    ("FM-CER-08 la fecha del precio no se compara", CER, "if (!FechaValorCuotaSolicitud.equalsIgnoreCase(FechaValorCuotaCargado))", "if (false)"),
    ("FM-CER-09 IdOmnibus 0 tambien es no creado", CER, "sol.IdOmnibus == null && !(_c.SI.equals(sol.Switch)", "(sol.IdOmnibus == null || sol.IdOmnibus.longValue() == 0L) && !(_c.SI.equals(sol.Switch)"),
    ("FM-CER-09 la errata se corrige (D-F3B-31)", CER, 'sol.Codigo = "UI_ONMIBUS_NO_CREADO";', 'sol.Codigo = "UI_OMNIBUS_NO_CREADO";'),
    ("FM-CER-11 la liquidacion previa exige folio mayor que 0 a la inversa", CER, "&& !_c.EstadoLiquidado.equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() == 0){",
     "&& !_c.EstadoLiquidado.equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() != 0){"),
    ("FM-CER-12 la asociada se compara con menor que", CER, "(sol.IdSolicitudAporteRescate.longValue() > sol.IdSolicitudAporteRescateRelacionado.longValue()", "(sol.IdSolicitudAporteRescate.longValue() < sol.IdSolicitudAporteRescateRelacionado.longValue()"),
    ("FM-CER-13 TGR se valida con APV-B", CER, "if(personaTGR == null && _c.BonificacionFiscal.equals(sol.IdRegimenTributarioAPV)) {", "if(personaTGR == null && _c.APV_B.equals(sol.IdRegimenTributarioAPV)) {"),
    ("FM-CER-13 APV-AN tambien se valida contra SII (D-F3B-08 corregido)", CER, "if(personaSII == null && _c.APV_B.equals(sol.IdRegimenTributarioAPV)) {",
     "if(personaSII == null && (_c.APV_B.equals(sol.IdRegimenTributarioAPV) || _c.APV_AN.equals(sol.IdRegimenTributarioAPV))) {"),
    ("FM-CER-14 la salida temprana no escribe el log", CER, T * 3 + "insertarLog(con, solicitudes, entrada, fechaSistema);\n" + T * 3 + "return out;", T * 3 + "return out;"),
    ("FM-CER-14 la salida temprana con contador 2", CER, T * 3 + "out.P_OUT_ERRORESCONTADOR = 1L;\n" + T * 3 + "out.P_OUT_RESULTADO.addAll", T * 3 + "out.P_OUT_ERRORESCONTADOR = 2L;\n" + T * 3 + "out.P_OUT_RESULTADO.addAll"),
    ("FM-CER-02 ids repetidos ya no se detectan", CER, "if (ids.contains(mov.IdSolicitudAporteRescate)) {", "if (false) {"),
    ("FM-CER-04 el traspaso APV pisa al no APV", CER, "hashTraspasos.putAll(hashTraspasosNoAPVAsociados);", "hashTraspasosNoAPVAsociados.putAll(hashTraspasos); hashTraspasos = hashTraspasosNoAPVAsociados;"),
    ("FM-CER-05 el uso del precio es GARANTIA", CER, 'paramsRegIn.IdUso ' + T * 4 + '= "CARTERA";', 'paramsRegIn.IdUso ' + T * 4 + '= "GARANTIA";'),
    ("FM-CER-15 los indicadores se piden de TODAS las solicitudes", CER, "List<Long> portafolios = filtrarSolicitudesPendientesYAdvertencias(solicitudes).map(solicitud->solicitud.IdPortafolio)",
     "List<Long> portafolios = solicitudes.stream().map(solicitud->solicitud.IdPortafolio)"),
    ("FM-CER-16 la inversion se manda con la moneda de pago", CER, "serie.IdMonedaInversion = solicitud.IdMoneda;", "serie.IdMonedaInversion = solicitud.IdMonedaPago;"),
    ("FM-CER-17 se recalcula aunque tenga comision NN relacionada", CER, "}else if(_c.NO.equals(solicitud.TieneComisionNNRelacionada)) {", "}else if(true) {"),
    ("FM-CER-17 el Spot de un aporte no conserva el monto de moneda sistema", CER, "&& solicitud.IdOperacionSpot != null && !solicitud.IdOperacionSpot.equals(0L) ? solicitud.MontoMonedaSistema : r.MontoMonedaSistema;",
     "&& false ? solicitud.MontoMonedaSistema : r.MontoMonedaSistema;"),
    ("FM-CER-18 la fecha efectiva es la menor", CER, "compareTo(Fecha.toDate(fechaSistema)) < 0 ? Fecha.toDate(fechaSistema) : Fecha.toDate(fechaEntrada);", "compareTo(Fecha.toDate(fechaSistema)) < 0 ? Fecha.toDate(fechaEntrada) : Fecha.toDate(fechaSistema);"),
    ("FM-CER-19 orden descendente", CER, "compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 1 ? 1 : -1;", "compareTo(segundaSolicitud.IdSolicitudAporteRescate) == 1 ? -1 : 1;"),
    ("FM-CER-20 el indicador por defecto es S", CER, 'public String ValorIndicador = "N";', 'public String ValorIndicador = "S";'),
    ("FM-CER-20 gana el ultimo indicador del portafolio", CER, "filter(parametro -> parametro.IdEntidad.equals(aporte.IdPortafolio)).findFirst().orElse(new ParametroOperaCSN());",
     "filter(parametro -> parametro.IdEntidad.equals(aporte.IdPortafolio)).reduce((x, y) -> y).orElse(new ParametroOperaCSN());"),
    ("FM-CER-21 los pares con listas dependientes", CER, "if(!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMoneda)){", "if(!portFlujo.contains(sol.IdPortafolio) && !monedaFlujo.contains(sol.IdMoneda)){"),
    ("FM-CER-23 la comision se formatea con la moneda de inversion", CER, "solicitud.MontoComision = MonedaFormato.formatear(con, solicitud.MontoComision, MonedaFormato.MONTO, solicitud.IdMonedaFondo).valor;",
     "solicitud.MontoComision = MonedaFormato.formatear(con, solicitud.MontoComision, MonedaFormato.MONTO, solicitud.IdMoneda).valor;"),
]

NUEVO_DIR = "BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/certificacion/"
S = "CertificarAporteRescateService.java"
R = "CertificarAporteRescateReglas.java"
NUEVO = [
    ("FM-CER-06 el estado aceptado ya no sirve", S, 'if (!"ING".equals(sol.EstadoSolicitud) && !"ACP".equals(sol.EstadoSolicitud)) {', 'if (!"ING".equals(sol.EstadoSolicitud)) {'),
    ("FM-CER-07 la fecha se compara con la FechaValorCuota", S, "if (!FechaLegacy.texto(sol.FechaCertificacion).equals(entrada.p_FechaCertificacion)) {", "if (!FechaLegacy.texto(sol.FechaValorCuota).equals(entrada.p_FechaCertificacion)) {"),
    ("FM-CER-08 la fecha del precio no se compara", S, "if (!fechaValorCuotaSolicitud.equalsIgnoreCase(fechaValorCuotaCargado)) {", "if (false) {"),
    ("FM-CER-09 IdOmnibus 0 tambien es no creado", S, 'sol.IdOmnibus == null && !("S".equals(sol.Switch)', '(sol.IdOmnibus == null || sol.IdOmnibus.longValue() == 0L) && !("S".equals(sol.Switch)'),
    ("FM-CER-09 la errata se corrige (D-F3B-31)", S, 'rechazar(sol, "UI_ONMIBUS_NO_CREADO");', 'rechazar(sol, "UI_OMNIBUS_NO_CREADO");'),
    ("FM-CER-10 el primer comprobante no manda", S, "sol.IdComprobanteTesoreria = comprobante.IdComprobante;\n                    break;", "sol.IdComprobanteTesoreria = comprobante.IdComprobante;"),
    ("FM-CER-11 la liquidacion previa con folio al reves", S, '!"LIQ".equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() == 0) {', '!"LIQ".equals(sol.EstadoLiquidacion) && sol.FolioCartaLiquidacion.longValue() != 0) {'),
    ("FM-CER-12 la asociada se compara con menor que", S, "(sol.IdSolicitudAporteRescate.longValue() > sol.IdSolicitudAporteRescateRelacionado.longValue()", "(sol.IdSolicitudAporteRescate.longValue() < sol.IdSolicitudAporteRescateRelacionado.longValue()"),
    ("FM-CER-13 APV-AN tambien se valida contra SII (D-F3B-08 corregido)", S, 'if (personaSII == null && "APV-B".equals(sol.IdRegimenTributarioAPV)) {', 'if (personaSII == null && ("APV-B".equals(sol.IdRegimenTributarioAPV) || "APV-AN".equals(sol.IdRegimenTributarioAPV))) {'),
    ("FM-CER-13 TGR se valida con APV-B", S, 'if (personaTGR == null && "BF".equals(sol.IdRegimenTributarioAPV)) {', 'if (personaTGR == null && "APV-B".equals(sol.IdRegimenTributarioAPV)) {'),
    ("FM-CER-14 la salida temprana no escribe el log", S, "insertarLog(solicitudes, entrada, fechaSistema);\n            return out;", "return out;"),
    ("FM-CER-14 la salida temprana con contador 2", S, "out.P_OUT_ERRORESCONTADOR = 1L;\n            out.P_OUT_RESULTADO.addAll", "out.P_OUT_ERRORESCONTADOR = 2L;\n            out.P_OUT_RESULTADO.addAll"),
    ("FM-CER-02 ids repetidos ya no se detectan", S, "if (ids.contains(mov.IdSolicitudAporteRescate)) {", "if (false) {"),
    ("FM-CER-04 el traspaso APV pisa al no APV", S, "traspasos.putAll(traspasosNoApv);", "traspasosNoApv.putAll(traspasos); traspasos = traspasosNoApv;"),
    ("FM-CER-05 el uso del precio es GARANTIA", S, 'd.IdUso = "CARTERA";', 'd.IdUso = "GARANTIA";'),
    ("FM-CER-15 los indicadores se piden de TODAS las solicitudes", S, "solicitudes.stream().filter(CertificarAporteRescateReglas::pendienteOAdvertencia).map(s -> s.IdPortafolio).distinct()", "solicitudes.stream().map(s -> s.IdPortafolio).distinct()"),
    ("FM-CER-16 la inversion se manda con la moneda de pago", S, "serie.IdMonedaInversion = sol.IdMoneda;", "serie.IdMonedaInversion = sol.IdMonedaPago;"),
    ("FM-CER-16 el factor de ajuste es UTM", S, 'serie.TipoFactorAjuste = "UF";', 'serie.TipoFactorAjuste = "UTM";'),
    ("FM-CER-17 se recalcula aunque tenga comision NN relacionada", S, '} else if ("N".equals(solicitud.TieneComisionNNRelacionada)) {', "} else if (true) {"),
    ("FM-CER-17 un aporte con Spot no conserva el monto de moneda sistema", S, 'solicitud.IdOperacionSpot != null && !solicitud.IdOperacionSpot.equals(0L) ? solicitud.MontoMonedaSistema', 'false ? solicitud.MontoMonedaSistema'),
    ("FM-CER-17 APOREINV no conserva el costo UF", S, 'solicitud.CostoUF = ("APOREINV".equals(solicitud.IdTipoMovimientoFondoMutuo)) ? solicitud.CostoUF : r.CostoEnFactorAjuste;', "solicitud.CostoUF = r.CostoEnFactorAjuste;"),
    ("FM-CER-18 la fecha efectiva es la menor", R, "return entrada.compareTo(sistema) < 0 ? sistema : entrada;", "return entrada.compareTo(sistema) < 0 ? entrada : sistema;"),
    ("FM-CER-19 orden descendente", R, ".compareTo(b.IdSolicitudAporteRescate) == 1 ? 1 : -1);", ".compareTo(b.IdSolicitudAporteRescate) == 1 ? -1 : 1);"),
    ("FM-CER-19 el rescate pendiente de custodia (ADV) no entra a las listas", R, '"PEN".equals(s.EstadoProceso) || "ADV".equals(s.EstadoProceso)', '"PEN".equals(s.EstadoProceso)'),
    ("FM-CER-20 el indicador por defecto es S", "CertificarAporteRescateContrato.java", 'public String ValorIndicador = "N";', 'public String ValorIndicador = "S";'),
    ("FM-CER-20 gana el ultimo indicador del portafolio", S, "filter(x -> x.IdEntidad.equals(aporte.IdPortafolio)).findFirst()", "filter(x -> x.IdEntidad.equals(aporte.IdPortafolio)).reduce((u, w) -> w)"),
    ("FM-CER-23 la comision se formatea con la moneda de inversion", R, "s.MontoComision = formato.formatear(s.MontoComision, FormatoMoneda.MONTO, s.IdMonedaFondo);", "s.MontoComision = formato.formatear(s.MontoComision, FormatoMoneda.MONTO, s.IdMoneda);"),
    ("FM-CER-23 el redondeo del monto del sistema se omite", R, "s.MontoMonedaSistema = formato.formatear(s.MontoMonedaSistema, FormatoMoneda.MONTO, s.IdMonedaSistema);", ""),
    ("el seam recibe una copia sin las solicitudes rechazadas por F-A", S, "ctx.solicitudes = solicitudes;", "ctx.solicitudes = solicitudes.stream().filter(x -> !\"RZD\".equals(x.EstadoProceso)).collect(Collectors.toList());"),
    ("la fachada traga la parada del seam", S, "return posteriores.continuar(ctx);", "try { return posteriores.continuar(ctx); } catch (RuntimeException e) { return out; }"),
    ("F-A / F-B escriben el log aunque haya validas", S, "        // F-B\n", "        insertarLog(solicitudes, entrada, fechaSistema);\n        // F-B\n"),
]


def sin_procedencia(d):
    """the dump as the equivalence tests compare it: no provenance line; an exception reduced to its class unless it is a plain java.lang.Exception (then the message counts)"""
    d = dict(d)
    d.pop("_provenance", None)
    e = d.get("excepcion")
    if e and ":" in e and e.split(":")[0] != "Exception":
        d["excepcion"] = e.split(":")[0]
    return d


def lf(texto):
    """the legacy sources have CRLF line ends: the mutations are written with LF"""
    return texto.replace(chr(13) + chr(10), chr(10))


def modo_legacy(filtro):
    original = lf(CER.read_text(encoding="utf-8", errors="replace"))
    total = rojas = 0
    for nombre, archivo, viejo, nuevo in LEGACY:
        if filtro and filtro not in nombre:
            continue
        if viejo not in original:
            print("%-80s: NOT APPLICABLE (text not found)" % nombre)
            continue
        with tempfile.TemporaryDirectory() as tmp:
            tmp = pathlib.Path(tmp)
            (tmp / "overlay").mkdir()
            (tmp / "overlay" / archivo.name).write_text(original.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
            env = dict(os.environ, LEGACY_OVERLAY=str(tmp / "overlay"), ORACLE_OUT=str(tmp / "out"), ORACLE_REPEAT="1", ORACLE_REPORT=str(tmp / "rep.txt"), ORACLE_WORK=str(tmp / "work"),
                       PYTHONUTF8="1")
            (tmp / "work").mkdir()
            r = subprocess.run([BASH, str(BASE / "legacy-oracle" / "record_cer.sh"), "FMM-CER"], env=env, capture_output=True, text=True)
            if not (tmp / "out").exists():
                print("%-80s: ORACLE FAILED (the mutant does not compile?)\n%s" % (nombre, (r.stdout[-300:] + r.stderr[-300:])))
                continue
            cambiados = []
            for g in sorted((BASE / "cases-cer").glob("FMM-CER-*")):
                dump = tmp / "out" / (g.name + ".json")
                if not dump.exists():
                    cambiados.append(g.name)
                    continue
                a = sin_procedencia(json.loads((g / "expected.json").read_text(encoding="utf-8")))
                b = sin_procedencia(json.loads(dump.read_text(encoding="utf-8")))
                if a != b:
                    cambiados.append(g.name)
            total += 1
            rojas += 1 if cambiados else 0
            print("%-80s: %d golden(s) changed  e.g. %s" % (nombre, len(cambiados), ", ".join(c[8:48] for c in cambiados[:2])), flush=True)
    print("legacy mutations that change at least one golden: %d of %d" % (rojas, total))


def modo_nuevo(copia, filtro):
    copia = pathlib.Path(copia).resolve()
    real = (WS / "modernized").resolve()
    if real in copia.parents or copia == real:
        sys.exit("refusing to mutate or run Maven inside the real modernized/ tree: pass a COPY outside the repository")
    modulo = copia / NUEVO_DIR
    if not modulo.exists():
        sys.exit("not found: " + str(modulo))
    total = rojas = 0
    for nombre, archivo, viejo, nuevo in NUEVO:
        if filtro and filtro not in nombre:
            continue
        f = (modulo / archivo).resolve()
        original = f.read_text(encoding="utf-8")
        if viejo not in original:
            print("%-80s: NOT APPLICABLE (text not found in %s)" % (nombre, archivo))
            continue
        try:
            f.write_text(original.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
            r = subprocess.run([MVN, "-o", "-q", "test", "-Dtest=CerEquivalenciaTest,CerReglasTest,CerCompuertaTest"], cwd=str(copia / "BcsFondoMutuoServicios"), capture_output=True, text=True)
        finally:
            f.write_text(original, encoding="utf-8", newline="\n")
        salida = r.stdout + r.stderr
        compila = "COMPILATION ERROR" not in salida
        rojo = r.returncode != 0 and compila
        total += 1
        rojas += 1 if rojo else 0
        print("%-80s: %s" % (nombre, "RED (tests fail)" if rojo else ("DOES NOT COMPILE" if not compila else "GREEN: NO TEST FAILS (hole)")), flush=True)
    print("new-code mutations that turn at least one test red: %d of %d" % (rojas, total))


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "legacy":
        modo_legacy(sys.argv[2] if len(sys.argv) > 2 else "")
    elif len(sys.argv) >= 3 and sys.argv[1] == "nuevo":
        modo_nuevo(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else "")
    else:
        sys.exit(__doc__)
