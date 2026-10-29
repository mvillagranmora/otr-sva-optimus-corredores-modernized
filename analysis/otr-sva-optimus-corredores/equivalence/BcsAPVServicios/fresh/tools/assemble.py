import glob, json, os, shutil, re

DUMP = r"V:\dumpnew"
RES = r"V:\modernized\otr-sva-optimus-corredores\BcsAPVServicios\src\test\resources"
TST = r"V:\modernized\otr-sva-optimus-corredores\BcsAPVServicios\src\test\java\cl\bcs\apv"
BASE = r"C:\Users\m.villagran.mora\Downloads\develop\nuam-modernizado\analysis\otr-sva-optimus-corredores\equivalence\BcsAPVServicios"
FRESH = os.path.join(BASE, 'fresh')
fam_dir = {'a': 'apv-a', 'b': 'familia_b', 'c': 'familia_c'}

cases = []
for fam in 'abc':
    for d in sorted(glob.glob(os.path.join(RES, 'apv-' + fam, 'cases', 'FRESH-*'))):
        name = os.path.basename(d)
        cid = name.split('_')[0]
        c = json.load(open(os.path.join(d, 'case.json'), encoding='utf-8'))
        # inputs
        idir = os.path.join(FRESH, 'inputs', cid)
        os.makedirs(idir, exist_ok=True)
        for f in ('case.json', 'seed.sql'):
            if os.path.exists(os.path.join(d, f)):
                shutil.copyfile(os.path.join(d, f), os.path.join(idir, f))
        files = sorted(glob.glob(os.path.join(DUMP, 'legacy', fam_dir[fam], name + '__*.txt')))
        assert files, name
        for lf in files:
            part = os.path.basename(lf)[len(name) + 2:-4]
            nf = os.path.join(DUMP, 'new', fam_dir[fam], os.path.basename(lf))
            assert os.path.exists(nf), nf
            for side, src in (('legacy', lf), ('new', nf)):
                dd = os.path.join(FRESH, side, cid)
                os.makedirs(dd, exist_ok=True)
                shutil.copyfile(src, os.path.join(dd, part + '.txt'))
            cases.append({'id': cid + '.' + part, 'input': cid, 'title': '%s (%s): %s' % (cid, c['servicio'], c['titulo']) + ' [' + part + ']',
                          'legacy': 'fresh/legacy/%s/%s.txt' % (cid, part), 'new': 'fresh/new/%s/%s.txt' % (cid, part), 'mask': []})

ex = json.load(open(os.path.join(BASE, 'cases.json'), encoding='utf-8'))
out = {
    'system': ex['system'],
    'module': ex['module'],
    'legacy': {'label': 'FRESH inputs (not used in development): the unmodified legacy cl.bcs.APV classes executed on in-memory H2 MODE=LEGACY with the family oracles (record.sh of apv-a / apv-b / apv-c, JVM zone UTC); canonical text of each comparison point as the tests dump it',
               'command': 'ORACLE_WORK=<short dir> bash BcsAPVServicios/src/test/resources/apv-<a|b|c>/legacy-oracle/record.sh FRESH-<A|B|C>  (scratch copy of the module; cases cloned from development cases with a new input, see fresh/tools/mkfresh.py)'},
    'new': {'label': 'modernized BcsAPVServicios src/main through legacy JSON -> BcsApvJavaCallAdapter -> typed use case on H2 -> adapter -> JSON (ApvPipelineDeCaso), scratch copy of apv-service + bcs-apv-api + bcs-apv-javacall-adapter, JDK 25',
            'command': 'mvn -o -f pom.xml -pl :bcs-apv-api,:bcs-apv-javacall-adapter,:apv-service test -Dtest=Fresh*Test -Dsurefire.failIfNoSpecifiedTests=false -Dequivalence.out=<dir>  (scratch classes fresh/tools/Fresh{A,B,C}*Test.java)'},
    'note': 'Each case is one comparison point (answer, table state, collaborator trace, sequences) of a fresh input. The inputs are in fresh/inputs/<id>/{case.json,seed.sql}. No mask is needed: the canonical text already carries <TS> for wall-clock dates and <NPE-MSG> for the JDK NPE message (family A support Canon/Dump), and the clock, sequences and collaborators are fixed on both sides.',
    'cases': cases,
    'leftOut': [
        'Malformed records not attempted: INTENT.md asks for behavioral equivalence on valid-format inputs only.',
        'Not exercised by these fresh inputs: the AutorizacionesController hook (being edited by another agent), the file-based loads (CargaMasiva*), ProcesarDistribucion, GrabarContratoSAAPV and the other classes of the three families; 20 inputs over 11 operations (A: ObtenerFolioSuscripcion, CambiarEstadoContratosSAAPV, BusquedaContratoSAAPVRecaudacion, CambiarEstadoRevocacionSAAPV; B: CalculoMontosDesinversion, CalculoRetencionTraspasos, CambiarEstadoTraspaso; C: CrearBancoPrevired, ModificarBancoPrevired, EliminarBancoPrevired, CrearAnotacionRecaudacion), with the same scripted collaborators outside APV as the development cases.',
        'Same limits as the development oracle: HANA SQL/DDL and views are inferred/seeded, stored procedures only on the Java side, H2 exception texts are not pinned.',
    ],
}
json.dump(out, open(os.path.join(BASE, 'fresh-cases.json'), 'w', encoding='utf-8', newline='\n'), indent=1, ensure_ascii=False)
print(len(cases), 'comparison points;', len({c['input'] for c in cases}), 'inputs')
