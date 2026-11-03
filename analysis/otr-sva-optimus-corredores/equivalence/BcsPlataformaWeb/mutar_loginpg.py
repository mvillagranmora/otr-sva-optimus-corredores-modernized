"""Mutacion de oraculo, goldens y (canarios) del codigo de la implementacion desechable, para probar que las pruebas LOGIN-PG pueden fallar.

Se corre sobre una COPIA del modulo (nunca sobre el repo) en la que ya estan las clases de produccion desechables y las pruebas pasan en verde.
Cada mutante cambia UN archivo, corre las clases de prueba indicadas y debe producir al menos una falla; luego se restaura el archivo.
Un mutante que sobrevive significa que las pruebas no fijan ese detalle: se agregan casos hasta que muera, o se documenta como equivalente.

Uso:  python -X utf8 mutar_loginpg.py <dir del modulo copia> <salida.json>
"""
import json
import os
import re
import subprocess
import sys

MOD = sys.argv[1]
SALIDA = sys.argv[2]
T = MOD + '/src/test/resources/loginpg/'
G = T + 'golden-hana/'
M = MOD + '/src/main/java/cl/bcs/plataforma/login/dialecto/'
S = MOD + '/src/main/resources/sql/postgres/'

NO_SPRING = 'LoginPgSqlHanaTest,LoginPgCatalogoTest,LoginPgTraduccionTest,LoginPgDialectoFuncionesTest,LoginPgResultadosH2Test'
SPRING = 'LoginPgNoDisponibleTest*,LoginPgSeleccionTest*'


def leer(p):
    return open(p, encoding='utf-8', newline='').read()


def escribir(p, s):
    open(p, 'w', encoding='utf-8', newline='').write(s)


def linea(texto, contiene):
    for l in texto.split('\n'):
        if contiene in l:
            return l
    raise SystemExit('linea no encontrada: ' + contiene)


def reemplazar(ruta, viejo, nuevo, veces=1):
    def f(s):
        if viejo not in s:
            raise SystemExit('no esta en %s: %r' % (ruta, viejo))
        return s.replace(viejo, nuevo, veces)
    return (ruta, f)


def borrar_linea(ruta, contiene):
    def f(s):
        l = linea(s, contiene)
        return s.replace(l + '\n', '', 1)
    return (ruta, f)


def intercambiar(ruta, a, b):
    def f(s):
        la, lb = linea(s, a), linea(s, b)
        pa, pb = la.split(','), lb.split(',')
        # swap the Postgres name (3rd field of objects, 4th of columns)
        i = 2 if ruta.endswith('objetos.csv') else 3
        pa[i], pb[i] = pb[i], pa[i]
        return s.replace(la, '\x00A').replace(lb, '\x00B').replace('\x00A', ','.join(pa)).replace('\x00B', ','.join(pb))
    return (ruta, f)


def agregar_linea(ruta, nueva):
    return (ruta, lambda s: s + nueva + '\n')


OBJ = T + 'oraculo-objetos.csv'
COL = T + 'oraculo-columnas.csv'
ALC = T + 'login-alcance.txt'
U = 'BSCL.Plataforma.BD::ACC.Usuario'

MUTANTES = []


def m(nombre, grupo, mut, tests=NO_SPRING):
    MUTANTES.append((nombre, grupo, mut, tests))


