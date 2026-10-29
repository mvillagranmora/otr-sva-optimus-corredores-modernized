#!/usr/bin/env python3
"""F3 W3 T1 (CertificarSolicitudAporteRescate): OWN driver of the shared tools/gen_doubles.py (that file is NOT edited). Same generator, one difference: the set of methods that
become scripted answers also holds `logicaAsync` (NotificarEstadoOrdenFondoMutuo.logicaAsync, the asynchronous notification Certificar fires at C:836), so that the double of that
collaborator records its call instead of throwing UnsupportedOperationException.

  py -3 -I gen_doubles_cer.py <outDir> <legacySrcFile> [<legacySrcFile> ...]
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "tools"))
import gen_doubles  # noqa: E402

gen_doubles.ENTRY = {"logica", "go", "call", "logicaAsync"}

if __name__ == "__main__":
    gen_doubles.main()
