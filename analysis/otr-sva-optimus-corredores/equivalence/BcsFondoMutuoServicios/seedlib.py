"""Schema aware INSERT builder for the oracle seeds (fake data only). Reads schema_spec.py (columns) and schema-types.sql (types and NOT NULL, REAL where the
catalog had them) so that a seed row only names the columns it cares about; every other NOT NULL column gets a neutral default of its type."""
import os
import re

from schema_spec import TABLES

HERE = os.path.dirname(os.path.abspath(__file__))
TYPES = {}   # "<table>.<col>" -> (sqltype, notnull)
for ln in open(os.path.join(HERE, "schema-types.sql"), encoding="utf-8"):
    m = re.match(r'CREATE DOMAIN "(.+)" AS (.+?); -- (.*)$', ln.rstrip("\n"))
    if m:
        TYPES[m.group(1)] = (m.group(2), m.group(3).endswith(" NOT NULL"))
FULL = {k.split("::")[1]: k for k in TABLES}      # "FMM.SerieDeFondo" -> full object name


def lit(t, v):
    if v is None:
        return "NULL"
    if t.startswith(("NVARCHAR", "VARCHAR")):
        return "'" + str(v).replace("'", "''") + "'"
    if t == "DATE":
        return "DATE '%s'" % v
    if t == "TIMESTAMP":
        return "TIMESTAMP '%s'" % v
    return str(v)


def default(t):
    if t.startswith("NVARCHAR"):
        return ""
    if t == "DATE":
        return "0001-01-01"
    if t == "TIMESTAMP":
        return "2026-03-02 00:00:00"
    if t == "TIME":
        return "00:00:00"
    return 0


def ins(short, **vals):
    """INSERT for the object FMM.X / POR.X ... ; unnamed NOT NULL columns get the default of their type; unnamed nullable ones stay NULL."""
    full = FULL[short]
    cols = [i.rsplit(":", 1)[0] for i in TABLES[full].split()]
    cols = list(dict.fromkeys(cols))
    real = [c for c in cols if (full + "." + c) in TYPES]
    for k in vals:
        if k not in real:
            raise KeyError("%s has no column %s in the oracle schema" % (short, k))
    names, vs = [], []
    for c in real:
        t, nn = TYPES[full + "." + c]
        if c in vals:
            names.append(c); vs.append(lit(t, vals[c]))
        elif nn:
            names.append(c); vs.append(lit(t, default(t)))
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (full, ", ".join('"%s"' % n for n in names), ", ".join(vs))
