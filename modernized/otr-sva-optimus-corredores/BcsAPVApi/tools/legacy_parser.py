# -*- coding: utf-8 -*-
"""Deterministic parser of the legacy BcsAPVServicios sources (READ ONLY).

Produces a model (dict) with, for every top-level legacy class:
  - lines, JavaCall or not, the Entrada / Salida class used by call() and the Gson flavour,
  - collaborators (other classes invoked, stored procedures, sql resources, file/config IO),
  - every nested POJO class (recursively) with ALL its fields (name, java type, defaults set by field initialisers
    or by the no-arg constructor) and its superclass.
Run:  PYTHONIOENCODING=utf-8 py -3 legacy_parser.py [--dump file.json]
"""
import json
import os
import re
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', '..', '..'))
LEGACY_SRC = os.path.join(ROOT, 'legacy', 'otr-sva-optimus-corredores', 'BSCL', 'APV', 'JAVA', 'BcsAPVServicios', 'src')
PKG_DIR = os.path.join(LEGACY_SRC, 'cl', 'bcs', 'APV')

# external legacy POJOs referenced by field types (read by hand from BcsUtils / PlataformaUtils)
EXTERNAL = {
    'MensajeSalida': [('id', 'Long', None), ('tipo', 'String', None), ('codigo', 'String', None), ('descripcion', 'String', None)],
}

MODS = ('public', 'private', 'protected', 'static', 'final', 'transient', 'volatile', 'abstract')
CLASS_RE = re.compile(
    r'(?:(?:public|private|protected|static|abstract|final)\s+)*class\s+(\w+)(?:\s+extends\s+([\w.]+))?'
    r'(?:\s+implements\s+([\w.,\s<>]+?))?\s*\{')


def mask(src):
    """Blank comments and string/char literals, keeping offsets (so masked and original index alike)."""
    out = list(src)
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith('//', i):
            j = src.find('\n', i)
            j = n if j < 0 else j
            for k in range(i, j):
                out[k] = ' '
            i = j
        elif src.startswith('/*', i):
            j = src.find('*/', i + 2)
            j = n if j < 0 else j + 2
            for k in range(i, j):
                if src[k] != '\n':
                    out[k] = ' '
            i = j
        elif c == '"' or c == "'":
            q = c
            j = i + 1
            while j < n and src[j] != q:
                if src[j] == '\\':
                    j += 1
                j += 1
            for k in range(i + 1, min(j, n)):
                if src[k] != '\n':
                    out[k] = ' '
            i = j + 1
        else:
            i += 1
    return ''.join(out)


def match_brace(m, start):
    depth = 0
    for i in range(start, len(m)):
        if m[i] == '{':
            depth += 1
        elif m[i] == '}':
            depth -= 1
            if depth == 0:
                return i
    return len(m) - 1


def split_members(m, lo, hi):
    """top-level members of m[lo:hi): list of (kind, header_start, header_end, body_start, body_end)."""
    i = lo
    members = []
    start = lo
    while i < hi:
        c = m[i]
        if c == ';':
            members.append(('stmt', start, i, None, None))
            start = i + 1
            i += 1
        elif c == '{':
            e = match_brace(m, i)
            members.append(('block', start, i, i, e))
            start = e + 1
            i = e + 1
            k = i
            while k < hi and m[k] in ' \t\r\n':
                k += 1
            if k < hi and m[k] == ';':
                i = k + 1
                start = i
        else:
            i += 1
    return members


def split_top_commas(s):
    parts, depth, cur = [], 0, ''
    for ch in s:
        if ch in '<([{':
            depth += 1
        elif ch in '>)]}':
            depth -= 1
        if ch == ',' and depth == 0:
            parts.append(cur)
            cur = ''
        else:
            cur += ch
    parts.append(cur)
    return parts


def find_eq(s):
    depth = 0
    for i, ch in enumerate(s):
        if ch == '<':
            depth += 1
        elif ch == '>':
            depth -= 1
        elif ch == '=' and depth == 0:
            return i
    return -1


def parse_field(hdr_masked, hdr_orig):
    lead = len(hdr_masked) - len(hdr_masked.lstrip())
    h = hdr_masked.strip()
    ho = hdr_orig[lead:lead + len(h)]
    while h.startswith('@'):
        mm = re.match(r'^@\w+(\([^)]*\))?\s*', h)
        h = h[mm.end():]
        ho = ho[mm.end():]
    eq = find_eq(h)
    decl = h if eq < 0 else h[:eq]
    if '(' in decl:
        return None
    init = None if eq < 0 else ho[eq + 1:].strip()
    toks = decl.strip().split()
    mods = set()
    while toks and toks[0] in MODS:
        mods.add(toks.pop(0))
    rest = ' '.join(toks)
    parts = split_top_commas(rest)
    t0 = re.match(r'^(.*?)\s*(\w+)\s*$', parts[0].strip(), re.S)
    if not t0 or not t0.group(1).strip():
        return None
    typ = re.sub(r'\s+', '', t0.group(1))
    if typ in ('return', 'throw', 'import', 'package', 'break', 'continue', 'else', 'new'):
        return None
    names = [t0.group(2)] + [p.strip() for p in parts[1:]]
    return {'mods': sorted(mods), 'type': typ, 'names': names, 'init': init}


