"""Clasifica los resultados de surefire de una corrida de las pruebas LOGIN-PG: cuantas en verde, cuantas en rojo y POR QUE estan en rojo.

La fase roja solo es valida si TODA falla por 'LOGIN-PG2 not implemented' (o por el guardia de cero casos): cualquier otro motivo (un error de
compilacion de la prueba, una prueba mal escrita, un fixture ausente) se lista aparte y hay que arreglarlo.

Uso: python -X utf8 clasifica_rojo.py <dir surefire-reports> <patron de clase, p.ej. LoginPg2> [--detalle]
"""
import glob
import os
import re
import sys
import xml.etree.ElementTree as ET


def main():
    d, patron = sys.argv[1], sys.argv[2]
    detalle = '--detalle' in sys.argv
    total = ok = omitidas = 0
    rojo_pg2 = []
    otros = []
    por_clase = {}
    for f in sorted(glob.glob(os.path.join(d, 'TEST-*.xml'))):
        nombre = os.path.basename(f)[5:-4]
        if patron not in nombre:
            continue
        root = ET.parse(f).getroot()
        for tc in root.iter('testcase'):
            total += 1
            c = por_clase.setdefault(nombre, [0, 0, 0])
            fallo = tc.find('failure')
            if fallo is None:
                fallo = tc.find('error')
            if tc.find('skipped') is not None:
                omitidas += 1
                c[2] += 1
            elif fallo is None:
                ok += 1
                c[0] += 1
            else:
                msg = (fallo.get('message') or '') + '\n' + (fallo.text or '')[:600]
                c[1] += 1
                if 'LOGIN-PG2 not implemented' in msg:
                    rojo_pg2.append((nombre, tc.get('name'), msg.split('\n')[0][:200]))
                else:
                    otros.append((nombre, tc.get('name'), msg.split('\n')[0][:300], msg))
    print('casos: %d  verde: %d  rojo por "LOGIN-PG2 not implemented": %d  rojo por OTRO motivo: %d  omitidas: %d' % (total, ok, len(rojo_pg2), len(otros), omitidas))
    for k, (v, r, o) in sorted(por_clase.items()):
        print('  %-90s verde %4d  rojo %4d  omitidas %d' % (k, v, r, o))
    if otros:
        print('\n--- ROJO POR OTRO MOTIVO (hay que arreglar) ---')
        for n, t, m, full in otros:
            print('%s :: %s\n    %s' % (n, t, m))
            if detalle:
                print(full)
    sys.exit(1 if otros else 0)


if __name__ == '__main__':
    main()
