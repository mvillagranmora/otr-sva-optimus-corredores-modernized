# -*- coding: utf-8 -*-
"""Resolves, for every legacy JavaCall operation, its Entrada/Salida class graph (all nested/inherited/imported POJOs)
into a flat, ordered model that the generators consume.  Deterministic: every iteration is over sorted/insertion-ordered data."""
import os
import re
import legacy_parser as lp

PRIMS = {'long': 'Long', 'int': 'Integer', 'double': 'Double', 'boolean': 'Boolean', 'float': 'Float', 'short': 'Short', 'byte': 'Byte'}
SCALARS = {'Long', 'Integer', 'Double', 'Float', 'Short', 'Byte', 'Boolean', 'String', 'BigDecimal', 'BigInteger', 'Date', 'Timestamp',
           'Object', 'JsonObject', 'JsonElement', 'JsonArray', 'LocalDate', 'LocalDateTime', 'Character', 'char'}
LIST_RE = re.compile(r'^(?:java\.util\.)?(?:List|ArrayList|Collection|LinkedList|Set|HashSet)<(.+)>$')
MAP_RE = re.compile(r'^(?:java\.util\.)?(?:Map|HashMap|LinkedHashMap|TreeMap)<(.+),(.+)>$')


def camel(name):
    """legacy field name -> camelCase typed name. p_IdEmpresa -> idEmpresa ; P_OUT_MENSAJES -> mensajes ; IdBonificacion -> idBonificacion ;
    ALLCAPS names are split with the word list below (P_OUT_IDREC -> idRec)."""
    n = re.sub(r'^(?:P_OUT_|p_out_|P_|p_)', '', name)
    if n and n.replace('_', '').isupper():
        if n in TOKENS_UP:
            n = TOKENS_UP[n]
        else:
            n = segmentar(n)
    elif '_' in n:
        n = ''.join(x[:1].upper() + x[1:] for x in n.split('_') if x)
    if not n:
        n = name
    m = re.match(r'^([A-Z]+)([A-Z][a-z].*)$', n)
    if m:
        n = m.group(1).lower() + m.group(2)
    else:
        n = n[0].lower() + n[1:]
    if n in JAVA_KEYWORDS:
        n = n + 'Valor'
    return n


PALABRAS = ['ADVERTENCIAS', 'ADVERTENCIA', 'ERRORES', 'ERROR', 'CONTADOR', 'MENSAJES', 'RESULTADO', 'CARGA', 'MASIVA', 'PORTAFOLIO', 'CUSTODIA',
            'SOLICITUDES', 'PREFERENCIA', 'PAGO', 'MONTO', 'PORCENTAJE', 'TRASPASO', 'RECAUDACION', 'CONTRATOS', 'CONTRATO', 'CLIENTE', 'EMPLEADOR',
            'TOTALES', 'TOTAL', 'MODAL', 'REF', 'IDS', 'ID', 'REC', 'EMP', 'CODIGO', 'TIPO', 'ESTADO', 'NOMBRE', 'FOLIO', 'FECHA', 'CANAL',
            'DETALLE', 'ORIGEN', 'DESTINO', 'PERFIL', 'MENSAJE', 'VALIDACION']
SIGLAS = ['SAAPV', 'FFMM', 'RV', 'PAC']


def segmentar(n):
    """ALLCAPS legacy name -> PascalCase words (dynamic programming: fewest unknown characters, then fewest pieces; SIGLAS kept upper case)."""
    conocidas = PALABRAS + SIGLAS
    partes = []
    for tramo in n.split('_'):
        mejor = {len(tramo): (0, 0, [])}
        for i in range(len(tramo) - 1, -1, -1):
            cand = None
            for w in conocidas:
                if tramo.startswith(w, i) and (i + len(w)) in mejor:
                    d, k, ps = mejor[i + len(w)]
                    c = (d, k + 1, [w] + ps)
                    if cand is None or c[:2] < cand[:2]:
                        cand = c
            if (i + 1) in mejor:
                d, k, ps = mejor[i + 1]
                c = (d + 1, k, ['?' + tramo[i]] + ps)
                if cand is None or c[:2] < cand[:2]:
                    cand = c
            mejor[i] = cand
        crudo = mejor[0][2]
        fusion = []
        for x in crudo:
            if x.startswith('?') and fusion and fusion[-1].startswith('?'):
                fusion[-1] += x[1:]
            else:
                fusion.append(x)
        partes.extend(x[1:] if x.startswith('?') else x for x in fusion)
    return ''.join(x if x in SIGLAS else x.capitalize() for x in partes)