def parse_class_body(orig, m, name, extends, lo, hi):
    cls = {'name': name, 'extends': extends, 'fields': [], 'nested': [], 'ctor_defaults': {}, 'has_ctor': False,
           'has_noarg_ctor': False}
    for kind, hs, he, bs, be in split_members(m, lo, hi):
        hm = m[hs:he]
        ho = orig[hs:he]
        hstrip = hm.strip()
        if not hstrip:
            continue
        if kind == 'block':
            mm = CLASS_RE.match(hstrip + ' {')
            if mm and mm.end() == len(hstrip) + 2:
                cls['nested'].append(parse_class_body(orig, m, mm.group(1), mm.group(2), bs + 1, be))
                continue
            mc = re.match(r'^(?:public|private|protected)?\s*' + re.escape(name) + r'\s*\(([^)]*)\)\s*(?:throws[\w\s,.]+)?$', hstrip)
            if mc:
                cls['has_ctor'] = True
                if not mc.group(1).strip():
                    cls['has_noarg_ctor'] = True
                    body_m = m[bs + 1:be]
                    body_o = orig[bs + 1:be]
                    for k2, a, b, _c, _d in split_members(body_m, 0, len(body_m)):
                        if k2 != 'stmt':
                            continue
                        full_m = body_m[a:b]
                        lead = len(full_m) - len(full_m.lstrip())
                        sm = full_m.strip()
                        so = body_o[a:b][lead:lead + len(sm)]
                        ma = re.match(r'^(?:this\.)?(\w+)\s*=(?!=)\s*', sm)
                        if ma:
                            cls['ctor_defaults'][ma.group(1)] = so[ma.end():].strip()
            continue
        f = parse_field(hm, ho)
        if f is None or 'static' in f['mods'] or 'transient' in f['mods']:
            continue
        for nm in f['names']:
            cls['fields'].append({'name': nm, 'type': f['type'], 'init': f['init'] if len(f['names']) == 1 else None})
    return cls


def parse_file(path):
    orig = open(path, encoding='utf-8', errors='replace').read()
    m = mask(orig)
    pkg = re.search(r'^\s*package\s+([\w.]+)\s*;', m, re.M).group(1)
    imports = re.findall(r'^\s*import\s+([\w.]+)\s*;', m, re.M)
    classes = []
    depth = 0
    i = 0
    while i < len(m):
        ch = m[i]
        if ch == '{':
            depth += 1
        elif ch == '}':
            depth -= 1
        elif depth == 0 and ch.isalpha() and (i == 0 or not (m[i - 1].isalnum() or m[i - 1] == '_')):
            mm = CLASS_RE.match(m, i)
            if mm:
                b = mm.end() - 1
                e = match_brace(m, b)
                c = parse_class_body(orig, m, mm.group(1), mm.group(2), b + 1, e)
                c['implements'] = (mm.group(3) or '').strip()
                classes.append(c)
                i = e + 1
                continue
        i += 1
    return {'file': path, 'package': pkg, 'imports': imports, 'classes': classes,
            'lines': orig.count('\n') + 1, 'orig': orig, 'masked': m}


def all_nested(cls):
    for n in cls['nested']:
        yield n
        yield from all_nested(n)


def analyse_call(fp):
    """Entrada / Salida classes used by call(), and the Gson flavour (plain Gson omits nulls; getGsonBuilder serializes nulls + date format)."""
    mm = fp['masked']
    orig = fp['orig']
    r = {'entrada': None, 'salida': None, 'gson': None, 'call_null': False, 'call_found': False}
    mc = re.search(r'public\s+JsonObject\s+call\s*\(', mm)
    if not mc:
        return r
    r['call_found'] = True
    b = mm.find('{', mc.end())
    e = match_brace(mm, b)
    body = orig[b:e]
    if re.search(r'return\s+null\s*;', body) and 'fromJson' not in body:
        r['call_null'] = True
        return r
    f = re.search(r'fromJson\s*\(\s*\w+(?:\.toString\(\))?\s*,\s*(\w+)\.class', body)
    if f:
        r['entrada'] = f.group(1)
    g = re.search(r'Gson\s+\w+\s*=\s*([^;]+);', body)
    if g:
        r['gson'] = 'builder' if 'getGsonBuilder' in g.group(1) else 'plain'
    l = re.search(r'(\w+)\s+\w+\s*=\s*logica\s*\(', body)
    if l:
        r['salida'] = l.group(1)
    return r


