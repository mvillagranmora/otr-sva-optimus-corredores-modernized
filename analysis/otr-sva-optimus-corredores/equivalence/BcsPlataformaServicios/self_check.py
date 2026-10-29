#!/usr/bin/env python3
"""HarnessSelfCheck for the ConsultarEstadoModulo oracle: proves the harness (cases + runner + compare.py) CAN FAIL.

  py -3 -I self_check.py [--work <scratchDir>]      writes HARNESS_SELFCHECK.json next to this file

Steps (each needs bash + JDK + ~/.m2 jars, no network, no HANA):
  0. comparator self check (compare.py --self-check): one-byte change in every golden is noticed.
  1. positive control: the unmodified legacy is re-run to a scratch dir and must equal the goldens in ALL cases (determinism).
  2. subject mutants (cl.bcs.oracle.MutantSubject, 8 plausible rewrite mistakes): each must make >= 1 case differ.
  3. legacy source mutations (LEGACY_OVERLAY with a mutated copy of ConsultarEstadoModulo.java, recompiled and re-run, plan section 6
     "Mutacion de goldens"): each must change >= 1 golden, i.e. the cases really pin that behaviour.
Exit code 0 only if every control behaves as required.
"""
import argparse
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REC = os.path.join(HERE, "legacy-oracle", "record.sh").replace("\\", "/")
LEGACY_SRC = os.path.normpath(os.path.join(HERE, "..", "..", "..", "..", "legacy", "otr-sva-optimus-corredores", "BSCL", "Plataforma",
                                           "JAVA", "BcsPlataformaServicios", "src", "cl", "bcs", "plataforma", "cre", "ConsultarEstadoModulo.java"))

# NOT plain "bash": CreateProcess finds C:\Windows\System32\bash.exe (WSL) first; the scripts need Git Bash (cygpath, Windows JDK)
BASH = next((p for p in [os.environ.get("BASH_EXE", ""), "C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe"]
             if p and os.path.exists(p)), "bash")

MUTANTS = ["reverse-order", "filter-by-module", "drop-last", "counter-1", "emit-nulls", "lower-estado", "swap-fields", "trim-name"]
# (name, old text, new text) applied to a COPY of the legacy source
SOURCE_MUTATIONS = [
    ("default-estado-ABT-to-CER", "IFNULL(\\\"IdEstado\\\",'ABT')", "IFNULL(\\\"IdEstado\\\",'CER')"),
    ("default-nombre-empty-to-dash", "IFNULL(\\\"CodigoModulo\\\",'')", "IFNULL(\\\"CodigoModulo\\\",'-')"),
    ("filter-by-IdEmpresa-removed", "where \\\"IdEmpresa\\\" = :IdEmpresa", "where 1 = 1 or \\\"IdEmpresa\\\" = :IdEmpresa"),
    ("error-counter-1", "salida.P_OUT_ERRORESCONTADOR = 0L;", "salida.P_OUT_ERRORESCONTADOR = 1L;"),
    ("order-by-added-D-ESM-3", "where \\\"IdEmpresa\\\" = :IdEmpresa\"", "where \\\"IdEmpresa\\\" = :IdEmpresa order by \\\"CodigoModulo\\\" desc\""),
    ("alias-nombre-renamed", "as \\\"NombreModulo\\\"", "as \\\"NombreModulo2\\\""),
    ("null-list-instead-of-empty", "salida.p_out_ListaModulos = new ArrayList<Modulo>();", "salida.p_out_ListaModulos = null;"),
    ("serialize-nulls", "Gson gson = new Gson();", "Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();"),
]


def bash(env_extra, args, cwd=HERE):
    env = dict(os.environ)
    env.update(env_extra)
    env.setdefault("JAVA_HOME", "/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot")
    return subprocess.run([BASH, REC] + args, cwd=cwd, env=env, capture_output=True, text=True, encoding="utf-8", errors="replace")


