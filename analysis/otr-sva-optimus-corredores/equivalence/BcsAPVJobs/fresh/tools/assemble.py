import glob, os, json, shutil, sys
dump, base = sys.argv[1], sys.argv[2]
cases = []
n = 0
names = sorted(os.path.basename(p) for p in glob.glob(os.path.join(dump, 'legacy', 'apv-jobs', '*.txt')))
for nm in names:
    full, facet = nm.split('__')
    cid = full.split('_')[0]
    for side in ('legacy', 'new'):
        d = os.path.join(base, 'fresh', side, cid)
        os.makedirs(d, exist_ok=True)
        shutil.copyfile(os.path.join(dump, side, 'apv-jobs', nm), os.path.join(d, facet))
    n += 1
    cases.append({'id': 'FRESH-%03d' % n, 'input': cid, 'title': 'fresh %s %s (%s)' % (cid, facet[:-4], full[len(cid) + 1:]),
                  'legacy': 'fresh/legacy/%s/%s' % (cid, facet), 'new': 'fresh/new/%s/%s' % (cid, facet), 'mask': []})
doc = {'system': 'otr-sva-optimus-corredores', 'module': 'BcsAPVJobs',
       'legacy': {'label': 'unmodified legacy BcsAPVJobs job classes (4) under the unmodified BcsJob.run() on in-memory H2 MODE=LEGACY, JVM UTC, en-US; oracle: BcsAPVJobs/src/test/resources/apv-jobs/legacy-oracle (record.sh) run on a scratch copy with 22 fresh cases APVJ-<job>-1xx',
               'command': 'LEGACY_ROOT=<legacy>/BSCL ORACLE_WORK=X:/ow bash legacy-oracle/record.sh APVJ-  (scratch copy)'},
       'new': {'label': 'modernized BcsAPVJobs src/main (unchanged) run on the same H2 seed, scripted collaborators and configuration, JDK 25',
               'command': 'mvn -o -pl :apv-jobs test -Dtest=*CasosTest -Dequivalence.out=<dir> (scratch copy with the fresh cases only)'},
       'note': 'Fresh inputs not used by development (ids 101+ per job). No masks added: stamp <TS>, <BASE> and <NPE-MSG> are already normalised inside the canonical text by the module Dump. Only valid-format inputs (INTENT.md).',
       'cases': cases}
json.dump(doc, open(os.path.join(base, 'fresh-cases.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
print(len(cases), 'comparisons')
