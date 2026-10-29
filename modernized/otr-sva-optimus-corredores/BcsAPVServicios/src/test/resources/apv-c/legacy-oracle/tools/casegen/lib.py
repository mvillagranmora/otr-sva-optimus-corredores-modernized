"""Case input generator of APV family C (recaudacion, PAC, bonificacion, cargas masivas, distribucion): helpers.

WHAT THIS IS: the SOURCE of the INPUTS of every case (cases/APV-C-NNN_slug/case.json and seed.sql). The EXPECTED values are never written here:
expected.json is recorded by legacy-oracle/record.sh executing the unmodified legacy class. Regenerating the inputs rewrites case.json/seed.sql
only; run record.sh afterwards for the cases whose inputs changed.
"""
import json
import os
import re

CASOS = []


def _legacy_apv():
    """Legacy APV source folder (read only): LEGACY_ROOT (the BSCL folder) or the workspace found by walking up from this file."""
    root = os.environ.get('LEGACY_ROOT')
    if not root:
        d = os.path.abspath(os.path.dirname(__file__))
        while d and not os.path.isdir(os.path.join(d, 'legacy')):
            n = os.path.dirname(d)
            if n == d:
                raise SystemExit('workspace root not found')
            d = n
        root = os.path.join(d, 'legacy', 'otr-sva-optimus-corredores', 'BSCL')
    return os.path.join(root, 'APV', 'JAVA', 'BcsAPVServicios', 'src', 'cl', 'bcs', 'APV')


_FUENTES = {}


def _lineas(archivo):
    if archivo not in _FUENTES:
        with open(os.path.join(_legacy_apv(), archivo), encoding='utf-8', errors='replace') as f:
            _FUENTES[archivo] = f.read().split(chr(10))
    return _FUENTES[archivo]


def L(archivo, inicio, fin=None):
    """Citation of the legacy lines a case pins, resolved from the SOURCE at generation time: the first line matching the regex `inicio`
    (and, with `fin`, the first line matching `fin` after it). A regex that matches nothing aborts the generation: no citation is ever guessed."""
    ls = _lineas(archivo)
    a = next((i for i, t in enumerate(ls) if re.search(inicio, t)), None)
    if a is None:
        raise SystemExit('anchor not found in %s: %s' % (archivo, inicio))
    if fin is None:
        return '%s:%d' % (archivo, a + 1)
    b = next((i for i in range(a, len(ls)) if re.search(fin, ls[i])), None)
    if b is None:
        raise SystemExit('end anchor not found in %s: %s' % (archivo, fin))
    return '%s:%d-%d' % (archivo, a + 1, b + 1)


# --------------------------------------------------------------------------------------------- SQL helpers
def lit(v):
    if v is None:
        return 'NULL'
    if isinstance(v, bool):
        return 'TRUE' if v else 'FALSE'
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def ins(table, **cols):
    names = ', '.join('"%s"' % k for k in cols)
    vals = ', '.join(lit(v) for v in cols.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (table, names, vals)


def seed(*stmts):
    out = []

    def plano(x):
        if isinstance(x, (list, tuple)):
            for y in x:
                plano(y)
        elif x:
            out.append(x)
    plano(stmts)
    return '\n'.join(out) + '\n'


def tabla(nombre, orden, columnas=None, enmascarar=None, excluir=None, expresiones=None):
    t = {'nombre': nombre, 'orden': orden}
    if columnas:
        t['columnas'] = columnas
    if enmascarar:
        t['enmascarar'] = enmascarar
    if excluir:
        t['excluir'] = excluir
    if expresiones:
        t['expresiones'] = expresiones
    return t


# --------------------------------------------------------------------------------------------- the case registry
def caso(num, servicio, slug, titulo, pina, entrada, seed_sql='', config=None, secuencias=None, colaboradores=None, tablas=None,
         enmascarar=None, sin_orden=None, metodo=None):
    """num: int (the id is APV-C-NNN); servicio: legacy class simple name; pina: 'File.java:line-range' of the branch the case pins.
    colaboradores: {"<Servicio>": {"salida": {...}} | {"salidas": [...]} | {"lanza": "text"}, "StoredProcedure": {"<proc>": {...}}} (see Hooks)
    enmascarar: keys whose date/timestamp text is written <TS> in the golden output/collaborator trace; "*Key" masks any primitive value of Key.
    metodo: None = the JavaCall entry point call(conn, JSON); "logica" = the TYPED public method logica(conn, SalidaRecaudacionTraspaso, IdEmpresa, IdUsuario) of ValidarMandatosPAC
    (the in-process operation cl.bcs.APV.ValidarMandatosPAC.logica; entrada = {"entrada": {typed fields}, "idEmpresa": n, "idUsuario": n})."""
    cid = 'APV-C-%03d' % num
    for c in CASOS:
        if c['id'] == cid:
            raise SystemExit('duplicate case id ' + cid)
    if len(cid) + 1 + len(slug) > 69:
        raise SystemExit('case directory name too long (Windows MAX_PATH): ' + cid + '_' + slug)
    CASOS.append(dict(id=cid, num=num, servicio=servicio, slug=slug, titulo=titulo, pina=pina, entrada=entrada, seed=seed_sql, config=config,
                      secuencias=secuencias, colaboradores=colaboradores, tablas=tablas or [], enmascarar=enmascarar, sinOrden=sin_orden, metodo=metodo))
    return cid


def escribir(raiz):
    cases = os.path.join(raiz, 'cases')
    os.makedirs(cases, exist_ok=True)
    esperados = set()
    for c in CASOS:
        d = os.path.join(cases, c['id'] + '_' + c['slug'])
        esperados.add(os.path.basename(d))
        os.makedirs(d, exist_ok=True)
        j = {'id': c['id'], 'titulo': c['titulo'], 'pina': c['pina'], 'servicio': c['servicio']}
        if c['metodo']:
            j['metodo'] = c['metodo']
        j['entrada'] = c['entrada']
        if c['config']:
            j['config'] = c['config']
        if c['secuencias']:
            j['secuencias'] = c['secuencias']
        if c['colaboradores']:
            j['colaboradores'] = c['colaboradores']
        if c['enmascarar']:
            j['enmascarar'] = c['enmascarar']
        if c['sinOrden']:
            j['sinOrden'] = c['sinOrden']
        j['tablas'] = c['tablas']
        with open(os.path.join(d, 'case.json'), 'w', encoding='utf-8', newline='\n') as f:
            json.dump(j, f, indent=2, ensure_ascii=False)
            f.write('\n')
        sp = os.path.join(d, 'seed.sql')
        if c['seed']:
            with open(sp, 'w', encoding='utf-8', newline='\n') as f:
                f.write(c['seed'])
        elif os.path.exists(sp):
            os.remove(sp)
    import shutil
    for d in os.listdir(cases):
        if d.startswith('APV-C-') and d not in esperados and os.path.isdir(os.path.join(cases, d)):
            shutil.rmtree(os.path.join(cases, d))   # a renamed or removed case: its directory (case.json, seed, golden) goes with it
    return esperados


def mensajes(*ms):
    """Scripted MensajeSalida list [(id, tipo, codigo, descripcion)] as JSON."""
    return [{'id': i, 'tipo': t, 'codigo': c, 'descripcion': d} for (i, t, c, d) in ms]
