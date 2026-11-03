"""Clasifica los resultados de surefire de una corrida de las pruebas LOGIN-PG3 (ajuste sin catalogo en produccion): cuantas en verde, cuantas en rojo y POR QUE.

La fase roja solo es valida si TODA falla por 'LOGIN-PG3 not implemented' (falta lo que pide el contrato nuevo), por 'LOGIN-PG3 catalog still in production'
(sigue lo que debe desaparecer) o por el guardia de cero casos: cualquier otro motivo (un error de compilacion de la prueba, una prueba mal escrita, un fixture
ausente) se lista aparte y hay que arreglarlo. Las clases que caen a nivel de contenedor (contexto Spring que no arranca) cuentan una vez.

Uso: python -X utf8 clasifica_rojo3.py <dir surefire-reports> <patron de clase, p.ej. LoginPg> [--detalle] [--desde <epoch segundos>]
  --desde ignora los TEST-*.xml mas viejos que ese instante (restos de corridas anteriores).
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET

MARCAS = ('LOGIN-PG3 not implemented', 'LOGIN-PG3 catalog still in production')


def main():
    d, patron = sys.argv[1], sys.argv[2]
    detalle = '--detalle' in sys.argv
    desde = 0.0
    if '--desde' in sys.argv:
        desde = float(sys.argv[sys.argv.index('--desde') + 1])
    total = ok = omitidas = 0
    rojo_nuevo = []
    rojo_catalogo = []
    otros = []
    por_clase = {}
    for f in sorted(glob.glob(os.path.join(d, 'TEST-*.xml'))):
        if os.path.getmtime(f) < desde:
            continue
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
                msg = (fallo.get('message') or '') + '\n' + (fallo.text or '')[:2500]
                c[1] += 1
                if MARCAS[0] in msg:
                    rojo_nuevo.append((nombre, tc.get('name'), msg.split('\n')[0][:200]))
                elif MARCAS[1] in msg:
                    rojo_catalogo.append((nombre, tc.get('name'), msg.split('\n')[0][:200]))
                else:
                    otros.append((nombre, tc.get('name'), msg.split('\n')[0][:300], msg))
    print('casos: %d  verde: %d  rojo por "LOGIN-PG3 not implemented": %d  rojo por "LOGIN-PG3 catalog still in production": %d  rojo por OTRO motivo: %d  omitidas: %d'
          % (total, ok, len(rojo_nuevo), len(rojo_catalogo), len(otros), omitidas))
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
