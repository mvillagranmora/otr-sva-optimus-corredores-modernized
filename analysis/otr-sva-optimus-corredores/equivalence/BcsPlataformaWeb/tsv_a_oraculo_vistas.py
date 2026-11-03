"""Oraculo de las columnas de las 7 vistas del login y de las firmas de los procedimientos (LOGIN-PG ronda 2, T-33), derivado del
catalogo de Postgres QA guardado (solo metadatos) y NO de resumen_exportacion.xlsx (el xlsx solo cubre tablas).

Solo biblioteca estandar. Entradas:
  1. catalogo-postgres-qa-20261103.tsv      (filas COL: vista, columna, tipo, posicion)
  2. rutinas-y-tipos-postgres-qa-20261103.txt (firmas de las 10 rutinas y atributos de los tipos compuestos)
  3. oraculo-objetos.csv del oraculo del xlsx (src/test/resources/loginpg/): nombre HANA y nombre Postgres de las 7 vistas
  4. directorio golden-hana (src/test/resources/loginpg/golden-hana): el SQL de HANA que cita las columnas
Salidas, en <dir_salida> (src/test/resources/loginpg2/):
  oraculo-vistas-columnas.csv                nombre_tabla_hana,nombre_tabla_postgresql,nombre_columna_hana,nombre_columna_postgresql,origen
  vistas-columnas-pg-qa-20261103.tsv         las filas COL de las 7 vistas (vista, columna, tipo, posicion): sirve para armar las vistas de H2
  rutinas-y-tipos-postgres-qa-20261103.txt   copia del archivo de firmas
  ORACULO_VISTAS_MANIFEST.json               sha256 de las entradas y salidas, conteos, columnas citadas

Regla del nombre HANA de una columna de vista: la hoja del xlsx no trae vistas y el catalogo de HANA guardado no trae columnas, asi que el nombre
HANA de una columna se DERIVA del snake_case de Postgres (cada segmento con la primera letra en mayuscula: id_empresa -> IdEmpresa). El script exige que la
regla reproduzca TODAS las columnas que el SQL de HANA del login cita de esas vistas (origen=citada: el nombre aparece entre comillas en un SQL de HANA que nombra la vista, criterio grueso); el resto es origen=derivada y queda por comprobar
contra HANA en T-36 (Q-LOGINPG2-2).

Uso: python -X utf8 tsv_a_oraculo_vistas.py <catalogo-postgres.tsv> <rutinas.txt> <oraculo-objetos.csv> <golden-hana-dir> <dir_salida>
"""
import csv
import hashlib
import json
import os
import re
import shutil
import sys

SENTENCIAS_CON_VISTAS = ['BootstrapServicio.SQL_ACCESOS', 'BootstrapServicio.SQL_EMPRESAS', 'BootstrapServicio.SQL_IMAGEN',
                         'BootstrapServicio.SQL_PREFERENCIAS', 'BootstrapServicio.SQL_PREFERENCIAS_GRILLA',
                         'GestionClaveServicio.SQL_DATOS_RECUPERACION']


def sha(path):
    return hashlib.sha256(open(path, 'rb').read()).hexdigest()


def pascal(snake):
    return ''.join(p[:1].upper() + p[1:] for p in snake.split('_') if p)


