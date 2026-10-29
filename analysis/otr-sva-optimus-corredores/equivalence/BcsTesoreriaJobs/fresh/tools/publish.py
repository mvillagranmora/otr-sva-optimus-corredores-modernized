import json, os, re, shutil, sys, hashlib
EQ = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\analysis\otr-sva-optimus-corredores\equivalence\BcsTesoreriaJobs"
DUMP = r"T:\dump2"
CASES = r"T:\otr\BcsTesoreriaJobs\src\test\resources\tesoreria-jobs\cases"
FR = os.path.join(EQ, "fresh")
ids = sorted(os.listdir(CASES))
pat = re.compile(r"^(.+?)__(.+)$")


def facets(side, cid):
    d = os.path.join(DUMP, side, "tesoreria-jobs")
    return {m.group(2): n for n in os.listdir(d) for m in [pat.match(n)] if m and m.group(1) == cid}


cases = []
for cid in ids:
    fl = facets("legacy", cid)
    fn = facets("new", cid)
    assert set(fl) == set(fn), cid
    for side, fm in (("legacy", fl), ("new", fn)):
        os.makedirs(os.path.join(FR, side, cid), exist_ok=True)
        for fac, n in fm.items():
            shutil.copyfile(os.path.join(DUMP, side, "tesoreria-jobs", n), os.path.join(FR, side, cid, fac + ".txt"))
    os.makedirs(os.path.join(FR, "input", cid), exist_ok=True)
    for f in os.listdir(os.path.join(CASES, cid)):
        if f != "expected.json":
            shutil.copyfile(os.path.join(CASES, cid, f), os.path.join(FR, "input", cid, f))
    title = json.load(open(os.path.join(CASES, cid, "case.json"), encoding="utf-8")).get("descripcion", "")
    for fac in sorted(fl):
        cases.append({"id": cid + "__" + fac, "input": cid, "title": title + " [" + fac + "]",
                      "legacy": "fresh/legacy/%s/%s.txt" % (cid, fac), "new": "fresh/new/%s/%s.txt" % (cid, fac), "mask": []})
doc = {"system": "otr-sva-optimus-corredores", "module": "BcsTesoreriaJobs",
       "legacy": {"label": "fresh inputs recorded by EXECUTING the unmodified legacy BcsTesoreriaJobs classes under the real BcsJob.run() on H2 MODE=LEGACY (legacy-oracle/record.sh on a scratch copy of the module whose cases/ holds only the fresh inputs)",
                  "command": "ORACLE_WORK=T:/ow LEGACY_BSCL=<legacy>/BSCL bash legacy-oracle/record.sh"},
       "new": {"label": "modernized BcsTesoreriaJobs src/main through the same golden test classes on the same fresh inputs",
               "command": "mvn -o -pl :tesoreria-jobs test -Dequivalence.out=T:\\dump -Dtest=*GoldenTest"},
       "note": "Fresh inputs (ids with X) not used in development. Masks live inside the canonical text (Dump: yyyyMMdd_HHmmss -> <TS>, temp base path -> ${RUTA_BASE}, OS error text, NPE text, identity hashes): the only varying values are wall-clock file names/dates. No extra mask is applied here.",
       "cases": cases}
json.dump(doc, open(os.path.join(EQ, "fresh-cases.json"), "w", encoding="utf-8", newline="\n"), indent=2, ensure_ascii=False)

# distinctness check against every development output
dev = os.path.join(EQ, "out", "legacy", "tesoreria-jobs")
devsig = {}
for n in os.listdir(dev):
    m = pat.match(n)
    devsig.setdefault(m.group(1), []).append((m.group(2), open(os.path.join(dev, n), "rb").read()))
devset = {hashlib.sha256(repr(sorted(v)).encode()).hexdigest() for v in devsig.values()}
bad = []
for cid in ids:
    sig = sorted((f[:-4], open(os.path.join(FR, "legacy", cid, f), "rb").read()) for f in os.listdir(os.path.join(FR, "legacy", cid)))
    if hashlib.sha256(repr(sig).encode()).hexdigest() in devset:
        bad.append(cid)
    emp = [f for f, b in sig if f == "archivos" and len(b) < 5]
print("inputs", len(ids), "cases", len(cases), "identical-to-dev-output:", bad)
