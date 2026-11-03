"""Prueba que las pruebas LOGIN-PG3 FALLAN (nunca se omiten) cuando falta un fixture: en una COPIA del modulo con la implementacion desechable (en verde) se mueve
fuera, de a una, cada carpeta de fixtures (loginpg, loginpg2, loginpg3), se corre la suite LoginPg y se vuelve a dejar en su lugar. Una corrida que termina en verde
o con pruebas omitidas es un fallo de las pruebas.

Uso: python -X utf8 fixture_ausente_loginpg3.py <dir del modulo copia> <salida.txt>
"""
import os
import re
import shutil
import subprocess
import sys

MOD = sys.argv[1]
SALIDA = sys.argv[2]
RES = MOD + '/src/test/resources/'
TMP = MOD + '/../fixtures-movidos/'


def correr():
    env = dict(os.environ)
    env['JAVA_HOME'] = 'C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot'
    env['PATH'] = '/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin:' + env['PATH']
    r = subprocess.run('mvn -o test -Dtest="LoginPg*Test,LoginPg2*Test,LoginPg3*Test" -Dsurefire.failIfNoSpecifiedTests=false', shell=True, cwd=MOD, env=env,
                       capture_output=True, text=True, encoding='utf-8', errors='replace')
    return r.stdout


def main():
    os.makedirs(TMP, exist_ok=True)
    informe = []
    for carpeta in ('loginpg', 'loginpg2', 'loginpg3'):
        origen = RES + carpeta
        destino = TMP + carpeta
        # Maven copies src/test/resources to target/test-classes and never deletes what is no longer there: the stale copy must go too
        copia = MOD + '/target/test-classes/' + carpeta
        copia_movida = TMP + 'target-' + carpeta
        shutil.move(origen, destino)
        if os.path.isdir(copia):
            shutil.move(copia, copia_movida)
        try:
            out = correr()
        finally:
            shutil.move(destino, origen)
            if os.path.isdir(copia_movida):
                if os.path.isdir(copia):
                    shutil.rmtree(copia)
                shutil.move(copia_movida, copia)
        resumen = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)\s*$', out, re.M)
        a, f, e, s = map(int, resumen[-1]) if resumen else (0, 0, 0, 0)
        faltantes = len(re.findall(r'FIXTURE MISSING', out))
        linea = 'sin %s/: ejecutadas %d, fallas %d, errores %d, omitidas %d, mensajes FIXTURE MISSING %d, BUILD %s' % (
            carpeta, a, f, e, s, faltantes, 'FAILURE' if 'BUILD FAILURE' in out else 'SUCCESS')
        print(linea, flush=True)
        informe.append(linea)
        if f + e == 0 or s > 0:
            informe.append('  ^ ESTO ES UN FALLO DE LAS PRUEBAS: con el fixture ausente no fallaron o se omitieron')
    open(SALIDA, 'w', encoding='utf-8').write('\n'.join(informe) + '\n')


if __name__ == '__main__':
    main()