# ---- oracle CSV: objects
m('O01 ACC.Usuario pg name +s', 'oraculo', reemplazar(OBJ, 't_pla_acc_usuario,', 't_pla_acc_usuarios,'))
m('O02 ACC.Sesion pg name suffix', 'oraculo', reemplazar(OBJ, 't_pla_acc_sesion,', 't_pla_acc_sesion_x,'))
m('O03 sequence Log renamed', 'oraculo', reemplazar(OBJ, 'sq_pla_log,', 'sq_pla_log2,'))
m('O04 sequence DetalleLog renamed', 'oraculo', reemplazar(OBJ, 'sq_pla_detalle_log,', 'sq_pla_detalle,'))
m('O05 ConsultarFormatos fn_ -> sp_', 'oraculo', reemplazar(OBJ, 'fn_pla_consultar_formatos', 'sp_pla_consultar_formatos'))
m('O06 RecuperarClaveUsuario renamed', 'oraculo', reemplazar(OBJ, 'sp_pla_acc_recuperar_clave_usuario', 'sp_pla_recuperar_clave_usuario'))
m('O07 view ConsultarEmpresa renamed', 'oraculo', reemplazar(OBJ, 'PLA.ConsultarEmpresa,v_', 'PLA.ConsultarEmpresa,vv_'))
m('O08 PLA.Configuracion typed vista', 'oraculo', reemplazar(OBJ, 'tabla,BSCL.Plataforma.BD::PLA.Configuracion,', 'vista,BSCL.Plataforma.BD::PLA.Configuracion,'))
m('O09 ACC.PasswordProhibidas row deleted', 'oraculo', borrar_linea(OBJ, '::ACC.PasswordProhibidas,'))
m('O10 swap pg names of Empleado and Persona', 'oraculo', intercambiar(OBJ, '::EMP.Empleado,', '::PER.Persona,'))
m('O11 pg name upper case', 'oraculo', reemplazar(OBJ, 't_pla_pla_configuracion,', 'T_pla_pla_configuracion,'))
m('O12 a table outside the login changed', 'oraculo', reemplazar(OBJ, 'BSCL.Plataforma.BD::PLA.Servicio,t_', 'BSCL.Plataforma.BD::PLA.Servicio,tt_'))
m('O13 extra row: same HANA name, other pg name', 'oraculo', agregar_linea(OBJ, 'tabla,BSCL.Plataforma.BD::ACC.Usuario,t_pla_acc_usuario_dup,Plataforma,BcsPlataformaWeb'))
m('O14 extra object invented', 'oraculo', agregar_linea(OBJ, 'tabla,BSCL.Plataforma.BD::ACC.Inventada,t_pla_acc_inventada,Plataforma,BcsPlataformaWeb'))
# ---- oracle CSV: columns
m('C01 Usuario.IdEmpresa -> idempresa', 'oraculo', reemplazar(COL, U + ',t_pla_acc_usuario,IdEmpresa,id_empresa', U + ',t_pla_acc_usuario,IdEmpresa,idempresa'))
m('C02 Usuario.CodigoUsuario row deleted', 'oraculo', borrar_linea(COL, U + ',t_pla_acc_usuario,CodigoUsuario,'))
m('C03 swap Usuario.IdUsuario and Usuario.IdEmpresa', 'oraculo', intercambiar(COL, U + ',t_pla_acc_usuario,IdUsuario,', U + ',t_pla_acc_usuario,IdEmpresa,'))
m('C04 PLA.Configuracion.Valor -> valor_x', 'oraculo', reemplazar(COL, 'PLA.Configuracion,t_pla_pla_configuracion,Valor,valor', 'PLA.Configuracion,t_pla_pla_configuracion,Valor,valor_x'))
m('C05 Sesion table name in column sheet', 'oraculo', reemplazar(COL, 'BSCL.Plataforma.BD::ACC.Sesion,t_pla_acc_sesion,IdUsuario', 'BSCL.Plataforma.BD::ACC.Sesion,t_pla_acc_sesiones,IdUsuario'))
m('C06 extra column invented for Usuario', 'oraculo', agregar_linea(COL, U + ',t_pla_acc_usuario,ColumnaNueva,columna_nueva'))
m('C07 a column of a table outside the login changed', 'oraculo', reemplazar(COL, 'BSCL.Plataforma.BD::PLA.Servicio,t_pla_pla_servicio,', 'BSCL.Plataforma.BD::PLA.Servicio,t_pla_pla_servicio,', 1) if False else reemplazar(COL, 'BSCL.APV.BD::PLA.ConfiguracionAPV,t_apv_pla_configuracion_apv,IdEmpresa,id_empresa', 'BSCL.APV.BD::PLA.ConfiguracionAPV,t_apv_pla_configuracion_apv,IdEmpresa,id_empresa_x'))
m('C08 Sesion.IdTokenSesion -> id_token', 'oraculo', reemplazar(COL, 't_pla_acc_sesion,IdTokenSesion,id_token_sesion', 't_pla_acc_sesion,IdTokenSesion,id_token'))
m('C09 DiaFeriado.Fecha -> fecha_feriado', 'oraculo', reemplazar(COL, 'REG.DiaFeriado,t_pla_reg_dia_feriado,Fecha,fecha', 'REG.DiaFeriado,t_pla_reg_dia_feriado,Fecha,fecha_feriado'))
m('C10 PLA.Log.IdLog -> id_log_x', 'oraculo', reemplazar(COL, 'PLA.Log,t_pla_pla_log,IdLog,id_log', 'PLA.Log,t_pla_pla_log,IdLog,id_log_x'))
# ---- login scope file
m('A01 scope: a login object removed', 'oraculo', borrar_linea(ALC, '::ACC.Sesion\t'))
m('A02 scope: a type changed', 'oraculo', reemplazar(ALC, 'tabla\tBSCL.Plataforma.BD::ACC.Usuario\t', 'vista\tBSCL.Plataforma.BD::ACC.Usuario\t'))
# ---- SQL goldens
m('G01 BUSCA_TOKEN TOP 1 -> TOP 2', 'golden', reemplazar(G + 'CrearSesionServicio.QUERY_BUSCA_TOKEN.sql', 'TOP 1', 'TOP 2'), NO_SPRING)
m("G02 CLOSE_SESION 'N' -> 'S'", 'golden', reemplazar(G + 'CrearSesionServicio.CLOSE_SESION.sql', "\"TokenValido\" = 'N'", "\"TokenValido\" = 'S'"))
m("G03 VALIDA_TOKEN 'S' -> 'N'", 'golden', reemplazar(G + 'FiltroSesion.SQL_VALIDA_TOKEN.sql', "'S'", "'N'"))
m('G04 BLOQUEO ADD_SECONDS space removed', 'golden', reemplazar(G + 'CrearSesionServicio.UPDATE_BLOQUEO_TEMPORAL.sql', 'ADD_SECONDS (', 'ADD_SECONDS('))
m('G05 RELOJ_UTC as -> AS', 'golden', reemplazar(G + 'CrearSesionServicio.QUERY_RELOJ_UTC.sql', 'as "UTC"', 'AS "UTC"'))
m('G06 FERIADOS date format', 'golden', reemplazar(G + 'BootstrapServicio.SQL_FERIADOS.sql', 'YYYY-MM-DD', 'YYYY/MM/DD'))
m('G07 INSERT_LOG parameter renamed', 'golden', reemplazar(G + 'AuditoriaSesion.INSERT_LOG.sql', ':IdLog', ':IdLogX'))
m('G08 trailing newline appended to a golden', 'golden', (G + 'CrearSesionServicio.QUERY_PARAMETRO.sql', lambda s: s + '\n'))
m('G09 INDEX.txt: an id removed', 'golden', borrar_linea(G + 'INDEX.txt', 'SesionDatos.QUERY_EMPRESA'))
m('G10 flows json: one text altered', 'golden', reemplazar(G + 'enviado-por-flujo.json', 'SELECT COUNT(1) AS OK', 'SELECT COUNT(2) AS OK'))
m('G11 flows json: a statement removed', 'golden', reemplazar(G + 'enviado-por-flujo.json', '"SELECT CURRENT_UTCTIMESTAMP as \\"UTC\\" FROM DUMMY",', ''))
m("G12 CONFIGURACIONES module name", 'golden', reemplazar(G + 'BootstrapServicio.SQL_CONFIGURACIONES.sql', "'APV' AS", "'APV2' AS"))
m('G13 QUERY_USUARIOS column swapped', 'golden', reemplazar(G + 'CrearSesionServicio.QUERY_USUARIOS.sql', '"Clave","FechaVigencia"', '"FechaVigencia","Clave"'))
m('G14 CLAVE_PROVISORIA LEFT -> INNER', 'golden', reemplazar(G + 'GestionClaveServicio.SQL_CLAVE_PROVISORIA.sql', 'LEFT JOIN', 'INNER JOIN'))
m("G15 SesionDatos.UPDATE_USUARIO 'S' -> 'N'", 'golden', reemplazar(G + 'SesionDatos.UPDATE_USUARIO.sql', "\"Conectado\" = 'S'", "\"Conectado\" = 'N'"))
m('G16 PALABRA_PROHIBIDA COUNT(1) -> COUNT(*)', 'golden', reemplazar(G + 'GestionClaveServicio.SQL_PALABRA_PROHIBIDA.sql', 'COUNT(1)', 'COUNT(*)'))
m('G17 SP_FORMATOS name altered', 'golden', reemplazar(G + 'BootstrapServicio.SP_FORMATOS.sql', 'ConsultarFormatos', 'ConsultarFormato'))
m('G18 flows json: CALL syntax lower case', 'golden', reemplazar(G + 'enviado-por-flujo.json', '{CALL BSCL.', '{call BSCL.'))
# ---- canaries on the throwaway production code (the tests must go red when the implementation breaks)
m('K01 postgres first-N: LIMIT n+1', 'codigo', reemplazar(M + 'PostgresDialecto.java', '" LIMIT " + n;', '" LIMIT " + (n + 1);'))
m('K02 hana first-N: TOP n+1', 'codigo', reemplazar(M + 'HanaDialecto.java', '"$1SELECT TOP " + n + " "', '"$1SELECT TOP " + (n + 1) + " "'))
m('K03 postgres UTC clock without time zone', 'codigo', reemplazar(M + 'PostgresDialecto.java', "AT TIME ZONE 'UTC'", "AT TIME ZONE 'CLT'"))
m('K04 postgres add-seconds in minutes', 'codigo', reemplazar(M + 'PostgresDialecto.java', "INTERVAL '1' SECOND", "INTERVAL '1' MINUTE"))
m('K05 postgres to-integer casts to varchar', 'codigo', reemplazar(M + 'PostgresDialecto.java', 'AS integer', 'AS varchar'))
m('K06 postgres date-to-text uses to_date', 'codigo', reemplazar(M + 'PostgresDialecto.java', '"to_char("', '"to_date("'))
m('K07 postgres sequence name not resolved', 'codigo', reemplazar(M + 'PostgresDialecto.java', "return \"nextval('\" + catalogo.nombrePostgres(secuenciaHana) + \"')\";", "return \"nextval('sq_pla_log')\";"))
m('K08 availability check removed from the page (no 503 before reading)', 'codigo', reemplazar(M + 'SentenciasSql.java', 'for (String id : ids) for (String o : objetosDe(id)) dialecto.exigirObjeto(o);', ''), SPRING)
m('K09 catalog ignores NO_DISPONIBLE', 'codigo', reemplazar(M + 'CatalogoEquivalencias.java', '&& !o.postgres().equals("NO_DISPONIBLE");', ';'))
m('K10 catalog tolerates conflicting duplicates', 'codigo', reemplazar(M + 'CatalogoEquivalencias.java', 'if (!previo.equals(o)) throw', 'if (false) throw'))
m('K11 hana dialect refuses a NO_DISPONIBLE object (HANA not untouched)', 'codigo', reemplazar(M + 'HanaDialecto.java', 'public void exigirObjeto(String objetoHana) {\n    }', 'public void exigirObjeto(String objetoHana) {\n        catalogo.nombrePostgres(objetoHana);\n    }'), SPRING)
m('K12 default dialect becomes postgres', 'codigo', reemplazar(M + 'DialectoConfig.java', '${plataforma.db.dialect:hana}', '${plataforma.db.dialect:postgres}'), SPRING)
m('K13 first-N accepts n=0', 'codigo', reemplazar(M + 'Dialectos.java', 'if (n < 1)', 'if (n < 0)'))
m('K14 postgres resource: LIMIT removed', 'codigo', reemplazar(S + 'CrearSesionServicio.QUERY_BUSCA_TOKEN.sql', ' LIMIT 1', ''))
m('K15 postgres resource: alias lost', 'codigo', reemplazar(S + 'SesionDatos.QUERY_EMPRESA.sql', 'AS "IdPais"', 'AS idpais'))
m('K16 postgres resource: wrong column', 'codigo', reemplazar(S + 'CrearSesionServicio.UPDATE_INTENTOS.sql', 'intentos_conexion', 'intentos_conexion_clave_provisoria'))
m('K17 postgres resource: HANA token left (FROM DUMMY)', 'codigo', reemplazar(S + 'CrearSesionServicio.QUERY_RELOJ_UTC.sql', "'UTC')", "'UTC') FROM DUMMY"))
m('K18 postgres resource: derived table without alias', 'codigo', reemplazar(S + 'BootstrapServicio.SQL_CONFIGURACIONES.sql', ') AS q WHERE', ') WHERE'))
m('K19 postgres resource: wrong literal', 'codigo', reemplazar(S + 'CrearSesionServicio.CLOSE_SESION.sql', "token_valido = 'N'", "token_valido = 'S'"))
m('K20 catalog: columns served from the wrong table', 'codigo', reemplazar(M + 'CatalogoEquivalencias.java', 'String pg = c.get(columnaHana);', 'String pg = c.get(columnaHana) == null ? null : c.get(columnaHana) + "x";'))


