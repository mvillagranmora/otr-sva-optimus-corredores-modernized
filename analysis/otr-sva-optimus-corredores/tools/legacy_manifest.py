"""SHA-256 manifest of the FROZEN legacy (read only: it never writes under legacy/).

  py -3 -I analysis/otr-sva-optimus-corredores/tools/legacy_manifest.py crear      # writes analysis/otr-sva-optimus-corredores/LEGACY_SHA256.manifest
  py -3 -I analysis/otr-sva-optimus-corredores/tools/legacy_manifest.py verificar  # compares the tree with the manifest

Run `crear` BEFORE `git init` / `git add` and `verificar` after the root repository exists (and after any checkout): every file of legacy/ must be byte-identical.
The .git directories inside legacy/ are excluded: they are going to be removed on purpose.
"""
import hashlib
import os
import sys

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..'))
LEGACY = os.path.join(RAIZ, 'legacy')
MANIFIESTO = os.path.join(RAIZ, 'analysis', 'otr-sva-optimus-corredores', 'LEGACY_SHA256.manifest')


def archivos():
    for d, subs, fs in os.walk(LEGACY):
        subs[:] = [s for s in subs if s != '.git']
        for f in fs:
            yield os.path.join(d, f)


def sha(p):
    h = hashlib.sha256()
    with open(p, 'rb') as fh:
        for bloque in iter(lambda: fh.read(1 << 20), b''):
            h.update(bloque)
    return h.hexdigest()


def crear():
    n = 0
    with open(MANIFIESTO, 'w', encoding='utf-8', newline='\n') as out:
        for p in sorted(archivos()):
            out.write('%s  %s\n' % (sha(p), os.path.relpath(p, RAIZ).replace(os.sep, '/')))
            n += 1
    print('manifest written:', MANIFIESTO, 'files:', n)


def verificar():
    esperado = {}
    for l in open(MANIFIESTO, encoding='utf-8'):
        s, r = l.rstrip('\n').split('  ', 1)
        esperado[r] = s
    visto = set()
    distintos, faltan, nuevos = [], [], []
    for p in archivos():
        r = os.path.relpath(p, RAIZ).replace(os.sep, '/')
        visto.add(r)
        if r not in esperado:
            nuevos.append(r)
        elif sha(p) != esperado[r]:
            distintos.append(r)
    faltan = [r for r in esperado if r not in visto]
    print('expected %d | different %d | missing %d | new %d' % (len(esperado), len(distintos), len(faltan), len(nuevos)))
    for etiqueta, lista in (('DIFFERENT', distintos), ('MISSING', faltan), ('NEW', nuevos)):
        for r in lista[:20]:
            print(' ', etiqueta, r)
    sys.exit(0 if not (distintos or faltan or nuevos) else 1)


if __name__ == '__main__':
    {'crear': crear, 'verificar': verificar}[sys.argv[1]]()
