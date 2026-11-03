"""Login real y cambio de clave real (ActualizarClave) del usuario acordado contra HANA QA y Postgres QA, con la aplicacion modernizada (java -jar).
Autorizado por el usuario 2026-11-03 solo para MAURICIO.VILLAGRAN: escribe lo que el login y el cambio de clave escriben; no borra nada.
La clave nueva se deja en .env (qa1.login.testpass) al terminar. Nunca imprime claves, hashes, tokens ni credenciales de base de datos.
Uso: python -X utf8 login_cambio_clave_real.py <hana|postgres>... (en el orden dado; la misma clave nueva en todos)"""
import hashlib, http.client, json, os, re, secrets, string, subprocess, sys, time

REPO = 'C:/Users/m.villagran.mora/Downloads/develop/nuam-modernizado'
EQ = REPO + '/analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb'
JAR = REPO + '/modernized/otr-sva-optimus-corredores/BcsPlataformaWeb/target/bcs-plataforma-web-1.0.0-SNAPSHOT.jar'
SCRATCH = 'C:/Users/MVILLA~1.MOR/AppData/Local/Temp/claude/6929690f61ec5c2f/57bf1e9e-455e-4135-b1b4-d2d7dfeea3c8/scratchpad'
M2 = os.path.expanduser('~/.m2/repository').replace('\\', '/')
CP = M2 + '/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar;' + M2 + '/com/sap/cloud/db/jdbc/ngdbc/2.8.12/ngdbc-2.8.12.jar'
JAVA = 'C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin/java.exe'
PUERTO = 18117
USUARIO = 'MAURICIO.VILLAGRAN'
PENDIENTE = SCRATCH + '/clave_nueva.pendiente'

motores = sys.argv[1:]
assert motores and all(m in ('hana', 'postgres') for m in motores)


def leer_env():
    d = {}
    for linea in open(REPO + '/.env', encoding='utf-8-sig').read().splitlines():
        if '=' in linea and not linea.strip().startswith('#'):
            k, _, v = linea.partition('=')
            d[k.strip()] = v.strip()
    return d


def sha(clave):
    return hashlib.sha256(clave.encode('utf-8')).hexdigest()


def generar():
    a = string.ascii_uppercase, string.ascii_lowercase, string.digits
    partes = [secrets.choice(x) for x in a for _ in range(4)] + [secrets.choice('#!@') for _ in range(2)] + [secrets.choice(string.ascii_letters + string.digits) for _ in range(4)]
    secrets.SystemRandom().shuffle(partes)
    return ''.join(partes)


def estado(motor, etiqueta):
    r = subprocess.run([JAVA, '-cp', CP, EQ + '/EstadoSesiones.java', REPO + '/.env', motor, USUARIO], capture_output=True, text=True, timeout=120)
    salida = [l for l in r.stdout.splitlines() if l.startswith(motor)]
    print('%-7s %s' % (etiqueta, salida[0] if salida else 'sin salida (codigo %s)' % r.returncode), flush=True)


def entorno(env, motor):
    h = dict(os.environ)
    if motor == 'hana':
        url = env['qa01.saphana.dbURL']
        props = [p for p in env.get('qa01.saphana.dbprop', '').split(';') if p]
        if props:
            url += ('&' if '?' in url else '?') + '&'.join(props)
        h.update({'SPRING_DATASOURCE_URL': url, 'SPRING_DATASOURCE_USERNAME': env['qa01.saphana.dbuser'], 'SPRING_DATASOURCE_PASSWORD': env['qa01.saphana.dbpass']})
    else:
        url = env['qa01.postgres.dbURL']
        if 'stringtype=' not in url:
            url += ('&' if '?' in url else '?') + 'stringtype=unspecified'  # H3: ver TRANSFORMATION_NOTES
        if 'escapeSyntaxCallMode=' not in url:
            url += '&escapeSyntaxCallMode=callIfNoReturn'  # los procedimientos de Postgres son PROCEDURE: sin esto pgjdbc emite SELECT (42809)
        h.update({'SPRING_DATASOURCE_URL': url, 'SPRING_DATASOURCE_USERNAME': env['qa01.postgres.dbuser'],
                  'SPRING_DATASOURCE_PASSWORD': env['qa01.postgres.dbpass'], 'SPRING_DATASOURCE_DRIVER_CLASS_NAME': 'org.postgresql.Driver',
                  'PLATAFORMA_DB_DIALECT': 'postgres', 'HANA_URL': url})
    return h


