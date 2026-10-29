"""FRESH inputs: CG-X* (GenerarArchivoCargos)."""
import sys
sys.path.insert(0, r"T:\otr\BcsTesoreriaJobs\src\test\resources\tesoreria-jobs\legacy-oracle\tools")
import gen_cg1 as cg
from gen_cg1 import det, proc

H = ["TipoRegistroOptimus", "Rut", "Fecha"]
M = ["TipoRegistroOptimus", "Cuenta", "pac-monto-cargo"]


def generar():
    c = cg.c
    c("CG-X01", "FRESH other bank, DEC 14,4 largest / zero / smallest amounts, ';' separator, TXT",
      fmt={"IdBanco": 15, "NombreBanco": "BANCO NUEVO", "SeparadorCampos": ";",
           "Detalles": [det(1, "Rut", "CHAR", 10), det(1, "Fecha", "DATE", 8, fmt="yyyyMMdd"), det(2, "Cuenta", "NUM", 8), det(2, "pac-monto-cargo", "DEC", 14, 4)]},
      proc_=proc((H, [["1", "76543210-K", "2025-12-31"]]), (M, [["2", "1", "99999999999.9999"], ["2", "22", "0.0000"], ["2", "333", "0.0001"]])),
      parametros={"DiaDescuento": "31", "FechaCargo": "2025-12-31", "IdUsuario": "1"})
    c("CG-X02", "FRESH CSV, DEC 12,2 boundaries 0.00 / 0.01 / 9999999999.99, RellenarCampo N",
      fmt={"FormatoSalida": "CSV", "RellenarCampo": "N"},
      proc_=proc((H, [["1", "1-9", "2024-02-29"]]), (M, [["2", "5", "0.00"], ["2", "6", "0.01"], ["2", "7", "9999999999.99"]])))
    c("CG-X03", "FRESH DATE dd-MM-yyyy: leap day, 2025-02-29 (lenient), year end, year start",
      fmt={"Detalles": [det(1, "Rut", "CHAR", 4), det(1, "Fecha", "DATE", 10, fmt="dd-MM-yyyy"), det(2, "Cuenta", "NUM", 4), det(2, "pac-monto-cargo", "DEC", 8, 2)]},
      proc_=proc((H, [["1", "A", "2024-02-29"], ["1", "B", "2025-02-29"], ["1", "C", "2025-12-31"], ["1", "D", "2025-01-01"]]), (M, [["2", "1", "1.00"]])))
    c("CG-X04", "FRESH unusual order: the type-2 result set comes first and the type-1 set second",
      proc_=proc((M, [["2", "42", "10.10"]]), (H, [["1", "55555555-5", "2025-06-30"]])))
    c("CG-X05", "FRESH DEC 5,2: 999.99 fits, 1000.00 and 12345.67 lose the leading digits",
      fmt={"Detalles": [det(1, "Rut", "CHAR", 4), det(1, "Fecha", "DATE", 8, fmt="yyyyMMdd"), det(2, "Cuenta", "NUM", 4), det(2, "pac-monto-cargo", "DEC", 5, 2)]},
      proc_=proc((H, [["1", "X", "2025-04-01"]]), (M, [["2", "1", "999.99"], ["2", "2", "1000.00"], ["2", "3", "12345.67"]])))
    c("CG-X06", "FRESH NUM width 8: zero, negative zero, already padded, exactly 8 digits, 9 digits",
      proc_=proc((H, [["1", "Z", "2025-04-01"]]), (M, [["2", "0", "1.5"], ["2", "-0", "1.5"], ["2", "00000012", "1.5"], ["2", "12345678", "1.5"], ["2", "123456789", "1.5"]])))
    c("CG-X07", "FRESH CHAR width 3 with multi-byte characters truncated, width 1 column, '||' separator",
      fmt={"SeparadorCampos": "||", "CantidadDeTiposDeRegistro": 1,
           "Detalles": [det(1, "Rut", "CHAR", 3), det(1, "Fecha", "CHAR", 1)]},
      proc_=proc((H, [["1", "\u00d1\u00d1\u00d1\u00d1", "xyz"], ["1", "a", ""]])))
    c("CG-X08", "FRESH RellenarCampo N with decimals 12.3 / 12.345 / 12.0 and CSV, DiaDescuento 30",
      fmt={"FormatoSalida": "CSV", "RellenarCampo": "N"}, parametros={"DiaDescuento": "30"},
      proc_=proc((H, [["1", "9", "2025-04-01"]]), (M, [["2", "1", "12.3"], ["2", "2", "12.345"], ["2", "3", "12.0"]])))
    c("CG-X09", "FRESH ',' separator, RutaTesoreria nested 'tes/sub/' (folder exists), empresa 7, user 123456",
      dirs=["tes", "tes/sub"], fmt={"SeparadorCampos": ",", "RutaTesoreria": "tes/sub/", "RutaCompleta": "${RUTA_BASE}/tes/sub/"}, parametros={"IdEmpresa": "7", "IdUsuario": "123456"})
    c("CG-X10", "FRESH 3 types, third set listed before first, 1 row each, IdBanco 99", fmt={"CantidadDeTiposDeRegistro": 3, "IdBanco": 99,
      "Detalles": [det(1, "Rut", "CHAR", 6), det(1, "Fecha", "DATE", 8, fmt="yyyyMMdd"), det(2, "Cuenta", "NUM", 5), det(2, "pac-monto-cargo", "DEC", 9, 3)]},
      proc_=proc((H, [["1", "9-9", "2025-03-01"]]), (M, [["2", "88", "0.999"]]), (H, [["1", "8-8", "2025-03-02"]])),
      parametros={"DiaDescuento": "2"})
    c("CG-X11", "FRESH CSV with a value that is only spaces and a leading-zero NUM", fmt={"FormatoSalida": "CSV"},
      proc_=proc((H, [["1", "   ", "2025-04-01"]]), (M, [["2", "0001", "0.10"]])))
    c("CG-X12", "FRESH 37 rows of two types alternating amounts with scale 2 and 4 digits",
      proc_=proc((H, [["1", "%d-%d" % (i, i % 10), "2025-04-%02d" % (i % 28 + 1)] for i in range(37)]),
                 (M, [["2", str(1000 + i), "%d.%02d" % (i * 137, (i * 7) % 100)] for i in range(37)])))
