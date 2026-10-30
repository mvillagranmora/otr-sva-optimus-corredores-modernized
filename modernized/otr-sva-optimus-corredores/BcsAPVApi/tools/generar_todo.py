# -*- coding: utf-8 -*-
"""Runs every generator in order: code (BcsAPVApi, BcsApvJavaCallAdapter, BcsAPVServicios skeleton) and the contract docs.
Usage: PYTHONIOENCODING=utf-8 py -3 generar_todo.py [--check] [--forzar-servicios]
The write mode (without --check) refuses to run (exit 3, nothing written) when BcsAPVServicios/.../application holds real services instead of generated
skeletons; --forzar-servicios overrides the refusal."""
import sys

import generar
import generar_contrato


def main():
    check = '--check' in sys.argv
    rc = 0
    idx, planes = generar.generar()
    if check:
        dif = generar.comprobar()
        print('code: generated files %d, differences %d' % (len(generar.FILES), len(dif)))
        rc |= 1 if dif else 0
    else:
        try:
            generar.escribir('--forzar-servicios' in sys.argv)
        except generar.ServiciosReales as e:
            print(e)
            sys.exit(3)
        print('code written: %d files, %d invocable operations' % (len(generar.FILES), sum(1 for p in planes.values() if p.caso)))
    try:
        generar_contrato.main()
    except SystemExit as e:
        rc |= int(e.code or 0)
    sys.exit(rc)


if __name__ == '__main__':
    main()
