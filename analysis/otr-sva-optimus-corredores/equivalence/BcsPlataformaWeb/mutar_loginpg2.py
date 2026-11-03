"""Mutacion de la ronda 2 de LOGIN-PG (vistas y procedimientos, T-33): prueba que las pruebas LoginPg2* y las adaptadas PUEDEN fallar.

Se corre sobre una COPIA del modulo (nunca sobre el repo) donde esta la implementacion desechable de la ronda 2 y todo pasa en verde. Cada mutante cambia UN archivo
(un texto SQL de Postgres, una fila del catalogo, el recurso de procedimientos, un golden de HANA, una linea de la implementacion desechable o de su cableado), corre
las clases de prueba del grupo y debe producir al menos una falla; despues se restaura el archivo. Un mutante que sobrevive significa que las pruebas no fijan ese
detalle: se agregan casos hasta que muera, o se documenta como equivalente.

Uso:  python -X utf8 mutar_loginpg2.py <dir del modulo copia> <salida.json> [regex de nombres de mutante a correr]
"""
import json
import os
import re
import subprocess
import sys

MOD = sys.argv[1]
SALIDA = sys.argv[2]
RES = MOD + '/src/main/resources/'
S = RES + 'sql/postgres/'
EQ = RES + 'equivalencias/'
G = MOD + '/src/test/resources/loginpg/golden-hana/'
F2 = MOD + '/src/test/resources/loginpg2/'
J = MOD + '/src/main/java/cl/bcs/plataforma/login/dialecto/'
PROC = J + 'ProcedimientosPostgres.java'

VISTAS = 'LoginPg2TraduccionVistasTest,LoginPg2VistasH2Test'
CATALOGO = 'LoginPg2CatalogoVistasTest,LoginPgCatalogoTest'
GOLDEN = 'LoginPgSqlHanaTest,LoginPg2TraduccionVistasTest,LoginPg2VistasH2Test'
PROCS = 'LoginPg2ProcedimientosTest,LoginPg2EventoArreglosTest,LoginPg2ServiciosTest'
SPRING = 'LoginPg2SeleccionProcedimientosTest*'
FIXTURE = 'LoginPg2VistasH2Test,LoginPg2CatalogoVistasTest,LoginPg2ProcedimientosTest'


def leer(p):
    return open(p, encoding='utf-8', newline='').read()


def escribir(p, s):
    open(p, 'w', encoding='utf-8', newline='').write(s)


def reemplazar(ruta, viejo, nuevo, veces=1):
    def f(s):
        if viejo not in s:
            raise SystemExit('no esta en %s: %r' % (ruta, viejo))
        return s.replace(viejo, nuevo, veces)
    return (ruta, f)


def regex(ruta, patron, nuevo):
    def f(s):
        r, n = re.subn(patron, nuevo, s, count=1, flags=re.S)
        if n == 0:
            raise SystemExit('regex sin efecto en %s: %r' % (ruta, patron))
        return r
    return (ruta, f)


def borrar_linea(ruta, contiene):
    def f(s):
        for l in s.split('\n'):
            if contiene in l:
                return s.replace(l + '\n', '', 1)
        raise SystemExit('linea no encontrada: ' + contiene)
    return (ruta, f)


def agregar(ruta, texto):
    return (ruta, lambda s: s + texto)


MUTANTES = []


def m(nombre, grupo, mut, tests):
    MUTANTES.append((nombre, grupo, mut, tests))