def correr(tests):
    env = dict(os.environ)
    env['JAVA_HOME'] = 'C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot'
    env['PATH'] = '/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin:' + env['PATH']
    r = subprocess.run('mvn -o test -Dtest="%s" -Dsurefire.failIfNoSpecifiedTests=false' % tests, shell=True, cwd=MOD, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace')
    out = r.stdout
    resumen = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)\s*$', out, re.M)
    compila = 'COMPILATION ERROR' in out
    if not resumen:
        return {'ejecutadas': 0, 'fallas': 0, 'errores': 0, 'compila': not compila, 'rc': r.returncode}
    a, b, c, d = map(int, resumen[-1])
    return {'ejecutadas': a, 'fallas': b, 'errores': c, 'compila': not compila, 'rc': r.returncode}


def main():
    base = correr(NO_SPRING)
    print('BASELINE', base, flush=True)
    if base['fallas'] or base['errores'] or base['ejecutadas'] == 0:
        raise SystemExit('el baseline no esta en verde: no se puede mutar')
    resultados = []
    for nombre, grupo, (ruta, f), tests in MUTANTES:
        original = leer(ruta)
        try:
            nuevo = f(original)
            if nuevo == original:
                raise SystemExit('el mutante no cambia nada: ' + nombre)
            escribir(ruta, nuevo)
            r = correr(tests)
        finally:
            escribir(ruta, original)
        muerto = (r['fallas'] + r['errores'] > 0) or not r['compila']
        r.update({'mutante': nombre, 'grupo': grupo, 'muerto': muerto, 'pruebas': tests})
        resultados.append(r)
        print(('KILLED  ' if muerto else 'SURVIVED'), nombre, '(%d tests, %d failures, %d errors)' % (r['ejecutadas'], r['fallas'], r['errores']), flush=True)
        json.dump({'baseline': base, 'resultados': resultados}, open(SALIDA, 'w', encoding='utf-8'), indent=2)
    vivos = [r['mutante'] for r in resultados if not r['muerto']]
    print('MUTANTS: %d, killed: %d, survived: %d' % (len(resultados), len(resultados) - len(vivos), len(vivos)))
    for v in vivos:
        print('  SURVIVED:', v)
    sys.exit(1 if vivos else 0)


if __name__ == '__main__':
    main()