def compare(dump_dir, self_check=False):
    cmd = [sys.executable, "-I", os.path.join(HERE, "compare.py"), "--new", dump_dir]
    if self_check:
        cmd.append("--self-check")
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    ids = [ln.split()[1] for ln in r.stdout.splitlines() if ln.startswith("  DIFFERS") or ln.startswith("  MISSING")]
    totals = next((json.loads(ln[len("totals:"):]) for ln in r.stdout.splitlines() if ln.startswith("totals:")), None)
    return r.returncode, totals, ids, r.stdout


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--work", default=os.path.join(os.environ.get("TEMP", "."), "esm-selfcheck"))
    a = ap.parse_args()
    work = a.work.replace("\\", "/")
    os.makedirs(work, exist_ok=True)
    res = {"controls": [], "passed": False}
    ok = True

    rc, tot, ids, out = compare(os.path.join(work, "legacy-rerun"), self_check=True)  # dump dir may not exist yet: only the self check line matters
    sc_line = next((ln for ln in out.splitlines() if ln.startswith("self check:")), "self check: <none>")
    c0 = "PASSED" in sc_line
    res["controls"].append({"id": "0-comparator-self-check", "required": "every golden vs one-byte-changed copy is reported different", "result": sc_line, "ok": c0})
    ok &= c0

    r = bash({"ORACLE_WORK": work + "/w-legacy", "ORACLE_OUT": work + "/legacy-rerun"}, [])
    rc, tot, ids, out = compare(os.path.join(work, "legacy-rerun"))
    c1 = r.returncode == 0 and rc == 0 and tot and tot["same"] == tot["cases"]
    res["controls"].append({"id": "1-positive-control-legacy-rerun", "required": "all goldens reproduced by a second run of the legacy", "totals": tot, "ok": bool(c1)})
    ok &= bool(c1)

    for m in MUTANTS:
        r = bash({"ORACLE_WORK": work + "/w-mut", "ORACLE_OUT": work + "/mut-" + m, "ORACLE_SUBJECT": "cl.bcs.oracle.MutantSubject", "ORACLE_MUTANT": m}, [])
        rc, tot, ids, out = compare(os.path.join(work, "mut-" + m))
        c = r.returncode == 0 and rc == 1 and tot and tot["differs"] >= 1
        res["controls"].append({"id": "2-subject-mutant-" + m, "required": ">= 1 case differs", "totals": tot, "casesThatDetect": ids, "ok": bool(c)})
        ok &= bool(c)

    src = open(LEGACY_SRC, encoding="utf-8").read()
    for name, old, new in SOURCE_MUTATIONS:
        if old not in src:
            res["controls"].append({"id": "3-source-mutation-" + name, "ok": False, "result": "pattern not found in the legacy source: update self_check.py"})
            ok = False
            continue
        ov = work + "/overlay-" + name
        os.makedirs(ov, exist_ok=True)
        with open(ov + "/ConsultarEstadoModulo.java", "w", encoding="utf-8", newline="") as f:
            f.write(src.replace(old, new, 1))
        r = bash({"ORACLE_WORK": work + "/w-src", "LEGACY_OVERLAY": ov, "ORACLE_OUT": work + "/src-" + name}, [])
        if r.returncode != 0:
            res["controls"].append({"id": "3-source-mutation-" + name, "required": ">= 1 golden changes", "result": "mutated copy did not compile/run (exit %d): %s" % (r.returncode, r.stdout[-300:] + r.stderr[-300:]), "ok": False})
            ok = False
            continue
        rc, tot, ids, out = compare(os.path.join(work, "src-" + name))
        c = rc == 1 and tot and tot["differs"] >= 1
        res["controls"].append({"id": "3-source-mutation-" + name, "required": ">= 1 golden changes", "totals": tot, "casesThatDetect": ids, "ok": bool(c)})
        ok &= bool(c)

    res["passed"] = bool(ok)
    res["summary"] = "%d/%d controls ok" % (sum(1 for c in res["controls"] if c["ok"]), len(res["controls"]))
    with open(os.path.join(HERE, "HARNESS_SELFCHECK.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(res, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(res["summary"], "PASSED" if ok else "FAILED")
    for c in res["controls"]:
        print(" ", "ok " if c["ok"] else "BAD", c["id"], c.get("totals", ""), c.get("casesThatDetect", "") if len(str(c.get("casesThatDetect", ""))) < 200 else "[many]")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
