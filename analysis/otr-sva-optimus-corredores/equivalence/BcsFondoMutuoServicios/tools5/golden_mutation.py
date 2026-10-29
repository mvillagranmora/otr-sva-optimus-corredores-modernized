#!/usr/bin/env python3
"""Step 5, golden mutation: MUTATED COPIES of 14 development goldens (never the goldens themselves) are compared, with the skill's compare.py, against the
dump of the real NEW implementation. Every mutated copy must be reported "differs"; two controls (the golden reformatted, no content change) must be "same".
  py -3 golden_mutation.py <newDumpDirNor> <newDumpDirMan> <outDir>
The mutations are semantic, not one-byte: a digit of a quota count, the scale of an amount, a counter, a message code, the servlet decision, a table row, a
collaborator call (dropped / changed argument), the exception class, the KEY ORDER of the output, the order of two results, a sequence request, a leftover local table.
Layout of <outDir>: legacy/<id>.json = the mutated golden copy, new/<id>.json = the dump of the new code, cases.json, GOLDEN_MUTATION.json (compare.py verdict).
"""
import json
import os
import re
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CASES = os.path.join(HERE, "..", "cases")
SKILL = os.path.join(os.path.expanduser("~"), ".claude", "plugins", "cache", "claude-plugins-official", "code-modernization", "1.0.0", "scripts", "compare.py")


def loads(raw):
    """JSON with the lexeme of every decimal preserved (BigDecimal scale 10.0000): floats are parsed as a placeholder string and restored by dumps()."""
    return json.loads(raw, parse_float=lambda s: "@@NUM" + s + "@@")


def dumps(d, **kw):
    return re.sub(r'"@@NUM([^"]*)@@"', r"\1", json.dumps(d, ensure_ascii=False, **kw))


def sub_cruda(d, pattern, repl):
    n = re.subn(pattern, repl, d["salidaCruda"], count=1)
    assert n[1] == 1, "mutation pattern not found: " + pattern
    d["salidaCruda"] = n[0]


def m_digit(d):
    sub_cruda(d, r'("NumeroCuotas":)(\d)', lambda m: m.group(1) + str((int(m.group(2)) + 1) % 10))


def m_scale(d):
    sub_cruda(d, r'("Monto":)(\d+)(,)', r"\g<1>\g<2>.0\3")


def m_counter(d):
    sub_cruda(d, r'("P_OUT_ERRORESCONTADOR":)(\d)', lambda m: m.group(1) + ("0" if m.group(2) != "0" else "1"))


def m_msg(d):
    sub_cruda(d, r'"codigo":"([A-Z_]+)"', lambda m: '"codigo":"' + m.group(1)[:-1] + 'X"')


def m_decision(d):
    d["decisionServlet"] = "ROLLBACK" if d["decisionServlet"] == "COMMIT" else "COMMIT"


def m_table(d):
    for rows in d["tablas"].values():
        if rows:
            k = next(k for k, v in rows[0].items() if isinstance(v, (int, float)) and not isinstance(v, bool))
            rows[0][k] = rows[0][k] + 1
            return
    raise AssertionError("no table row to mutate")


