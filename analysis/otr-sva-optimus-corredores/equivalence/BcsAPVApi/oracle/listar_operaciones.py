# -*- coding: utf-8 -*-
"""Writes ops.json: the 64 invocable APV operations with the legacy binary names of their Entrada / Salida classes and the Gson flavour of call().

Uses the generator's legacy reader (modernized/.../BcsAPVApi/tools: modelo, generar.construir_plan) ONLY to know which classes to ask the real legacy
classes for; nothing is written to the module and the generator's write mode is never called. The Gson flavour is read again from the body of call().
Usage:  py -3 -I listar_operaciones.py <ops.json>
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
WS = os.path.abspath(os.path.join(HERE, '..', '..', '..', '..', '..'))
TOOLS = os.path.join(WS, 'modernized', 'otr-sva-optimus-corredores', 'BcsAPVApi', 'tools')
sys.dont_write_bytecode = True
sys.path.insert(0, TOOLS)

import generar            # noqa: E402
import legacy_parser as lp  # noqa: E402


def binario(clase):
    archivo, ruta = clase.key
    if archivo.startswith('pojo/'):
        return 'cl.bcs.APV.pojo.' + '$'.join(ruta)
    return 'cl.bcs.APV.' + '$'.join(ruta)


def main():
    out = sys.argv[1]
    idx, planes = generar.construir_plan()
    ops = []
    for nombre in sorted(planes):
        p = planes[nombre]
        if p.caso is None:
            continue
        fp = idx.files[nombre]
        mc = re.search(r'public\s+JsonObject\s+call\s*\(', fp['masked'])
        b = fp['masked'].find('{', mc.end())
        e = lp.match_brace(fp['masked'], b)
        body = fp['orig'][b:e]
        g = re.search(r'Gson\s+\w+\s*=\s*([^;]+);', body)
        expr = re.sub(r'\s+', ' ', g.group(1)).strip()
        if expr == 'new Gson()':
            gson = 'plain'
        elif expr == 'Utils.getGsonBuilder()':
            gson = 'builder'
        else:
            raise SystemExit('unexpected Gson expression in %s: %s' % (nombre, expr))
        if not re.search(r'\bgson\.toJson\(', body):
            raise SystemExit('call() of %s does not serialise with the Gson variable' % nombre)
        ops.append({'legacy': p.op.fqn, 'nombre': nombre, 'caso': p.caso, 'familia': p.familia, 'gson': gson,
                    'entrada': binario(p.op.entrada), 'salida': binario(p.op.salida),
                    'mapeo': 'cl.bcs.apv.adapter.javacall.mapeo.%sMapeo' % p.caso})
    with open(out, 'w', encoding='utf-8', newline='\n') as fh:
        json.dump({'operaciones': ops}, fh, indent=1, ensure_ascii=False)
        fh.write('\n')
    print('operations: %d (plain %d, builder %d)' % (len(ops), sum(o['gson'] == 'plain' for o in ops), sum(o['gson'] == 'builder' for o in ops)))


if __name__ == '__main__':
    main()
