"""GenerarArchivoPago cases, part 2: FormatoArchivo.formatear boundaries (PG-F01..), one column 'V' of one record type per case (TXT output)."""
from gen_pgbase import fila_detalle, fila_formato, p, proc

# (description, TipoDato, Largo, Decimales, value, RellenarCampo, Formato, ValorDefecto, SeparadorDecimales)
CASOS = [
    ("CHAR pad right", "CHAR", 8, 0, "abc", "S", None, "", "."),
    ("CHAR exact length", "CHAR", 3, 0, "abc", "S", None, "", "."),
    ("CHAR truncated", "CHAR", 3, 0, "abcdef", "S", None, "", "."),
    ("CHAR outer spaces are trimmed first", "CHAR", 8, 0, "  ab  ", "S", None, "", "."),
    ("CHAR empty value takes the default", "CHAR", 6, 0, "", "S", None, "DEF", "."),
    ("CHAR blank value takes the default", "CHAR", 6, 0, "   ", "S", None, "DEF", "."),
    ("CHAR default longer than the width is truncated", "CHAR", 2, 0, "", "S", None, "DEFAULT", "."),
    ("CHAR literal backslash-n becomes a line break", "CHAR", 4, 0, "\\n", "S", None, "", "."),
    ("CHAR null value -> '' (exception swallowed)", "CHAR", 5, 0, None, "S", None, "", "."),
    ("CHAR without padding keeps the trimmed value", "CHAR", 8, 0, "abc", "N", None, "", "."),
    ("CHAR without padding still truncates", "CHAR", 2, 0, "abcdef", "N", None, "", "."),
    ("CHAR RellenarCampo lower case s pads (equalsIgnoreCase)", "CHAR", 6, 0, "ab", "s", None, "", "."),
    ("CHAR RellenarCampo null -> '' (exception swallowed)", "CHAR", 6, 0, "ab", None, None, "", "."),
    ("CHAR length 0 -> invalid format width, '' (exception swallowed)", "CHAR", 0, 0, "abc", "S", None, "", "."),
    ("NUM pad zeros", "NUM", 6, 0, "1234", "S", None, "", "."),
    ("NUM longer than the width is kept", "NUM", 3, 0, "123456", "S", None, "", "."),
    ("NUM empty value takes the default", "NUM", 5, 0, "", "S", None, "77", "."),
    ("NUM null -> ''", "NUM", 5, 0, None, "S", None, "", "."),
    ("NUM without padding", "NUM", 6, 0, "12", "N", None, "", "."),
    ("NUM negative", "NUM", 6, 0, "-5", "S", None, "", "."),
    ("DEC 10,2 with decimal part 5 is LEFT padded to 05 (legacy defect)", "DEC", 10, 2, "1234.5", "S", None, "", "."),
    ("DEC normal 1234.56", "DEC", 10, 2, "1234.56", "S", None, "", "."),
    ("DEC decimals truncated (not rounded) 1234.567", "DEC", 10, 2, "1234.567", "S", None, "", "."),
    ("DEC without dot -> ArrayIndexOutOfBounds swallowed -> ''", "DEC", 10, 2, "1500", "S", None, "", "."),
    ("DEC decimal separator comma", "DEC", 10, 2, "12.34", "S", None, "", ","),
    ("DEC decimal separator empty (digits glued)", "DEC", 10, 2, "12.34", "S", None, "", ""),
    ("DEC null separator -> 'null'", "DEC", 10, 2, "12.34", "S", None, "", None),
    ("DEC integer part longer than the width is kept", "DEC", 6, 2, "123456.78", "S", None, "", "."),
    ("DEC negative", "DEC", 10, 2, "-12.34", "S", None, "", "."),
    ("DEC zero decimals -> invalid width, ''", "DEC", 6, 0, "12.7", "S", None, "", "."),
    ("DEC without padding keeps the parts", "DEC", 10, 2, "12.3", "N", None, "", "."),
    ("DEC empty value takes the default", "DEC", 10, 2, "", "S", None, "0.50", "."),
    ("DEC null -> ''", "DEC", 10, 2, None, "S", None, "", "."),
    ("DATE yyyyMMdd", "DATE", 8, 0, "2025-04-01", "S", "yyyyMMdd", "", "."),
    ("DATE dd/MM/yyyy padded to 12", "DATE", 12, 0, "2025-04-01", "S", "dd/MM/yyyy", "", "."),
    ("DATE longer than the width is kept", "DATE", 4, 0, "2025-04-01", "S", "yyyyMMdd", "", "."),
    ("DATE without format: value padded", "DATE", 12, 0, "2025-04-01", "S", None, "", "."),
    ("DATE empty format string: value padded", "DATE", 12, 0, "2025-04-01", "S", "", "", "."),
    ("DATE invalid text -> '' (ParseException swallowed)", "DATE", 8, 0, "no-es-fecha", "S", "yyyyMMdd", "", "."),
    ("DATE lenient month 13", "DATE", 8, 0, "2025-13-01", "S", "yyyyMMdd", "", "."),
    ("DATE empty value with format: only padded", "DATE", 8, 0, "", "S", "yyyyMMdd", "", "."),
    ("DATE empty value takes the default date", "DATE", 8, 0, "", "S", "yyyyMMdd", "2025-01-31", "."),
    ("DATE null -> ''", "DATE", 8, 0, None, "S", "yyyyMMdd", "", "."),
    ("unknown TipoDato -> ''", "TEXTO", 8, 0, "abc", "S", None, "", "."),
    ("TipoDato lower case 'char' is unknown -> ''", "char", 8, 0, "abc", "S", None, "", "."),
    ("field without detail definition -> '' but the separator is still written", "OTRO", 8, 0, "abc", "S", None, "", "."),
]


def generar():
    for i, (desc, dato, largo, dec, valor, rell, fmt, defecto, sep) in enumerate(CASOS, 1):
        campo = "NOEXISTE" if dato == "OTRO" else "V"
        det = [fila_detalle(1, "V", dato, largo, dec, defecto, formato=fmt)]
        p("PG-F%02d" % i, desc, formatos=[fila_formato(RellenarCampo=rell, SeparadorDecimales=sep)], detalles=det,
          proc_=proc((["TipoRegistroOptimus", campo], [["1", valor]])))
