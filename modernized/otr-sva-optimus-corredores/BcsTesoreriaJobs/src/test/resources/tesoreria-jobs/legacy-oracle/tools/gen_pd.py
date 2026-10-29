"""BcsJobProcesarDistribucion cases (PD-01..PD-36)."""
from gen_common import PROGRAMA, escribir, merge


def m(tipo, desc, i=1, codigo=None):
    return {"id": i, "tipo": tipo, "codigo": codigo or "COD%d" % i, "descripcion": desc}


BASE = {"job": "ProcesarDistribucion", "idPrograma": 900, "dirs": ["tes"], "reloj": "2025-04-01 10:15:30",
        "parametros": {"IdEmpresa": "1", "IdUsuario": "21", "Simular": "N"},
        "colaboradores": {"ProcesarDistribucion": {"salida": {"P_OUT_ERRORESCONTADOR": 1, "P_OUT_MENSAJES": [
            m("ERROR", "Mandato sin cuenta", 1), m("OK", "Procesado", 2), m("ADVERTENCIA", "Monto distinto", 3)]}}},
        "tablas": [PROGRAMA]}


def c(cid, desc, seed=None, mensajes=None, **over):
    caso = merge(BASE, over)
    if mensajes is not None:
        caso["colaboradores"]["ProcesarDistribucion"] = {"salida": {"P_OUT_MENSAJES": mensajes}}
    caso["descripcion"] = desc
    escribir(cid, caso, seed)


def generar():
    c("PD-01", "happy path: CSV with BOM, header and one row per message, RutaSalida, two commits (job + flag reset), COMMIT")
    c("PD-02", "no messages: header row only", mensajes=[])
    c("PD-03", "Simular missing -> null reaches the service", parametros={"Simular": None})
    c("PD-04", "Simular S", parametros={"Simular": "S"})
    c("PD-05", "Simular empty string", parametros={"Simular": ""})
    c("PD-06", "IdEmpresa missing -> NumberFormatException before anything runs", parametros={"IdEmpresa": None})
    c("PD-07", "IdUsuario missing -> NumberFormatException", parametros={"IdUsuario": None})
    c("PD-08", "IdEmpresa not a number", parametros={"IdEmpresa": "x1"})
    c("PD-09", "IdUsuario with leading zeros '007' -> 7", parametros={"IdUsuario": "007"})
    c("PD-10", "ProcesarDistribucion throws -> ERROR, rollback twice, nothing else called",
      colaboradores={"ProcesarDistribucion.lanza": "lanza:Exception:distribucion fallida"})
    c("PD-11", "ValidarMandatosPAC throws -> ERROR, rollback twice, no file",
      colaboradores={"ValidarMandatosPAC.lanza": "lanza:RuntimeException:mandatos no validables"})
    c("PD-12", "ValidaEjecucionProcesoRecPAC throws AFTER the first commit -> ERROR; the file and RutaSalida stay committed",
      colaboradores={"ValidaEjecucionProcesoRecPAC.lanza": "lanza:RuntimeException:flag no actualizable"})
    c("PD-13", "folder 'tes' missing -> FileNotFoundException, ERROR; the mandates were already validated", dirs=[])
    c("PD-14", "message with null tipo -> NullPointerException", mensajes=[m(None, "sin tipo", 1)])
    c("PD-15", "message with null id -> NullPointerException", mensajes=[{"id": None, "tipo": "OK", "codigo": "X", "descripcion": "sin id"}])
    c("PD-16", "tipo in lower case 'error' is written as is", mensajes=[m("error", "minuscula", 1)])
    c("PD-17", "null codigo and descripcion -> empty CSV fields (quoted fields only for non-null)",
      mensajes=[{"id": 1, "tipo": "OK", "codigo": None, "descripcion": None}])
    c("PD-18", "descripcion with quote, ';', line break and accents", mensajes=[m("ERROR", "monto \"1.000\"; cliente Ñandú\nsegunda linea áéí", 1)])
    c("PD-19", "100 messages", mensajes=[m("ERROR" if i % 3 == 0 else "OK", "mensaje %d" % i, i) for i in range(1, 101)])
    c("PD-20", "salida with a null message list -> NullPointerException", colaboradores={"ProcesarDistribucion": {"salida": {"P_OUT_MENSAJES": None}}})
    c("PD-21", "PLA.RutaBaseArchivos without trailing slash", config={"PLATAFORMA/PLA.RutaBaseArchivos": "${RUTA_BASE}"})
    c("PD-22", "PLA.RutaBaseArchivos with a double trailing slash: '//' is collapsed", config={"PLATAFORMA/PLA.RutaBaseArchivos": "${RUTA_BASE}//"})
    c("PD-23", "file name from the database clock 2025-12-31 23:59:59", reloj="2025-12-31 23:59:59")
    c("PD-24", "PLA.FechaSistema not configured -> FechaDB fails after the mandates were validated",
      config={"PLATAFORMA/PLA.FechaSistema": None})
    c("PD-25", "request rows and processed lists reach ValidarMandatosPAC as returned", colaboradores={"ProcesarDistribucion": {"salida": {
        "P_OUT_ERRORESCONTADOR": 2, "P_OUT_MENSAJES": [m("OK", "ok", 1)],
        "P_OUT_SOLICITUDES_PROCESADAS": [{"IdentServicio": "S1", "MontoDeposito": 1500.5}],
        "P_OUT_SOLICITUDES_NO_PROCESADAS": [{"IdentServicio": "S2", "MontoDeposito": 20}]}}})
    c("PD-26", "error counter of the service is ignored by the job", mensajes=[m("ERROR", "e", 1)],
      colaboradores={"ProcesarDistribucion": {"salida": {"P_OUT_ERRORESCONTADOR": 99, "P_OUT_MENSAJES": [m("ERROR", "e", 1)]}}})
    c("PD-27", "program 555 with its own row: only that row gets the RutaSalida", idPrograma=555, seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" VALUES (555, 'ANTES');")
    c("PD-28", "program id without a row: the update matches nothing, run commits", idPrograma=777)
    c("PD-29", "IdEmpresa 3: the flag reset still uses empresa 1 (hard-coded in the legacy)", parametros={"IdEmpresa": "3"})
    c("PD-30", "unicode in codigo", mensajes=[m("OK", "ok", 1, codigo="CÓDIGO-Ñ")])
    c("PD-31", "descripcion with leading and trailing spaces is not trimmed", mensajes=[m("OK", "  espacios  ", 1)])
    c("PD-32", "very long descripcion (2000 characters)", mensajes=[m("OK", "x" * 2000, 1)])
    c("PD-33", "message ids large (Long)", mensajes=[m("OK", "grande", 9223372036854775807)])
    c("PD-34", "tipo 'ERROR' and 'Error' both counted by equalsIgnoreCase but written as is", mensajes=[m("ERROR", "a", 1), m("Error", "b", 2)])
    c("PD-35", "empty codigo and descripcion strings", mensajes=[{"id": 1, "tipo": "OK", "codigo": "", "descripcion": ""}])
    c("PD-36", "descripcion that looks like a formula '=1+1' is written verbatim", mensajes=[m("OK", "=1+1", 1)])