# ---------------------------------------------------------------- texto de Postgres de las seis sentencias
E = S + 'BootstrapServicio.SQL_EMPRESAS.sql'
I = S + 'BootstrapServicio.SQL_IMAGEN.sql'
P = S + 'BootstrapServicio.SQL_PREFERENCIAS.sql'
GR = S + 'BootstrapServicio.SQL_PREFERENCIAS_GRILLA.sql'
A = S + 'BootstrapServicio.SQL_ACCESOS.sql'
D = S + 'GestionClaveServicio.SQL_DATOS_RECUPERACION.sql'
m('V01 EMPRESAS: WHERE removed', 'vistas', reemplazar(E, " WHERE id_estado = 'ACT'", ''), VISTAS)
m('V02 EMPRESAS: literal ACT -> BLO', 'vistas', reemplazar(E, "'ACT'", "'BLO'"), VISTAS)
m('V03 EMPRESAS: alias Nombre lost its case', 'vistas', reemplazar(E, 'AS "Nombre"', 'AS nombre'), VISTAS)
m('V04 IMAGEN: literal in lower case', 'vistas', reemplazar(I, "'LOGO_CORREDOR_LOGIN'", "'logo_corredor_login'"), VISTAS)
m('V05 IMAGEN: wrong column for Binario', 'vistas', reemplazar(I, 'binario AS', 'extension AS'), VISTAS)
m('V06 PREFERENCIAS: user filter removed', 'vistas', reemplazar(P, ' AND id_usuario = :IdUsuario', ''), VISTAS)
m('V07 PREFERENCIAS: Llave and Valor swapped', 'vistas', reemplazar(P, 'llave AS "Llave", valor AS "Valor"', 'valor AS "Llave", llave AS "Valor"'), VISTAS)
m('V08 GRILLA: company filter removed', 'vistas', reemplazar(GR, ' WHERE id_empresa = :IdEmpresa', ''), VISTAS)
m('V09 ACCESOS: mostrar_acceso filter removed', 'vistas', reemplazar(A, " AND mostrar_acceso = 'S'", ''), VISTAS)
m('V10 ACCESOS: NULLS FIRST removed from padre', 'vistas', reemplazar(A, 'padre NULLS FIRST', 'padre'), VISTAS)
m('V11 ACCESOS: order by orden dropped', 'vistas', reemplazar(A, ', orden NULLS FIRST', ''), VISTAS)
m('V12 ACCESOS: alias IdServicio renamed', 'vistas', reemplazar(A, 'AS "IdServicio"', 'AS "IdServicioX"'), VISTAS)
m('V13 ACCESOS: explicit list replaced by *', 'vistas', regex(A, r'SELECT .*? FROM v_pla_acc', 'SELECT * FROM v_pla_acc'), VISTAS)
m('V14 DATOS: LEFT JOIN of the person view becomes JOIN', 'vistas', reemplazar(D, 'LEFT JOIN v_socneg_per_consultar_persona', 'JOIN v_socneg_per_consultar_persona'), VISTAS)
m('V15 DATOS: LIMIT 1 removed', 'vistas', reemplazar(D, ' LIMIT 1', ''), VISTAS)
m('V16 DATOS: corredora taken from the person name', 'vistas', reemplazar(D, 'dtos.razon_social AS "corredora"', 'per.nombre_completo AS "corredora"'), VISTAS)
m('V17 DATOS: EMP arm compares id_cliente', 'vistas', reemplazar(D, 'usu.id_entidad = cli.id_empleado', 'usu.id_entidad = cli.id_cliente'), VISTAS)
m('V18 DATOS: contract joined on the person', 'vistas', reemplazar(D, 'dtos.id_empresa = cli.id_empresa', 'dtos.id_empresa = cli.id_persona'), VISTAS)
m('V19 DATOS: PER arm removed', 'vistas', reemplazar(D, " OR (usu.tipo_entidad = 'PER' AND usu.id_entidad = cli.id_persona)", ''), VISTAS)
m('V20 DATOS: a HANA token left (TOP 1 and no LIMIT)', 'vistas', reemplazar(D, 'SELECT cli.id_persona', 'SELECT TOP 1 cli.id_persona'), VISTAS)
# ---------------------------------------------------------------- catalogo de columnas (production CSV)
COLS = EQ + 'equivalencias-columnas.csv'
m('C01 view column renamed in the production CSV', 'catalogo', reemplazar(COLS, 'ACC.ConsultarUsuarioAcceso,v_pla_acc_consultar_usuario_acceso,IdAcceso,id_acceso', 'ACC.ConsultarUsuarioAcceso,v_pla_acc_consultar_usuario_acceso,IdAcceso,id_acceso_x'), CATALOGO)
m('C02 view column row deleted from the production CSV', 'catalogo', borrar_linea(COLS, 'PLA.ConsultarEmpresa,v_pla_pla_consultar_empresa,Nombre,nombre'), CATALOGO)
m('C03 a view row with another Postgres view name', 'catalogo', reemplazar(COLS, 'PER.ConsultarPersona,v_socneg_per_consultar_persona,IdPersona,id_persona', 'PER.ConsultarPersona,v_socneg_per_consultar_persona_x,IdPersona,id_persona'), CATALOGO)
m('C04 a table row changed (tables must stay as they were)', 'catalogo', reemplazar(COLS, 'ACC.Usuario,t_pla_acc_usuario,IdUsuario,id_usuario', 'ACC.Usuario,t_pla_acc_usuario,IdUsuario,id_usuario_x'), CATALOGO)
m('C05 an invented view column row', 'catalogo', agregar(COLS, 'BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa,v_pla_pla_consultar_empresa,Inventada,inventada\n'), CATALOGO)
m('C06 all view rows lost (CSV as in round 1)', 'catalogo', regex(COLS, r'\nBSCL\.Plataforma\.BD\.Vistas::ACC\.ConsultarUsuarioAcceso,.*', '\n'), CATALOGO)
# ---------------------------------------------------------------- recurso de procedimientos
PR = EQ + 'procedimientos-postgres.csv'
m('R01 admin OUT legacy name with an underscore', 'recurso', reemplazar(PR, 'P_OUT_ESADMIN', 'P_OUT_ES_ADMIN'), PROCS)
m('R02 two OUT names of fechas swapped', 'recurso', reemplazar(PR, 'P_OUT_FECHAHORASISTEMALOCAL,p_out_fecha_hora_sistema_local', 'P_OUT_FECHAHORASISTEMAUTC,p_out_fecha_hora_sistema_local'), PROCS)
m('R03 the function typed as procedure', 'recurso', reemplazar(PR, 'fn_pla_consultar_formatos,FUNCION', 'fn_pla_consultar_formatos,PROCEDIMIENTO', 100), PROCS)
m('R04 an ATRIBUTO row deleted', 'recurso', borrar_linea(PR, 'p_valores.llave'), PROCS)
m('R05 bigint IN typed integer', 'recurso', reemplazar(PR, 'p_id_usuario,p_id_usuario,bigint', 'p_id_usuario,p_id_usuario,integer'), PROCS)
m('R06 a ninth procedure added to the resource', 'recurso', agregar(PR, 'BSCL.Plataforma.BD.Servicios::AgregarLog,sp_pla_agregar_log,PROCEDIMIENTO,1,IN,p_id_tipo_log,p_id_tipo_log,character varying\n'), PROCS)
m('R07 formats column label', 'recurso', reemplazar(PR, 'PrecioDecimales,precio_decimales', 'PrecioDecimal,precio_decimales'), PROCS)
m('R08 mail OUT position swapped with the counter', 'recurso', reemplazar(PR, 'RecuperarClaveUsuario,sp_pla_acc_recuperar_clave_usuario,PROCEDIMIENTO,4,OUT,P_OUT_MAIL', 'RecuperarClaveUsuario,sp_pla_acc_recuperar_clave_usuario,PROCEDIMIENTO,4,OUT,P_OUT_ADVERTENCIASCONTADOR'), PROCS)
# ---------------------------------------------------------------- goldens de HANA
m("H01 golden SQL_EMPRESAS literal", 'golden', reemplazar(G + 'BootstrapServicio.SQL_EMPRESAS.sql', "'ACT'", "'ACX'"), GOLDEN)
m('H02 golden SQL_ACCESOS order by swapped', 'golden', reemplazar(G + 'BootstrapServicio.SQL_ACCESOS.sql', 'ORDER BY "Padre", "Orden"', 'ORDER BY "Orden", "Padre"'), GOLDEN)
m('H03 golden SQL_DATOS_RECUPERACION TOP 2', 'golden', reemplazar(G + 'GestionClaveServicio.SQL_DATOS_RECUPERACION.sql', 'TOP 1', 'TOP 2'), GOLDEN)
m('H04 golden SQL_PREFERENCIAS filter removed', 'golden', reemplazar(G + 'BootstrapServicio.SQL_PREFERENCIAS.sql', ' AND "IdUsuario" = :IdUsuario', ''), GOLDEN)
m('H05 golden SQL_IMAGEN literal', 'golden', reemplazar(G + 'BootstrapServicio.SQL_IMAGEN.sql', 'LOGO_CORREDOR_LOGIN', 'LOGO_CORREDOR'), GOLDEN)
m('H06 golden SQL_PREFERENCIAS_GRILLA view renamed', 'golden', reemplazar(G + 'BootstrapServicio.SQL_PREFERENCIAS_GRILLA.sql', 'ConsultarConfiguracionGrilla', 'ConsultarConfiguracionGrillas'), GOLDEN)
m('H07 golden SP_FECHAS name', 'golden', reemplazar(G + 'BootstrapServicio.SP_FECHAS.sql', 'ObtenerFechas', 'ObtenerFecha'), GOLDEN)
m('H08 golden SQL_DATOS_RECUPERACION LEFT -> INNER', 'golden', reemplazar(G + 'GestionClaveServicio.SQL_DATOS_RECUPERACION.sql', 'LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT', 'INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT'), GOLDEN)
# ---------------------------------------------------------------- fixtures y oraculos de la ronda 2
m('F01 H2 fixture: the NULL-padre row removed', 'fixture', regex(F2 + 'vistas-h2.json', r'\[10, "JPEREZ", 8, "SinOrden".*?\],\n', ''), FIXTURE)
m('F02 H2 fixture: the excluded empresa row removed', 'fixture', regex(F2 + 'vistas-h2.json', r'\[2, "Inactiva S.A.", "BLO", "EMP2"\],\n', ''), FIXTURE)
m('F03 H2 fixture: logo bytes changed', 'fixture', reemplazar(F2 + 'vistas-h2.json', 'hex:0102ff', 'hex:0102fe'), FIXTURE)
m('F04 oracle view columns: a Postgres column renamed', 'fixture', reemplazar(F2 + 'oraculo-vistas-columnas.csv', ',IdEstado,id_estado,', ',IdEstado,id_estado_x,'), FIXTURE)
m('F05 routines oracle: a parameter type changed', 'fixture', reemplazar(F2 + 'rutinas-y-tipos-postgres-qa-20261103.txt', 'OUT p_out_valor character varying', 'OUT p_out_valor integer'), FIXTURE)
m('F06 routines oracle: composite attribute order', 'fixture', reemplazar(F2 + 'rutinas-y-tipos-postgres-qa-20261103.txt', 'TIPO ty_pla_evt_agregar_evento_valores | 2 llave character varying(40)', 'TIPO ty_pla_evt_agregar_evento_valores | 2 llaves character varying(40)'), FIXTURE)
m('F07 view TSV: a column type changed', 'fixture', reemplazar(F2 + 'vistas-columnas-pg-qa-20261103.tsv', 'v_pla_pla_consultar_empresa\tnombre\tcharacter varying', 'v_pla_pla_consultar_empresa\tnombre\tinteger'), FIXTURE)
# ---------------------------------------------------------------- implementacion desechable de los procedimientos
m('J01 call text in upper case', 'codigo', reemplazar(PROC, '"{call "', '"{CALL "'), PROCS)
m('J02 INOUT bound with Types.NULL', 'codigo', reemplazar(PROC, 'cs.setNull(p.pos(), Types.OTHER);', 'cs.setNull(p.pos(), Types.NULL);'), PROCS)
m('J03 INOUT not registered', 'codigo', reemplazar(PROC, 'if (!p.modo().equals("IN")) cs.registerOutParameter', 'if (p.modo().equals("OUT")) cs.registerOutParameter'), PROCS)
m('J04 integer OUT registered as VARCHAR', 'codigo', reemplazar(PROC, 'case "integer" -> Types.INTEGER;', 'case "integer" -> Types.VARCHAR;'), PROCS)
m('J05 answer under the Postgres names', 'codigo', reemplazar(PROC, 'r.addProperty(p.hana(), cs.getString(p.pos()));', 'r.addProperty(p.pg(), cs.getString(p.pos()));'), PROCS)
m('J06 cursor result set not closed', 'codigo', reemplazar(PROC, 'try (rs) {', '{'), PROCS)
m('J07 callable statement not closed', 'codigo', reemplazar(PROC, 'try (CallableStatement cs = con.prepareCall(sb.toString())) {', '{ CallableStatement cs = con.prepareCall(sb.toString());'), PROCS)
m('J08 commit after execute', 'codigo', reemplazar(PROC, 'cs.execute();', 'cs.execute(); con.commit();'), PROCS)
m('J09 literal: comma does not force quotes', 'codigo', reemplazar(PROC, "c == ',' || ", ''), PROCS)
m('J10 literal: backslash not doubled', 'codigo', reemplazar(PROC, 'valor.replace("\\\\", "\\\\\\\\").replace(', 'valor.replace('), PROCS)
m('J11 attributes in reverse order', 'codigo', reemplazar(PROC, 'for (String[] a : d.atributos().get(p.pg())) {', 'List<String[]> inv = new ArrayList<>(d.atributos().get(p.pg())); java.util.Collections.reverse(inv); for (String[] a : inv) {'), PROCS)
m('J12 empty text not quoted', 'codigo', reemplazar(PROC, 'boolean cita = valor.isEmpty();', 'boolean cita = false;'), PROCS)
m('J13 bytea not converted to hex', 'codigo', reemplazar(PROC, 'a[1].equals("bytea")', 'false'), PROCS)
m('J14 empty table becomes a NULL array', 'codigo', reemplazar(PROC, 'return con.createArrayOf(tipo, elementos.toArray());', 'return elementos.isEmpty() ? null : con.createArrayOf(tipo, elementos.toArray());'), PROCS)
m('J15 absent origin bound as NULL', 'codigo', regex(PROC, r'if \(p\.pg\(\)\.equals\("p_aplica_origen_evento"\)\) cs\.setString\(p\.pos\(\), ""\);\s*else cs\.setNull', 'cs.setNull'), PROCS)
m('J16 NULL bigint typed VARCHAR', 'codigo', reemplazar(PROC, 'cs.setNull(p.pos(), Types.BIGINT)', 'cs.setNull(p.pos(), Types.VARCHAR)'), PROCS)
# J17 (name matched after strip) is an EQUIVALENT mutant: the catalog lookup right after it (nombrePostgres) still refuses the unstripped name with the 503 exception,
# so both guards answer 503 and only the exception class differs. It is kept out of the list on purpose; J17b is the meaningful one.
m('J17b an unknown procedure is served as the first one of the resource', 'codigo', reemplazar(PROC, 'defs.get(ruta);', 'defs.getOrDefault(ruta, defs.values().iterator().next());'), PROCS)
m('J18 NO_DISPONIBLE ignored (name from the resource)', 'codigo', reemplazar(PROC, 'String pg = catalogo.nombrePostgres(ruta);', 'String pg = d.pg();'), PROCS)
m('J19 the input JSON is modified', 'codigo', reemplazar(PROC, 'Def d = ruta == null', 'entrada.addProperty("x", 1); Def d = ruta == null'), PROCS)
m('J20 function through prepareCall', 'codigo', reemplazar(PROC, 'con.prepareStatement("SELECT * FROM "', 'con.prepareCall("SELECT * FROM "'), PROCS)
m('J21 function labels not mapped to HANA names', 'codigo', reemplazar(PROC, 'aHana.getOrDefault(l, l)', 'l'), PROCS)
m('J22 NULL OUT value as the text null', 'codigo', reemplazar(PROC, 'r.addProperty(p.hana(), cs.getString(p.pos()));', 'r.addProperty(p.hana(), String.valueOf(cs.getString(p.pos())));'), PROCS)
m('J23 IN keys matched case-sensitively', 'codigo', reemplazar(PROC, 'if (norm(k).equals(n)) return k;', 'if (k.equals(nombre)) return k;'), PROCS)
m('J24 array type name qualified', 'codigo', reemplazar(PROC, 'con.createArrayOf(tipo,', 'con.createArrayOf("bscl." + tipo,'), PROCS)
m('J25 a bigint IN bound as text', 'codigo', reemplazar(PROC, 'cs.setLong(p.pos(), Long.parseLong(v.getAsString()));', 'cs.setString(p.pos(), v.getAsString());'), PROCS)
m('J26 first cursor read from the second position', 'codigo', reemplazar(PROC, 'Object o = cs.getObject(p.pos());', 'Object o = cs.getObject(p.pos() == 6 ? 8 : p.pos());'), PROCS)
m('J27 a non-array table is read as one row', 'codigo', reemplazar(PROC, 'if (e != null && e.isJsonArray()) {', 'if (e != null && e.isJsonObject()) {'), PROCS)
# ---------------------------------------------------------------- selector y cableado
m('W01 selector inverted', 'codigo', reemplazar(J + 'ProcedimientosPorDialecto.java', '"postgres".equals(dialecto.nombre())', '"hana".equals(dialecto.nombre())'), SPRING)
m('W02 selector copies the JSON', 'codigo', reemplazar(J + 'ProcedimientosPorDialecto.java', 'postgres.llamar(con, rutaServicio, entrada)', 'postgres.llamar(con, rutaServicio, entrada.deepCopy())'), SPRING)
m('W03 selector bean not primary', 'codigo', reemplazar(J + 'ProcedimientosConfiguracion.java', '@Primary\n', ''), SPRING)
m('W04 selector always uses HANA', 'codigo', reemplazar(J + 'ProcedimientosPorDialecto.java', 'return "postgres".equals(dialecto.nombre()) ? postgres.llamar(con, rutaServicio, entrada) : hana.llamar(con, rutaServicio, entrada);', 'return hana.llamar(con, rutaServicio, entrada);'), SPRING)


