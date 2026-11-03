"""T-36: compara el bootstrap de login y de home de BcsPlataformaWeb en modo HANA y en modo Postgres contra QA reales (java -jar).
Solo el usuario MAURICIO.VILLAGRAN (login real: escribe sesion/auditoria). No llama a ningun endpoint de clave. Enmascara secretos antes de guardar.
Reutiliza las definiciones (leer_env, entorno, login...) de login_cambio_clave_real.py sin ejecutar su cambio de clave.
Los JSON de las respuestas NO se versionan: T36_SALIDA_JSON debe apuntar a una carpeta fuera del repositorio (el informe .txt si se versiona).
Uso: T36_SALIDA_JSON=<carpeta fuera del repo> python -X utf8 t36_bootstrap.py"""
import hashlib, http.client, json, os, re, subprocess, sys, time

EQ = os.path.dirname(os.path.abspath(__file__)).replace('\\', '/')
sys.argv = [sys.argv[0], 'hana']
SRC = open(EQ + '/login_cambio_clave_real.py', encoding='utf-8').read()
exec(compile(SRC.split('\nenv = leer_env()')[0], 'login_cambio_clave_real(defs)', 'exec'))

SENSIBLE = re.compile(r'token|clave|hash|pass|usuario|nombre|mail|correo|rut|login|ip|sesion|session|menu', re.I)


def get(ruta):
    c = http.client.HTTPConnection('127.0.0.1', PUERTO, timeout=120)
    c.request('GET', ruta)
    r = c.getresponse()
    return r.status, r.read().decode('utf-8', 'replace')


def post_raw(ruta, cab):
    c = http.client.HTTPConnection('127.0.0.1', PUERTO, timeout=120)
    c.request('POST', ruta, body='', headers=cab)
    r = c.getresponse()
    return r.status, r.read().decode('utf-8', 'replace')


def js(texto):
    try:
        return json.loads(texto)
    except ValueError:
        return None


def enmascarar(x, en_sesion=False):
    """Copia segura para guardar: secretos como <masked>, binarios del logo resumidos."""
    if isinstance(x, dict):
        out = {}
        for k, v in x.items():
            if en_sesion and isinstance(v, str) and SENSIBLE.search(k):
                out[k] = '<masked>'
            elif k == 'Binario' and isinstance(v, str):
                out[k] = '<base64 len=%d sha256=%s>' % (len(v), hashlib.sha256(v.encode()).hexdigest()[:12])
            else:
                out[k] = enmascarar(v, en_sesion or k == 'BCS_SESION')
        return out
    if isinstance(x, list):
        return [enmascarar(v, en_sesion) for v in x]
    return x


def hoja(x, ruta=''):
    out = {}
    if isinstance(x, dict):
        if not x:
            out[ruta] = {}
        for k, v in x.items():
            out.update(hoja(v, ruta + '/' + k))
    elif isinstance(x, list):
        out[ruta + '[len]'] = len(x)
        for i, v in enumerate(x):
            out.update(hoja(v, '%s[%d]' % (ruta, i)))
    else:
        out[ruta] = x
    return out


FECHA = re.compile(r'^\d{4}-\d\d-\d\d|^\d\d[-/]\d\d[-/]\d{4}')


def tipo(v):
    return 'null' if v is None else type(v).__name__


def corto(v, ruta):
    if '/BCS_SESION' in ruta and isinstance(v, str):
        return '"<masked>"'
    if ruta.endswith('/Binario'):
        return '<base64 len=%d>' % len(v)
    s = json.dumps(v, ensure_ascii=False)
    return s if len(s) <= 70 else s[:67] + '...'


