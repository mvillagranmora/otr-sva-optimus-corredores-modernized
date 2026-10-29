"""GA: GenerarArchivo (the generic file job): structure, TXT / CSV, directories and failures."""
from gen_fmt import *

H = ["TipoRegistroOptimus", "Cuenta", "Nombre", "Monto"]
F3 = [detalle("Cuenta", "NUM", 6), detalle("Nombre", "CHAR", 10), detalle("Monto", "DEC", 9, 2, orden=3)]
ROWS = [["1", "123", "Ana", "1500.5"], ["1", "45", "Beatriz Gonzalez", "20.75"]]


def gen_ga():
    def g(n, desc, seed, colab, **kw):
        caso_ga("GA-%02d" % n, desc, seed, colab, **kw)

    g(1, "TXT, three formatted fields, a separator after each field (also the last one), two rows", [formato(), *F3], proc(resultado(H, ROWS)))
    g(2, "TXT with no data rows in the result set: the file is created empty", [formato(), *F3], proc(resultado(H, [])))
    g(3, "TXT with two record types (header and detail): each is formatted with its own definition", [formato(tipos=2), detalle("Cuenta", "NUM", 6, tipo_registro=1),
      detalle("Cuenta", "CHAR", 8, tipo_registro=2), detalle("Nombre", "CHAR", 10, tipo_registro=2)],
      proc(resultado(["TipoRegistroOptimus", "Cuenta"], [["1", "9"]]), resultado(["TipoRegistroOptimus", "Cuenta", "Nombre"], [["2", "77", "Luis"], ["2", "78", "Marta"]])))
    g(4, "CSV without column names: semicolon separated, a value with a quote is quoted and doubled, a semicolon alone is not quoted", [formato(salida="CSV", rellenar="N"),
      detalle("Cuenta", "CHAR", 20), detalle("Nombre", "CHAR", 30), detalle("Monto", "CHAR", 20)],
      proc(resultado(H, [["1", "a;b", "di\"jo", "1"], ["1", "x", "y", "z"]])))
    g(5, "CSV with column names: the header REPLACES the first data row of the result set (the row is lost)", [formato(salida="CSV", agrega="S", rellenar="N"),
      detalle("Cuenta", "CHAR", 20), detalle("Nombre", "CHAR", 20), detalle("Monto", "CHAR", 20)], proc(resultado(["TipoRegistroOptimus", "Cuenta", "Nombre", "Monto"], ROWS + [["1", "7", "Zoe", "3"]])))
    g(6, "CSV with TipoRegistroOptimus NOT first: ArrayIndexOutOfBoundsException swallowed, the run still succeeds, the file stays open and empty",
      [formato(salida="CSV"), *F3], proc(resultado(["Cuenta", "Nombre", "Monto", "TipoRegistroOptimus"], [["1", "a", "2", "1"]])))
    g(7, "no format for the code: NullPointerException (the count is null)", [formato(codigo="OTRO")], proc(resultado(H, ROWS)))
    g(8, "format with zero record types: nothing is generated, the run succeeds", [formato(tipos=0), *F3], proc(resultado(H, ROWS)))
    g(9, "the procedure returns no result set: no file, the run succeeds", [formato(), *F3], proc())
    g(10, "the procedure fails: swallowed, no file, the run succeeds", [formato(), *F3], mezclar(proc(resultado(H, ROWS)), {"procedimiento.lanza": "fallo del procedimiento"}))
    g(11, "the directory is missing (one level): created", [formato(ruta="${RUTA_BASE}/nuevo/"), *F3], proc(resultado(H, ROWS)), dirs=())
    g(12, "two missing levels: mkdir fails, the file cannot be created, the write errors are swallowed; BAT.Programa gets a path that does not exist",
      [formato(ruta="${RUTA_BASE}/a/b/"), *F3], proc(resultado(H, ROWS)), dirs=())
    g(13, "unknown output format XLS: an empty file with that extension", [formato(salida="XLS"), *F3], proc(resultado(H, ROWS)))
    g(14, "lower case txt is not TXT: an empty file", [formato(salida="txt"), *F3], proc(resultado(H, ROWS)))
    g(15, "SeparadorCampos null: the text 'null' is appended after every field", [formato(sep=None), *F3], proc(resultado(H, ROWS)))
    g(16, "SeparadorCampos empty", [formato(sep=""), *F3], proc(resultado(H, ROWS)))
    g(17, "file name with spaces and accents", [formato(nombre="Saldos de Cuentas Ñandú"), *F3], proc(resultado(H, ROWS)))
    g(18, "SQL injection shape: the code is pasted into the query; two active formats match, the last row wins", [formato(id=1, codigo="A"), formato(id=2, codigo="B", servicio="SP_B", nombre="B"),
      detalle("Cuenta", "NUM", 6, id_formato=1), detalle("Cuenta", "CHAR", 3, id_formato=2)], proc(resultado(["TipoRegistroOptimus", "Cuenta"], [["1", "5"]])),
      p=con(PARAMS, CodigoFormatoArchivo="A' OR '1'='1"))
    g(19, "inactive format", [formato(estado="INA"), *F3], proc(resultado(H, ROWS)))
    g(20, "format of another company", [formato(empresa=2), *F3], proc(resultado(H, ROWS)))
    g(21, "IdEmpresa missing: NumberFormatException", [formato(), *F3], proc(resultado(H, ROWS)), p=con(PARAMS, IdEmpresa=None))
    g(22, "IdUsuario not numeric", [formato(), *F3], proc(resultado(H, ROWS)), p=con(PARAMS, IdUsuario="x"))
    g(23, "CodigoFormatoArchivo missing: the text null is searched, no format: NullPointerException", [formato(), *F3], proc(resultado(H, ROWS)), p=con(PARAMS, CodigoFormatoArchivo=None))
    g(24, "the program of the run is 901: p_IdPrograma and the BAT.Programa update use it", [formato(), *F3,
      "INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" (\"IdPrograma\", \"RutaSalida\", \"RutaLog\", \"Comentario\") VALUES (901, 'OTRA', 'OTRA-LOG', 'OTRO');"],
      proc(resultado(H, ROWS)), idPrograma=901)
    g(25, "the program row does not exist: the update changes nothing", [formato(), *F3], proc(resultado(H, ROWS)), idPrograma=777)
    g(26, "SQL NULL values in the data: formatted as empty values", [formato(), *F3], proc(resultado(H, [["1", None, None, None]])))
    g(27, "TipoRegistroOptimus not numeric: NumberFormatException swallowed, the file is left empty", [formato(), *F3], proc(resultado(H, [["x", "1", "a", "1"]])))
    g(28, "TipoRegistroOptimus NULL", [formato(), *F3], proc(resultado(H, [["1", "1", "a", "1"], [None, "2", "b", "2"]])))
    g(29, "service name with a quote and more record types: the call text", [formato(servicio="SP\"X", tipos=3), *F3], proc(resultado(H, ROWS)))
    g(30, "300 rows in TXT", [formato(), *F3], proc(resultado(H, [["1", str(i), "N%d" % i, "%d.5" % i] for i in range(300)])))
    g(31, "300 rows in CSV with column names", [formato(salida="CSV", agrega="S", rellenar="N"), *F3], proc(resultado(H, [["1", str(i), "N%d" % i, "%d.5" % i] for i in range(300)])))
    g(32, "accents and symbols in the data, TXT", [formato(rellenar="N"), *F3], proc(resultado(H, [["1", "1", "Ángel ñú €", "1"]])))
    g(33, "accents in CSV", [formato(salida="CSV", rellenar="N"), *F3], proc(resultado(H, [["1", "1", "Ángel ñú €", "1"]])))
    g(34, "AgregoNombreColumna S in a TXT file: no header", [formato(agrega="S"), *F3], proc(resultado(H, ROWS)))
    g(35, "CSV with column names and two result sets: each set loses its first row", [formato(salida="CSV", agrega="S", tipos=2, rellenar="N"),
      detalle("Cuenta", "CHAR", 5, tipo_registro=1), detalle("Cuenta", "CHAR", 5, tipo_registro=2), detalle("Nombre", "CHAR", 9, tipo_registro=2)],
      proc(resultado(["TipoRegistroOptimus", "Cuenta"], [["1", "A"], ["1", "B"]]), resultado(["TipoRegistroOptimus", "Cuenta", "Nombre"], [["2", "C", "c"], ["2", "D", "d"]])))
    g(36, "a column that is not in the format is written as an empty value", [formato(), detalle("Cuenta", "NUM", 6)], proc(resultado(H, ROWS)))
    g(37, "empty values with a default and a null default", [formato(), detalle("Cuenta", "CHAR", 6, defecto="ND"), detalle("Nombre", "CHAR", 6, defecto=None)], proc(resultado(H, [["1", "", ""]])))
    g(38, "RutaModulo and name go to BAT.Programa, not the full path", [formato(ruta_modulo="modulo/sub/"), *F3], proc(resultado(H, ROWS)))
    g(39, "the format file name has the extension of the output format in upper case", [formato(salida="CSV", nombre="REPORTE"), *F3], proc(resultado(H, ROWS)))
    g(40, "format details loaded in table order, not by Orden", [formato(), detalle("Monto", "CHAR", 5, orden=9), detalle("Cuenta", "CHAR", 5, orden=1)], proc(resultado(["TipoRegistroOptimus", "Cuenta", "Monto"], [["1", "a", "b"]])))


if __name__ == "__main__":
    gen_ga()
