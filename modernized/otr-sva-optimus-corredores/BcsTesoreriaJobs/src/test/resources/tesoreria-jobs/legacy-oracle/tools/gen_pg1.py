"""GenerarArchivoPago cases, part 1: format lookup, parameters, outputs (TXT / CSV / DAT), file creation, nomina update, failures (PG-01..PG-45)."""
from gen_pgbase import DET1, PROC1, fila_detalle, fila_formato, p, proc

R2 = proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "11111111-1", "1500.50"]]))


def generar():
    p("PG-01", "happy path TXT generic: lines with '|' and CRLF (TipoSaltoLinea W), NombreArchivo of the nomina, RutaSalida")
    p("PG-02", "TipoSaltoLinea L -> LF line ends", formatos=[fila_formato(TipoSaltoLinea="L")])
    p("PG-03", "TipoSaltoLinea null -> NullPointerException is swallowed inside Archivo.escribir: no line is written (legacy defect)",
      formatos=[fila_formato(TipoSaltoLinea=None)])
    p("PG-04", "TipoSaltoLinea 'X' (neither W nor L) -> LF", formatos=[fila_formato(TipoSaltoLinea="X")])
    p("PG-05", "FormatoSalida CSV: ';' separated, quotes only when needed",
      formatos=[fila_formato(FormatoSalida="CSV")])
    p("PG-06", "FormatoSalida txt (lower case) is TXT too", formatos=[fila_formato(FormatoSalida="txt")])
    p("PG-07", "FormatoSalida DAT is TXT too", formatos=[fila_formato(FormatoSalida="DAT")])
    p("PG-08", "FormatoSalida XLS: no branch writes anything, empty file, still updates nomina and RutaSalida",
      formatos=[fila_formato(FormatoSalida="XLS")])
    p("PG-09", "no format row -> constructor swallows the error -> NullPointerException on CantidadDeTiposDeRegistro", formatos=[])
    p("PG-10", "format inactive (IdEstado INA) is not found", formatos=[fila_formato(IdEstado="INA")])
    p("PG-11", "two active formats: the LAST row read wins and its details are loaded",
      formatos=[fila_formato(NombreArchivoSalida="PRIMERO"), fila_formato(IdFormatoArchivo=6, NombreArchivoSalida="SEGUNDO")],
      detalles=[fila_detalle(1, "Rut", "CHAR", 10, idf=6), fila_detalle(1, "Monto", "DEC", 12, 2, idf=6, orden=2)])
    p("PG-12", "format of another bank / currency / medio is not selected", formatos=[fila_formato(IdBanco=99)])
    p("PG-13", "CantidadDeTiposDeRegistro 0 -> nothing generated, COMMIT, no file, no nomina update", formatos=[fila_formato(CantidadDeTiposDeRegistro=0)])
    p("PG-14", "no detail rows: every field formats to '' (exception swallowed in the constructor)", detalles=[])
    p("PG-15", "procedure without result sets -> 'NO EXISTE DATA' return, COMMIT, no file", proc_={"resultados": []})
    p("PG-16", "procedure with an empty result set -> empty file, nomina updated", proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [])))
    p("PG-17", "procedure throws -> ERROR (wrapped twice), nothing persisted", colaboradores={"procedimiento.lanza": "sp caido"})
    p("PG-18", "3 record types -> 8 placeholders in the CALL", formatos=[fila_formato(CantidadDeTiposDeRegistro=3)])
    p("PG-19", "bound parameters: empresa, nomina, format id, currency, medio de pago", parametros={"IdNominaTransferencia": "48"})
    p("PG-20", "IdEmpresa missing -> NumberFormatException (wrapped)", parametros={"IdEmpresa": None})
    p("PG-21", "IdNominaTransferencia missing -> NumberFormatException", parametros={"IdNominaTransferencia": None})
    p("PG-22", "IdBanco not a number -> NumberFormatException", parametros={"IdBanco": "x"})
    p("PG-23", "IdMoneda missing -> the text 'null' is searched: no format", parametros={"IdMoneda": None})
    p("PG-24", "IdMedioDePago with a quote -> SQL syntax error swallowed in the constructor -> NullPointerException (SQL injection shape)",
      parametros={"IdMedioDePago": "T'RA"})
    p("PG-25", "IdMoneda ' OR '1'='1 : the SQL is built by String.format, the injected condition matches the format",
      parametros={"IdMoneda": "X' OR '1'='1"})
    p("PG-26", "destination folder missing but its parent exists: mkdir creates it", dirs=[], formatos=[fila_formato(RutaCompleta="${RUTA_BASE}/nueva/")])
    p("PG-27", "destination two levels deep and missing: no folder, FileNotFoundException swallowed, no file, but nomina and RutaSalida are updated (legacy defect)",
      dirs=[], formatos=[fila_formato(RutaCompleta="${RUTA_BASE}/a/b/")])
    p("PG-28", "RutaCompleta without trailing slash: path and name are concatenated as is",
      formatos=[fila_formato(RutaCompleta="${RUTA_BASE}/tes")])
    p("PG-29", "nomina row of another empresa: the nomina update matches nothing (log NOK), run commits",
      extra="UPDATE \"BSCL.Tesoreria.BD::TES.NominaTransferencia\" SET \"IdEmpresa\" = 2;")
    p("PG-30", "nomina does not exist: update matches nothing",
      extra="DELETE FROM \"BSCL.Tesoreria.BD::TES.NominaTransferencia\";")
    p("PG-31", "NombreArchivoSalida null -> 'null_<ts>.TXT'", formatos=[fila_formato(NombreArchivoSalida=None)])
    p("PG-32", "RutaTesoreria null -> RutaSalida starts with 'null'", formatos=[fila_formato(RutaTesoreria=None)])
    p("PG-33", "SeparadorCampos null -> 'null' appended after each value", formatos=[fila_formato(SeparadorCampos=None)])
    p("PG-34", "SeparadorCampos empty", formatos=[fila_formato(SeparadorCampos="")])
    p("PG-35", "program id 555 with its own row", idPrograma=555, extra="INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" VALUES (555, 'ANTES');")
    p("PG-36", "300 rows", proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "%08d-K" % i, "%d.25" % i] for i in range(300)])))
    p("PG-37", "TipoRegistroOptimus not numeric -> NumberFormatException",
      proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [["x", "1-9", "1.5"]])))
    p("PG-38", "TipoRegistroOptimus column missing: record type 0 -> every field formats to ''",
      proc_=proc((["Rut", "Monto"], [["1-9", "1.5"]])))
    p("PG-39", "unicode in a TXT value", proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "Ñandúá", "1.5"]])))
    p("PG-40", "CSV value with quote, ';' and line break", formatos=[fila_formato(FormatoSalida="CSV", RellenarCampo="N")],
      proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "a\"b;c\nd", "1.5"]])))
    p("PG-41", "CSV: TipoRegistroOptimus not the first column -> index -1 -> ArrayIndexOutOfBoundsException",
      formatos=[fila_formato(FormatoSalida="CSV")], proc_=proc((["Rut", "TipoRegistroOptimus", "Monto"], [["1-9", "1", "1.5"]])))
    p("PG-42", "CSV: the last column of the array stays null (one element shorter than the columns)",
      formatos=[fila_formato(FormatoSalida="CSV")])
    p("PG-43", "column name compared ignoring case for TipoRegistroOptimus ('tiporegistrooptimus')",
      proc_=proc((["tiporegistrooptimus", "Rut", "Monto"], [["1", "1-9", "1.5"]])))
    p("PG-44", "second result set with another record type", formatos=[fila_formato(CantidadDeTiposDeRegistro=2)],
      detalles=DET1 + [fila_detalle(2, "Cta", "NUM", 6, orden=3)],
      proc_=proc((["TipoRegistroOptimus", "Rut", "Monto"], [["1", "1-9", "1.5"]]), (["TipoRegistroOptimus", "Cta"], [["2", "77"]])))
    p("PG-45", "RellenarCampo N: raw values", formatos=[fila_formato(RellenarCampo="N")])
