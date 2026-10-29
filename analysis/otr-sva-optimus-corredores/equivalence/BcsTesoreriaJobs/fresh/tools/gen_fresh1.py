"""FRESH inputs (not used by development): CM-X*, PD-X*. Run: py -3 gen_fresh.py  (writes into the scratch copy's cases/)."""
import sys
sys.path.insert(0, r"T:\otr\BcsTesoreriaJobs\src\test\resources\tesoreria-jobs\legacy-oracle\tools")
import gen_cm1 as cm
import gen_pd as pd
from gen_cm1 import MSG, MODULOS, SALIDA, local

VENT = "UPDATE \"BSCL.Plataforma.BD::BAT.RangoHorario\" SET \"HoraInicio\" = '%s', \"HoraFin\" = '%s';"


def generar():
    def c(cid, desc, **o):  # the window-only cases also carry a distinct user and record count, so the output differs from every development case
        n = int(cid[-2:])
        if n in (1, 2, 3, 4, 5, 6, 11):
            o.setdefault("parametros", {})["IdUsuario"] = str(300 + n)
            o.setdefault("colaboradores", {}).setdefault("CargaMasivaIngresoEgresoDAPFFMM", SALIDA(P_OUT_REGISTROSINGRESADOSCONTADOR=40 + n)["CargaMasivaIngresoEgresoDAPFFMM"])
        cm.c(cid, desc, **o)
    c("CM-X01", "FRESH local 19:59:58 (two seconds before the default end) -> inside", reloj=local("19:59:58"))
    v = VENT % ("09:30:15", "17:45:45")
    c("CM-X02", "FRESH window 09:30:15-17:45:45, local 17:45:44 -> inside", seed=v, reloj=local("17:45:44"))
    c("CM-X03", "FRESH window 09:30:15-17:45:45, local 17:45:45 (end exact) -> outside", seed=v, reloj=local("17:45:45"))
    c("CM-X04", "FRESH window 09:30:15-17:45:45, local 09:30:15 (start exact) -> outside", seed=v, reloj=local("09:30:15"))
    c("CM-X05", "FRESH window 09:30:15-17:45:45, local 09:30:16 -> inside", seed=v, reloj=local("09:30:16"))
    c("CM-X06", "FRESH one-second-wide window 12:00:00-12:00:02, local 12:00:01 -> inside",
      seed=VENT % ("12:00:00", "12:00:02"), reloj=local("12:00:01"))
    c("CM-X07", "FRESH offset -14400, UTC 12:00:01 = local 08:00:01 -> inside; year-end previous day", reloj="2025-12-31 12:00:01",
      config={"PLATAFORMA/PLA.SegundosZonaHoraria": "-14400", "PLATAFORMA/PLA.FechaSistema": "2025-12-31"},
      colaboradores={"ObtenerDiaAnterior": {"salida": {"fechas": [{"fechaCalculada": "2025-12-30"}]}}},
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"FechaSistema\" = '2025-12-30';")
    c("CM-X08", "FRESH maximum counters: 2147483647 records, Casilla S, IdEmpresa 2, IdUsuario 99999", parametros={"EnviarArchivoACasilla": "S", "IdEmpresa": "2", "IdUsuario": "99999"},
      colaboradores=SALIDA(P_OUT_REGISTROSINGRESADOSCONTADOR=2147483647),
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"IdEmpresa\" = 2;")
    c("CM-X09", "FRESH FFMM, 3 errors nothing loaded, decimals and accents in messages, unusual order of tipos",
      parametros={"TipoArchivoCarga": "FFMM"},
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=3, P_OUT_REGISTROSINGRESADOSCONTADOR=0,
                           P_OUT_RUTA_ARCHIVO="CARGA/CDB_Validacion_IngresoEgreso_FFMM_20250401_121530.csv",
                           P_OUT_MENSAJES=[MSG("ERROR", "Linea 12: monto 1.234,56 invalido (max 999.999,99)", 3), MSG("OK", "Linea 1", 1),
                                           MSG("ERROR", "Linea 7: cuota 0,0001 menor al minimo", 2), MSG("ERROR", "Fondo \u00c1\u00d1O 2025 cerrado", 9)]))
    c("CM-X10", "FRESH DAP, one record loaded and one error (success branch), file name of another day",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=1, P_OUT_REGISTROSINGRESADOSCONTADOR=1, P_OUT_RUTA_ARCHIVO="CARGA/CDB_Validacion_IngresoEgreso_DAP_20251231_235959.csv",
                           P_OUT_MENSAJES=[MSG("ERROR", "una", 1)]))
    c("CM-X11", "FRESH TES closed and CTA closed with another module OPEN between them, local 19:59:59",
      colaboradores=MODULOS(("TES", "CER"), ("ORD", "ABI"), ("CTA", "CER")), reloj=local("19:59:59"))
    c("CM-X12", "FRESH PAU.Ejecucion of the previous day is PEN, FFMM, window changed 07:00:00-23:59:59 local 23:59:58",
      seed=(VENT % ("07:00:00", "23:59:59")) + "\nUPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"Estado\" = 'PEN';",
      parametros={"TipoArchivoCarga": "FFMM"}, reloj="2025-04-02 02:59:58")

    p = pd.c
    p("PD-X01", "FRESH three messages in a non-sequential id order and decimals / accents in the text", mensajes=[
        pd.m("OK", "Distribuido 1.234.567,89 a cuenta 0012", 30), pd.m("ERROR", "Saldo -0,01 invalido \u00d1u\u00f1oa", 2), pd.m("ADVERTENCIA", "dif 0,50", 11)])
    p("PD-X02", "FRESH IdEmpresa 12 IdUsuario 9999 Simular S clock one second before the year end", reloj="2025-12-31 23:59:59",
      parametros={"IdEmpresa": "12", "IdUsuario": "9999", "Simular": "S"})
    p("PD-X03", "FRESH exactly one message with long id and a code of 30 chars", mensajes=[pd.m("OK", "unica", 2147483648, codigo="C" * 30)])
    p("PD-X04", "FRESH lower and upper mixed tipo plus empty description and a trailing-space code", mensajes=[
        pd.m("oK", "", 5, codigo="AB  "), pd.m("ERROR", "e1", 4), pd.m("Advertencia", "a", 6)])
    p("PD-X05", "FRESH 7 messages whose descriptions contain commas, tabs and a single quote", mensajes=[
        pd.m("OK", "a,b\tc'd %d" % i, i) for i in range(1, 8)])
