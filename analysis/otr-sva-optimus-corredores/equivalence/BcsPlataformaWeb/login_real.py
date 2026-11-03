"""Login real (CrearSesion de punta a punta) contra HANA QA o Postgres QA con el usuario acordado.
Escribe solo lo que el login escribe (sesion, cierre de sesiones anteriores, auditoria, contadores); no borra nada.
Nunca imprime clave, hash, token ni credenciales de base de datos."""
import hashlib, http.client, json, os, subprocess, sys, time

REPO = 'C:/Users/m.villagran.mora/Downloads/develop/nuam-modernizado'
JAR = REPO + '/modernized/otr-sva-optimus-corredores/BcsPlataformaWeb/target/bcs-plataforma-web-1.0.0-SNAPSHOT.jar'
SCRATCH = 'C:/Users/MVILLA~1.MOR/AppData/Local/Temp/claude/6929690f61ec5c2f/57bf1e9e-455e-4135-b1b4-d2d7dfeea3c8/scratchpad'
OLD = 'C:/Users/MVILLA~1.MOR/AppData/Local/Temp/claude/C--Users-m-villagran-mora-Downloads-develop-nuam-modernizado/57bf1e9e-455e-4135-b1b4-d2d7dfeea3c8/scratchpad'
M2 = os.path.expanduser('~/.m2/repository').replace('\\', '/')
CP = M2 + '/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar;' + M2 + '/com/sap/cloud/db/jdbc/ngdbc/2.8.12/ngdbc-2.8.12.jar'
JAVA = 'C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin/java.exe'
PUERTO = 18117
USUARIO = 'MAURICIO.VILLAGRAN'

motor = sys.argv[1]
assert motor in ('hana', 'postgres')


def leer_env():
    d = {}
    for linea in open(REPO + '/.env', encoding='utf-8-sig').read().splitlines():
        if '=' in linea and not linea.strip().startswith('#'):
            k, _, v = linea.partition('=')
            d[k.strip()] = v.strip()
    return d


def estado(etiqueta):
    r = subprocess.run([JAVA, '-cp', CP, SCRATCH + '/EstadoSesiones.java', REPO + '/.env', motor, USUARIO], capture_output=True, text=True, timeout=120)
    salida = [l for l in r.stdout.splitlines() if l.startswith(motor)]
    print('%-7s %s' % (etiqueta, salida[0] if salida else 'sin salida (codigo %s)' % r.returncode), flush=True)
    return salida[0] if salida else ''


env = leer_env()
clave_plana = env['qa1.login.testpass']
clave_navegador = hashlib.sha256(clave_plana.encode('utf-8')).hexdigest()
hijo_env = dict(os.environ)
if motor == 'hana':
    url = env['qa01.saphana.dbURL']
    props = [p for p in env.get('qa01.saphana.dbprop', '').split(';') if p]
    if props:
        url += ('&' if '?' in url else '?') + '&'.join(props)
    hijo_env.update({'SPRING_DATASOURCE_URL': url, 'SPRING_DATASOURCE_USERNAME': env['qa01.saphana.dbuser'], 'SPRING_DATASOURCE_PASSWORD': env['qa01.saphana.dbpass']})
else:
    hijo_env.update({'SPRING_DATASOURCE_URL': env['qa01.postgres.dbURL'], 'SPRING_DATASOURCE_USERNAME': env['qa01.postgres.dbuser'],
                     'SPRING_DATASOURCE_PASSWORD': env['qa01.postgres.dbpass'], 'SPRING_DATASOURCE_DRIVER_CLASS_NAME': 'org.postgresql.Driver',
                     'PLATAFORMA_DB_DIALECT': 'postgres', 'HANA_URL': env['qa01.postgres.dbURL']})

print('== login real contra', motor.upper(), 'QA, usuario', USUARIO, flush=True)
antes = estado('ANTES')
if not antes or 'ERROR' in antes or 'no existe' in antes:
    sys.exit('no se pudo leer el estado previo; no se hace el login')

log = open(SCRATCH + '/app_%s.log' % motor, 'wb')
app = subprocess.Popen([JAVA, '-jar', JAR, '--server.port=%d' % PUERTO], env=hijo_env, stdout=log, stderr=subprocess.STDOUT)
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
    print('aplicacion lista:', listo, '(codigo de salida %s)' % app.poll() if app.poll() is not None else '', flush=True)
    if not listo:
        sys.exit('la aplicacion no arranco; ver el log del scratchpad (app_%s.log), no se hizo ningun login' % motor)
    cuerpo = json.dumps({'p_IdEmpresa': '1', 'p_NombreUsuario': USUARIO, 'p_ClaveUsuario': clave_navegador, 'p_IdCanal': 'COR'})
    c = http.client.HTTPConnection('127.0.0.1', PUERTO, timeout=60)
    c.request('POST', '/PLA/CrearSesion', body=cuerpo, headers={'Content-Type': 'application/json'})
    r = c.getresponse()
    texto = r.read().decode('utf-8', 'replace')
    try:
        j = json.loads(texto)
    except ValueError:
        j = {}
    token = j.get('P_OUT_TOKEN')
    print('HTTP', r.status, '| token recibido:', bool(token), '| largo del token:', len(token) if token else 0,
          '| P_OUT_ERRORESCONTADOR:', j.get('P_OUT_ERRORESCONTADOR'), '| claves de la respuesta:', sorted(j.keys()), flush=True)
finally:
    app.terminate()
    try:
        app.wait(timeout=20)
    except subprocess.TimeoutExpired:
        app.kill()
    log.close()
    print('aplicacion detenida (codigo %s)' % app.returncode, flush=True)
estado('DESPUES')