NO_SERVICIO = {'SQL', 'Utils', 'Log', 'Fecha', 'Gson', 'GsonBuilder', 'JsonParser', 'PlataformaUtils', 'Diccionario', 'ArrayList', 'HashMap',
               'StoredProcedure', 'BigDecimal', 'ConfiguracionSistema'}


def collaborators(fp, cls_name):
    """Collaborators found by static reading (main() excluded): peer services (logica/call), stored procedures, sql resources, config
    parameters, sequences, dictionary use, file IO."""
    orig = fp['orig']
    mm = fp['masked']
    mi = mm.find('public static void main')
    scope = orig if mi < 0 else orig[:mi]
    jc, sp, sqls, io, cfg, seq = set(), set(), set(), set(), set(), set()
    for m in re.finditer(r'\b([A-Z]\w+)\s+(\w+)\s*=\s*new\s+\1\s*\(\s*\)\s*;', scope):
        cn, var = m.group(1), m.group(2)
        if cn != cls_name and cn not in NO_SERVICIO and re.search(r'\b' + var + r'\.(?:logica|call)\s*\(', scope):
            jc.add(cn)
    for m in re.finditer(r'new\s+([A-Z]\w+)\s*\(\s*\)\s*\.\s*(?:logica|call)\s*\(', scope):
        if m.group(1) != cls_name and m.group(1) not in NO_SERVICIO:
            jc.add(m.group(1))
    for m in re.finditer(r'new\s+StoredProcedure\s*\(\s*"([^"]+)"', scope):
        sp.add(m.group(1))
    for m in re.finditer(r'getParametroConfiguracionString\s*\([^;"]*"([^"]+)"\s*,\s*"([^"]+)"', scope):
        cfg.add(m.group(1) + '/' + m.group(2))
    for m in re.finditer(r'getSecuencia\s*\([^;"]*"([^"]+)"', scope):
        seq.add(m.group(1))
    for m in re.finditer(r'"(cl/bcs/[\w/]+\.sql)"', scope):
        sqls.add(m.group(1).split('/')[-1])
    for key in ('FileWriter', 'FileOutputStream', 'BufferedWriter', 'FileReader', 'BufferedReader', 'FileInputStream', 'Files.write',
                'Files.read', 'new File(', 'Base64'):
        if key in scope:
            io.add(key)
    for fn in sorted(sqls):
        try:
            txt = open(os.path.join(PKG_DIR, 'sql', fn), encoding='utf-8', errors='replace').read()
        except OSError:
            continue
        for m in re.finditer(r'(?im)^\s*CALL\s+"([^"]+)"', txt):
            sp.add(m.group(1))
    dic = 'Diccionario.getMensaje' in scope
    imps = sorted(i for i in fp['imports']
                  if i.startswith('cl.bcs.') and not i.startswith('cl.bcs.utils.') and not i.startswith('cl.bcs.APV.')
                  and not i.endswith('MensajeSalida') and not i.endswith('PlataformaUtils')
                  and not i.endswith('.Diccionario') and not i.endswith('.ConfiguracionSistema'))
    return {'javacall': sorted(jc), 'storedprocs': sorted(sp), 'sql_files': sorted(sqls), 'io': sorted(io), 'config': sorted(cfg),
            'secuencias': sorted(seq), 'diccionario': dic, 'imports_externos': imps}


def load():
    files = {}
    for fn in sorted(os.listdir(PKG_DIR)):
        if fn.endswith('.java'):
            files[fn[:-5]] = parse_file(os.path.join(PKG_DIR, fn))
    pojos = {}
    pdir = os.path.join(PKG_DIR, 'pojo')
    for fn in sorted(os.listdir(pdir)):
        if fn.endswith('.java'):
            pojos[fn[:-5]] = parse_file(os.path.join(pdir, fn))
    return files, pojos


def main():
    files, pojos = load()
    out = {}
    for name, fp in files.items():
        top = [c for c in fp['classes'] if c['name'] == name]
        out[name] = {'lines': fp['lines'], 'implements': top[0].get('implements', '') if top else '',
                     'call': analyse_call(fp), 'collab': collaborators(fp, name)}
    n_jc = sum(1 for v in out.values() if 'JavaCall' in v['implements'])
    print(len(files), 'top-level legacy files;', n_jc, 'implement JavaCall;', len(pojos), 'pojo files')
    if '--dump' in sys.argv:
        with open(sys.argv[sys.argv.index('--dump') + 1], 'w', encoding='utf-8') as fh:
            json.dump(out, fh, indent=1, ensure_ascii=False)
    return out


if __name__ == '__main__':
    main()