def m_drop_call(d):
    d["colaboradores"].pop(len(d["colaboradores"]) // 2)


def m_call_arg(d):
    for c in d["colaboradores"]:
        if c["argumentos"] and isinstance(c["argumentos"][0], dict):
            k = next(k for k, v in c["argumentos"][0].items() if isinstance(v, int) and not isinstance(v, bool))
            c["argumentos"][0][k] += 1
            return
    raise AssertionError("no collaborator argument to mutate")


def m_exception(d):
    d["excepcion"] = "IllegalStateException:" + d["excepcion"].split(":", 1)[1]


def m_key_order(d):
    s = loads(d["salidaCruda"])
    items = list(s.items())
    items[0], items[1] = items[1], items[0]
    d["salidaCruda"] = dumps(dict(items), separators=(",", ":"))


def m_swap_results(d):
    s = loads(d["salidaCruda"])
    s["P_OUT_RESULTADO"][0], s["P_OUT_RESULTADO"][1] = s["P_OUT_RESULTADO"][1], s["P_OUT_RESULTADO"][0]
    d["salidaCruda"] = dumps(s, separators=(",", ":"))


def m_sequence(d):
    d["secuenciasSolicitadas"].append(1)


def m_session(d):
    d["sesion"]["tablasLocalesSinDrop"].append("#p_Bloqueo_fantasma")


def m_drop_result(d):
    s = loads(d["salidaCruda"])
    s["P_OUT_RESULTADO"].pop()
    d["salidaCruda"] = dumps(s, separators=(",", ":"))


PLAN = [  # (golden id, mutation name, function)
    ("FMM-NOR-02_rescate-por-monto", "digit of NumeroCuotas in the output", m_digit),
    ("FMM-NOR-04_aporte-cin-manual-saldo-ok", "scale of Monto 100000 -> 100000.0", m_scale),
    ("FMM-NOR-05_aporte-cin-saldo-insuficiente", "error counter flipped", m_counter),
    ("FMM-NOR-15_rescate-mayor-a-custodia", "message code renamed", m_msg),
    ("FMM-NOR-03_rescate-total", "servlet decision COMMIT -> ROLLBACK", m_decision),
    ("FMM-NOR-04_aporte-cin-manual-saldo-ok", "numeric column of the inserted request +1", m_table),
    ("FMM-NOR-18_multi-solicitud-mixta", "a collaborator call dropped", m_drop_call),
    ("FMM-NOR-43_dos-aportes-cin-mismo-portafolio", "argument of a collaborator call changed", m_call_arg),
    ("FMM-NOR-29_serie-inexistente", "exception class changed", m_exception),
    ("FMM-NOR-21_lista-vacia", "key order of the output JSON swapped", m_key_order),
    ("FMM-NOR-20_bloqueo-duplicado", "a leftover local table in the session", m_session),
    ("FMM-MAN-13_normal-y-transferencia-orden-y-escritura", "order of two results swapped", m_swap_results),
    ("FMM-MAN-15_dos-pasadas-de-normal-consumen-dos-secuencias", "an extra sequence request", m_sequence),
    ("FMM-MAN-04_siete-destinos-orden-fijo", "last result dropped", m_drop_result),
]
CONTROLS = ["FMM-NOR-03_rescate-total", "FMM-MAN-13_normal-y-transferencia-orden-y-escritura"]
MASK = [{"regex": "\"_provenance\": \"(?:[^\"\\\\]|\\\\.)*\"", "why": "provenance text names the subject that produced the dump (legacy oracle vs new subject): not behaviour"}]


def main():
    nor, man, out = sys.argv[1:4]
    for sub in ("legacy", "new"):
        os.makedirs(os.path.join(out, sub), exist_ok=True)
    cases = []
    for i, (cid, nombre, fn) in enumerate(PLAN, 1):
        d = loads(open(os.path.join(CASES, cid, "expected.json"), encoding="utf-8").read())
        fn(d)
        mid = "GM%02d_%s" % (i, cid.split("_")[0])
        open(os.path.join(out, "legacy", mid + ".json"), "w", encoding="utf-8", newline="\n").write(dumps(d, indent=2) + "\n")
        shutil.copyfile(os.path.join(man if "-MAN-" in cid else nor, cid + ".json"), os.path.join(out, "new", mid + ".json"))
        cases.append({"id": mid, "title": "mutated copy of %s: %s" % (cid, nombre), "legacy": "legacy/%s.json" % mid, "new": "new/%s.json" % mid, "mask": MASK})
    for j, cid in enumerate(CONTROLS, 1):
        mid = "CTRL%02d_%s" % (j, cid.split("_")[0])
        d = loads(open(os.path.join(CASES, cid, "expected.json"), encoding="utf-8").read())
        open(os.path.join(out, "legacy", mid + ".json"), "w", encoding="utf-8", newline="\n").write(dumps(d, indent=2) + "\n")   # no change: only re-serialized
        shutil.copyfile(os.path.join(man if "-MAN-" in cid else nor, cid + ".json"), os.path.join(out, "new", mid + ".json"))
        cases.append({"id": mid, "title": "CONTROL, golden %s re-serialized without any change (expected: same)" % cid, "legacy": "legacy/%s.json" % mid, "new": "new/%s.json" % mid, "mask": MASK})
    json.dump({"system": "BcsFondoMutuoServicios", "cases": cases}, open(os.path.join(out, "cases.json"), "w", encoding="utf-8", newline="\n"), indent=1)
    print("written", len(cases), "cases; run compare in", out)


if __name__ == "__main__":
    main()
