"""FRESH fake deterministic values with boundary cases (independent of the development generator gen_values.py).
Rows differ by index i: i=0 is a plain non-null row (ListMapResultSet infers column types from row 1); later rows cycle
through boundary values: empty string, maximum-length text, accents/escapes, zero, negative, huge, leap days."""
import base64
import struct
import zlib

STR = [
    "Ñandú Pérez Núñez Ångström ¿Qué? ñ",          # i=1 (i=0 below): accents
    "",                                              # empty
    None,                                            # long text (filled to the field length)
    " O'Higgins & Cía. \"Sur\" <Ltda> ; / | ",       # spaces, quotes, ampersand, angle brackets (no backslash: Facet.rtfText of the harness throws on "\'5c")
    "Z",                                             # one char
    "Línea 1 de texto muy largo con acentos áéíóú",  # medium
]
LONG = "Texto largo de prueba FRESH sin cliente real, repetido para llenar el campo hasta su limite maximo. "


def png_b64(w, h, seed):
    rows = b""
    for y in range(h):
        row = b"\x00"
        for x in range(w):
            edge = x in (0, w - 1) or y in (0, h - 1)
            row += bytes((200, 0, 0) if edge else ((seed + x * 5) % 256, (seed * 3 + y * 7) % 256, (x * y + seed) % 256))
        rows += row

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0)) + \
        chunk(b"IDAT", zlib.compress(rows, 9)) + chunk(b"IEND", b"")
    return base64.b64encode(png).decode("ascii")


def _string(name, i, ln):
    n = name.lower()
    cap = ln if ln else 80
    if i == 0:
        v = "Fresh %s base" % name
        if "rut" in n or "documento" in n or "identidad" in n:
            v = "7.654.321-K"
        return v[:cap]
    k = STR[(i - 1) % len(STR)]
    if k is None:
        k = (LONG * 4)[:min(cap, 240)]
    if ln is not None and ln <= 1 and k == "":
        return ""
    return k[:cap]


def _number(n, jrc, scale, i):
    big = [1234.5, -98765.43, 99999999999.99, 0.0, 0.01, -0.5, 1e9 + 0.25]
    if jrc == "xsd:long":
        return [321, 0, -42, 2147483647, 9999999999, 1, -1, 7][i % 8]
    s = 2 if scale is None else min(scale, 4)
    v = big[i % len(big)]
    if any(k in n for k in ("porcentaje", "tasa", "rentabilidad")) and i % 3:
        v = [0.0, 100.0, -3.25][i % 3]
    return round(v, s) if s else float(round(v))


def synth(name, jrc, sql, i, dash=False):
    """-> (value, schema type)"""
    st = sql[0] if sql else None
    if jrc == "blob":
        return png_b64(64 + i % 3, 24, 40 + i), "blob"
    if jrc in ("xsd:long", "xsd:decimal"):
        return _number(name.lower(), jrc, sql[2] if sql else None, i), "number"
    days = ["2024-02-29", "2000-01-01", "1999-12-31", "2023-07-04", "2024-12-31", "2025-02-28"]
    d = days[i % len(days)]
    if dash:
        return "%d-%d-%s" % (29 - i % 3, 2 + i % 9, d[:4]), "string"
    if jrc == "xsd:date":
        return d, "string"
    if jrc == "xsd:dateTime":
        return "%s 23:%02d:59.0" % (d, 59 - i % 40), "string"
    if jrc == "xsd:time":
        return "23:59:%02d" % (i % 60), "string"
    return _string(name, i, sql[1] if sql and st in ("NVARCHAR", "VARCHAR") else None), "string"
