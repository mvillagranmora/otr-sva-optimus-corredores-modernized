# -*- coding: utf-8 -*-
"""Writes cases.json (default behaviour, 64 operations x Entrada/Salida) and fresh-cases.json (fresh inputs) of BcsAPVApi from the recorded outputs.
Paths inside the cases files are relative to the folder of the cases file (equivalence/BcsAPVApi). Run AFTER correr_legacy.sh / correr_nuevo.sh.

Usage:  py -3 -I make_cases.py            (reads ops.json, ../fresh/input/index.json, ../fresh/*/rechazadas.txt)
"""
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
EQ = os.path.abspath(os.path.join(HERE, '..'))

LEGACY = {'label': 'Legacy Java 8 source (BcsAPVServicios cl.bcs.APV.*), real classes compiled with JDK 25 --release 11, Gson 2.13.2',
          'command': 'oracle/compilar_legacy.sh; oracle/correr_legacy.sh defecto|entradas|ejecutar'}
NEW = {'label': 'Modernized BcsAPVApi records + BcsApvJavaCallAdapter mappers, JDK 25, Gson 2.13.2',
       'command': 'oracle/correr_nuevo.sh defecto|ejecutar'}


def cargar(p):
    with open(p, encoding='utf-8') as fh:
        return json.load(fh)


def casos_defecto(ops):
    cases = []
    for op in ops:
        c = op['caso']
        for lado, titulo in (('entrada', 'Entrada'), ('salida', 'Salida')):
            for d in ('legacy', 'new'):
                assert os.path.isfile(os.path.join(EQ, d, c, lado + '.json')), (d, c, lado)
            cases.append({'id': 'APV-%s-%s' % (c, lado.upper()),
                          'title': '%s: %s instantiated with its default constructor and serialised with the Gson of call() (%s)' % (c, titulo, op['gson']),
                          'legacy': 'legacy/%s/%s.json' % (c, lado), 'new': 'new/%s/%s.json' % (c, lado),
                          'note': 'legacy class %s' % op[lado]})
    return cases


def main():
    ops = cargar(os.path.join(HERE, 'ops.json'))['operaciones']
    assert len(ops) == 64, len(ops)
    cases = casos_defecto(ops)
    with open(os.path.join(EQ, 'cases.json'), 'w', encoding='utf-8', newline='\n') as fh:
        json.dump({'system': 'BcsAPVApi', 'legacy': LEGACY, 'new': NEW, 'cases': cases}, fh, indent=1, ensure_ascii=False)
        fh.write('\n')
    print('cases.json: %d cases' % len(cases))

    fresh_idx = cargar(os.path.join(EQ, 'fresh', 'input', 'index.json'))['entradas']
    rech = {}
    for lado in ('legacy', 'new'):
        p = os.path.join(EQ, 'fresh', lado, 'rechazadas.txt')
        rech[lado] = {l.split('\t')[0]: l.split('\t', 1)[1].strip() for l in open(p, encoding='utf-8').read().splitlines() if l.strip()}
    cases, descartados = [], []
    for f in fresh_idx:
        i = f['id']
        if i in rech['legacy']:
            descartados.append({'id': i, 'por': 'the legacy rejects the input', 'motivo': rech['legacy'][i]})
            continue
        nuevo_rechaza = rech['new'].get(i)
        assert os.path.isfile(os.path.join(EQ, 'fresh', 'legacy', i, 'out.json')), i
        assert nuevo_rechaza or os.path.isfile(os.path.join(EQ, 'fresh', 'new', i, 'out.json')), i
        desc = {'P1': 'every field populated, lists of 2, nested objects filled', 'P2': 'limits: MAX/MIN numbers, BigDecimal scales, empty/long/unicode strings, extreme dates, empty lists, null nested',
                'P3': 'partial request: every second key removed, every third kept key null, two unknown keys'}[f['perfil']]
        cases.append({'id': i, 'input': i, 'title': '%s %s, profile %s: legacy JSON in -> typed record -> legacy JSON out (%s)' % (f['caso'], f['lado'], f['perfil'], desc),
                      'legacy': 'fresh/legacy/%s/out.json' % i, 'new': 'fresh/new/%s/out.json' % i, 'note': 'input: fresh/input/%s.json' % i + ('; THE NEW SIDE REJECTS IT (no output file, the case is reported missing): ' + nuevo_rechaza if nuevo_rechaza else '')})
    left = [
        'Malformed records (INTENT.md): analysis/otr-sva-optimus-corredores/INTENT.md does not exist in this workspace, so nothing says the legacy behaviour on malformed input must be reproduced '
        'byte for byte, quirks included; malformed JSON (wrong types, truncated text) was NOT exercised on BcsAPVApi. Unknown keys and nulls (profile P3) were.',
        'Business logic (logica()) of the 64 operations is not run by this oracle: it needs HANA; BcsAPVApi only holds the typed contract and the adapter mappers are the only behaviour on the new side. '
        'The comparison covers the JSON <-> record conversion and the legacy defaults, not the use case outcomes (those belong to BcsAPVServicios).']
    if descartados:
        left.append('%d fresh input(s) discarded because the legacy rejects them: %s' % (len(descartados), ', '.join(d['id'] for d in descartados[:20])))
    with open(os.path.join(EQ, 'fresh-cases.json'), 'w', encoding='utf-8', newline='\n') as fh:
        json.dump({'system': 'BcsAPVApi', 'legacy': LEGACY, 'new': NEW, 'cases': cases, 'leftOut': left, 'discarded': descartados}, fh, indent=1, ensure_ascii=False)
        fh.write('\n')
    print('fresh-cases.json: %d cases, %d discarded' % (len(cases), len(descartados)))


if __name__ == '__main__':
    main()
