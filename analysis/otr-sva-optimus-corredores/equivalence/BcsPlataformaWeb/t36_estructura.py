"""T-36: reduce las respuestas completas de bootstrap (guardadas fuera del repositorio por t36_bootstrap.py) a su ESTRUCTURA: claves y tipos, sin ningun valor.
- objeto con pocas claves: cada clave con la estructura de su valor;
- objeto con muchas claves, o con claves que parecen datos, o con mas de 5 claves cuyos valores tienen todos la misma estructura (mapas de datos: diccionario, preferencias, formatos por moneda...): solo "<mapa: N claves>" y la estructura de los tipos de sus valores, sin los nombres de las claves;
- lista: su largo y la estructura de la union de sus elementos;
- escalar: solo el nombre del tipo ("string", "number", "boolean", "null").
No contiene nombres, ids, fechas, textos ni credenciales. Uso: python -X utf8 t36_estructura.py <carpeta con los JSON completos> (salida junto a este script)."""
import json, os, sys

LIMITE_CLAVES = 40
EQ = os.path.dirname(os.path.abspath(__file__)).replace('\\', '/')


def tipo(v):
    if v is None:
        return 'null'
    if isinstance(v, bool):
        return 'boolean'
    if isinstance(v, (int, float)):
        return 'number'
    return 'string'


def fusiona(a, b):
    """union de dos estructuras del mismo nivel"""
    if a == b:
        return a
    if isinstance(a, dict) and isinstance(b, dict):
        r = dict(a)
        for k, v in b.items():
            r[k] = fusiona(r[k], v) if k in r else v
        return r
    if isinstance(a, list) and isinstance(b, list):
        return [fusiona(a[0], b[0])] if a and b else (a or b)
    uniones = sorted(set((a if isinstance(a, str) else '<estructura>').split('|')) | set((b if isinstance(b, str) else '<estructura>').split('|')))
    return '|'.join(uniones)


def estructura(v):
    if isinstance(v, dict):
        if len(v) > LIMITE_CLAVES or any('::' in k or k.isdigit() for k in v):
            valores = None
            for x in v.values():
                e = estructura(x)
                valores = e if valores is None else fusiona(valores, e)
            return {'<mapa>': len(v), '<valores>': valores}
        hijos = {k: estructura(x) for k, x in v.items()}
        if len(v) > 5 and len({json.dumps(h, sort_keys=True) for h in hijos.values()}) == 1:
            # muchas claves con valores de la misma estructura: es un mapa de datos, no una estructura fija
            return {'<mapa>': len(v), '<valores>': next(iter(hijos.values()))}
        return hijos
    if isinstance(v, list):
        elemento = None
        for x in v:
            e = estructura(x)
            elemento = e if elemento is None else fusiona(elemento, e)
        return {'<lista>': len(v), '<elemento>': elemento}
    return tipo(v)


carpeta = sys.argv[1]
for k in ('login', 'home'):
    for m in ('hana', 'postgres'):
        v = json.load(open('%s/t36-%s-%s-20261103.json' % (carpeta, k, m), encoding='utf-8'))
        s = estructura(v)
        json.dump(s, open('%s/t36-estructura-%s-%s.json' % (EQ, k, m), 'w', encoding='utf-8', newline='\n'), ensure_ascii=False, indent=1, sort_keys=True)
        print(k, m, 'ok')
