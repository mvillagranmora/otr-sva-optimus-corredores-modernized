"""Deduces column types/lengths from the stored procedures (templates/<Dominio>/**/Servicios/*.sql) that feed a template.

The .rpt embeds the CALL of its procedure in its command table; the procedure name after '::' is the file name of the
.sql beside it. The procedure declares its result columns as   "Name" NVARCHAR(40) / DECIMAL(26,8) / BIGINT / DATE /
BLOB ...   (table variables, often inside comments): the first declaration of every name wins.
"""
import os
import re

COL = re.compile(r'"([A-Za-z0-9_]+)"\s+(NVARCHAR|VARCHAR|NCLOB|CLOB|DECIMAL|DOUBLE|REAL|BIGINT|INTEGER|SMALLINT|TINYINT|'
                 r'DATE|SECONDDATE|TIMESTAMP|TIME|BLOB)\b\s*(?:\(\s*(\d+)\s*(?:,\s*(\d+)\s*)?\))?', re.I)
PROC = re.compile(r'::([A-Za-z0-9_]+)"')


def find_sql(templates_root, domain, command_text):
    """Path of the .sql that the command text calls, or None."""
    m = PROC.search(command_text or "")
    if not m:
        return None
    want = m.group(1).lower() + ".sql"
    for root, _dirs, files in os.walk(os.path.join(templates_root, domain)):
        for f in files:
            if f.lower() == want:
                return os.path.join(root, f)
    return None


def columns(sql_path):
    """{name: (type, length, scale)} from the first declaration of each quoted identifier."""
    if not sql_path:
        return {}
    with open(sql_path, encoding="utf-8", errors="replace") as fh:
        text = fh.read()
    out = {}
    for m in COL.finditer(text):
        n = m.group(1)
        if n not in out:
            out[n] = (m.group(2).upper(), int(m.group(3)) if m.group(3) else None, int(m.group(4)) if m.group(4) else None)
    for line in text.splitlines():           # SELECT ... <expression> AS "Alias"   (type guessed from the expression)
        m = ALIAS.search(line)
        if m and m.group(2) not in out:
            out[m.group(2)] = guess(m.group(1))
    return out


ALIAS = re.compile(r'(.*?)\bAS\s+"([A-Za-z0-9_]+)"\s*,?\s*(--.*)?$', re.I)
CASTP = re.compile(r'AS\s+(NVARCHAR|VARCHAR|DECIMAL|BIGINT|INTEGER|DATE)\s*(?:\(\s*(\d+)\s*(?:,\s*(\d+)\s*)?\))?', re.I)


def guess(expr):
    e = expr.lower()
    c = CASTP.search(expr)
    if c:
        return (c.group(1).upper(), int(c.group(2)) if c.group(2) else None, int(c.group(3)) if c.group(3) else None)
    if "to_date" in e or "to_dats" in e:
        return ("DATE", None, None)
    if "||" in e or "to_nvarchar" in e or "to_varchar" in e or "upper(" in e or "'" in e:
        return ("NVARCHAR", None, None)
    if any(k in e for k in ("sum(", "round(", "count(", "coalesce(", "abs(")) and "'" not in e:
        return ("DECIMAL", None, 2)
    return None
