"""GenerarArchivoCargos cases, part 1: structure, parameters, outcome, FTP storage, failures (CG-01..CG-45)."""
from gen_common import PROGRAMA, escribir, merge

CAB = {"nombre": "BSCL.Plataforma.BD::FTP.Cabecera", "orden": "\"IdCabecera\"", "enmascarar": ["FechaCreacion"]}
DET = {"nombre": "BSCL.Plataforma.BD::FTP.Detalle", "orden": "\"IdDetalle\""}


def det(tipo, campo, dato, largo, dec=0, defecto="", orden=1, fmt=None):
    return {"IdFormatoArchivoDetalle": 1, "CodigoFormatoArchivo": "PAC", "TipoRegistro": tipo, "Campo": campo, "TipoDato": dato, "Largo": largo,
            "Decimales": dec, "ValorDefecto": defecto, "Orden": orden, "DescripcionFormatoDetalle": "d", "Formato": fmt}


def formato(**o):
    f = {"IdFormatoArchivo": 8, "CodigoFormatoArchivo": "PAC_CARGOS", "IdBanco": 10, "NombreBanco": "BANCO FICTICIO", "IdMoneda": "CLP", "IdMedioDePago": "TRA",
         "FormatoSalida": "TXT", "Descripcion": "Cargos PAC", "Servicio": "SP_CARGOS_PAC", "RutaTesoreria": "tes/", "RutaCompleta": "${RUTA_BASE}/tes/",
         "CantidadDeTiposDeRegistro": 2, "NombreArchivoSalida": "CARGOS", "SeparadorCampos": "|", "SeparadorDecimales": ".", "RellenarCampo": "S",
         "Detalles": [det(1, "Rut", "CHAR", 10), det(1, "Fecha", "DATE", 8, fmt="yyyyMMdd"), det(2, "Cuenta", "NUM", 8), det(2, "pac-monto-cargo", "DEC", 12, 2)]}
    return merge(f, o)


def proc(*sets):
    return {"resultados": [{"columnas": c, "filas": f} for c, f in sets]}


DOS = proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "11111111-1", "2025-04-01"]]),
           (["TipoRegistroOptimus", "Cuenta", "pac-monto-cargo"], [["2", "12345", "1500.50"], ["2", "987", "20.5"]]))

BASE = {"job": "Cargos", "idPrograma": 900, "dirs": ["tes"], "reloj": "2025-04-01 15:15:30",
        "parametros": {"IdEmpresa": "1", "IdUsuario": "21", "DiaDescuento": "0", "FechaCargo": "2025-04-05"},
        "secuencias": {"BSCL.Plataforma.BD.Secuencias::Detalle": 500, "BSCL.Plataforma.BD.Secuencias::Cabecera": 70},
        "colaboradores": {"ConsultarFormatoArchivo": {"formatos": [formato()]}, "procedimiento": DOS},
        "tablas": [PROGRAMA, CAB, DET]}


def c(cid, desc, seed=None, fmt=None, proc_=None, **over):
    caso = merge(BASE, over)
    if fmt is not None:
        caso["colaboradores"]["ConsultarFormatoArchivo"] = {"formatos": [formato(**fmt)]}
    if proc_ is not None:
        caso["colaboradores"]["procedimiento"] = proc_
    caso["descripcion"] = desc
    escribir(cid, caso, seed)


