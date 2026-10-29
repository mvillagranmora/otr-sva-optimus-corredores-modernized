"""F3 W2 canary of the goldens and of the tests of CargarValorCuota (plan F2 section 7 "mutation of goldens"; the instruction: break the code in one small way that matters,
confirm that at least one test goes red, restore). Two modes; nothing under legacy/ or under the real module is ever touched:

  py -3 -I canary_cvc.py legacy [text of the mutation name]
      one-line mutations of a COPY of the legacy CargarValorCuota.java (and of BcsUtils SQL.java, for the binder) are run through record_cvc.sh with LEGACY_OVERLAY and
      ORACLE_OUT in a temporary directory (the goldens are NOT rewritten) and every dump is compared with its golden (the _provenance line aside). A mutation that changes no
      golden is an EQUIVALENT mutant or a HOLE of the case set. Prints "<mutation>: <n> golden(s) changed".

  py -3 -I canary_cvc.py nuevo <copy of the module outside the repo> [text of the mutation name]
      one-line mutations of a candidate implementation of the production package cl.bcs.fondomutuo.valorcuota inside a COPY of the module (the real module is never touched;
      Maven is never run on it): after each mutation `mvn -o test -Dtest=CvcEquivalenciaTest,CvcReglasTest,CvcCompuertaTest,CvcAnfitrionTest` runs in the copy and the mutation must make at least
      one test fail. The old / new texts below are those of the throwaway validation implementation described in API_ASSUMED.md; against another implementation a text that is
      not found is reported as NOT APPLICABLE (adapt the text, do not read it as a pass).
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
CVC = LEG / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo" / "CargarValorCuota.java"
SQLJ = LEG / "Plataforma" / "JAVA" / "BcsUtils" / "src" / "cl" / "bcs" / "utils" / "SQL.java"
BASH = shutil.which("bash") or "C:/Program Files/Git/usr/bin/bash.exe"
MVN = shutil.which("mvn") or "mvn"

# (name, file, old text, new text)
LEGACY = [
    ("escribe tambien con p_SimularOperacion nulo", CVC, "if(_c.NO.equals(entrada.p_SimularOperacion)) {", 'if(!"S".equals(entrada.p_SimularOperacion)) {'),
    ("moneda se valida con n minuscula", CVC, '"N".equals(allFunds) && !valorCuota.IDMONEDA', '"N".equalsIgnoreCase(allFunds) && !valorCuota.IDMONEDA'),
    ("variacion HALF_EVEN -> HALF_UP", CVC, "resultado.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_EVEN)", "resultado.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_UP)"),
    ("variacion x100 -> x10", CVC, "multiply(new BigDecimal(100)).abs()", "multiply(new BigDecimal(10)).abs()"),
    ("fecha igual a la del sistema es futura", CVC, "fechaCierre.compareTo(fechaSistemaComparacion) == 1", "fechaCierre.compareTo(fechaSistemaComparacion) >= 0"),
    ("vigente = la fecha mas antigua", CVC, "valorCuota2.FechaVigencia.compareTo(valorCuota1.FechaVigencia) == 1 ? 1 : -1", "valorCuota2.FechaVigencia.compareTo(valorCuota1.FechaVigencia) == 1 ? -1 : 1"),
    ("dia anterior con un objeto por fila (D-F3A-18 corregido)", CVC, "InObtenerDiaAnterior.FechasIn FDA = new InObtenerDiaAnterior.FechasIn();\n\t\tfor(ValorCuota fecha : entrada.p_ValorCuota) {",
     "for(ValorCuota fecha : entrada.p_ValorCuota) {\n\t\t\tInObtenerDiaAnterior.FechasIn FDA = new InObtenerDiaAnterior.FechasIn();"),
    ("se aplica el FLOOR por decimales (D-F3A-17 corregido)", CVC, "resultado.ValorCuota = valorCuota.VALORCUOTA;", "resultado.ValorCuota = resultado.ValorCuota;"),
    ("formato OK sin distinguir mayusculas", CVC, "if(!_c.OK.equals(valorCuota.MENSAJEVALIDACION)) {", "if(!_c.OK.equalsIgnoreCase(valorCuota.MENSAJEVALIDACION)) {"),
    ("cabecera de log 159 -> 160", CVC, "log.IdCabeceraLog = 159L;", "log.IdCabeceraLog = 160L;"),
    ("el duplicado no se marca", CVC, "seriesDuplicadas.put(valorCuotaEntrada, new Object());", ""),
    ("modo ISIN sin distinguir mayusculas deja de serlo", CVC, 'if("S".equalsIgnoreCase(entrada.p_AllFunds)) {\n\t\t\tSQL sql = new SQL();', 'if("S".equals(entrada.p_AllFunds)) {\n\t\t\tSQL sql = new SQL();'),
    ("se invierte el orden: historico antes de actualizar la ultima carga", CVC, "actualizarUltimaCarga(con, valoresCuotaHistoricoInsertar);\n\t\t\tinsertarValorCuotaHistorico(con, valoresCuotaHistoricoInsertar);",
     "insertarValorCuotaHistorico(con, valoresCuotaHistoricoInsertar);\n\t\t\tactualizarUltimaCarga(con, valoresCuotaHistoricoInsertar);"),
    ("el log se escribe antes del vigente", CVC, "insertarValorCuota(con, valoresCuotaInsertar);\n\t\t\tagregarLog(con, entrada, valoresCuotaHistoricoInsertar);",
     "agregarLog(con, entrada, valoresCuotaHistoricoInsertar);\n\t\t\tinsertarValorCuota(con, valoresCuotaInsertar);"),
    ("el historico incluye las filas con error", CVC, "filter(valorCuota->esEstadoCorrecto(valorCuota.Estado)).collect(Collectors.toList());\n\t\tlogger.trace(\"FIN::CargarValorCuota.obtenerValoresCuotaHistoricosInsertar\");",
     "collect(Collectors.toList());\n\t\tlogger.trace(\"FIN::CargarValorCuota.obtenerValoresCuotaHistoricosInsertar\");"),
    ("lista vacia: contador 2", CVC, "out.P_OUT_ERRORESCONTADOR = 1L;", "out.P_OUT_ERRORESCONTADOR = 2L;"),
    ("el valor negativo se rechaza", CVC, "valorCuota.VALORCUOTA.compareTo(BigDecimal.ZERO) == 0", "valorCuota.VALORCUOTA.compareTo(BigDecimal.ZERO) <= 0"),
    ("el dia anterior es habil (diaHabil S)", CVC, 'ObtenerDiaAnterior.diaHabil = "N";', 'ObtenerDiaAnterior.diaHabil = "S";'),
    ("el binder del lote redondea HALF_UP (16 decimales)", SQLJ, "val = ((BigDecimal)val).setScale(16, RoundingMode.HALF_DOWN);", "val = ((BigDecimal)val).setScale(16, RoundingMode.HALF_UP);"),
    ("el binder del lote redondea a 8 decimales", SQLJ, "val = ((BigDecimal)val).setScale(16, RoundingMode.HALF_DOWN);", "val = ((BigDecimal)val).setScale(8, RoundingMode.HALF_DOWN);"),
]

NUEVO_DIR = "BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/valorcuota/"
NUEVO = [
    ("escribe con p_SimularOperacion distinto de S", "CargarValorCuotaService.java", 'if ("N".equals(entrada.p_SimularOperacion)) {', 'if (!"S".equals(entrada.p_SimularOperacion)) {'),
    ("el log falla y se propaga", "CargarValorCuotaService.java", "} catch (Exception e) {\n            // AgregarLog.java:239-241", "} catch (Error e) {\n            // AgregarLog.java:239-241"),
    ("se llama al log con la lista vacia", "CargarValorCuotaService.java", "if (valoresCuotas.isEmpty()) {\n            return;\n        }", ""),
    ("las ids se piden antes de actualizar la ultima carga", "CargarValorCuotaService.java",
     "escritura.actualizarUltimaCarga(historico);\n            List<Long> ids = secuencias.siguientes(SECUENCIA, (long) historico.size());",
     "List<Long> ids = secuencias.siguientes(SECUENCIA, (long) historico.size());\n            escritura.actualizarUltimaCarga(historico);"),
    ("vigente = la fecha mas antigua", "CargarValorCuotaService.java", "return v2.FechaVigencia.compareTo(v1.FechaVigencia) == 1 ? 1 : -1;", "return v2.FechaVigencia.compareTo(v1.FechaVigencia) == 1 ? -1 : 1;"),
    ("dia anterior con un objeto por fila", "CargarValorCuotaService.java", "ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn fda = new ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn();\n        for (ValorCuota fecha : entrada.p_ValorCuota) {",
     "for (ValorCuota fecha : entrada.p_ValorCuota) {\n            ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn fda = new ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn();"),
    ("variacion HALF_UP", "CargarValorCuotaService.java", "r.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_EVEN)", "r.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_UP)"),
    ("moneda se valida siempre", "CargarValorCuotaService.java", '"N".equals(allFunds) && !valorCuota.IDMONEDA', '!valorCuota.IDMONEDA'),
    ("el valor se trunca a los decimales de la serie", "CargarValorCuotaService.java", "r.ValorCuota = valorCuota.VALORCUOTA;", "r.ValorCuota = valorCuota.VALORCUOTA.setScale(decimales.intValue(), RoundingMode.FLOOR);"),
    ("el binder redondea HALF_UP", "../requisitos/SqlLote.java", "RoundingMode.HALF_DOWN", "RoundingMode.HALF_UP"),
    ("el existente se detecta (D-F3A-16 corregido)", "CargarValorCuotaService.java", "ValorCuotaVigente existente = existentes.get(clave.IdSerieFondo);",
     "ValorCuotaVigente existente = existentes.get(clave.IdSerieFondo + \"_\" + clave.FechaVigencia);"),
    ("el historico ignora el filtro de empresa del update (la fila de otra empresa pasa a N)", "CargarValorCuotaEscrituraJdbc.java", 'SqlRecursos.modulo("CargarValorCuota_UpdateValorCuotaUltimaCarga.sql");',
     'SqlRecursos.modulo("CargarValorCuota_UpdateValorCuotaUltimaCarga.sql").replace("AND \\"IdEmpresa\\" = :IdEmpresa", "");'),
    ("modo ISIN: la fecha de calculo no se lee de la fila (usa la del sistema)", "CargarValorCuotaService.java", "FechaLegacy.aFechaSql(valorCuota.FECHACALCULO)", "FechaLegacy.aFechaSql(fechaSistema)"),
    # EQUIVALENT mutant, kept as a note: ("ISIN sin entrada en el mapa -> getOrDefault") raises NullPointerException either way (the next null is FECHACIERRE) and the tests compare the class
    ("el contador es 1 con errores de requisitos", "CargarValorCuotaService.java", "out.P_OUT_ERRORESCONTADOR = ConteoErrores.errores(requisitos.p_out_Mensajes);", "out.P_OUT_ERRORESCONTADOR = Math.min(1L, ConteoErrores.errores(requisitos.p_out_Mensajes));"),
    ("la compuerta no cierra por defecto", "CargarValorCuotaJavaCallHandler.java", "if (!compuertas.listasServidas().contains(CargarValorCuotaConfiguracion.LISTA)) {", "if (false) {"),
    ("el log agrega las filas con error", "CargarValorCuotaService.java", "agregarLog(entrada, historico);", "agregarLog(entrada, out.P_OUT_RESULTADO);"),
]


def sin_procedencia(d):
    d = dict(d)
    d.pop("_provenance", None)
    return d


def lf(texto):
    """the legacy sources have CRLF line ends: the mutations are written with LF"""
    return texto.replace(chr(13) + chr(10), chr(10))


def modo_legacy(filtro):
    originales = {CVC: lf(CVC.read_text(encoding="utf-8", errors="replace")), SQLJ: lf(SQLJ.read_text(encoding="utf-8", errors="replace"))}
    total = rojas = 0
    for nombre, archivo, viejo, nuevo in LEGACY:
        if filtro and filtro not in nombre:
            continue
        original = originales[archivo]
        if viejo not in original:
            print("%-72s: NOT APPLICABLE (text not found)" % nombre)
            continue
        with tempfile.TemporaryDirectory() as tmp:
            tmp = pathlib.Path(tmp)
            (tmp / "overlay").mkdir()
            (tmp / "overlay" / archivo.name).write_text(original.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
            env = dict(os.environ, LEGACY_OVERLAY=str(tmp / "overlay"), ORACLE_OUT=str(tmp / "out"), ORACLE_REPEAT="1", ORACLE_REPORT=str(tmp / "rep.txt"),
                       ORACLE_WORK=str(tmp / "work"), PYTHONUTF8="1")
            (tmp / "work").mkdir()
            r = subprocess.run([BASH, str(BASE / "legacy-oracle" / "record_cvc.sh"), "FMM-CVC"], env=env, capture_output=True, text=True)
            if not (tmp / "out").exists():
                print("%-72s: ORACLE FAILED (the mutant does not compile?)\n%s" % (nombre, (r.stdout[-300:] + r.stderr[-300:])))
                continue
            cambiados = 0
            for g in sorted((BASE / "cases-cvc").glob("FMM-CVC-*")):
                dump = tmp / "out" / (g.name + ".json")
                if not dump.exists():
                    cambiados += 1
                    continue
                a = sin_procedencia(json.loads((g / "expected.json").read_text(encoding="utf-8")))
                b = sin_procedencia(json.loads(dump.read_text(encoding="utf-8")))
                if a != b:
                    cambiados += 1
            total += 1
            rojas += 1 if cambiados else 0
            print("%-72s: %d golden(s) changed" % (nombre, cambiados), flush=True)
    print("legacy mutations that change at least one golden: %d of %d" % (rojas, total))


def modo_nuevo(copia, filtro):
    copia = pathlib.Path(copia).resolve()
    real = (WS / "modernized").resolve()
    if real in copia.parents or copia == real:
        sys.exit("refusing to mutate or run Maven inside the real modernized/ tree: pass a COPY outside the repository")
    modulo = copia / NUEVO_DIR
    if not modulo.exists():
        sys.exit("no production package found in the copy: %s" % modulo)
    total = rojas = 0
    for nombre, archivo, viejo, nuevo in NUEVO:
        if filtro and filtro not in nombre:
            continue
        ruta = (modulo / archivo).resolve()
        if not ruta.exists():
            print("%-72s: NOT APPLICABLE (file %s not found)" % (nombre, archivo))
            continue
        original = ruta.read_text(encoding="utf-8")
        if viejo not in original:
            print("%-72s: NOT APPLICABLE (text not found)" % nombre)
            continue
        ruta.write_text(original.replace(viejo, nuevo, 1), encoding="utf-8", newline="\n")
        try:
            r = subprocess.run([MVN, "-o", "-q", "test", "-Dtest=CvcEquivalenciaTest,CvcReglasTest,CvcCompuertaTest,CvcAnfitrionTest", "-Dsurefire.failIfNoSpecifiedTests=false"],
                               cwd=str(copia / "BcsFondoMutuoServicios"), capture_output=True, text=True)
        finally:
            ruta.write_text(original, encoding="utf-8", newline="\n")
        salida = r.stdout + r.stderr
        if "COMPILATION ERROR" in salida:
            print("%-72s: DOES NOT COMPILE" % nombre)
            continue
        fallos = [l for l in salida.splitlines() if l.startswith("[ERROR] Tests run:") and "Failures: 0, Errors: 0" not in l]
        total += 1
        rojas += 1 if r.returncode != 0 else 0
        print("%-72s: %s" % (nombre, ("RED (%s)" % (fallos[-1].replace("[ERROR] ", "") if fallos else "build failed")) if r.returncode != 0 else "GREEN (HOLE: no test noticed)"), flush=True)
    print("implementation mutations that turn at least one test red: %d of %d" % (rojas, total))


def main():
    if len(sys.argv) < 2 or sys.argv[1] not in ("legacy", "nuevo"):
        sys.exit(__doc__)
    if sys.argv[1] == "nuevo":
        modo_nuevo(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else None)
    else:
        modo_legacy(sys.argv[2] if len(sys.argv) > 2 else None)


if __name__ == "__main__":
    main()
