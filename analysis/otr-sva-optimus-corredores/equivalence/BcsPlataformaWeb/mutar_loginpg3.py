"""Mutacion del ajuste LOGIN-PG3 (dialecto sin catalogo en produccion): prueba que las pruebas LoginPg* PUEDEN fallar.

Se corre sobre una COPIA del modulo (nunca sobre el repo) donde esta la implementacion desechable del contrato nuevo y todo pasa en verde. Cada mutante cambia UN
archivo (un texto SQL de Postgres, un archivo de nombre, una constante de secuencia, una definicion de procedimiento, un oraculo o fixture, un golden de HANA, una
linea de la implementacion desechable o de su cableado, o AGREGA un archivo que debe estar prohibido), corre las clases de prueba del grupo y debe producir al menos
una falla; despues se restaura el archivo (o se borra el agregado). Un mutante que sobrevive significa que las pruebas no fijan ese detalle: se agregan casos hasta
que muera, o se documenta como equivalente.

Uso:  python -X utf8 mutar_loginpg3.py <dir del modulo copia> <salida.json> [regex de nombres de mutante a correr]
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
SH = RES + 'sql/hana/'
G = MOD + '/src/test/resources/loginpg/golden-hana/'
F1 = MOD + '/src/test/resources/loginpg/'
F2 = MOD + '/src/test/resources/loginpg2/'
F3 = MOD + '/src/test/resources/loginpg3/'
J = MOD + '/src/main/java/cl/bcs/plataforma/login/dialecto/'
DEFS = J + 'DefinicionesProcedimientos.java'
PROC = J + 'ProcedimientosPostgres.java'
PGD = J + 'PostgresDialecto.java'
HND = J + 'HanaDialecto.java'
SQD = J + 'SqlDialecto.java'
SEN = J + 'SentenciasSql.java'

TEXTOS = 'LoginPgTraduccionTest,LoginPg2TraduccionVistasTest,LoginPg3OraculoSentenciasTest,LoginPgResultadosH2Test,LoginPg2VistasH2Test'
ORACULO_SOLO = 'LoginPg3OraculoSentenciasTest'
SENT = 'LoginPg3SentenciasTest,LoginPg3SecuenciasTest,LoginPg3SinCatalogoTest'
DIAL = 'LoginPgDialectoTest,LoginPg3SecuenciasTest,LoginPgResultadosH2Test'
DEF = 'LoginPg3DefinicionesTest,LoginPg2ProcedimientosTest,LoginPg2EventoArreglosTest,LoginPg2ServiciosTest'
DEF_SOLO = 'LoginPg3DefinicionesTest'
ORA = 'LoginPgOraculoTest,LoginPg2CatalogoVistasTest,LoginPg3OraculoSentenciasTest,LoginPg3SecuenciasTest,LoginPg3DefinicionesTest'
SPR = 'LoginPgSeleccionTest*,LoginPgSinTextoPostgresTest*,LoginPg2SeleccionProcedimientosTest*'
HANA = 'LoginPgSqlHanaTest'
STRUCT = 'LoginPg3SinCatalogoTest,LoginPgDialectoTest,LoginPg3SentenciasTest'


def leer(p):
    return open(p, encoding='utf-8', newline='').read()


def escribir(p, s):
    os.makedirs(os.path.dirname(p), exist_ok=True)
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


def nuevo_archivo(ruta, texto):
    """el archivo NO existe: el mutante lo crea y se borra despues"""
    return (ruta, lambda s: texto)


MUTANTES = []


def m(nombre, grupo, mut, tests):
    MUTANTES.append((nombre, grupo, mut, tests))


# ---------------------------------------------------------------- textos de Postgres (sql/postgres/<id>.sql)
m('P01 VALIDA_TOKEN: literal S -> N', 'texto', reemplazar(S + 'FiltroSesion.SQL_VALIDA_TOKEN.sql', "token_valido = 'S'", "token_valido = 'N'"), TEXTOS)
m('P02 UPDATE_INTENTOS: sets another (existing) column', 'texto', reemplazar(S + 'CrearSesionServicio.UPDATE_INTENTOS.sql', 'SET intentos_conexion', 'SET conectado'), TEXTOS)
m('P03 BLOQUEO_TEMPORAL: *60 -> *600', 'texto', reemplazar(S + 'CrearSesionServicio.UPDATE_BLOQUEO_TEMPORAL.sql', 'integer)*60)', 'integer)*600)'), TEXTOS)
m('P04 BUSCA_TOKEN: LIMIT 1 removed', 'texto', reemplazar(S + 'CrearSesionServicio.QUERY_BUSCA_TOKEN.sql', ' LIMIT 1', ''), TEXTOS)
m('P05 RELOJ_UTC: zone UTC -> CLT', 'texto', reemplazar(S + 'CrearSesionServicio.QUERY_RELOJ_UTC.sql', "'UTC')", "'CLT')"), TEXTOS)
m('P06 FERIADOS: date format changed', 'texto', reemplazar(S + 'BootstrapServicio.SQL_FERIADOS.sql', "'YYYY-MM-DD'", "'DD-MM-YYYY'"), TEXTOS)
m('P07 QUERY_USUARIOS: LEFT JOIN of c2 becomes JOIN', 'texto', reemplazar(S + 'CrearSesionServicio.QUERY_USUARIOS.sql', 'LEFT JOIN t_pla_pla_configuracion c2', 'JOIN t_pla_pla_configuracion c2'), TEXTOS)
m('P08 QUERY_USUARIOS: usuario_negocios read from usuario_clientes', 'texto', reemplazar(S + 'CrearSesionServicio.QUERY_USUARIOS.sql', 'usuario_negocios AS', 'usuario_clientes AS'), TEXTOS)
m('P09 ACCESOS: NULLS FIRST removed from padre', 'texto', reemplazar(S + 'BootstrapServicio.SQL_ACCESOS.sql', 'padre NULLS FIRST', 'padre'), TEXTOS)
m('P10 DICCIONARIO: reads another (existing) table', 'texto', reemplazar(S + 'BootstrapServicio.SQL_DICCIONARIO.sql', 'FROM t_pla_pla_diccionario', 'FROM t_pla_pla_log'), TEXTOS)
m('P11 EMPRESAS: reads another (existing) view', 'texto', reemplazar(S + 'BootstrapServicio.SQL_EMPRESAS.sql', 'v_pla_pla_consultar_empresa', 'v_pla_pla_consultar_imagen'), TEXTOS)
m('P12 DATOS_RECUPERACION: EMP arm compares id_cliente', 'texto', reemplazar(S + 'GestionClaveServicio.SQL_DATOS_RECUPERACION.sql', 'usu.id_entidad = cli.id_empleado', 'usu.id_entidad = cli.id_cliente'), TEXTOS)
m('P13 INSERT_LOG: last two values swapped', 'texto', reemplazar(S + 'AuditoriaSesion.INSERT_LOG.sql', ':IdPersona,:CodigoAutenticacion)', ':CodigoAutenticacion,:IdPersona)'), TEXTOS)
m('P14 VALIDA_TOKEN: a table that is not in the oracle (only the oracle check runs)', 'oraculo-dev', reemplazar(S + 'FiltroSesion.SQL_VALIDA_TOKEN.sql', 't_pla_acc_sesion', 't_pla_acc_sesiones'), ORACULO_SOLO)
m('P15 VALIDA_TOKEN: a column that is not in the oracle (only the oracle check runs)', 'oraculo-dev', reemplazar(S + 'FiltroSesion.SQL_VALIDA_TOKEN.sql', 'id_token_sesion', 'id_token_sesionx'), ORACULO_SOLO)
m('P16 ACCESOS: order by a column of ANOTHER view (only the oracle check runs)', 'oraculo-dev', reemplazar(S + 'BootstrapServicio.SQL_ACCESOS.sql', 'orden NULLS FIRST', 'razon_social NULLS FIRST'), ORACULO_SOLO)
m('P17 FERIADOS: a table of the oracle that is not the statement\'s (v_ view instead; only the oracle check runs)', 'oraculo-dev', reemplazar(S + 'BootstrapServicio.SQL_FERIADOS.sql', 'FROM t_pla_reg_dia_feriado', 'FROM t_pla_reg_dia_feriado, t_pla_pla_log'), ORACULO_SOLO)
m('P18 IMAGEN: a function-like name that is not in the oracle (only the oracle check runs)', 'oraculo-dev', reemplazar(S + 'BootstrapServicio.SQL_IMAGEN.sql', 'FROM v_pla_pla_consultar_imagen', 'FROM fn_pla_inventada'), ORACULO_SOLO)
# ---------------------------------------------------------------- archivos con un nombre (sql/postgres/<id>.sql de los SP_ y SECUENCIA_)
m('N01 SP_FECHAS file: a name that is not in the oracle', 'nombre', reemplazar(S + 'BootstrapServicio.SP_FECHAS.sql', 'sp_pla_obtener_fechas', 'sp_pla_obtener_fecha'), SENT)
m('N02 SECUENCIA_LOG file holds the other sequence', 'nombre', reemplazar(S + 'AuditoriaSesion.SECUENCIA_LOG.sql', 'sq_pla_log', 'sq_pla_detalle_log'), SENT)
m('N03 SP_FORMATOS file with a blank inside the name', 'nombre', reemplazar(S + 'BootstrapServicio.SP_FORMATOS.sql', 'fn_pla_consultar_formatos', 'fn_pla consultar_formatos'), SENT)
m('N04 SP_RECUPERAR file holds two lines', 'nombre', reemplazar(S + 'GestionClaveServicio.SP_RECUPERAR.sql', 'sp_pla_acc_recuperar_clave_usuario', 'sp_pla_acc_recuperar_clave_usuario\nsp_pla_extra'), SENT)
m('N05 sql/indice.txt loses an id', 'nombre', reemplazar(RES + 'sql/indice.txt', 'FiltroSesion.SQL_VALIDA_TOKEN\n', ''), SENT + ',LoginPgSqlHanaTest')
# ---------------------------------------------------------------- constantes de secuencias (Java)
m('S01 Log sequence constant: sq_pla_log -> sq_pla_log2', 'secuencia', reemplazar(PGD, "nextval('sq_pla_log')", "nextval('sq_pla_log2')"), DIAL)
m('S02 DetalleLog constant gives the Log sequence', 'secuencia', reemplazar(PGD, "nextval('sq_pla_detalle_log')", "nextval('sq_pla_log')"), DIAL)
m('S03 an unknown sequence is served instead of a 503', 'secuencia', regex(PGD, r'throw new UnsupportedOperationException\("Sequence[^;]*;', 'return "nextval(\'sq_otra\')";'), DIAL)
m('S04 HANA sequence without quotes', 'secuencia', reemplazar(HND, '"\\"" + secuenciaHana + "\\".NEXTVAL"', 'secuenciaHana + ".NEXTVAL"'), DIAL)
m('S05 Postgres no-FROM select keeps FROM DUMMY', 'secuencia', reemplazar(PGD, 'return "SELECT " + expresion;', 'return "SELECT " + expresion + " FROM DUMMY";'), DIAL)
m('S06 exigirHana inverted', 'secuencia', reemplazar(SQD, '!"hana".equals(nombre())', '"hana".equals(nombre())'), DIAL + ',LoginPgDespachadoresTest')
m('S07 Postgres dialect named postgresql', 'secuencia', reemplazar(PGD, 'return "postgres";', 'return "postgresql";'), DIAL)
# ---------------------------------------------------------------- oraculos y fixtures
m('O01 oracle objects: a sequence name changed', 'oraculo', reemplazar(F1 + 'oraculo-objetos.csv', 'sq_pla_log\n', 'sq_pla_logs\n') if False else reemplazar(F1 + 'oraculo-objetos.csv', ',sq_pla_log,', ',sq_pla_logs,'), ORA)
m('O02 oracle columns: a column renamed', 'oraculo', reemplazar(F1 + 'oraculo-columnas.csv', ',IdUsuario,id_usuario', ',IdUsuario,id_usuarioo'), ORA)
m('O03 scope file: one object removed', 'oraculo', borrar_linea(F1 + 'login-alcance.txt', 'sq_pla_detalle_log'), ORA)
m('O04 oracle view columns: a column renamed', 'oraculo', reemplazar(F2 + 'oraculo-vistas-columnas.csv', ',IdEstado,id_estado,', ',IdEstado,id_estado_x,'), ORA)
m('O05 routines evidence: a parameter type changed', 'oraculo', reemplazar(F2 + 'rutinas-y-tipos-postgres-qa-20261103.txt', 'OUT p_out_valor character varying', 'OUT p_out_valor integer'), ORA)
m('O06 routines evidence: a composite attribute renamed', 'oraculo', reemplazar(F2 + 'rutinas-y-tipos-postgres-qa-20261103.txt', 'TIPO ty_pla_evt_agregar_evento_valores | 2 llave character varying(40)', 'TIPO ty_pla_evt_agregar_evento_valores | 2 llaves character varying(40)'), ORA)
m('O07 view TSV: a column type changed', 'oraculo', reemplazar(F2 + 'vistas-columnas-pg-qa-20261103.tsv', 'v_pla_pla_consultar_empresa\tnombre\tcharacter varying', 'v_pla_pla_consultar_empresa\tnombre\tinteger'), ORA)
m('O08 hand-written names fixture: a name changed', 'fixture', reemplazar(F3 + 'nombres-postgres-esperados.tsv', 'sp_pla_evt_agregar_evento', 'sp_pla_evt_agregar_eventos'), SENT)
m('O09 H2 view fixture: the NULL-padre row removed', 'fixture', regex(F2 + 'vistas-h2.json', r'\[10, "JPEREZ", 8, "SinOrden".*?\],\n', ''), 'LoginPg2VistasH2Test')
m('O10 H2 view fixture: logo bytes changed', 'fixture', reemplazar(F2 + 'vistas-h2.json', 'hex:0102ff', 'hex:0102fe'), 'LoginPg2VistasH2Test')
# ---------------------------------------------------------------- red de seguridad de HANA
m('H01 golden QUERY_BUSCA_TOKEN: TOP 1 -> TOP 2', 'hana', reemplazar(G + 'CrearSesionServicio.QUERY_BUSCA_TOKEN.sql', 'TOP 1', 'TOP 2'), HANA)
m('H02 golden SP_FECHAS: another name', 'hana', reemplazar(G + 'BootstrapServicio.SP_FECHAS.sql', 'ObtenerFechas', 'ObtenerFecha'), HANA)
m('H03 production HANA text changed (sql/hana)', 'hana', reemplazar(SH + 'FiltroSesion.SQL_VALIDA_TOKEN.sql', "'S'", "'N'"), HANA)
m('H04 golden SECUENCIA_DETALLE: another sequence', 'hana', reemplazar(G + 'AuditoriaSesion.SECUENCIA_DETALLE.sql', 'DetalleLog', 'Log'), HANA)
# ---------------------------------------------------------------- definiciones de los procedimientos (Java)
m('D01 admin OUT at position 2 instead of 3', 'definicion', reemplazar(DEFS, 'new Parametro(3, "OUT", "P_OUT_ESADMIN"', 'new Parametro(2, "OUT", "P_OUT_ESADMIN"'), DEF)
m('D02 admin legacy name with an underscore', 'definicion', reemplazar(DEFS, '"P_OUT_ESADMIN"', '"P_OUT_ES_ADMIN"'), DEF_SOLO)
m('D03 a bigint IN typed integer (bound as text)', 'definicion', reemplazar(DEFS, 'new Parametro(2, "IN", "p_id_usuario", "p_id_usuario", "bigint")', 'new Parametro(2, "IN", "p_id_usuario", "p_id_usuario", "integer")'), DEF_SOLO)
m('D04 the function name of the formats', 'definicion', reemplazar(DEFS, '"fn_pla_consultar_formatos", true', '"fn_pla_consultar_formato", true'), DEF_SOLO)
m('D05 a label of the formats function', 'definicion', reemplazar(DEFS, 'Map.entry("precio_decimales", "PrecioDecimales")', 'Map.entry("precio_decimales", "PrecioDecimal")'), DEF_SOLO)
m('D06 value attribute keys swapped (Llave/Valor)', 'definicion', reemplazar(DEFS, '"character varying(40)", "Llave"),\n                                new Atributo(3, "valor", "character varying(5000)", "Valor")', '"character varying(40)", "Valor"),\n                                new Atributo(3, "valor", "character varying(5000)", "Llave")'), DEF_SOLO)
m('D07 the bytea attribute typed text (no hex)', 'definicion', reemplazar(DEFS, '"archivo_adjunto", "bytea"', '"archivo_adjunto", "text"'), DEF_SOLO)
m('D08 error cursor typed varchar (not a cursor)', 'definicion', reemplazar(DEFS, 'new Parametro(8, "INOUT", "P_OUT_ERRORESDETALLE", "p_out_errores_detalle", "refcursor")', 'new Parametro(8, "INOUT", "P_OUT_ERRORESDETALLE", "p_out_errores_detalle", "character varying")'), DEF_SOLO)
# D09 is an EQUIVALENT mutant of the throwaway implementation: it handles a cursor by its TYPE (refcursor: bound to NULL and registered OTHER) and the mode label
# only separates IN from the rest, so INOUT and OUT of a cursor give the same JDBC calls and the same answer: the mode is not observable at the JDBC seam.
m('D09 warnings cursor mode OUT instead of INOUT (equivalent)', 'definicion', reemplazar(DEFS, 'new Parametro(6, "INOUT", "P_OUT_ADVERTENCIASDETALLE"', 'new Parametro(6, "OUT", "P_OUT_ADVERTENCIASDETALLE"'), DEF_SOLO)
m('D10 origin of the event parameter renamed (default NULL instead of empty)', 'definicion', reemplazar(DEFS, '"p_aplica_origen_evento", "p_aplica_origen_evento", "character varying"', '"p_aplica_origen_evento", "p_aplica_origen", "character varying"'), DEF)
m('D11 key of the formats answer', 'definicion', reemplazar(DEFS, '"P_OUT_MONEDAFORMATO"));', '"P_OUT_MONEDAFORMATOS"));'), DEF_SOLO)
m('D12 a ninth procedure served', 'definicion', regex(DEFS, r'(TODAS\.put\("BSCL\.Plataforma\.BD\.Servicios::ACC\.ConsultarUsuarioAdmin")', 'TODAS.put("BSCL.Plataforma.BD.Servicios::AgregarLog", new DefinicionProcedimiento("BSCL.Plataforma.BD.Servicios::AgregarLog", "sp_pla_agregar_log", false, List.of(), Map.of(), Map.of(), null));\n        \\1'), DEF)
m('D13 an attribute of the events type dropped', 'definicion', regex(DEFS, r'new Atributo\(6, "id_portafolio", "bigint", "IdPortafolio"\)\),', 'new Atributo(5, "id_persona_destinatario", "bigint", "IdPersonaDestinatario")),'), DEF_SOLO)
m('D14 status code legacy name with an underscore', 'definicion', reemplazar(DEFS, '"P_STATUSCODE"', '"P_STATUS_CODE"'), DEF_SOLO)
m('D15 the Postgres name of the event procedure', 'definicion', reemplazar(DEFS, '"sp_pla_evt_agregar_evento", false', '"sp_pla_evt_agregar_evento_x", false'), DEF_SOLO)
m('D16 the timestamp attribute type', 'definicion', reemplazar(DEFS, '"fecha_creacion_sistema", "timestamp(0) without time zone"', '"fecha_creacion_sistema", "bytea"'), DEF_SOLO)
# ---------------------------------------------------------------- codigo de SentenciasSql y de los procedimientos
m('C01 a missing Postgres file returns an empty text', 'codigo', regex(SEN, r'if \(texto == null\) \{\s*throw new UnsupportedOperationException[^;]*;\s*\}', 'if (texto == null) { texto = ""; }'), SENT)
m('C02 the 503 message loses the id', 'codigo', reemplazar(SEN, '"Statement " + id + " has no "', '"Statement has no "'), SENT)
m('C03 Postgres reads the HANA file', 'codigo', reemplazar(SEN, '"/sql/" + dialecto.nombre() + "/"', '"/sql/hana/"'), SENT + ',LoginPgTraduccionTest')
m('C04 exigirDisponibles does nothing', 'codigo', reemplazar(SEN, '            texto(id);\n        }\n    }', '        }\n    }'), SENT)
m('C05 exigirDisponibles checks only the first id', 'codigo', reemplazar(SEN, '            texto(id);\n        }\n    }', '            texto(id);\n            break;\n        }\n    }'), SENT)
m('C06 an unknown id is a 503 (the index is not checked first)', 'codigo', reemplazar(SEN, '        if (!ids.contains(id)) {\n            throw new IllegalArgumentException("unknown statement " + id);\n        }\n', ''), SENT)
m('C07 de() answers the HANA text', 'codigo', reemplazar(SEN, '        return texto(id);\n    }\n\n    private static String abreviar', '        return hana(id);\n    }\n\n    private static String abreviar'), SENT)
m('C08 texto made final (a double cannot override it)', 'codigo', reemplazar(SEN, 'public String texto(String id)', 'public final String texto(String id)'), SENT)
m('C09 predeterminadas() is Postgres', 'codigo', reemplazar(SEN, 'new SentenciasSql(new HanaDialecto())', 'new SentenciasSql(new PostgresDialecto())'), HANA)
m('C10 dialecto() returns a new dialect', 'codigo', reemplazar(SEN, '        return dialecto;\n    }\n\n    /** Every', '        return new HanaDialecto();\n    }\n\n    /** Every'), SENT)
m('C11 an unknown procedure answers an empty JSON', 'codigo', regex(PROC, r'if \(definicion == null\) \{\s*throw new UnsupportedOperationException[^;]*;', 'if (definicion == null) { return new JsonObject();'), DEF)
m('C12 commit after execute', 'codigo', reemplazar(PROC, 'llamada.execute();', 'llamada.execute();\n            con.commit();'), DEF)
m('C13 selector inverted', 'codigo', reemplazar(J + 'ProcedimientosPorDialecto.java', '"postgres".equals(dialecto.nombre())', '"hana".equals(dialecto.nombre())'), SPR)
m('C14 selector bean not primary', 'codigo', reemplazar(J + 'ProcedimientosConfiguracion.java', '    @Primary\n', ''), SPR)
m('C15 an invalid dialect value falls back to HANA', 'codigo', reemplazar(J + 'DialectoConfiguracion.java', "default -> throw new IllegalStateException(PROPIEDAD + \" must be 'hana' or 'postgres', got '\" + valor + \"'\");", 'default -> new HanaDialecto();'), SPR)
# ---------------------------------------------------------------- lo que debe estar AUSENTE (se agrega y la estructura debe caer)
m('X01 the catalog class is back', 'ausencia', nuevo_archivo(J + 'CatalogoEquivalencias.java', 'package cl.bcs.plataforma.login.dialecto;\n\npublic class CatalogoEquivalencias {\n}\n'), STRUCT)
m('X02 an equivalencias CSV resource is back', 'ausencia', nuevo_archivo(RES + 'equivalencias/equivalencias-objetos.csv', 'tipo_objeto,nombre_hana,nombre_postgres\n'), STRUCT)
m('X03 a loose CSV anywhere in resources', 'ausencia', nuevo_archivo(RES + 'otra/cosa.csv', 'a,b\n'), STRUCT)
m('X04 a sql/comun folder', 'ausencia', nuevo_archivo(RES + 'sql/comun/x.sql', 'select 1\n'), STRUCT)
m('X05 a source reads a csv by a string literal', 'ausencia', nuevo_archivo(J + 'Lector.java', 'package cl.bcs.plataforma.login.dialecto;\n\nclass Lector {\n    static final String RUTA = "/datos/tabla.csv";\n}\n'), STRUCT)
m('X06 SqlDialecto gets a function back', 'ausencia', reemplazar(SQD, '    /** A select of an expression with no table. */', '    default String ahoraUtc() {\n        return "now()";\n    }\n\n    /** A select of an expression with no table. */'), STRUCT)
m('X07 PostgresDialecto gets a public helper back', 'ausencia', reemplazar(PGD, '    @Override\n    public String nombre()', '    public String recortar(String x) {\n        return x;\n    }\n\n    @Override\n    public String nombre()'), STRUCT)
m('X08 HanaDialecto gets a constructor with a parameter', 'ausencia', reemplazar(HND, '    public HanaDialecto() {\n    }', '    public HanaDialecto() {\n    }\n\n    public HanaDialecto(Object catalogo) {\n    }'), STRUCT)
m('X09 SentenciasSql gets objetosDe back', 'ausencia', reemplazar(SEN, '    /** Every statement id. */', '    public java.util.Collection<String> objetosDe(String id) {\n        return java.util.List.of();\n    }\n\n    /** Every statement id. */'), SENT)
m('X10 DialectoConfiguracion declares a third bean', 'ausencia', reemplazar(J + 'DialectoConfiguracion.java', '    @Bean\n    SentenciasSql sentenciasSql', '    @Bean\n    Object catalogoDeMentira() {\n        return new Object();\n    }\n\n    @Bean\n    SentenciasSql sentenciasSql'), 'LoginPgSeleccionTest*')
m('X11 ProcedimientosPostgres gets a second public constructor with a parameter', 'ausencia', reemplazar(PROC, '    public ProcedimientosPostgres() {\n    }', '    public ProcedimientosPostgres() {\n    }\n\n    public ProcedimientosPostgres(Object catalogo) {\n    }'), STRUCT)


def limpiar_agregados():
    """Maven never deletes from target/classes what no longer exists in src/main: a file ADDED by a mutant (a class, a CSV) would stay and poison the next runs.
    Remove from src/main every empty directory a mutant created, and from target/classes every resource and class that has no source any more."""
    java = MOD + '/src/main/java/'
    res = RES
    for raiz in (MOD + '/src/main/java', MOD + '/src/main/resources'):
        for d, _, _ in sorted(os.walk(raiz), key=lambda x: -len(x[0])):
            if d != raiz and not os.listdir(d):
                os.rmdir(d)
    clases = MOD + '/target/classes'
    if not os.path.isdir(clases):
        return
    for d, _, archivos in os.walk(clases):
        for a in archivos:
            ruta = os.path.join(d, a)
            rel = os.path.relpath(ruta, clases).replace('\\', '/')
            if a.endswith('.class'):
                fuente = java + rel.split('$')[0].replace('.class', '') + '.java'
                if not os.path.exists(fuente):
                    os.remove(ruta)
            elif not os.path.exists(res + rel):
                os.remove(ruta)
    for d, _, _ in sorted(os.walk(clases), key=lambda x: -len(x[0])):
        if d != clases and not os.listdir(d):
            os.rmdir(d)


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
    return {'ejecutadas': a, 'fallas': b, 'errores': c, 'omitidas': d, 'compila': compila, 'rc': r.returncode}


def main():
    limpiar_agregados()
    bases = {}
    filtro = sys.argv[3] if len(sys.argv) > 3 else None
    for t in sorted(set(x[3] for x in MUTANTES if not filtro or re.search(filtro, x[0]))):
        bases[t] = correr(t)
        print('BASELINE', t, bases[t], flush=True)
        if bases[t]['fallas'] or bases[t]['errores'] or bases[t]['ejecutadas'] == 0:
            raise SystemExit('el baseline no esta en verde: no se puede mutar: ' + t)
    resultados = []
    for nombre, grupo, (ruta, f), tests in MUTANTES:
        if filtro and not re.search(filtro, nombre):
            continue
        existia = os.path.exists(ruta)
        original = leer(ruta) if existia else ''
        try:
            nuevo = f(original)
            if existia and nuevo == original:
                raise SystemExit('el mutante no cambia nada: ' + nombre)
            escribir(ruta, nuevo)
            r = correr(tests)
        finally:
            if existia:
                escribir(ruta, original)
            elif os.path.exists(ruta):
                os.remove(ruta)
            limpiar_agregados()
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
