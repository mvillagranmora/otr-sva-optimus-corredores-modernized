"""Oraculo independiente del catalogo de equivalencias HANA -> Postgres (T-32), derivado de resumen_exportacion.xlsx.

Solo biblioteca estandar (no hay openpyxl). Lee las hojas `objetos` y `columnas` del xlsx (zip + XML) y escribe, para las pruebas
de BcsPlataformaWeb (src/test/resources/loginpg/):

  oraculo-objetos.csv    tipo_objeto,nombre_hana,nombre_postgres,dominio,modulo_backend      (todas las filas de la hoja)
  oraculo-columnas.csv   nombre_tabla_hana,nombre_tabla_postgresql,nombre_columna_hana,nombre_columna_postgresql
  login-alcance.txt      tipo<TAB>nombre_hana<TAB>nombre_postgres de cada nombre BSCL...::... que usa el codigo de produccion (src/main/java)
  ORACULO_MANIFEST.json  sha256 del xlsx y de cada salida, conteos y hallazgos (duplicados, tipos raros)

Este script NO genera los CSV de produccion (src/main/resources/equivalencias/): eso es de T-32. Si el que implemente reutiliza este
script, las pruebas dejan de ser independientes: se pide escribir el generador de produccion aparte.

Politica de duplicados (la que fijan las pruebas): una clave repetida con la MISMA fila se descarta una vez y se informa; una clave repetida con
valores distintos aborta (codigo 2) porque la tabla es ambigua.

Uso:  python -X utf8 xlsx_a_oraculo.py <resumen_exportacion.xlsx> <dir_salida> <src/main/java del modulo>
"""
import csv
import hashlib
import json
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

NS = {'m': 'http://schemas.openxmlformats.org/spreadsheetml/2006/main',
      'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'}


def col(ref):
    s = re.match(r'[A-Z]+', ref).group(0)
    n = 0
    for ch in s:
        n = n * 26 + ord(ch) - 64
    return n - 1


def hojas(path):
    z = zipfile.ZipFile(path)
    ss = []
    if 'xl/sharedStrings.xml' in z.namelist():
        for si in ET.fromstring(z.read('xl/sharedStrings.xml')).findall('m:si', NS):
            ss.append(''.join(t.text or '' for t in si.iter('{%s}t' % NS['m'])))
    wb = ET.fromstring(z.read('xl/workbook.xml'))
    rels = {r.get('Id'): r.get('Target') for r in ET.fromstring(z.read('xl/_rels/workbook.xml.rels'))}
    out = {}
    for s in wb.find('m:sheets', NS):
        tgt = rels[s.get('{%s}id' % NS['r'])].lstrip('/')
        if not tgt.startswith('xl/'):
            tgt = 'xl/' + tgt
        rows = []
        for row in ET.fromstring(z.read(tgt)).iter('{%s}row' % NS['m']):
            vals = {}
            for c in row.findall('m:c', NS):
                v = c.find('m:v', NS)
                t = c.get('t')
                if t == 's' and v is not None:
                    val = ss[int(v.text)]
                elif t == 'inlineStr':
                    val = ''.join(x.text or '' for x in c.iter('{%s}t' % NS['m']))
                else:
                    val = v.text if v is not None else None
                vals[col(c.get('r'))] = val
            if vals:
                rows.append([vals.get(i) for i in range(max(vals) + 1)])
        out[s.get('name')] = rows
    return out


def sha(path):
    return hashlib.sha256(open(path, 'rb').read()).hexdigest()


def dedup(filas, clave, nombre, hallazgos):
    vistos = {}
    salida = []
    for f in filas:
        k = clave(f)
        if k in vistos:
            if vistos[k] != f:
                print('ABORTA: clave repetida con valores distintos en %s: %s -> %s vs %s' % (nombre, k, vistos[k], f))
                sys.exit(2)
            hallazgos.append('%s: fila repetida identica descartada: %s' % (nombre, k))
            continue
        vistos[k] = f
        salida.append(f)
    return salida


def escribir(path, cabecera, filas):
    with open(path, 'w', encoding='utf-8', newline='') as fh:
        w = csv.writer(fh, lineterminator='\n')
        w.writerow(cabecera)
        for f in filas:
            w.writerow(f)


