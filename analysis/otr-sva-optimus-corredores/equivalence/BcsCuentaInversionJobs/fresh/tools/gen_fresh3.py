"""FRESH inputs (verification step 4.2), part 3: GenerarArchivo structure (GA) and formatter (GF). Inputs only."""
from gen_fmt import *

H = ["TipoRegistroOptimus", "Cuenta", "Nombre", "Monto"]


def ga(n, desc, seed, colab, **kw):
    caso_ga("GA-F%02d" % n, "fresh: " + desc, seed, colab, **kw)


def gf(n, desc, tipo, largo, valor, decimales=0, defecto="", fmt=None, rellenar="S", sep_dec=".", sep="|"):
    seed = [formato(rellenar=rellenar, sep_dec=sep_dec, codigo="ARCHIVO_X", sep=sep), detalle("C", tipo, largo, decimales, defecto, formato_fecha=fmt)]
    caso_ga("GF-F%02d" % n, "fresh: " + desc, seed, proc(resultado(["TipoRegistroOptimus", "C"], [["1", valor]])))


def gen():
    F = [detalle("Cuenta", "NUM", 4), detalle("Nombre", "CHAR", 6), detalle("Monto", "DEC", 12, 3, orden=3)]
    ga(1, "TXT three record types, rows of type 3 and 1 in this order in one set", [formato(tipos=3, sep=";"), detalle("Cuenta", "NUM", 3, tipo_registro=1), detalle("Cuenta", "CHAR", 4, tipo_registro=2),
       detalle("Nombre", "CHAR", 5, tipo_registro=3)], proc(resultado(["TipoRegistroOptimus", "Cuenta", "Nombre"], [["3", "9", "zz"], ["1", "5", "q"]]), resultado(["TipoRegistroOptimus", "Cuenta"], [["2", "8"]])))
    ga(2, "CSV with header and a single data row (the row is replaced by the header)", [formato(salida="CSV", agrega="S", rellenar="N"), *F], proc(resultado(H, [["1", "1", "Solo", "1.5"]])))
    ga(3, "CSV without header, 1000 rows and a value with a line break and a quote", [formato(salida="CSV", rellenar="N"), *F],
       proc(resultado(H, [["1", str(i), "N%d" % i, "%d.25" % i] for i in range(999)] + [["1", "0", "l1\nl2 \"q\"", "0.0"]])))
    ga(4, "TXT, empty separator, no padding, decimals 3", [formato(sep="", rellenar="N"), *F], proc(resultado(H, [["1", "7", "Ana", "10.125"], ["1", "70", "Zoe", "0.5"]])))
    ga(5, "two result sets in TXT, the second empty", [formato(tipos=2), detalle("Cuenta", "NUM", 5, tipo_registro=1), detalle("Cuenta", "NUM", 5, tipo_registro=2)],
       proc(resultado(["TipoRegistroOptimus", "Cuenta"], [["1", "11"], ["1", "12"]]), resultado(["TipoRegistroOptimus", "Cuenta"], [])))
    ga(6, "company 3, other file name and module path", [formato(empresa=3, nombre="Sal_2024", ruta_modulo="mod/x/"), *F], proc(resultado(H, [["1", "5", "Lia", "2.5"]])), p=con(PARAMS, IdEmpresa="3"))
    ga(7, "CSV with header and 2 rows keeps only the second", [formato(salida="CSV", agrega="S", rellenar="N"), *F], proc(resultado(H, [["1", "1", "A", "1"], ["1", "2", "B", "2.50"]])))
    ga(8, "TXT: NUM column zero, DEC zero, CHAR with trailing spaces", [formato(), *F], proc(resultado(H, [["1", "0", "ab   ", "0.00"]])))
    gf(1, "CHAR width 1", "CHAR", 1, "xyz")
    gf(2, "CHAR width 255 padded", "CHAR", 255, "fin")
    gf(3, "NUM width 1 value 7", "NUM", 1, "7")
    gf(4, "NUM width 20 zero", "NUM", 20, "0")
    gf(5, "NUM leading zeros not stripped", "NUM", 6, "0007")
    gf(6, "DEC 1 decimal, width 6", "DEC", 6, "99.9", decimales=1)
    gf(7, "DEC zero value", "DEC", 8, "0.00", decimales=2)
    gf(8, "DEC many decimals truncated", "DEC", 14, "3.14159265", decimales=6)
    gf(9, "DEC largest 8 digits", "DEC", 11, "99999999.99", decimales=2)
    gf(10, "DEC no padding, comma separator, 4 decimals", "DEC", 12, "7.12345", decimales=4, rellenar="N", sep_dec=",")
    gf(11, "DATE leap day yyyy/MM/dd", "DATE", 10, "2024-02-29", fmt="yyyy/MM/dd")
    gf(12, "DATE month name format", "DATE", 12, "2025-03-31", fmt="MMM yyyy")
    gf(13, "DATE width 1 truncates", "DATE", 1, "2025-03-31", fmt="dd/MM/yyyy")
    gf(14, "CHAR tab and quote in the value, other separator", "CHAR", 8, "a\tb\"c", sep=";")
    gf(15, "DATE yyyyMMdd on year end, no padding", "DATE", 8, "2025-12-31", fmt="yyyyMMdd", rellenar="N")


if __name__ == "__main__":
    gen()
