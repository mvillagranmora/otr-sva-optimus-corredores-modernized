"""BcsJobCargaMasivaIngresoEgresoDAPFFMM cases, part 2: service outcome, parameters, configuration, failures, files (CM-31..CM-60)."""
from gen_cm1 import MSG, SALIDA, c, local

PRG = "\"BSCL.Plataforma.BD::BAT.Programa\""


def generar():
    c("CM-31", "service: errors > 0 and nothing loaded -> error file with the ERROR messages joined by '. ', AgregarLog N",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=2, P_OUT_REGISTROSINGRESADOSCONTADOR=0,
                           P_OUT_MENSAJES=[MSG("ERROR", "Linea 1 invalida", 1), MSG("ERROR", "Linea 2 invalida", 2)]))
    c("CM-32", "service: only ERROR messages count; ADVERTENCIA / OK / null type are skipped",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=1, P_OUT_REGISTROSINGRESADOSCONTADOR=0,
                           P_OUT_MENSAJES=[MSG("ADVERTENCIA", "aviso", 1), MSG("ERROR", "falla", 2), MSG("OK", "bien", 3),
                                           {"id": 4, "tipo": None, "codigo": "X", "descripcion": "sin tipo"}, MSG("error", "minuscula", 5)]))
    c("CM-33", "service: errors > 0 but some records loaded -> success branch (file of the service, log S with the count)",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=2, P_OUT_REGISTROSINGRESADOSCONTADOR=5, P_OUT_MENSAJES=[MSG("ERROR", "x", 1)]))
    c("CM-34", "service: errors > 0, nothing loaded and no messages -> error file with an empty message",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=1, P_OUT_REGISTROSINGRESADOSCONTADOR=0))
    c("CM-35", "service: empty file path and no errors -> return false (ROLLBACK, no RutaSalida)",
      colaboradores=SALIDA(P_OUT_RUTA_ARCHIVO=""))
    c("CM-36", "service: errors > 0, nothing loaded and empty file path -> error branch wins",
      colaboradores=SALIDA(P_OUT_RUTA_ARCHIVO="", P_OUT_ERRORESCONTADOR=1, P_OUT_REGISTROSINGRESADOSCONTADOR=0, P_OUT_MENSAJES=[MSG("ERROR", "m", 1)]))
    c("CM-37", "service: zero records and zero errors with a path -> success log with 0 records",
      colaboradores=SALIDA(P_OUT_REGISTROSINGRESADOSCONTADOR=0))
    c("CM-38", "service: 1234567 records ingested -> count in the log text", colaboradores=SALIDA(P_OUT_REGISTROSINGRESADOSCONTADOR=1234567))
    c("CM-39", "service: null file path is not '' -> success branch, RutaSalida written as NULL",
      colaboradores=SALIDA(P_OUT_RUTA_ARCHIVO=None))
    c("CM-40", "service: 40 ERROR messages -> long joined text",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=40, P_OUT_REGISTROSINGRESADOSCONTADOR=0,
                           P_OUT_MENSAJES=[MSG("ERROR", "error numero %d" % i, i) for i in range(1, 41)]))
    c("CM-41", "error text with quote, separator, line break and accents in the one-line CSV (opencsv shim semantics)",
      colaboradores=SALIDA(P_OUT_ERRORESCONTADOR=1, P_OUT_REGISTROSINGRESADOSCONTADOR=0,
                           P_OUT_MENSAJES=[MSG("ERROR", "campo \"RUT\"; niño\nlinea 2 áé", 1)]))
    c("CM-42", "EnviarArchivoACasilla = S is passed to the service", parametros={"EnviarArchivoACasilla": "S"})
    c("CM-43", "EnviarArchivoACasilla missing -> null in the service input", parametros={"EnviarArchivoACasilla": None})
    c("CM-44", "IdEmpresa 3 / IdUsuario 77 reach every collaborator", parametros={"IdEmpresa": "3", "IdUsuario": "77"},
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"IdEmpresa\" = 3;")
    c("CM-45", "IdEmpresa missing -> NumberFormatException", parametros={"IdEmpresa": None})
    c("CM-46", "IdEmpresa not a number -> NumberFormatException", parametros={"IdEmpresa": "abc"})
    c("CM-47", "IdUsuario missing -> NumberFormatException", parametros={"IdUsuario": None})
    c("CM-48", "IdUsuario with spaces ' 21' -> NumberFormatException", parametros={"IdUsuario": " 21"})
    c("CM-49", "PLA.FechaSistema not configured -> legacy message", config={"PLATAFORMA/PLA.FechaSistema": None})
    c("CM-50", "PLA.PaisSistema not configured -> legacy message", config={"PLATAFORMA/PLA.PaisSistema": None})
    c("CM-51", "PLA.PaisSistema not numeric -> NumberFormatException", config={"PLATAFORMA/PLA.PaisSistema": "CL"})
    c("CM-52", "PLA.SegundosZonaHoraria not configured -> legacy message", config={"PLATAFORMA/PLA.SegundosZonaHoraria": None})
    c("CM-53", "PLA.SegundosZonaHoraria not numeric -> NumberFormatException", config={"PLATAFORMA/PLA.SegundosZonaHoraria": "UTC-3"})
    c("CM-54", "ConsultarEstadoModulo throws -> ERROR, rollback twice (job + BcsJob)",
      colaboradores={"ConsultarEstadoModulo.lanza": "lanza:RuntimeException:modulo no disponible"})
    c("CM-55", "ConsultarEstadoModulo answers a null module list -> NullPointerException",
      colaboradores={"ConsultarEstadoModulo": {"salida": {"p_out_ListaModulos": None}}})
    c("CM-56", "ObtenerDiaAnterior answers no dates -> IndexOutOfBoundsException",
      colaboradores={"ObtenerDiaAnterior": {"salida": {"fechas": []}}})
    c("CM-57", "ObtenerDiaAnterior throws -> ERROR", colaboradores={"ObtenerDiaAnterior.lanza": "lanza:RuntimeException:sin calendario"})
    c("CM-58", "CargaMasivaIngresoEgresoDAPFFMM throws -> ERROR, nothing persisted", colaboradores={"CargaMasivaIngresoEgresoDAPFFMM.lanza": "lanza:Exception:falla de carga"})
    c("CM-59", "getCabeceras throws -> ERROR", colaboradores={"getCabeceras.lanza": "lanza:SQLException:tabla FTP no existe"})
    c("CM-60", "AgregarLog throws after the RutaSalida update -> ERROR, the update is rolled back",
      colaboradores={"AgregarLog.lanza": "lanza:RuntimeException:log no disponible"},
      seed="UPDATE \"BSCL.Plataforma.BD::PAU.Ejecucion\" SET \"Estado\" = 'PEN';")
    c("CM-61", "no CARGA directory: the error file cannot be written, RutaSalida becomes the empty string", dirs=[],
      colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-62", "PLA.RutaBaseArchivos without trailing slash: base + relative folder is concatenated as is -> folder not found",
      config={"PLATAFORMA/PLA.RutaBaseArchivos": "${RUTA_BASE}"}, colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-63", "TES.RutaSalidaArchivosCargaMasiva not configured -> legacy message", config={"TESORERIA/TES.RutaSalidaArchivosCargaMasiva": None},
      colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-64", "other program id 555: only that BAT.Programa row is updated", idPrograma=555,
      seed="INSERT INTO " + PRG + " VALUES (555, 'ANTES');", colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-65", "program id without a BAT.Programa row: the update matches nothing, the run still commits", idPrograma=777,
      colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-66", "error file name uses the LOCAL time of the run (12:05:09 -> 120509), not the UTC one", reloj=local("12:05:09"),
      colaboradores={"cabeceras": {"PRO": 0, "PEN": 0}})
    c("CM-67", "outside the window: error file + AgregarLog with the 'fuera de horario' text", reloj=local("21:00:00"))
    c("CM-68", "FFMM + closed module: file name carries FFMM", parametros={"TipoArchivoCarga": "FFMM"},
      colaboradores={"ConsultarEstadoModulo": {"salida": {"p_out_ListaModulos": [{"NombreModulo": "TES", "IdEstadoModulo": "CER"}]}}})
