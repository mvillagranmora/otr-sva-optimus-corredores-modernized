"""Deterministic FAKE values for a field (no client data). Rules: the type is the Crystal field type (xsd:*); the SQL only refines
lengths/scales; dates are NOT written dd-MM-yyyy / today's day (the print-date masks must never touch data); strings keep
the SQL length; the first row is never null (ListMapResultSet infers column types from row 1)."""
import base64
import struct
import zlib

TEXTS = ["JOSE PENA NUNEZ", "MARIA ANGELICA SOTO", "INVERSIONES NORTE LTDA", "CARLOS MUNOZ ESPINOZA", "ANA GONZALEZ"]
ACCENT = ["José Peña Ñuñez", "María Angélica", "Compañía del Sur S.A.", "República €"]


def png_b64(w=120, h=48):
    """Small deterministic PNG (gradient + border), base64."""
    rows = b""
    for y in range(h):
        row = b"\x00"
        for x in range(w):
            edge = x in (0, w - 1) or y in (0, h - 1)
            row += bytes((0, 0, 0) if edge else (30 + x * 2 % 200, 80 + y * 3 % 150, 160))
        rows += row

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0)) + \
        chunk(b"IDAT", zlib.compress(rows, 9)) + chunk(b"IEND", b"")
    return base64.b64encode(png).decode("ascii")


def _string(name, i, ln):
    n = name.lower()
    if "fecha" in n or n.startswith("date"):
        v = "%02d/%02d/2025" % (10 + i, 3 + i)
    elif "hora" in n:
        v = "10:%02d" % (30 + i)
    elif "email" in n or "correo" in n:
        v = "cliente%d@example.test" % i
    elif "rut" in n or "documento" in n or "identidad" in n:
        v = "12.345.67%d-%d" % (i, i + 1)
    elif "telefono" in n or "fono" in n:
        v = "+56 2 2345 %04d" % (6700 + i)
    elif "moneda" in n:
        v = ["CLP", "USD", "UF"][i % 3]
    elif any(k in n for k in ("nombre", "razon", "cliente", "apellido", "titular", "beneficiario")):
        v = ACCENT[i % len(ACCENT)] if i % 2 else TEXTS[i % len(TEXTS)]
    elif any(k in n for k in ("direccion", "comuna", "ciudad", "region", "pais")):
        v = "Av. Providencia %d, Ñuñoa" % (1000 + i)
    elif ln is not None and ln <= 1:
        v = "S"
    elif any(k in n for k in ("folio", "numero", "codigo", "correlativo", "id")):
        v = "%d" % (100000 + i)
    else:
        v = ("%s %d" % (name, i))
    return v[:ln] if ln else v[:60]


def synth(name, jrc, sql, i):
    """-> (value, schema type). jrc = xsd:string|decimal|long|date|dateTime|time|blob ; sql = (type,len,scale) or None"""
    st = sql[0] if sql else None
    if jrc == "blob":
        return png_b64(40, 16), "blob"
    if jrc == "xsd:long":
        return 1000 + i * 7, "number"
    if jrc == "xsd:decimal":
        n = name.lower()
        if any(k in n for k in ("porcentaje", "tasa", "rentabilidad")):
            return round(12.5 + i * 1.25, 2), "number"
        scale = sql[2] if sql and sql[2] is not None else 2
        return round(1234567.891 * (i + 1) / 3, min(scale, 4) if scale else 0), "number"
    if jrc == "xsd:date":
        return "2025-%02d-%02d" % (3 + i, 10 + i), "string"
    if jrc == "xsd:dateTime":
        return "2025-%02d-%02d 10:%02d:00.0" % (3 + i, 10 + i, 30 + i), "string"
    if jrc == "xsd:time":
        return "10:%02d:00" % (30 + i), "string"
    return _string(name, i, sql[1] if sql and st in ("NVARCHAR", "VARCHAR") else None), "string"
