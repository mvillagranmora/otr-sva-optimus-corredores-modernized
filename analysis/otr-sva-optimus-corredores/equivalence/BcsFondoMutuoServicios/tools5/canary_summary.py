#!/usr/bin/env python3
"""Summarises equivalence/canary/BcsFondoMutuoServicios-solicitud/<n>.log (written by canary5.sh) into CANARY_SUMMARY.json next to the logs.
  py -3 canary_summary.py <canaryDir>
Per mutation: description, JUnit red run (tests run / failures / errors of the module total), oracle result on the FRESH inputs, green control after the restore.
A mutation is KILLED when the red JUnit run has failures or errors, or the oracle changed at least one fresh dump; the green control must pass.
"""
import json
import os
import re
import sys

TOTAL = re.compile(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: \d+$")


def parse(path):
    text = open(path, encoding="utf-8", errors="replace").read()
    head = text.split("\n")[0]
    red, _, rest = text.partition("--- JUnit on the mutant copy (must be RED)")
    red_part, _, tail = rest.partition("--- fresh inputs through the oracle")
    oracle_part, _, green = tail.partition("--- green control")
    def total(block):
        m = [TOTAL.search(l.strip().replace("[ERROR] ", "").replace("[INFO] ", "")) for l in block.split("\n")]
        m = [x for x in m if x]
        return {"testsRun": int(m[-1].group(1)), "failures": int(m[-1].group(2)), "errors": int(m[-1].group(3))} if m else None
    failed = sorted(set(re.findall(r"\] (cl\.bcs\.\S+?\.\w+Test)\.(\w+) ", red_part)))
    o = re.findall(r"ORACLE: (.*)", oracle_part)
    return {"mutation": head.replace("MUTATION ", ""), "red": total(red_part), "redFailedTests": ["%s.%s" % (a.split(".")[-1], b) for a, b in failed][:12],
            "oracleFresh": o, "green": total(green), "greenBuild": "BUILD SUCCESS" in green}


def main():
    d = sys.argv[1]
    out = {}
    for n in range(1, 9):
        p = os.path.join(d, "%d.log" % n)
        if not os.path.exists(p):
            continue
        r = parse(p)
        red = r["red"] or {}
        r["junitKilled"] = bool(red.get("failures") or red.get("errors"))
        r["oracleKilled"] = any(x.startswith("DETECTED") for x in r["oracleFresh"])
        r["killed"] = r["junitKilled"] or r["oracleKilled"]
        out[str(n)] = r
    out["_summary"] = {"mutations": len(out), "killed": sum(1 for k, v in out.items() if k != "_summary" and v["killed"]),
                       "junitKilled": sum(1 for k, v in out.items() if k != "_summary" and v["junitKilled"]),
                       "oracleKilled": sum(1 for k, v in out.items() if k != "_summary" and v["oracleKilled"])}
    json.dump(out, open(os.path.join(d, "CANARY_SUMMARY.json"), "w", encoding="utf-8", newline="\n"), indent=1, ensure_ascii=False)
    print(json.dumps(out["_summary"]))
    for k, v in out.items():
        if k != "_summary":
            print(k, v["mutation"][:70], "| red:", v["red"], "| oracle:", v["oracleFresh"], "| green:", v["green"], v["greenBuild"])


if __name__ == "__main__":
    main()
