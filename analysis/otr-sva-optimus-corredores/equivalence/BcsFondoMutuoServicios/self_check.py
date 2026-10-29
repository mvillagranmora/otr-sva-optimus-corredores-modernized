#!/usr/bin/env python3
"""HarnessSelfCheck for the FondoMutuo solicitud oracle: proves the harness (cases + runner + compare.py) CAN FAIL.

  py -3 -I self_check.py [--work <scratchDir>]      writes HARNESS_SELFCHECK.json next to this file

Steps (each needs Git Bash + JDK + ~/.m2 jars, no network, no HANA):
  0. comparator self check (compare.py --self-check): one-byte change in every golden is noticed.
  1. positive control: the unmodified legacy is re-run to a scratch dir and must equal the goldens in ALL cases (determinism: "second run reproduces").
  2. subject mutants (cl.bcs.oracle.MutantSubject, 10 plausible rewrite mistakes): each must make >= 1 case differ.
  3. legacy source mutations (LEGACY_OVERLAY with a mutated COPY of a legacy source, recompiled and re-run): each must change >= 1 golden, i.e. the cases pin it.
Exit code 0 only if every control behaves as required. Legacy sources are only read.
"""
import argparse
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REC = os.path.join(HERE, "legacy-oracle", "record.sh").replace("\\", "/")
FM = os.path.normpath(os.path.join(HERE, "..", "..", "..", "..", "legacy", "otr-sva-optimus-corredores", "BSCL", "FondoMutuo", "JAVA", "BcsFondoMutuoServicios",
                                   "src", "cl", "bcs", "fondomutuo"))
# NOT plain "bash": CreateProcess finds C:\Windows\System32\bash.exe (WSL) first; the scripts need Git Bash (cygpath, Windows JDK)
BASH = next((p for p in [os.environ.get("BASH_EXE", ""), "C:/Program Files/Git/bin/bash.exe", "C:/Program Files/Git/usr/bin/bash.exe"] if p and os.path.exists(p)), "bash")

MUTANTS = ["counter-0", "counter-missing", "drop-message", "resultado-monto-plus-1", "delete-solicitudes", "update-estado", "lock-left-behind",
           "drop-collaborator-call", "sequence-skipped", "emit-nulls"]
# (file, name, old text, new text) applied to a COPY of the legacy source
SOURCE_MUTATIONS = [
    ("CrearSolicitudAporteRescateNormal.java", "estado-ingresado-to-pendiente", "sol.Estado =  _c.EstadoIngresado;", "sol.Estado =  _c.EstadoPendiente;"),
    ("CrearSolicitudAporteRescateNormal.java", "lock-release-removed", "sql.call(con, SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD, params, new HashMap<String, Class>());",
     "if (solicitudesBloqueadas.size() > 99) sql.call(con, SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD, params, new HashMap<String, Class>());"),
    ("CrearSolicitudAporteRescateNormal.java", "modulo-hardcoded", "sol.IdModulo = entrada.p_IdModulo;", "sol.IdModulo = \"XXX\";"),
    ("CrearSolicitudAporteRescateNormal.java", "glosa-overwritten", "sol.Glosa = solicitud.Glosa;", "sol.Glosa = \"mutada\";"),
    ("CrearSolicitudAporteRescateNormal.java", "overnight-S", "sol.Overnight = _c.NO;", "sol.Overnight = _c.SI;"),
    ("CrearSolicitudAporteRescateNormal.java", "transferencia-uses-cuotas", "solicitudSalida.MontoTransferencia = sol.Monto;", "solicitudSalida.MontoTransferencia = sol.NumeroCuotas;"),
    ("ManejadorSolicitudAporteRescate.java", "second-pass-simulates", "in.p_SimularOperacion = \"N\";", "in.p_SimularOperacion = \"S\";"),
    ("ManejadorSolicitudAporteRescate.java", "counter-plus-one", "out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;", "out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR + 1;"),
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
    ap.add_argument("--work", default=os.path.join(os.environ.get("TEMP", "."), "fmm-selfcheck"))
    a = ap.parse_args()
    work = a.work.replace("\\", "/")
    os.makedirs(work, exist_ok=True)
    res = {"controls": [], "passed": False}
    ok = True

    rc, tot, ids, out = compare(os.path.join(work, "legacy-rerun"), self_check=True)
    sc_line = next((ln for ln in out.splitlines() if ln.startswith("self check:")), "self check: <none>")
    c0 = "PASSED" in sc_line
    res["controls"].append({"id": "0-comparator-self-check", "required": "every golden vs one-byte-changed copy is reported different", "result": sc_line, "ok": c0})
    ok &= c0

    r = bash({"ORACLE_WORK": work + "/w-legacy", "ORACLE_OUT": work + "/legacy-rerun"}, [])
    rc, tot, ids, out = compare(os.path.join(work, "legacy-rerun"))
    c1 = r.returncode == 0 and rc == 0 and tot and tot["same"] == tot["cases"]
    res["controls"].append({"id": "1-positive-control-legacy-rerun", "required": "all goldens reproduced by a second run of the legacy", "totals": tot, "ok": bool(c1)})
    ok &= bool(c1)

    for m in MUTANTS:   # classes are already compiled in w-legacy: only the run is repeated
        r = bash({"ORACLE_WORK": work + "/w-legacy", "ORACLE_SKIP_COMPILE": "1", "ORACLE_OUT": work + "/mut-" + m, "ORACLE_SUBJECT": "cl.bcs.oracle.MutantSubject",
                  "ORACLE_MUTANT": m}, [])
        rc, tot, ids, out = compare(os.path.join(work, "mut-" + m))
        c = r.returncode == 0 and rc == 1 and tot and tot["differs"] >= 1
        res["controls"].append({"id": "2-subject-mutant-" + m, "required": ">= 1 case differs", "totals": tot, "casesThatDetect": ids, "ok": bool(c)})
        ok &= bool(c)

    for fname, name, old, new in SOURCE_MUTATIONS:
        src = open(os.path.join(FM, fname), encoding="utf-8").read()
        if old not in src:
            res["controls"].append({"id": "3-source-mutation-" + name, "ok": False, "result": "pattern not found in the legacy source: update self_check.py"})
            ok = False
            continue
        ov = work + "/overlay-" + name
        os.makedirs(ov, exist_ok=True)
        with open(ov + "/" + fname, "w", encoding="utf-8", newline="") as f:
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