def main():
    xlsx, salida, fuentes = sys.argv[1], sys.argv[2], sys.argv[3]
    h = hojas(xlsx)
    hallazgos = []
    obj_cab = ['tipo_objeto', 'nombre_hana', 'nombre_postgres', 'dominio', 'modulo_backend']
    col_cab = ['nombre_tabla_hana', 'nombre_tabla_postgresql', 'nombre_columna_hana', 'nombre_columna_postgresql']
    assert h['objetos'][0] == obj_cab, h['objetos'][0]
    assert h['columnas'][0] == col_cab, h['columnas'][0]
    objetos = []
    for f in h['objetos'][1:]:
        f = (f + [None] * 5)[:5]
        if any(c is None or c == '' for c in f[:3]):
            hallazgos.append('objetos: fila con celda vacia en las 3 primeras columnas: %s' % f)
        objetos.append([c if c is not None else '' for c in f])
    columnas = [[c if c is not None else '' for c in (f + [None] * 4)[:4]] for f in h['columnas'][1:]]
    objetos = dedup(objetos, lambda f: f[1], 'objetos(nombre_hana)', hallazgos)
    columnas = dedup(columnas, lambda f: (f[0], f[2]), 'columnas(tabla,columna)', hallazgos)

    for f in objetos:
        for c in f:
            if c != c.strip():
                hallazgos.append('objetos: espacios en los extremos: %r' % f)
    for f in objetos:
        if f[0] == 'procedimiento' and f[2].startswith('fn_'):
            hallazgos.append('objetos: tipo_objeto=procedimiento pero nombre_postgres es funcion (fn_): %s -> %s' % (f[1], f[2]))
        if f[2] in ('NO_DISPONIBLE', ''):
            hallazgos.append('objetos: marcado %r: %s' % (f[2], f[1]))
    if not any(f[2] == 'NO_DISPONIBLE' for f in objetos):
        hallazgos.append('objetos: NINGUNA fila trae NO_DISPONIBLE en esta version de la tabla (la marca existe solo en el contrato de las pruebas)')

    p_obj = salida + '/oraculo-objetos.csv'
    p_col = salida + '/oraculo-columnas.csv'
    escribir(p_obj, obj_cab, objetos)
    escribir(p_col, col_cab, columnas)

    # alcance: nombres HANA que cita el codigo de produccion (mismo regex con que se midieron los 55 de TODO_MVP.md 10.5)
    import os
    nombres = set()
    for raiz, _, archivos in os.walk(fuentes):
        for a in archivos:
            if a.endswith('.java'):
                txt = open(os.path.join(raiz, a), encoding='utf-8').read()
                nombres.update(re.findall(r'BSCL\.[A-Za-z.]*::[A-Za-z.]*', txt))
    por_hana = {f[1]: f for f in objetos}
    alcance = []
    for n in sorted(nombres):
        if n not in por_hana:
            print('ABORTA: el codigo cita un nombre sin equivalencia: %s' % n)
            sys.exit(3)
        alcance.append([por_hana[n][0], n, por_hana[n][2]])
    p_alc = salida + '/login-alcance.txt'
    with open(p_alc, 'w', encoding='utf-8', newline='') as fh:
        for t, n, p in alcance:
            fh.write('%s\t%s\t%s\n' % (t, n, p))

    cuenta = {}
    for t, _, _ in alcance:
        cuenta[t] = cuenta.get(t, 0) + 1
    manifiesto = {
        'xlsx_sha256': sha(xlsx),
        'filas_objetos': len(objetos),
        'filas_columnas': len(columnas),
        'tablas_con_columnas': len(set(f[0] for f in columnas)),
        'alcance_nombres': len(alcance),
        'alcance_por_tipo': cuenta,
        'sha256': {'oraculo-objetos.csv': sha(p_obj), 'oraculo-columnas.csv': sha(p_col), 'login-alcance.txt': sha(p_alc)},
        'hallazgos': hallazgos,
    }
    with open(salida + '/ORACULO_MANIFEST.json', 'w', encoding='utf-8', newline='') as fh:
        json.dump(manifiesto, fh, indent=2, ensure_ascii=False)
        fh.write('\n')
    print(json.dumps(manifiesto, indent=2, ensure_ascii=False))


if __name__ == '__main__':
    main()
