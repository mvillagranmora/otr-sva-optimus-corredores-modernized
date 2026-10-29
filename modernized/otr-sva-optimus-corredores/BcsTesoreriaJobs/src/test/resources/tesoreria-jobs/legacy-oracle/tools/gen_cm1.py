"""BcsJobCargaMasivaIngresoEgresoDAPFFMM cases, part 1: guards, time window, outcome of the load (CM-01..CM-30)."""
from gen_common import PROGRAMA, escribir, merge

MODULOS = lambda *m: {"ConsultarEstadoModulo": {"salida": {"p_out_ListaModulos": [{"NombreModulo": n, "IdEstadoModulo": e} for n, e in m]}}}
SALIDA = lambda **s: {"CargaMasivaIngresoEgresoDAPFFMM": {"salida": merge({"P_OUT_ERRORESCONTADOR": 0, "P_OUT_RUTA_ARCHIVO": "CARGA/CDB_Validacion_IngresoEgreso_DAP_20250401_121530.csv",
                                                                       "P_OUT_REGISTROSINGRESADOSCONTADOR": 3, "P_OUT_MENSAJES": []}, s)}}
MSG = lambda t, d, i=1: {"id": i, "tipo": t, "codigo": "C" + str(i), "descripcion": d}

BASE = {
    "job": "CargaMasiva", "idPrograma": 900,
    "parametros": {"IdEmpresa": "1", "IdUsuario": "21", "TipoArchivoCarga": "DAP", "EnviarArchivoACasilla": "N"},
    "reloj": "2025-04-01 15:15:30", "dirs": ["CARGA"],
    "colaboradores": merge(merge({"cabeceras": {"PRO": 0, "PEN": 1}, "ObtenerDiaAnterior": {"salida": {"fechas": [{"fechaCalculada": "2025-03-31"}]}}},
                                 MODULOS(("TES", "ABI"), ("CTA", "ABI"))), SALIDA()),
    "tablas": [PROGRAMA],
}


def c(cid, desc, seed=None, **over):
    caso = merge(BASE, over)
    caso["descripcion"] = desc
    escribir(cid, caso, seed)


def local(h):  # the UTC instant whose local time (PLA.SegundosZonaHoraria = -10800) is h
    hh, mm, ss = map(int, h.split(":"))
    return "2025-04-01 %02d:%02d:%02d" % (hh + 3, mm, ss)


def generar():
    c("CM-01", "happy path: load OK, run output = file of the service, AgregarLog S, COMMIT")
    c("CM-02", "TES module closed -> error file, RutaSalida, return true, no AgregarLog", colaboradores=MODULOS(("TES", "CER"), ("CTA", "ABI")))
    c("CM-03", "CTA module closed -> CUENTA INVERSION message", colaboradores=MODULOS(("TES", "ABI"), ("CTA", "CER")))
    c("CM-04", "both closed -> both messages concatenated", colaboradores=MODULOS(("TES", "CER"), ("CTA", "CER")))
    c("CM-05", "estado 'cer' lower case is closed (equalsIgnoreCase)", colaboradores=MODULOS(("TES", "cer")))
    c("CM-06", "another module closed is ignored", colaboradores=MODULOS(("ORD", "CER"), ("TES", "ABI")))
    c("CM-07", "module name in lower case is not TES", colaboradores=MODULOS(("tes", "CER")))
    c("CM-08", "file already loaded (PRO headers) -> error file with the dictionary message", colaboradores={"cabeceras": {"PRO": 2, "PEN": 1}})
    c("CM-09", "no pending file -> error file SIN_ARCHIVO_PARA_CARGAR", colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-10", "previous business day PEN in PAU.Ejecucion -> message, error file, AgregarLog N",
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"Estado\" = 'PEN';")
    c("CM-11", "no PAU.Ejecucion row for the previous day -> default PEN", seed="DELETE FROM \"BSCL.Plataforma.BD::PAU.Ejecucion\";")
    c("CM-12", "PAU.Ejecucion with NULL Estado -> COALESCE gives PEN", seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"Estado\" = NULL;")
    c("CM-13", "PAU.Ejecucion with another Estado (ERR) is not PEN -> continues", seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"Estado\" = 'ERR';")
    c("CM-14", "PAU.Ejecucion of another empresa only -> default PEN", seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"IdEmpresa\" = 2;")
    c("CM-15", "previous business day taken from ObtenerDiaAnterior (2025-03-28)",
      colaboradores={"ObtenerDiaAnterior": {"salida": {"fechas": [{"fechaCalculada": "2025-03-28"}]}}},
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"FechaSistema\" = '2025-03-28';")
    c("CM-16", "no BAT.RangoHorario -> message, error file, AgregarLog N", seed="DELETE FROM \"BSCL.Plataforma.BD::BAT.RangoHorario\";")
    c("CM-17", "two BAT.RangoHorario rows: the first one is used",
      seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.RangoHorario\" VALUES ('CARGA_MASIVA_INGRESO_EGRESO_DAP_FFMM', '00:00:00', '01:00:00');")
    c("CM-18", "RangoHorario of another purpose only -> none configured",
      seed="UPDATE \"BSCL.Plataforma.BD::BAT.RangoHorario\" SET \"TipoProposito\" = 'OTRO';")
    c("CM-19", "local time 07:59:59 -> outside the window", reloj=local("07:59:59"))
    c("CM-20", "local time exactly the start 08:00:00 -> OUTSIDE (isAfter is exclusive)", reloj=local("08:00:00"))
    c("CM-21", "local time 08:00:01 -> inside", reloj=local("08:00:01"))
    c("CM-22", "local time 19:59:59 -> inside", reloj=local("19:59:59"))
    c("CM-23", "local time exactly the end 20:00:00 -> OUTSIDE (isBefore is exclusive)", reloj=local("20:00:00"))
    c("CM-24", "local time 23:59:59 -> outside", reloj=local("23:59:59"))
    c("CM-25", "window 22:00-06:00 crossing midnight: never inside",
      seed="UPDATE \"BSCL.Plataforma.BD::BAT.RangoHorario\" SET \"HoraInicio\" = '22:00:00', \"HoraFin\" = '06:00:00';", reloj=local("23:00:00"))
    c("CM-26", "positive time-zone offset +3600 moves the local time into the window", reloj="2025-04-01 06:30:00",
      config={"PLATAFORMA/PLA.SegundosZonaHoraria": "3600"})
    c("CM-27", "UTC after midnight: CURRENT_DATE is the UTC date 2025-04-02 while the local time is 23:00 of the 1st", reloj="2025-04-02 02:00:00",
      seed="UPDATE \"BSCL.Plataforma.BD::BAT.RangoHorario\" SET \"HoraFin\" = '23:30:00';\nUPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"FechaSistema\" = '2025-04-01';",
      colaboradores={"ObtenerDiaAnterior": {"salida": {"fechas": [{"fechaCalculada": "2025-04-01"}]}}})
    c("CM-28", "TipoArchivoCarga parameter missing -> return false (ROLLBACK, no log)", parametros={"TipoArchivoCarga": None})
    c("CM-29", "TipoArchivoCarga empty -> return false (ROLLBACK)", parametros={"TipoArchivoCarga": ""})
    c("CM-30", "FFMM load type: file names and log type use FFMM", parametros={"TipoArchivoCarga": "FFMM"},
      colaboradores=SALIDA(P_OUT_RUTA_ARCHIVO="CARGA/CDB_Validacion_IngresoEgreso_FFMM_20250401_121530.csv"))
