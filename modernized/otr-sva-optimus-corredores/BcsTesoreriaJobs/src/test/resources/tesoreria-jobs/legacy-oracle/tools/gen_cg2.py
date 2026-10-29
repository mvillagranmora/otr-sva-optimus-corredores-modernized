"""GenerarArchivoCargos cases, part 2: FormatoArchivo.formatear boundaries (CG-F01..), one column 'V' of one record type per case."""
from gen_cg1 import c, det, formato, proc

# (id, description, TipoDato, Largo, Decimales, value, RellenarCampo, Formato, ValorDefecto)
CASOS = [
    ("CHAR pad right", "CHAR", 8, 0, "abc", "S", None, ""),
    ("CHAR exact length", "CHAR", 3, 0, "abc", "S", None, ""),
    ("CHAR truncated", "CHAR", 3, 0, "abcdef", "S", None, ""),
    ("CHAR value with outer spaces is NOT trimmed", "CHAR", 8, 0, "  ab  ", "S", None, ""),
    ("CHAR empty value with default: the default is ignored (raw value is formatted)", "CHAR", 5, 0, "", "S", None, "DEF"),
    ("CHAR null value -> NullPointerException", "CHAR", 5, 0, None, "S", None, ""),
    ("CHAR length 0", "CHAR", 0, 0, "abc", "S", None, ""),
    ("CHAR without padding returns the raw value", "CHAR", 8, 0, "abc", "N", None, ""),
    ("CHAR without padding does not truncate", "CHAR", 2, 0, "abcdef", "N", None, ""),
    ("NUM pad zeros left", "NUM", 6, 0, "1234", "S", None, ""),
    ("NUM longer than length is not truncated", "NUM", 3, 0, "123456", "S", None, ""),
    ("NUM negative value: spaces become zeros after the sign", "NUM", 6, 0, "-5", "S", None, ""),
    ("NUM empty value", "NUM", 4, 0, "", "S", None, ""),
    ("NUM null value -> 'null' text with zeros", "NUM", 6, 0, None, "S", None, ""),
    ("NUM with inner space", "NUM", 6, 0, "1 2", "S", None, ""),
    ("NUM without padding replaces spaces by zeros", "NUM", 6, 0, "1 2", "N", None, ""),
    ("DEC 10,2 normal", "DEC", 10, 2, "1234.5", "S", None, ""),
    ("DEC truncates (RoundingMode.DOWN) 1234.567", "DEC", 10, 2, "1234.567", "S", None, ""),
    ("DEC without dot is read as 0 (legacy defect)", "DEC", 10, 2, "1500", "S", None, ""),
    ("DEC negative value", "DEC", 10, 2, "-12.5", "S", None, ""),
    ("DEC integer digits above the width lose the leading digits", "DEC", 6, 2, "123456.78", "S", None, ""),
    ("DEC zero decimals", "DEC", 6, 0, "12.7", "S", None, ""),
    ("DEC 0.999 rounds down", "DEC", 8, 2, "0.999", "S", None, ""),
    ("DEC not a number -> NumberFormatException", "DEC", 8, 2, "ab.c", "S", None, ""),
    ("DEC null -> NullPointerException", "DEC", 8, 2, None, "S", None, ""),
    ("DEC without padding returns the raw value", "DEC", 8, 2, "12.345", "N", None, ""),
    ("DEC many decimals 3", "DEC", 12, 3, "7.12399", "S", None, ""),
    ("DATE yyyyMMdd", "DATE", 8, 0, "2025-04-01", "S", "yyyyMMdd", ""),
    ("DATE dd/MM/yyyy padded to 12", "DATE", 12, 0, "2025-04-01", "S", "dd/MM/yyyy", ""),
    ("DATE format longer than the width is not truncated", "DATE", 4, 0, "2025-04-01", "S", "yyyyMMdd", ""),
    ("DATE without format: the value padded", "DATE", 12, 0, "2025-04-01", "S", None, ""),
    ("DATE empty format string", "DATE", 12, 0, "2025-04-01", "S", "", ""),
    ("DATE invalid text -> ParseException", "DATE", 8, 0, "no-es-fecha", "S", "yyyyMMdd", ""),
    ("DATE lenient month 13 rolls over", "DATE", 8, 0, "2025-13-01", "S", "yyyyMMdd", ""),
    ("DATE with time part: the time is ignored", "DATE", 8, 0, "2025-04-01 23:59:59", "S", "yyyyMMdd", ""),
    ("DATE null with format -> NullPointerException", "DATE", 8, 0, None, "S", "yyyyMMdd", ""),
    ("DATE null without format -> the text 'null'", "DATE", 8, 0, None, "S", None, ""),
    ("DATE applies even without padding", "DATE", 8, 0, "2025-04-01", "N", "yyyyMMdd", ""),
    ("unknown TipoDato trims the value", "TEXTO", 8, 0, "  abc  ", "S", None, ""),
    ("unknown TipoDato uses the default for an empty value", "TEXTO", 8, 0, "", "S", None, "DEF  "),
    ("unknown TipoDato with a null value and a default", "TEXTO", 8, 0, None, "S", None, "X"),
    ("TipoDato lower case 'char' is unknown", "char", 8, 0, "abc", "S", None, ""),
]


def generar():
    for i, (desc, dato, largo, dec, valor, rell, fmt, defecto) in enumerate(CASOS, 1):
        f = {"CantidadDeTiposDeRegistro": 1, "RellenarCampo": rell, "Detalles": [det(1, "V", dato, largo, dec, defecto, fmt=fmt)]}
        p = proc((["TipoRegistroOptimus", "V"], [["1", valor]]))
        c("CG-F%02d" % i, desc, fmt=f, proc_=p)