def correr(tests):
    env = dict(os.environ)
    env['JAVA_HOME'] = 'C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.4.101-hotspot'
    env['PATH'] = '/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin:' + env['PATH']
    r = subprocess.run('mvn -o test -Dtest="%s" -Dsurefire.failIfNoSpecifiedTests=false' % tests, shell=True, cwd=MOD, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace')
    out = r.stdout
    resumen = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)\s*$', out, re.M)
    compila = 'COMPILATION ERROR' not in out
    if not resumen:
        return {'ejecutadas': 0, 'fallas': 0, 'errores': 0, 'compila': compila, 'rc': r.returncode}
    a, b, c, d = map(int, resumen[-1])
    return {'ejecutadas': a, 'fallas': b, 'errores': c, 'compila': compila, 'rc': r.returncode}


def main():
    bases = {}
    filtro0 = sys.argv[3] if len(sys.argv) > 3 else None
    for t in sorted(set(x[3] for x in MUTANTES if not filtro0 or re.search(filtro0, x[0]))):
        bases[t] = correr(t)
        print('BASELINE', t, bases[t], flush=True)
        if bases[t]['fallas'] or bases[t]['errores'] or bases[t]['ejecutadas'] == 0:
            raise SystemExit('el baseline no esta en verde: no se puede mutar: ' + t)
    resultados = []
    filtro = sys.argv[3] if len(sys.argv) > 3 else None
    for nombre, grupo, (ruta, f), tests in MUTANTES:
        if filtro and not re.search(filtro, nombre):
            continue
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
        print(('KILLED  ' if muerto else 'SURVIVED'), nombre, '(%d tests, %d failures, %d errors, compila=%s)' % (r['ejecutadas'], r['fallas'], r['errores'], r['compila']), flush=True)
        json.dump({'baselines': bases, 'resultados': resultados}, open(SALIDA, 'w', encoding='utf-8'), indent=2)
    vivos = [r['mutante'] for r in resultados if not r['muerto']]
    print('MUTANTS: %d, killed: %d, survived: %d' % (len(resultados), len(resultados) - len(vivos), len(vivos)))
    for v in vivos:
        print('  SURVIVED:', v)
    sys.exit(1 if vivos else 0)


if __name__ == '__main__':
    main()
