"""Row builder for the step 3B seeds: reads the generated H2 schema (schema-types.sql / schema-fmm.sql) and writes an INSERT with EVERY column of the
table (a type default unless overridden), so a seed only states what the case is about."""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
_types = {}
_tables = {}


def _load():
    if _types:
        return
    for line in open(os.path.join(HERE, "schema-types.sql"), encoding="utf-8"):
        m = re.match(r'CREATE DOMAIN "([^"]+)" AS ([A-Z]+)', line)
        if m:
            _types[m.group(1)] = m.group(2)
    cur = None
    for line in open(os.path.join(HERE, "schema-fmm.sql"), encoding="utf-8"):
        m = re.match(r'CREATE (?:LOCAL TEMPORARY )?TABLE "([^"]+)" \(', line)
        if m:
            cur = m.group(1)
            _tables[cur] = []
            continue
        m = re.match(r'\s+"([^"]+)" "([^"]+)"', line)
        if m and cur:
            _tables[cur].append((m.group(1), m.group(2)))
        elif line.startswith(")"):
            cur = None


def _default(tipo):
    return {"NVARCHAR": "''", "BIGINT": "0", "INTEGER": "0", "DECIMAL": "0", "DATE": "DATE '0001-01-01'",
            "TIMESTAMP": "TIMESTAMP '2026-03-02 00:00:00'"}[tipo]


def _lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, RawSql):
        return str(v)
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


class RawSql(str):
    pass


def fecha(texto):
    return RawSql("DATE '%s'" % texto)


def fila(tabla, **ov):
    _load()
    cols = _tables[tabla]
    names = {c for c, _ in cols}
    for k in ov:
        if k not in names:
            raise KeyError("%s has no column %s" % (tabla, k))
    nombres = ", ".join('"%s"' % c for c, _ in cols)
    valores = ", ".join(_lit(ov[c]) if c in ov else _default(_types[d]) for c, d in cols)
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, nombres, valores)


SOLICITUD = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
VISTA_SOLICITUD = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
INTERFAZ_AGF = "BSCL.FondoMutuo.BD::FMM.InterfazAgf"