JAVA_KEYWORDS = {'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class', 'const', 'continue', 'default', 'do',
                 'double', 'else', 'enum', 'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements', 'import', 'instanceof',
                 'int', 'interface', 'long', 'native', 'new', 'package', 'private', 'protected', 'public', 'return', 'short', 'static',
                 'strictfp', 'super', 'switch', 'synchronized', 'this', 'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while',
                 'record', 'var', 'yield', 'null', 'true', 'false'}

# all-caps legacy names (P_OUT_*) split into words by hand
TOKENS_UP = {
    'MENSAJES': 'mensajes', 'ERRORESCONTADOR': 'contadorErrores', 'ADVERTENCIASCONTADOR': 'contadorAdvertencias',
}


OVERRIDES_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'overrides')
OVERRIDE_RE = re.compile(r'^OVERRIDE-DEFAULT:\s*([\w.]+)\s+(\w+)\s*=\s*\[null\]\s*$')
OVERRIDES = None       # (legacy class path, legacy field) -> ('lista_nula', file name)
OVERRIDES_USADOS = set()


def cargar_overrides():
    """Hand overrides of defaults the static reading of the legacy cannot see (see tools/overrides/*.md). One machine-readable line per field:
    `OVERRIDE-DEFAULT: <Outer.Inner legacy class> <legacy field> = [null]` (the legacy constructor does `lista.add(null)`: the default is a list with one null)."""
    global OVERRIDES
    if OVERRIDES is None:
        OVERRIDES = {}
        if os.path.isdir(OVERRIDES_DIR):
            for fn in sorted(os.listdir(OVERRIDES_DIR)):
                if fn.endswith('.md'):
                    for linea in open(os.path.join(OVERRIDES_DIR, fn), encoding='utf-8').read().splitlines():
                        m = OVERRIDE_RE.match(linea.strip())
                        if m:
                            OVERRIDES[(m.group(1), m.group(2))] = ('lista_nula', fn)
    return OVERRIDES


def verificar_overrides():
    """Every override must have hit a field of the legacy model: a stale override (class renamed in the legacy) stops the generator."""
    sin = sorted(set(cargar_overrides()) - OVERRIDES_USADOS)
    assert not sin, 'tools/overrides: override(s) that match no legacy field: %s' % sin


class Clase:
    """A resolved legacy class (nested or top-level)."""

    def __init__(self, archivo, ruta, raw, fp):
        self.archivo = archivo      # legacy file/class holding it ("GrabarContratoSAAPV" or "pojo/Banco")
        self.ruta = ruta            # tuple of nested names, last is the class itself
        self.raw = raw
        self.fp = fp
        self.nombre = raw['name']
        self.fqn = ('cl.bcs.APV.pojo.' if archivo.startswith('pojo/') else 'cl.bcs.APV.') + '.'.join(ruta)

    @property
    def key(self):
        return (self.archivo, self.ruta)


class Indice:
    def __init__(self):
        self.files, self.pojos = lp.load()
        self.clases = {}      # key -> Clase
        self.por_archivo = {}  # archivo -> list of Clase
        for nm, fp in self.files.items():
            self._indexar(nm, fp)
        for nm, fp in self.pojos.items():
            self._indexar('pojo/' + nm, fp)

    def _indexar(self, archivo, fp):
        lst = []

        def rec(c, ruta):
            cl = Clase(archivo, ruta + (c['name'],), c, fp)
            self.clases[cl.key] = cl
            lst.append(cl)
            for n in c['nested']:
                rec(n, ruta + (c['name'],))
        for c in fp['classes']:
            rec(c, ())
        self.por_archivo[archivo] = lst

    def resolver(self, nombre, contexto):
        """simple (possibly Outer.Inner) type name used inside `contexto` Clase -> Clase | None."""
        nombre = nombre.split('.')[-1] if nombre.count('.') == 0 else nombre
        simple = nombre.split('.')[-1]
        fp = contexto.fp
        archivo = contexto.archivo
        # 1. nested in the same file (prefer the enclosing chain: innermost first)
        cands = [c for c in self.por_archivo[archivo] if c.nombre == simple]
        if cands:
            # prefer sibling in same outer path
            cands.sort(key=lambda c: -len(set(c.ruta) & set(contexto.ruta)))
            return cands[0]
        # 2. imports
        for imp in fp['imports']:
            if imp.endswith('.' + simple) and imp.startswith('cl.bcs.APV.'):
                partes = imp[len('cl.bcs.APV.'):].split('.')
                if partes[0] == 'pojo':
                    k = ('pojo/' + partes[1], (partes[1],))
                    return self.clases.get(k)
                k = (partes[0], tuple(partes[0:]))
                if k in self.clases:
                    return self.clases[k]
        # 3. Outer.Inner qualified
        if '.' in nombre:
            partes = nombre.split('.')
            for a, lst in self.por_archivo.items():
                if a == partes[0]:
                    for c in lst:
                        if c.nombre == partes[-1]:
                            return c
        # 4. any APV top-level pojo / file with that simple name
        for a, lst in self.por_archivo.items():
            for c in lst:
                if len(c.ruta) == 1 and c.nombre == simple:
                    return c
        return None