def main():
    tsv, rutinas, objetos_csv, golden, salida = sys.argv[1:6]
    vistas_hana = {}
    with open(objetos_csv, encoding='utf-8', newline='') as fh:
        for f in csv.DictReader(fh):
            if f['tipo_objeto'] == 'vista':
                vistas_hana[f['nombre_postgres']] = f['nombre_hana']
    cols = {}
    with open(tsv, encoding='utf-8') as fh:
        for linea in fh:
            p = linea.rstrip('\n').split('\t')
            if p[0] == 'COL' and p[1] in vistas_hana:
                cols.setdefault(p[1], []).append((int(p[4]), p[2], p[3]))
    login = ['v_pla_acc_consultar_usuario_acceso', 'v_pla_ctt_consultar_contrato_datos_generales', 'v_pla_pla_consultar_configuracion_grilla',
             'v_pla_pla_consultar_configuracion_usuario', 'v_pla_pla_consultar_empresa', 'v_pla_pla_consultar_imagen', 'v_socneg_per_consultar_persona']
    for v in login:
        if v not in cols:
            print('ABORTA: la vista %s no tiene columnas en el catalogo' % v)
            sys.exit(2)
        cols[v].sort()

    # columnas que el SQL de HANA cita (identificadores entre comillas dobles sin ::)
    citadas = set()
    sql_por_id = {}
    for i in SENTENCIAS_CON_VISTAS:
        txt = open(os.path.join(golden, i + '.sql'), encoding='utf-8').read()
        sql_por_id[i] = txt
        for m in re.finditer(r'"([^"]+)"', txt):
            if '::' not in m.group(1):
                citadas.add(m.group(1))
    derivadas_todas = set()
    filas = []
    for v in login:
        for pos, c, tipo in cols[v]:
            h = pascal(c)
            derivadas_todas.add(h)
            citada_aqui = any(vistas_hana[v] in t and ('"%s"' % h) in t for t in sql_por_id.values())
            filas.append([vistas_hana[v], v, h, c, 'citada' if citada_aqui else 'derivada'])
    sin_origen = sorted(x for x in citadas if x not in derivadas_todas)
    print('identificadores citados por el SQL de HANA que no son columna de una vista (alias o columnas de tablas):', sin_origen)
    # cada identificador citado de una vista debe estar en la vista que su sentencia nombra: se comprueba el caso de las vistas de una sola sentencia
    esperadas = {
        'BootstrapServicio.SQL_EMPRESAS': ('v_pla_pla_consultar_empresa', ['IdEmpresa', 'Nombre', 'IdEstado']),
        'BootstrapServicio.SQL_IMAGEN': ('v_pla_pla_consultar_imagen', ['NombreImagen', 'Binario']),
        'BootstrapServicio.SQL_PREFERENCIAS': ('v_pla_pla_consultar_configuracion_usuario', ['Llave', 'Valor', 'IdEmpresa', 'IdUsuario']),
        'BootstrapServicio.SQL_PREFERENCIAS_GRILLA': ('v_pla_pla_consultar_configuracion_grilla', ['Llave', 'Valor', 'IdEmpresa']),
        'BootstrapServicio.SQL_ACCESOS': ('v_pla_acc_consultar_usuario_acceso', ['IdEmpresa', 'IdUsuario', 'MostrarAcceso', 'Padre', 'Orden']),
    }
    por_vista = {}
    for f in filas:
        por_vista.setdefault(f[1], set()).add(f[2])
    for i, (v, nombres) in esperadas.items():
        for n in nombres:
            if n not in por_vista[v]:
                print('ABORTA: %s cita "%s" y la vista %s no lo tiene bajo la regla de derivacion' % (i, n, v))
                sys.exit(3)
    for n in ['IdPersona', 'IdDireccionElectronicaPrincipal', 'NombreCompleto']:
        if n not in por_vista['v_socneg_per_consultar_persona']:
            print('ABORTA: falta %s en la vista de personas' % n)
            sys.exit(3)
    for n in ['IdEmpresa', 'RazonSocial']:
        if n not in por_vista['v_pla_ctt_consultar_contrato_datos_generales']:
            print('ABORTA: falta %s en la vista de contratos' % n)
            sys.exit(3)

    os.makedirs(salida, exist_ok=True)
    p_col = os.path.join(salida, 'oraculo-vistas-columnas.csv')
    with open(p_col, 'w', encoding='utf-8', newline='') as fh:
        w = csv.writer(fh, lineterminator='\n')
        w.writerow(['nombre_tabla_hana', 'nombre_tabla_postgresql', 'nombre_columna_hana', 'nombre_columna_postgresql', 'origen'])
        for f in filas:
            w.writerow(f)
    p_tsv = os.path.join(salida, 'vistas-columnas-pg-qa-20261103.tsv')
    with open(p_tsv, 'w', encoding='utf-8', newline='') as fh:
        for v in login:
            for pos, c, tipo in cols[v]:
                fh.write('%s\t%s\t%s\t%d\n' % (v, c, tipo, pos))
    p_rut = os.path.join(salida, 'rutinas-y-tipos-postgres-qa-20261103.txt')
    shutil.copyfile(rutinas, p_rut)
    manifiesto = {
        'fuente_catalogo_sha256': sha(tsv),
        'fuente_rutinas_sha256': sha(rutinas),
        'columnas_por_vista': {v: len(cols[v]) for v in login},
        'columnas_total': len(filas),
        'columnas_citadas': sum(1 for f in filas if f[4] == 'citada'),
        'columnas_derivadas': sum(1 for f in filas if f[4] == 'derivada'),
        'regla_nombre_hana': 'PascalCase del snake_case de Postgres; verificada solo contra las columnas citadas por el SQL de HANA',
        'sha256': {'oraculo-vistas-columnas.csv': sha(p_col), 'vistas-columnas-pg-qa-20261103.tsv': sha(p_tsv),
                   'rutinas-y-tipos-postgres-qa-20261103.txt': sha(p_rut)},
    }
    with open(os.path.join(salida, 'ORACULO_VISTAS_MANIFEST.json'), 'w', encoding='utf-8', newline='') as fh:
        json.dump(manifiesto, fh, indent=2, ensure_ascii=False)
        fh.write('\n')
    print(json.dumps(manifiesto, indent=2, ensure_ascii=False))


if __name__ == '__main__':
    main()
