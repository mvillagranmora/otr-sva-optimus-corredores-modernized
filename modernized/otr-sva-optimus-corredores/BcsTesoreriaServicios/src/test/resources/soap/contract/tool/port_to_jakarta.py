#!/usr/bin/env python3
"""One-shot mechanical port of the legacy wsimport/wsdl2java types to the modern package (javax -> jakarta).

Only a namespace/package rewrite: javax.xml.bind -> jakarta.xml.bind, javax.jws -> jakarta.jws,
javax.xml.ws -> jakarta.xml.ws, package cl.bcs.tesoreria.webservices.recaudacion ->
cl.bcs.tesoreria.infrastructure.soap. Annotation values, names, propOrder and structure are untouched.

Ported: every type except the *_SoapHttpPortImpl (logic, lives in the modern *Endpoint classes), the
*_Server mains (Endpoint.publish bootstrap) and CXFStartServlet (CXF/servlet wiring) - those three groups are
runtime wiring and stay a declared blocker.

Usage: py -3 -I port_to_jakarta.py --legacy-dir <recaudacion dir> --out-dir <.../infrastructure/soap>
"""
import argparse
import os
import re

SKIP = re.compile(r"(_SoapHttpPortImpl|_Server|CXFStartServlet)\.java$")
OLD_PKG = "cl.bcs.tesoreria.webservices.recaudacion"
NEW_PKG = "cl.bcs.tesoreria.infrastructure.soap"


def read_text(path):
    raw = open(path, "rb").read()
    for enc in ("utf-8", "cp1252"):
        try:
            return raw.decode(enc)
        except UnicodeDecodeError:
            pass
    return raw.decode("latin-1")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--legacy-dir", required=True)
    ap.add_argument("--out-dir", required=True)
    a = ap.parse_args()
    n = 0
    for f in sorted(os.listdir(a.legacy_dir)):
        if not f.endswith(".java") or SKIP.search(f):
            continue
        s = read_text(os.path.join(a.legacy_dir, f))
        s = s.replace(OLD_PKG, NEW_PKG)
        s = s.replace("javax.xml.bind", "jakarta.xml.bind").replace("javax.jws", "jakarta.jws")
        s = s.replace("javax.xml.ws", "jakarta.xml.ws")
        s = s.replace("\r\n", "\n")
        with open(os.path.join(a.out_dir, f), "w", encoding="utf-8", newline="\n") as o:
            o.write(s)
        n += 1
    print("ported %d files" % n)


if __name__ == "__main__":
    main()