def parse_tipo(t):
    """java type string -> dict(base, lista(bool), mapa(bool))"""
    t = re.sub(r'\s+', '', t)
    t = t.replace('java.util.', '').replace('java.math.', '').replace('java.sql.', '').replace('java.lang.', '')
    if t in PRIMS:
        return {'base': PRIMS[t], 'lista': False, 'mapa': False, 'prim': t}
    m = LIST_RE.match(t)
    if m:
        inner = parse_tipo(m.group(1))
        inner['lista'] = True
        return inner
    if t.endswith('[]'):
        inner = parse_tipo(t[:-2])
        inner['lista'] = True
        return inner
    m = MAP_RE.match(t)
    if m:
        return {'base': t, 'lista': False, 'mapa': True}
    if t in ('List', 'ArrayList'):
        return {'base': 'Object', 'lista': True, 'mapa': False}
    return {'base': t, 'lista': False, 'mapa': False}


def default_de(init, base, lista):
    """Translate a Java initialiser expression into a neutral descriptor:
    ('null',) | ('lit', java_literal_text) | ('lista',) | ('bd', 'ZERO'|'<n>') | ('desconocido', text)."""
    if init is None:
        return None
    s = init.strip()
    if s == 'null':
        return ('null',)
    if re.match(r'^new\s+(?:java\.util\.)?(?:ArrayList|LinkedList|HashSet)\s*(<[^>]*>)?\s*\(\s*\)$', s):
        return ('lista',)
    if re.match(r'^new\s+(?:java\.util\.)?(?:HashMap|LinkedHashMap)\s*(<[^>]*>)?\s*\(\s*\)$', s):
        return ('mapa',)
    m = re.match(r'^(-?\d+)[Ll]$', s)
    if m:
        return ('long', m.group(1))
    m = re.match(r'^(?:Long\.valueOf|new\s+Long)\(\s*(-?\d+)[Ll]?\s*\)$', s)
    if m:
        return ('long', m.group(1))
    m = re.match(r'^-?\d+$', s)
    if m:
        return ('int', s)
    m = re.match(r'^(-?\d+\.\d+)[dDfF]?$', s)
    if m:
        return ('double', m.group(1))
    if s in ('true', 'false'):
        return ('bool', s)
    if re.match(r'^"(?:[^"\\]|\\.)*"$', s):
        return ('str', s)
    if s in ('BigDecimal.ZERO', 'BigDecimal.ONE', 'BigDecimal.TEN'):
        return ('bd', s.split('.')[1])
    m = re.match(r'^new\s+BigDecimal\(\s*"?(-?[\d.]+)"?\s*\)$', s)
    if m:
        return ('bd', m.group(1))
    m = re.match(r'^BigDecimal\.valueOf\(\s*(-?[\d.]+)[LlDd]?\s*\)$', s)
    if m:
        return ('bd', m.group(1))
    m = re.match(r'^new\s+(\w+)\s*\(\s*\)$', s)
    if m:
        return ('nuevo', m.group(1))
    return ('desconocido', s)


class Campo:
    def __init__(self, legacy, tipo_java, base, lista, mapa, clase_ref, default, origen):
        self.legacy = legacy          # JSON key (legacy field name)
        self.tipo_java = tipo_java    # original java type text
        self.base = base              # element type simple name (Long, String, BigDecimal, Clase fqn...)
        self.lista = lista
        self.mapa = mapa
        self.clase_ref = clase_ref    # Clase | None  (None for scalars / external)
        self.default = default        # None | tuple (see default_de)
        self.origen = origen          # class that declares it (for inheritance)
        self.nombre = camel(legacy)

    @property
    def escalar(self):
        return self.clase_ref is None and self.base in SCALARS

    @property
    def externo(self):
        return self.clase_ref is None and self.base not in SCALARS and not self.mapa and self.base != 'MensajeSalida'