def generar():
    c("CG-01", "happy path TXT: two record types, FTP header + lines, RutaSalida, COMMIT")
    c("CG-02", "CSV output: ';' separated, no forced quotes, IdCodigoTipo CARCSV", fmt={"FormatoSalida": "CSV"})
    c("CG-03", "DiaDescuento 5 -> ActivarMandatosArchivoCargo(5) after the FTP inserts", parametros={"DiaDescuento": "5"})
    c("CG-04", "DiaDescuento 0 -> mandates not activated")
    c("CG-05", "DiaDescuento negative -> not activated", parametros={"DiaDescuento": "-2"})
    c("CG-06", "DiaDescuento 1 is the boundary of > 0", parametros={"DiaDescuento": "1"})
    c("CG-07", "FTP rows: ids from the sequences (Detalle 500.., Cabecera 70), cabecera PEN, user of the format = IdUsuario param",
      parametros={"IdUsuario": "77"})
    c("CG-08", "FormatoSalida DAT: IdCodigoTipo CARTXT but Archivo rejects the extension", fmt={"FormatoSalida": "DAT"})
    c("CG-09", "FormatoSalida lower case 'txt' is not TXT nor CSV -> rejected", fmt={"FormatoSalida": "txt"})
    c("CG-10", "ConsultarFormatoArchivo answers an empty list -> IndexOutOfBoundsException", colaboradores={"ConsultarFormatoArchivo": {"formatos": []}})
    c("CG-11", "CantidadDeTiposDeRegistro 0 -> nothing generated, return true (COMMIT), no file", fmt={"CantidadDeTiposDeRegistro": 0})
    c("CG-12", "CantidadDeTiposDeRegistro null -> NullPointerException", fmt={"CantidadDeTiposDeRegistro": None})
    c("CG-13", "procedure without any result set -> getService null -> NullPointerException (legacy defect)", proc_={"resultados": []})
    c("CG-14", "procedure with an empty result set -> empty file, header stored with 0 lines",
      proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [])))
    c("CG-15", "three record types -> 7 placeholders in the CALL", fmt={"CantidadDeTiposDeRegistro": 3})
    c("CG-16", "one record type -> 5 placeholders", fmt={"CantidadDeTiposDeRegistro": 1})
    c("CG-17", "procedure throws -> ERROR, nothing persisted", colaboradores={"procedimiento.lanza": "procedimiento caido"})
    c("CG-18", "FechaCargo missing -> bound as null string", parametros={"FechaCargo": None})
    c("CG-19", "DiaDescuento missing -> NumberFormatException", parametros={"DiaDescuento": None})
    c("CG-20", "DiaDescuento not a number -> NumberFormatException", parametros={"DiaDescuento": "cinco"})
    c("CG-21", "IdEmpresa missing -> NumberFormatException", parametros={"IdEmpresa": None})
    c("CG-22", "IdUsuario missing -> NumberFormatException", parametros={"IdUsuario": None})
    c("CG-23", "destination folder does not exist -> FileNotFoundException, ERROR", dirs=[])
    c("CG-24", "ConsultarFormatoArchivo throws -> ERROR", colaboradores={"ConsultarFormatoArchivo.lanza": "lanza:Exception:formato no disponible"})
    c("CG-25", "ActivarMandatos throws after the FTP inserts -> ERROR, all rolled back", parametros={"DiaDescuento": "3"},
      colaboradores={"ActivarMandatosArchivoCargo.lanza": "lanza:RuntimeException:mandatos no activables"})
    c("CG-26", "SeparadorCampos null -> the text 'null' is appended after every value of a TXT line", fmt={"SeparadorCampos": None})
    c("CG-27", "SeparadorCampos empty -> values glued", fmt={"SeparadorCampos": ""})
    c("CG-28", "RellenarCampo N -> raw values, no padding", fmt={"RellenarCampo": "N"})
    c("CG-29", "RellenarCampo lower case 's' is not S -> no padding", fmt={"RellenarCampo": "s"})
    c("CG-30", "RellenarCampo null -> NullPointerException", fmt={"RellenarCampo": None})
    c("CG-31", "RutaTesoreria null -> RutaSalida starts with the text 'null'", fmt={"RutaTesoreria": None})
    c("CG-32", "NombreArchivoSalida null -> file name starts with 'null_'", fmt={"NombreArchivoSalida": None})
    c("CG-33", "200 rows -> 200 FTP lines, 200 sequence values", proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "%08d-K" % i, "2025-04-01"] for i in range(200)])))
    c("CG-34", "unicode values in a TXT file are written with the platform charset (UTF-8 here)",
      proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "Ñandú áé", "2025-04-01"]])))
    c("CG-35", "CSV value with quote, separator and line break", fmt={"FormatoSalida": "CSV", "RellenarCampo": "N"},
      proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "a\"b;c\nd", "2025-04-01"]])))
    c("CG-36", "TipoRegistroOptimus column missing -> record type stays 0 -> NO EXISTE FORMATO DETALLE",
      proc_=proc((["Rut", "Fecha"], [["11111111-1", "2025-04-01"]])))
    c("CG-37", "record type without a detail definition -> exception, an empty file is left behind",
      proc_=proc((["TipoRegistroOptimus", "Rut"], [["9", "x"]])))
    c("CG-38", "column without a detail definition -> exception",
      proc_=proc((["TipoRegistroOptimus", "Rut", "Otra"], [["1", "x", "y"]])))
    c("CG-39", "no details configured at all -> Detalles empty -> exception", fmt={"Detalles": []})
    c("CG-40", "TipoRegistroOptimus not numeric -> NumberFormatException",
      proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["uno", "x", "2025-04-01"]])))
    c("CG-41", "TXT file name: NombreArchivoSalida_yyyyMMdd_HHmmss.TXT (masked), RutaSalida = RutaTesoreria + name")
    c("CG-42", "program id 555 with its own BAT.Programa row", idPrograma=555, seed="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" VALUES (555, 'ANTES');")
    c("CG-43", "IdEmpresa 3 is copied into the format (procedure first argument)", parametros={"IdEmpresa": "3"})
    c("CG-44", "second result set empty, first with data", proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "1-9", "2025-04-01"]]),
                                                                    (["TipoRegistroOptimus", "Cuenta", "pac-monto-cargo"], [])))
    c("CG-45", "three result sets of which the middle one has several rows", fmt={"CantidadDeTiposDeRegistro": 3},
      proc_=proc((["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "1-9", "2025-04-01"]]),
                 (["TipoRegistroOptimus", "Cuenta", "pac-monto-cargo"], [["2", "1", "1.5"], ["2", "2", "2.5"], ["2", "3", "3.5"]]),
                 (["TipoRegistroOptimus", "Rut", "Fecha"], [["1", "2-7", "2025-04-02"]])))