def post(ruta, cuerpo, cabeceras=None):
    c = http.client.HTTPConnection('127.0.0.1', PUERTO, timeout=90)
    h = {'Content-Type': 'application/json'}
    h.update(cabeceras or {})
    c.request('POST', ruta, body=json.dumps(cuerpo), headers=h)
    r = c.getresponse()
    texto = r.read().decode('utf-8', 'replace')
    try:
        return r.status, json.loads(texto)
    except ValueError:
        return r.status, {}


def login(clave):
    s, j = post('/PLA/CrearSesion', {'p_IdEmpresa': '1', 'p_NombreUsuario': USUARIO, 'p_ClaveUsuario': sha(clave), 'p_IdCanal': 'COR'})
    return s, j.get('P_OUT_TOKEN'), j.get('P_OUT_ERRORESCONTADOR')


env = leer_env()
actual = env['qa1.login.testpass']
nueva = generar()
open(PENDIENTE, 'w', encoding='utf-8').write(nueva)  # solo en el scratchpad de la sesion; se borra al terminar
clave_vigente = {m: actual for m in motores}
hechos = []

for motor in motores:
    print('== %s QA, usuario %s' % (motor.upper(), USUARIO), flush=True)
    estado(motor, 'ANTES')
    log = open(SCRATCH + '/app_cambio_%s.log' % motor, 'wb')
    app = subprocess.Popen([JAVA, '-jar', JAR, '--server.port=%d' % PUERTO], env=entorno(env, motor), stdout=log, stderr=subprocess.STDOUT)
    try:
        listo = False
        for _ in range(90):
            time.sleep(2)
            if app.poll() is not None:
                break
            try:
                c = http.client.HTTPConnection('127.0.0.1', PUERTO, timeout=3)
                c.request('GET', '/PLA/Ping')
                if c.getresponse().status == 200:
                    listo = True
                    break
            except OSError:
                pass
        print('aplicacion lista:', listo, flush=True)
        if not listo:
            print('la aplicacion no arranco; no se hizo nada en', motor)
            continue
        s, token, errores = login(actual)
        print('1 login con la clave actual: HTTP', s, '| token:', bool(token), '| P_OUT_ERRORESCONTADOR:', errores, flush=True)
        if not token:
            print('sin sesion: no se cambia la clave en', motor)
            continue
        s, j = post('/PLA/ActualizarClave', {'p_CodigoUsuario': USUARIO, 'Clave1': nueva, 'Clave2': nueva}, {'X-SESSION-TOKEN': token})
        print('2 ActualizarClave: HTTP', s, '| Estado:', j.get('Estado'), '| claves de la respuesta:', sorted(j.keys()), flush=True)
        if s != 200 or j.get('Estado') != 'OK':
            print('el cambio no se aplico en', motor, '| detalle de la politica:', j.get('status'))
            continue
        s, token2, errores = login(nueva)
        print('3 login con la clave nueva: HTTP', s, '| token:', bool(token2), '| P_OUT_ERRORESCONTADOR:', errores, flush=True)
        if token2:
            hechos.append(motor)
            clave_vigente[motor] = nueva
        else:
            print('ATENCION: la clave se cambio pero el login con la nueva fallo en', motor)
            hechos.append(motor + '(sin verificar)')
            clave_vigente[motor] = nueva
    finally:
        app.terminate()
        try:
            app.wait(timeout=20)
        except subprocess.TimeoutExpired:
            app.kill()
        log.close()
        print('aplicacion detenida', flush=True)
    estado(motor, 'DESPUES')

if hechos:
    ruta = REPO + '/.env'
    texto = open(ruta, encoding='utf-8-sig', newline='').read()
    nuevo = re.sub(r'(?m)^(qa1\.login\.testpass=).*$', lambda m: m.group(1) + nueva, texto)
    assert nuevo != texto
    open(ruta, 'w', encoding='utf-8', newline='').write(nuevo)
    print('.env actualizado con la clave nueva (qa1.login.testpass); motores con la clave nueva:', hechos)
    pendientes = [m for m in motores if m not in [h.split('(')[0] for h in hechos]]
    if pendientes:
        print('ATENCION: en %s sigue la clave anterior' % pendientes)
else:
    print('no se cambio ninguna clave; .env sin cambios')
os.remove(PENDIENTE)