def _campos_de(idx, cl):
    """All Gson-visible fields of a class including inherited (superclass first, as Gson orders them: subclass first then super; the
    JSON object order is irrelevant for equality but we keep subclass fields first like Gson does)."""
    propios = []
    raw = cl.raw
    # defaults: constructor assignments win over field initialisers (constructor runs last)
    ctor_def = raw['ctor_defaults'] if raw['has_noarg_ctor'] or not raw['has_ctor'] else {}
    sin_defaults = raw['has_ctor'] and not raw['has_noarg_ctor']
    for f in raw['fields']:
        ti = parse_tipo(f['type'])
        ref = None
        base = ti['base']
        if base == 'MensajeSalida':
            ref = None
        elif base not in SCALARS and not ti['mapa']:
            ref = idx.resolver(base, cl)
        init = None if sin_defaults else (ctor_def.get(f['name'], f['init']))
        d = default_de(init, base, ti['lista'])
        if d and d[0] == 'nuevo':
            ref2 = idx.resolver(d[1], cl)
            d = ('nuevo', ref2) if ref2 is not None else ('desconocido', init)
        c = Campo(f['name'], f['type'], base, ti['lista'], ti['mapa'], ref, d, cl.fqn)
        c.prim = ti.get('prim')
        ov = cargar_overrides().get((cl.fqn[len('cl.bcs.APV.'):], f['name']))
        if ov is not None:
            assert ti['lista'], 'override [null] on a field that is not a list: %s' % f['name']
            c.default = (ov[0], ov[1])
            OVERRIDES_USADOS.add((cl.fqn[len('cl.bcs.APV.'):], f['name']))
        c.fecha = None
        if base in ('Date', 'Timestamp'):
            tj = f['type']
            imps = cl.fp['imports']
            if tj.startswith('java.sql.') or (not tj.startswith('java.util.') and 'java.sql.' + base in imps):
                c.fecha = 'sql' if base == 'Date' else 'timestamp'
            else:
                c.fecha = 'util'
        propios.append(c)
    if raw['extends']:
        sup = idx.resolver(raw['extends'], cl)
        if sup is not None and sup.nombre != 'BcsPojo':
            propios.extend(_campos_de(idx, sup))
    # a subclass constructor can set defaults on inherited fields too
    for c in propios:
        if c.default is None and c.legacy in ctor_def and c.origen != cl.fqn:
            c.default = default_de(ctor_def[c.legacy], c.base, c.lista)
    return propios


_CACHE = {}


def campos_de(idx, cl):
    k = (id(idx), cl.key)
    if k not in _CACHE:
        _CACHE[k] = desambiguar(_campos_de(idx, cl))
    return _CACHE[k]


def desambiguar(campos):
    """two legacy names can collapse to the same camelCase name (SalidaRevocacionContrato: P_OUT_ERRORESCONTADOR and ContadorErrores)."""
    vistos = {}
    for c in campos:
        if c.nombre in vistos:
            c.nombre = ('salida' + c.nombre[0].upper() + c.nombre[1:]) if c.legacy.startswith('P_OUT_') else (c.nombre + 'Legacy')
            if c.nombre in vistos:
                c.nombre += '2'
        vistos[c.nombre] = c
    return campos


class Operacion:
    pass


def construir():
    idx = Indice()
    ops = {}
    for nombre, fp in idx.files.items():
        tops = [c for c in fp['classes'] if c['name'] == nombre]
        if not tops:
            continue
        impl = tops[0].get('implements', '')
        call = lp.analyse_call(fp)
        o = Operacion()
        o.nombre = nombre
        o.fqn = 'cl.bcs.APV.' + nombre
        o.lineas = fp['lines']
        o.javacall = 'JavaCall' in impl
        o.call = call
        o.collab = lp.collaborators(fp, nombre)
        o.clase = idx.clases[(nombre, (nombre,))]
        o.entrada = idx.resolver(call['entrada'], o.clase) if call['entrada'] else None
        o.salida = idx.resolver(call['salida'], o.clase) if call['salida'] else None
        ops[nombre] = o
    return idx, ops


def grafo(idx, raiz):
    """ordered list of Clase reachable from raiz (raiz first), via field types (not through inheritance parents, those are flattened)."""
    orden = []
    vistos = set()

    def rec(c):
        if c.key in vistos:
            return
        vistos.add(c.key)
        orden.append(c)
        for f in campos_de(idx, c):
            if f.clase_ref is not None:
                rec(f.clase_ref)
    rec(raiz)
    return orden


if __name__ == '__main__':
    idx, ops = construir()
    sin = []
    ext = {}
    for o in ops.values():
        for raiz in (o.entrada, o.salida):
            if raiz is None:
                continue
            for c in grafo(idx, raiz):
                for f in campos_de(idx, c):
                    if f.externo:
                        ext.setdefault(f.base, set()).add(o.nombre)
    print('externos', {k: sorted(v) for k, v in ext.items()})
