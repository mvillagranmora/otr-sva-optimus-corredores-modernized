"""Case input generator of BcsAPVJobs: helpers.

WHAT THIS IS: the SOURCE of the INPUTS of every case (cases/<id>_<slug>/case.json and seed.sql). The EXPECTED values are never written here:
expected.json is recorded by legacy-oracle/record.sh executing the unmodified legacy job class. Regenerating the inputs rewrites case.json / seed.sql
only; run record.sh afterwards for the cases whose inputs changed.
"""
import json
import os
import re

CASOS = []

T_PROGRAMA = 'BSCL.Plataforma.BD::BAT.Programa'
T_PARAMS = 'BSCL.Plataforma.BD::BAT.ParametrosPrograma'
T_LOG = 'BSCL.Plataforma.BD::BAT.LogPrograma'


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
        else:
            out.append(x)
    plano(stmts)
    return '\n'.join(out) + '\n'


def tabla(nombre, orden, columnas=None, enmascarar=None, excluir=None):
    t = {'nombre': nombre, 'orden': orden}
    if columnas:
        t['columnas'] = columnas
    if enmascarar:
        t['enmascarar'] = enmascarar
    if excluir:
        t['excluir'] = excluir
    return t


def programa(idp, ruta=None):
    return ins(T_PROGRAMA, IdPrograma=idp, RutaSalida=ruta)


def parametros(idp, *pares):
    """BAT.ParametrosPrograma rows (IdSecuencia 1..n): pares = (NombreParametro, Valor)."""
    return [ins(T_PARAMS, IdPrograma=idp, IdSecuencia=i + 1, NombreParametro=n, Valor=v) for i, (n, v) in enumerate(pares)]


TABLAS_BASE = [tabla(T_PROGRAMA, '"IdPrograma"', ['IdPrograma', 'RutaSalida'], enmascarar=['RutaSalida']),
               tabla(T_LOG, '"IdPrograma", "Mensaje"', ['IdTipoPrograma', 'IdPrograma', 'Mensaje'])]


def caso(prefijo, num, job, slug, titulo, pina, idp=777, parametros=None, seed_sql='', config=None, colaboradores=None, tablas=None,
         carpetas=None, pin_mensaje=False, rule=None):
    """prefijo: CMP | OBF | FMB | RVB; pina: legacy 'File.java:line-range' the case pins (cited, never guessed)."""
    cid = 'APVJ-%s-%03d' % (prefijo, num)
    if any(c['id'] == cid for c in CASOS):
        raise SystemExit('duplicate case id ' + cid)
    c = {'id': cid, 'slug': slug, 'titulo': titulo, 'pina': pina, 'job': job, 'idPrograma': idp,
         'parametros': parametros or {}, 'carpetas': carpetas if carpetas is not None else [],
         'tablas': tablas if tablas is not None else TABLAS_BASE}
    if config:
        c['config'] = config
    if colaboradores:
        c['colaboradores'] = colaboradores
    if pin_mensaje:
        c['pinMensaje'] = True
    if rule:
        c['rule'] = rule
    c['_seed'] = seed_sql
    CASOS.append(c)
    return c


def escribir(raiz):
    carpeta = os.path.join(raiz, 'cases')
    os.makedirs(carpeta, exist_ok=True)
    for c in CASOS:
        d = os.path.join(carpeta, c['id'] + '_' + c['slug'])
        os.makedirs(d, exist_ok=True)
        cuerpo = {k: v for k, v in c.items() if not k.startswith('_')}
        with open(os.path.join(d, 'case.json'), 'w', encoding='utf-8', newline='\n') as f:
            f.write(json.dumps(cuerpo, indent=2, ensure_ascii=False) + '\n')
        s = os.path.join(d, 'seed.sql')
        if c['_seed']:
            with open(s, 'w', encoding='utf-8', newline='\n') as f:
                f.write(c['_seed'])
        elif os.path.exists(s):
            os.remove(s)
    return len(CASOS)