def comparar(a, b):
    """a=HANA, b=Postgres."""
    fa, fb = hoja(a), hoja(b)
    lineas, res = [], {'claves': len(set(fa) | set(fb)), 'iguales': 0, 'datos': 0, 'estructura': 0}
    for k in sorted(set(a) - set(b)):
        lineas.append('  ESTRUCTURA  clave de 1er nivel solo en HANA: %s' % k)
        res['estructura'] += 1
    for k in sorted(set(b) - set(a)):
        lineas.append('  ESTRUCTURA  clave de 1er nivel solo en Postgres: %s' % k)
        res['estructura'] += 1
    por_bloque = {}
    for r in sorted(set(fa) | set(fb)):
        bloque = r.split('/')[1].split('[')[0] if '/' in r else r
        if r in fa and r in fb:
            va, vb = fa[r], fb[r]
            if va == vb and tipo(va) == tipo(vb):
                res['iguales'] += 1
                continue
            num = lambda v: isinstance(v, (int, float)) and not isinstance(v, bool)
            if num(va) and num(vb):
                if va == vb:
                    cl, k = 'ESTRUCTURA(entero vs decimal, mismo valor)', 'estructura'
                else:
                    cl, k = 'DATOS(valor distinto)', 'datos'
            elif tipo(va) != tipo(vb):
                cl, k = 'ESTRUCTURA(tipo %s vs %s)' % (tipo(va), tipo(vb)), 'estructura'
            elif isinstance(va, str) and FECHA.match(va) and FECHA.match(vb) and re.sub(r'\D', '', va)[:8] == re.sub(r'\D', '', vb)[:8] and len(va) != len(vb):
                cl, k = 'ESTRUCTURA(formato de fecha)', 'estructura'
            else:
                cl, k = 'DATOS(valor distinto)', 'datos'
            res[k] += 1
            por_bloque.setdefault(bloque, []).append('  %-34s %s | HANA=%s | PG=%s' % (cl, r, corto(va, r), corto(vb, r)))
        else:
            lado = 'HANA' if r in fa else 'Postgres'
            v = fa.get(r, fb.get(r))
            cl = 'DATOS(entrada solo en %s)' % lado
            res['datos'] += 1
            por_bloque.setdefault(bloque, []).append('  %-34s %s | %s' % (cl, r, corto(v, r)))
    for bloque, ls in sorted(por_bloque.items()):
        lineas.append(' bloque %s: %d diferencias' % (bloque, len(ls)))
        lineas.extend(ls[:25])
        if len(ls) > 25:
            lineas.append('  ... (%d mas, mismo patron)' % (len(ls) - 25))
    return lineas, res


def forma(x):
    if isinstance(x, dict):
        return 'objeto(%d claves)' % len(x)
    if isinstance(x, list):
        return 'lista(%d)' % len(x)
    return tipo(x) if not isinstance(x, str) or x != '<falta>' else '<falta>'


def correr(motor, env, informe):
    log = open(SCRATCH + '/app_t36_%s.log' % motor, 'wb')
    app = subprocess.Popen([JAVA, '-jar', JAR, '--server.port=%d' % PUERTO], env=entorno(env, motor), stdout=log, stderr=subprocess.STDOUT)
    resp = {}
    try:
        listo = False
        for _ in range(90):
            time.sleep(2)
            if app.poll() is not None:
                break
            try:
                if get('/PLA/Ping')[0] == 200:
                    listo = True
                    break
            except OSError:
                pass
        informe.append('[%s] aplicacion lista: %s' % (motor, listo))
        if not listo:
            return resp
        s, t = get('/PLA/bootstrap/login')
        resp['login'] = js(t)
        informe.append('[%s] GET /PLA/bootstrap/login -> HTTP %d' % (motor, s))
        s, tok, err = login(env['qa1.login.testpass'])
        informe.append('[%s] POST /PLA/CrearSesion -> HTTP %d | token: %s | P_OUT_ERRORESCONTADOR: %s' % (motor, s, bool(tok), err))
        if tok:
            s, t = post_raw('/PLA/bootstrap/home', {'X-SESSION-TOKEN': tok})
            resp['home'] = js(t)
            informe.append('[%s] POST /PLA/bootstrap/home -> HTTP %d' % (motor, s))
            s2, _ = post_raw('/PLA/bootstrap/home', {})
            informe.append('[%s] POST /PLA/bootstrap/home sin token -> HTTP %d (control)' % (motor, s2))
    finally:
        app.terminate()
        try:
            app.wait(timeout=20)
        except subprocess.TimeoutExpired:
            app.kill()
        log.close()
        informe.append('[%s] aplicacion detenida' % motor)
    return resp


def main():
    env = leer_env()
    informe, R = [], {}
    for m in ('hana', 'postgres'):
        R[m] = correr(m, env, informe)
    for m in R:
        for k, v in R[m].items():
            if v is not None:
                json.dump(enmascarar(v), open('%s/t36-%s-%s-20261103.json' % (os.environ['T36_SALIDA_JSON'], k, m), 'w', encoding='utf-8'), ensure_ascii=False, indent=1, sort_keys=True)
    informe.append('')
    for k in ('login', 'home'):
        a, b = R['hana'].get(k), R['postgres'].get(k)
        informe.append('=== bootstrap %s (HANA vs Postgres) ===' % k)
        if a is None or b is None:
            informe.append('  sin respuesta JSON en algun motor (HANA=%s, PG=%s)' % (a is not None, b is not None))
            continue
        informe.append('  formas por clave de 1er nivel:')
        for c in sorted(set(a) | set(b)):
            informe.append('    %-28s HANA=%-18s PG=%s' % (c, forma(a[c]) if c in a else '<falta>', forma(b[c]) if c in b else '<falta>'))
        ls, res = comparar(a, b)
        informe.append('  hojas comparadas: %(claves)d | iguales: %(iguales)d | difieren por datos: %(datos)d | difieren por estructura/tipo: %(estructura)d' % res)
        informe.extend(ls)
    open(EQ + '/t36-bootstrap-qa-20261103.txt', 'w', encoding='utf-8').write('\n'.join(informe) + '\n')
    print('\n'.join(informe))


main()
