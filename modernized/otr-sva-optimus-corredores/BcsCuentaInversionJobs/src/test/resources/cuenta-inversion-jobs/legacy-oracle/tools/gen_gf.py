"""GF: the field formatter of GenerarArchivo (FormatoArchivo.formatear), exercised through the job: one TXT file with one field C, so the file is
the formatted value followed by the separator '|'. CHAR / NUM / DEC / DATE and the unknown / missing definitions, with the padding switch."""
from gen_fmt import *

n_ = [0]


def gf(desc, tipo, largo, valor, decimales=0, defecto="", fmt=None, rellenar="S", sep_dec=".", campo="C", extra=None, columnas=None, fila=None):
    n_[0] += 1
    seed = [formato(rellenar=rellenar, sep_dec=sep_dec, codigo="ARCHIVO_X", sep="|"), detalle("C", tipo, largo, decimales, defecto, formato_fecha=fmt)] + (extra or [])
    cols = columnas or ["TipoRegistroOptimus", campo]
    row = fila or ["1", valor]
    caso_ga("GF-%02d" % n_[0], desc, seed, proc(resultado(cols, [row])))


def gen_gf():
    # CHAR
    gf("CHAR padded on the right to the width", "CHAR", 8, "abc")
    gf("CHAR not padded when RellenarCampo is N", "CHAR", 8, "abc", rellenar="N")
    gf("CHAR longer than the width is cut", "CHAR", 4, "abcdefgh")
    gf("CHAR exactly the width", "CHAR", 3, "abc")
    gf("CHAR empty value takes ValorDefecto", "CHAR", 6, "", defecto="N/A")
    gf("CHAR empty value and empty default", "CHAR", 5, "")
    gf("CHAR SQL NULL value takes ValorDefecto", "CHAR", 6, None, defecto="ND")
    gf("CHAR value is trimmed before the padding", "CHAR", 6, "  ab  ")
    gf("CHAR with accents: the width counts characters", "CHAR", 6, "Ñandú")
    gf("CHAR width 0: the padding format fails and the field is empty", "CHAR", 0, "abc")
    gf("CHAR a default longer than the width is cut", "CHAR", 3, "", defecto="DEFECTO")
    gf("RellenarCampo in lower case s still pads (equalsIgnoreCase)", "CHAR", 6, "ab", rellenar="s")
    # NUM
    gf("NUM padded with zeros on the left", "NUM", 6, "42")
    gf("NUM not padded when RellenarCampo is N", "NUM", 6, "42", rellenar="N")
    gf("NUM longer than the width is NOT cut", "NUM", 3, "123456")
    gf("NUM empty value takes ValorDefecto", "NUM", 5, "", defecto="7")
    gf("NUM an inner space becomes a zero", "NUM", 6, "1 2")
    gf("NUM negative: the sign ends up inside the zeros", "NUM", 6, "-5")
    gf("NUM empty value and empty default: all zeros", "NUM", 4, "")
    # DEC
    gf("DEC width 9 with 2 decimals: integer part 7 digits, decimals truncated", "DEC", 9, "123.456", decimales=2)
    gf("DEC without a decimal point: the value becomes 0 (the integer is lost)", "DEC", 9, "5", decimales=2)
    gf("DEC one decimal digit is padded on the LEFT: 1234.5 -> 1234.05", "DEC", 9, "1234.5", decimales=2)
    gf("DEC integer part longer than its width is not cut", "DEC", 6, "1234567.1", decimales=2)
    gf("DEC negative amount", "DEC", 9, "-12.5", decimales=2)
    gf("DEC not padded when RellenarCampo is N", "DEC", 9, "12.5", decimales=2, rellenar="N")
    gf("DEC decimal separator comma", "DEC", 9, "12.34", decimales=2, sep_dec=",")
    gf("DEC empty decimal separator", "DEC", 9, "12.34", decimales=2, sep_dec="")
    gf("DEC null decimal separator: the text null in the middle", "DEC", 9, "12.34", decimales=2, sep_dec=None)
    gf("DEC zero decimals: the padding format fails and the field is empty", "DEC", 5, "12.5", decimales=0)
    gf("DEC NULL Decimales column: read as 0", "DEC", 5, "12.5", decimales=None)
    gf("DEC text value with a dot", "DEC", 9, "abc.def", decimales=2)
    gf("DEC value starting with the dot", "DEC", 9, ".5", decimales=2)
    gf("DEC value ending with the dot", "DEC", 9, "5.", decimales=2)
    gf("DEC value with two dots uses the first two parts", "DEC", 9, "1.2.3", decimales=3)
    gf("DEC empty value takes ValorDefecto", "DEC", 9, "", decimales=2, defecto="9.5")
    # DATE
    gf("DATE reformatted dd/MM/yyyy and padded to the width", "DATE", 12, "2025-03-31", fmt="dd/MM/yyyy")
    gf("DATE without a format: the value padded", "DATE", 12, "2025-03-31")
    gf("DATE out of range is rolled over (lenient parser)", "DATE", 10, "2025-13-45", fmt="dd/MM/yyyy")
    gf("DATE not a date: the error is swallowed and the field is empty", "DATE", 10, "abc", fmt="dd/MM/yyyy")
    gf("DATE empty value takes ValorDefecto and is reformatted", "DATE", 10, "", defecto="2000-01-01", fmt="dd/MM/yyyy")
    gf("DATE empty value with a format and no default: spaces", "DATE", 10, "", fmt="dd/MM/yyyy")
    gf("DATE yyyyMMdd", "DATE", 8, "2025-03-31", fmt="yyyyMMdd")
    gf("DATE with a time part: the date prefix is parsed", "DATE", 10, "2025-03-31 10:20:30", fmt="dd/MM/yyyy")
    # unknown / missing definitions and the other failure shapes
    gf("unknown TipoDato: the field is empty", "BOOL", 5, "x")
    gf("TipoDato in lower case is not CHAR: empty", "char", 5, "x")
    gf("the column has no definition: empty", "CHAR", 5, "x", campo="Z")
    gf("RellenarCampo null: NullPointerException swallowed, the field is empty", "CHAR", 5, "x", rellenar=None)
    gf("ValorDefecto null with an empty value: swallowed, the field is empty", "CHAR", 5, "", defecto=None)
    gf("ValorDefecto null with a value: no effect", "CHAR", 5, "ab", defecto=None)
    gf("two definitions of the same field: the first in table order wins", "CHAR", 5, "abcdefgh", extra=[detalle("C", "CHAR", 2)])
    gf("a definition of another record type is not used", "CHAR", 5, "x", extra=None, columnas=["TipoRegistroOptimus", "C"], fila=["2", "x"])


if __name__ == "__main__":
    gen_gf()
