package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.ArregloGrabado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Respuesta;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Sentencia;
import cl.bcs.plataforma.login.dialecto.LoginPg2Recursos.Parametro;
import cl.bcs.plataforma.login.dialecto.LoginPg2Recursos.Rutina;

/**
 * LOGIN-PG round 2, part B (T-33): the 8 stored procedures of the login in Postgres, pinned at the JDBC seam with a recording fake (JdbcGrabador): H2 has no
 * such procedures, no refcursor OUT and no composite arrays, and no connection to any real database is made. Pinned here: the signatures (oracle from the QA
 * catalog), the resource that maps each HANA procedure, the call text, the order and JDBC type of every parameter, the registered OUT parameters, the refcursor
 * handling, the key mapping of the answer (snake_case OUT names back to the LEGACY names the login reads), the 503 of everything else, and error propagation.
 * Every assumption about pgjdbc behaviour is a Q-LOGINPG2-n in API_ASSUMED_LOGINPG2.md, to be settled by a read-only execution in T-36.
 */
class LoginPg2ProcedimientosTest {

    static final LoginPg2Recursos.Rutinas RUT = LoginPg2Recursos.Rutinas.cargar();
    private static final LoginPgRecursos.Oraculo XLSX = LoginPgRecursos.Oraculo.cargar();

    static ProcedimientoAlmacenado pg() {
        return ApiLoginPg3.procedimientosPostgres();
    }

    static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    /** layout normalised: whitespace runs are one space, none next to ( ) , { }: the call text is pinned without layout */
    static String norm(String sql) {
        return sql.strip().replaceAll("\\s+", " ").replaceAll(" ?([(),{}]) ?", "$1");
    }

    static int tipoJdbc(String tipoPg) {
        return switch (tipoPg) {
            case "character varying" -> Types.VARCHAR;
            case "integer" -> Types.INTEGER;
            case "refcursor" -> Types.OTHER;
            default -> throw new AssertionError("no JDBC type pinned for " + tipoPg);
        };
    }

    /** the scripted OUT values of a Postgres routine by parameter NAME, as the fake wants them by index */
    static Map<Integer, Object> porIndice(Rutina r, Map<String, Object> porNombre) {
        Map<Integer, Object> m = new HashMap<>();
        porNombre.forEach((n, v) -> m.put(r.indice(n), v));
        return m;
    }

    // ================================================================================================ the case table

    record Caso(String id, String hana, String pgNombre, String llamada, String entrada, Map<String, Object> binds, Map<String, Object> salidas, String esperado) {
        @Override
        public String toString() {
            return id;
        }
    }

    static final String EVENTO_ENTRADA = "{\"P_IDEMPRESA\":1,\"P_IDUSUARIO\":7,\"P_IDMODULO\":\"PLA\","
            + "\"P_EVENTOS\":[{\"CodigoTipoEvento\":\"SOLICITUD_CAMBIO_CLAVE\",\"IdSecuencia\":0,\"FechaCreacionSistema\":\"2026-11-03\",\"IdPersonaRemitente\":0,"
            + "\"IdPersonaDestinatario\":777,\"IdPortafolio\":0}],"
            + "\"P_VALORES\":[{\"IdSecuencia\":0,\"Llave\":\"Nombre\",\"Valor\":\"Juan Perez\"},{\"IdSecuencia\":0,\"Llave\":\"ClaveProvisoria\",\"Valor\":\"Xk3!q\"}],"
            + "\"P_ARCHIVOSADJUNTOS\":[],"
            + "\"P_IDDIRECCIONES\":[{\"IdSecuencia\":0,\"TipoDestinatario\":\"TO\",\"IdDireccion\":5551}],"
            + "\"P_APLICAORIGENEVENTO\":\"\"}";

    static Map<String, Object> mapa(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) m.put((String) pares[i], pares[i + 1]);
        return m;
    }

    static List<Caso> casos() {
        List<Caso> c = new ArrayList<>();
        c.add(new Caso("SP_USUARIO_ADMIN", LoginPg2Recursos.ADMIN, "sp_pla_acc_consultar_usuario_admin", "{call sp_pla_acc_consultar_usuario_admin(?,?,?)}",
                "{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}", mapa("p_id_empresa", 1L, "p_id_usuario", 7L), mapa("p_out_es_admin", "S"), "{\"P_OUT_ESADMIN\":\"S\"}"));
        c.add(new Caso("SP_OBTENER_CONFIGURACION", LoginPg2Recursos.CONFIG, "sp_pla_obtener_configuracion", "{call sp_pla_obtener_configuracion(?,?,?)}",
                "{\"p_IdEmpresa\":1,\"p_CodigoParametro\":\"PLA.ConflictoAccesoDefecto\"}", mapa("p_id_empresa", 1L, "p_codigo_parametro", "PLA.ConflictoAccesoDefecto"),
                mapa("p_out_valor", "N"), "{\"P_OUT_VALOR\":\"N\"}"));
        c.add(new Caso("SP_FECHAS", LoginPg2Recursos.FECHAS, "sp_pla_obtener_fechas", "{call sp_pla_obtener_fechas(?,?,?,?,?,?,?)}", "{\"p_IdEmpresa\":1}",
                mapa("p_id_empresa", 1L),
                mapa("p_out_fecha_sistema_utc", "2026-11-03", "p_out_fecha_sistema_local", "2026-11-02", "p_out_fecha_hora_sistema_utc", "2026-11-03 12:00:00",
                        "p_out_fecha_hora_sistema_local", "2026-11-02 09:00:00", "p_out_fecha_hora_utc", "2026-11-03 12:00:01", "p_out_fecha_hora_local", "2026-11-02 09:00:01"),
                "{\"P_OUT_FECHASISTEMAUTC\":\"2026-11-03\",\"P_OUT_FECHASISTEMALOCAL\":\"2026-11-02\",\"P_OUT_FECHAHORASISTEMAUTC\":\"2026-11-03 12:00:00\","
                        + "\"P_OUT_FECHAHORASISTEMALOCAL\":\"2026-11-02 09:00:00\",\"P_OUT_FECHAHORAUTC\":\"2026-11-03 12:00:01\",\"P_OUT_FECHAHORALOCAL\":\"2026-11-02 09:00:01\"}"));
        c.add(new Caso("SP_FECHA_HORA", LoginPg2Recursos.FECHA_HORA, "sp_pla_obtener_fecha_hora_sistema", "{call sp_pla_obtener_fecha_hora_sistema(?,?)}", "{\"p_IdEmpresa\":1}",
                mapa("p_id_empresa", 1L), mapa("p_out_fecha_hora_sistema", "2026-11-03 09:30:00"), "{\"P_OUT_FECHAHORASISTEMA\":\"2026-11-03 09:30:00\"}"));
        c.add(new Caso("SP_RECUPERAR", LoginPg2Recursos.RECUPERAR, "sp_pla_acc_recuperar_clave_usuario", "{call sp_pla_acc_recuperar_clave_usuario(?,?,?,?,?,?,?,?)}",
                "{\"p_IdEmpresa\":1,\"p_Username\":\"JPEREZ\",\"p_Token\":\"A1B2C\"}", mapa("p_id_empresa", 1L, "p_username", "JPEREZ", "p_token", "A1B2C"),
                mapa("p_out_mail", "jperez@example.invalid", "p_out_advertencias_contador", 0, "p_out_errores_contador", 0),
                "{\"P_OUT_MAIL\":\"jperez@example.invalid\",\"P_OUT_ADVERTENCIASCONTADOR\":\"0\",\"P_OUT_ADVERTENCIASDETALLE\":[],\"P_OUT_ERRORESCONTADOR\":\"0\",\"P_OUT_ERRORESDETALLE\":[]}"));
        c.add(new Caso("SP_CONFIRMAR", LoginPg2Recursos.CONFIRMAR, "sp_pla_acc_confirmar_cambio_clave_usuario", "{call sp_pla_acc_confirmar_cambio_clave_usuario(?,?,?,?,?,?,?,?,?)}",
                "{\"p_IdEmpresa\":1,\"p_Username\":\"JPEREZ\",\"p_Password\":\"Nueva-Clave-1\",\"p_Token\":\"A1B2C\"}",
                mapa("p_id_empresa", 1L, "p_username", "JPEREZ", "p_password", "Nueva-Clave-1", "p_token", "A1B2C"),
                mapa("p_status_code", 0, "p_out_advertencias_contador", 0, "p_out_errores_contador", 0),
                "{\"P_STATUSCODE\":\"0\",\"P_OUT_ADVERTENCIASCONTADOR\":\"0\",\"P_OUT_ADVERTENCIASDETALLE\":[],\"P_OUT_ERRORESCONTADOR\":\"0\",\"P_OUT_ERRORESDETALLE\":[]}"));
        c.add(new Caso("SP_AGREGAR_EVENTO", LoginPg2Recursos.EVENTO, "sp_pla_evt_agregar_evento", "{call sp_pla_evt_agregar_evento(?,?,?,?,?,?,?,?,?)}", EVENTO_ENTRADA,
                mapa("p_id_empresa", 1L, "p_id_usuario", 7L, "p_id_modulo", "PLA",
                        "p_eventos", new ArregloGrabado("ty_pla_evt_agregar_evento_eventos", List.of("(SOLICITUD_CAMBIO_CLAVE,0,2026-11-03,0,777,0)")),
                        "p_valores", new ArregloGrabado("ty_pla_evt_agregar_evento_valores", List.of("(0,Nombre,\"Juan Perez\")", "(0,ClaveProvisoria,Xk3!q)")),
                        "p_archivos_adjuntos", new ArregloGrabado("ty_pla_evt_agregar_evento_archivos_adjuntos", List.of()),
                        "p_id_direcciones", new ArregloGrabado("ty_pla_evt_agregar_evento_id_direcciones", List.of("(0,5551,TO)")), "p_aplica_origen_evento", ""),
                mapa("p_out_id_evento", List.of(JdbcGrabador.fila("id_evento", 99))), "{\"P_OUT_IDEVENTO\":[{\"id_evento\":\"99\"}]}"));
        return c;
    }

    static Stream<Caso> casosStream() {
        return casos().stream();
    }

    // ================================================================================================ A. oracle self-checks (green)

    @Test
    @DisplayName("LOGIN-PG2 signatures oracle: the 8 routines of the login (7 procedures and the function fn_pla_consultar_formatos) with 3, 0, 3, 7, 8, 9, 9 and 2 parameters; 4 composite types of the event with 6, 3, 4 and 3 attributes")
    void oraculo_de_firmas() {
        assertEquals(8, RUT.rutinas.size());
        assertEquals(7, RUT.rutinas.values().stream().filter(r -> !r.esFuncion()).count());
        assertEquals(1, RUT.rutinas.values().stream().filter(Rutina::esFuncion).count());
        Map<String, Integer> n = new TreeMap<>();
        for (Caso c : casos()) n.put(c.pgNombre(), RUT.rutina(c.pgNombre()).parametros().size());
        n.put("fn_pla_consultar_formatos", RUT.rutina("fn_pla_consultar_formatos").parametros().size());
        assertEquals(new TreeMap<>(Map.of("sp_pla_acc_consultar_usuario_admin", 3, "sp_pla_obtener_configuracion", 3, "sp_pla_obtener_fechas", 7, "sp_pla_obtener_fecha_hora_sistema", 2,
                "sp_pla_acc_recuperar_clave_usuario", 8, "sp_pla_acc_confirmar_cambio_clave_usuario", 9, "sp_pla_evt_agregar_evento", 9, "fn_pla_consultar_formatos", 0)), n);
        assertEquals(18, RUT.rutina("fn_pla_consultar_formatos").columnasRetorno().size());
        assertEquals(6, RUT.tipos.get("ty_pla_evt_agregar_evento_eventos").size());
        assertEquals(3, RUT.tipos.get("ty_pla_evt_agregar_evento_valores").size());
        assertEquals(4, RUT.tipos.get("ty_pla_evt_agregar_evento_archivos_adjuntos").size());
        assertEquals(3, RUT.tipos.get("ty_pla_evt_agregar_evento_id_direcciones").size());
    }

    @Test
    @DisplayName("LOGIN-PG2 signatures oracle: the Postgres name of each of the 8 HANA procedures is the one of the xlsx; three INOUT refcursor parameters in the event and the two recovery procedures carry no value in")
    void nombres_y_modos() {
        for (Caso c : casos()) assertEquals(c.pgNombre(), XLSX.objeto(c.hana()).postgres(), c.id());
        assertEquals("fn_pla_consultar_formatos", XLSX.objeto(LoginPg2Recursos.FORMATOS).postgres());
        for (String r : List.of("sp_pla_acc_recuperar_clave_usuario", "sp_pla_acc_confirmar_cambio_clave_usuario", "sp_pla_evt_agregar_evento")) {
            for (Parametro p : RUT.rutina(r).parametros()) {
                assertEquals(p.tipo().equals("refcursor"), p.modo().equals("INOUT"), r + "." + p.nombre());
            }
        }
        assertTrue(RUT.rutina("sp_pla_evt_agregar_evento").parametro("p_aplica_origen_evento").conDefault());
        assertTrue(RUT.rutina("sp_pla_evt_agregar_evento").parametro("p_out_id_evento").conDefault());
    }

    @Test
    @DisplayName("LOGIN-PG2 legacy keys: every key the login sends to or reads from a procedure appears in the Java source of BootstrapServicio / GestionClaveServicio, and the OUT ones are the rule's image of a Postgres OUT name")
    void claves_legadas_que_el_login_usa() throws Exception {
        String bootstrap = Files.readString(Path.of("src/main/java/cl/bcs/plataforma/login/bootstrap/BootstrapServicio.java"), StandardCharsets.UTF_8);
        String clave = Files.readString(Path.of("src/main/java/cl/bcs/plataforma/login/clave/GestionClaveServicio.java"), StandardCharsets.UTF_8);
        for (String k : List.of("P_OUT_ESADMIN", "P_OUT_VALOR", "P_OUT_MONEDAFORMATO", "P_OUT_FECHAHORASISTEMALOCAL", "p_IdEmpresa", "p_IdUsuario", "p_CodigoParametro")) {
            assertThat(bootstrap).as(k).contains("\"" + k + "\"");
        }
        for (String k : List.of("P_OUT_ERRORESCONTADOR", "P_OUT_MAIL", "P_OUT_FECHAHORASISTEMA", "p_Username", "p_Token", "p_Password", "P_IDEMPRESA", "P_IDUSUARIO", "P_IDMODULO",
                "P_EVENTOS", "P_VALORES", "P_ARCHIVOSADJUNTOS", "P_IDDIRECCIONES", "P_APLICAORIGENEVENTO")) {
            assertThat(clave).as(k).contains("\"" + k + "\"");
        }
        Map<String, String> salidas = Map.of("p_out_es_admin", "P_OUT_ESADMIN", "p_out_valor", "P_OUT_VALOR", "p_out_fecha_hora_sistema_local", "P_OUT_FECHAHORASISTEMALOCAL",
                "p_out_errores_contador", "P_OUT_ERRORESCONTADOR", "p_out_mail", "P_OUT_MAIL", "p_out_fecha_hora_sistema", "P_OUT_FECHAHORASISTEMA",
                "p_out_errores_detalle", "P_OUT_ERRORESDETALLE", "p_out_advertencias_detalle", "P_OUT_ADVERTENCIASDETALLE");
        salidas.forEach((pg, legado) -> assertEquals(legado, LoginPg2Recursos.nombreLegadoDeSalida(pg)));
        // every IN key the login sends is, ignoring case and underscores, an IN parameter of its procedure
        Map<String, List<String>> envia = Map.of("sp_pla_acc_consultar_usuario_admin", List.of("p_IdEmpresa", "p_IdUsuario"), "sp_pla_obtener_configuracion", List.of("p_IdEmpresa", "p_CodigoParametro"),
                "sp_pla_obtener_fechas", List.of("p_IdEmpresa"), "sp_pla_obtener_fecha_hora_sistema", List.of("p_IdEmpresa"), "sp_pla_acc_recuperar_clave_usuario",
                List.of("p_IdEmpresa", "p_Username", "p_Token"), "sp_pla_acc_confirmar_cambio_clave_usuario", List.of("p_IdEmpresa", "p_Username", "p_Password", "p_Token"),
                "sp_pla_evt_agregar_evento", List.of("P_IDEMPRESA", "P_IDUSUARIO", "P_IDMODULO", "P_EVENTOS", "P_VALORES", "P_ARCHIVOSADJUNTOS", "P_IDDIRECCIONES", "P_APLICAORIGENEVENTO"));
        envia.forEach((rutina, claves) -> {
            Set<String> ins = new TreeSet<>();
            RUT.rutina(rutina).parametros().stream().filter(Parametro::esEntrada).filter(p -> p.modo().equals("IN")).forEach(p -> ins.add(LoginPg2Recursos.normalizar(p.nombre())));
            Set<String> enviadas = new TreeSet<>();
            claves.forEach(k -> enviadas.add(LoginPg2Recursos.normalizar(k)));
            assertEquals(ins, enviadas, rutina);
        });
    }

    // ================================================================================================ C. the JDBC contract (red)

    @ParameterizedTest(name = "LOGIN-PG2 {0}: prepareCall with the call text, every IN bound at its position with its type, INOUT refcursors bound to NULL, OUT and INOUT registered with their JDBC type, one execute")
    @MethodSource("casosStream")
    void contrato_jdbc_del_procedimiento(Caso c) throws Exception {
        Rutina r = RUT.rutina(c.pgNombre());
        JdbcGrabador g = new JdbcGrabador().guion(s -> Respuesta.salidas(porIndice(r, c.salidas())));
        JsonObject entrada = json(c.entrada());
        JsonObject copia = entrada.deepCopy();
        JsonObject out = pg().llamar(g.conexion(), c.hana(), entrada);
        Sentencia s = g.unica();
        assertEquals("prepareCall", s.via);
        assertEquals(c.llamada(), norm(s.sql), "call text");
        Set<Integer> enlazados = new TreeSet<>();
        Map<Integer, Integer> registradas = new TreeMap<>();
        for (Parametro p : r.parametros()) {
            if (p.modo().equals("IN")) {
                assertThat(c.binds()).as("the case binds " + p.nombre()).containsKey(p.nombre());
                assertTrue(JdbcGrabador.iguales(c.binds().get(p.nombre()), s.valor(p.posicion())), p.nombre() + ": expected " + c.binds().get(p.nombre()) + " got " + s.valor(p.posicion()));
                enlazados.add(p.posicion());
            } else if (p.modo().equals("INOUT")) {
                JdbcGrabador.Enlace e = s.enlace(p.posicion());
                assertNull(e.valor(), p.nombre() + " (INOUT refcursor) is bound to NULL");
                assertEquals(Types.OTHER, e.tipoSql(), p.nombre() + " NULL is typed OTHER");
                enlazados.add(p.posicion());
            }
            if (p.esSalida()) registradas.put(p.posicion(), tipoJdbc(p.tipo()));
        }
        assertEquals(enlazados, s.enlaces.keySet(), "parameters bound (no OUT parameter is bound as a value, no extra)");
        assertEquals(registradas, s.salidas, "registerOutParameter: position -> JDBC type");
        assertTrue(s.ejecutada);
        assertTrue(s.cerrada, "the statement is closed");
        s.conjuntos.forEach(rs -> assertTrue(rs.cerrado, "every cursor result set is closed"));
        assertThat(s.lecturas).as("OUT values are read by index").isNotEmpty();
        assertEquals(json(c.esperado()), out);
        assertEquals(copia, entrada, "the input JSON is not modified");
        assertThat(g.otrasLlamadas).as("no commit, rollback, setAutoCommit or close on the caller's connection").isEmpty();
    }

    @ParameterizedTest(name = "LOGIN-PG2 {0}: the answer carries ONLY the OUT values under the legacy names (no IN echoed, no snake_case key left)")
    @MethodSource("casosStream")
    void la_respuesta_solo_lleva_salidas_con_nombres_legados(Caso c) throws Exception {
        Rutina r = RUT.rutina(c.pgNombre());
        JdbcGrabador g = new JdbcGrabador().guion(s -> Respuesta.salidas(porIndice(r, c.salidas())));
        JsonObject out = pg().llamar(g.conexion(), c.hana(), json(c.entrada()));
        Set<String> esperadas = new TreeSet<>();
        r.parametros().stream().filter(Parametro::esSalida).forEach(p -> esperadas.add(LoginPg2Recursos.nombreLegadoDeSalida(p.nombre())));
        assertEquals(esperadas, new TreeSet<>(out.keySet()));
    }

    @Test
    @DisplayName("LOGIN-PG2 the call text of the 7 procedures is the JDBC escape form {call name(?,...)} with one ? per parameter, OUT ones included, in the order of the Postgres signature")
    void forma_de_la_llamada() throws Exception {
        for (Caso c : casos()) {
            Rutina r = RUT.rutina(c.pgNombre());
            assertEquals("{call" + " " + c.pgNombre() + "(" + String.join(",", java.util.Collections.nCopies(r.parametros().size(), "?")) + ")}", c.llamada().replace("{call ", "{call "));
        }
    }

    // ------------------------------------------------------------------------------------------------ the function (SELECT * FROM)

    private static final String[] COLUMNAS_FORMATOS = { "id_uso_moneda_formato", "id_moneda", "id_tipo_instrumento", "id_tipo_operacion", "precio_decimales", "precio_truca_redondea",
            "tasa_decimales", "tasa_truca_redondea", "cantidad_decimales", "cantidad_truca_redondea", "monto_decimales", "monto_truca_redondea", "tipo_cambio_decimales",
            "tipo_cambio_truca_redondea", "paridad_decimales", "paridad_truca_redondea", "puntos_decimales", "puntos_truca_redondea" };

    private static Map<String, Object> filaFormato(String uso, String moneda, Object decimales, String trunca) {
        Object[] pares = new Object[COLUMNAS_FORMATOS.length * 2];
        for (int i = 0; i < COLUMNAS_FORMATOS.length; i++) {
            String c = COLUMNAS_FORMATOS[i];
            Object v = switch (c) {
                case "id_uso_moneda_formato" -> uso;
                case "id_moneda" -> moneda;
                case "id_tipo_instrumento" -> "RF";
                case "id_tipo_operacion" -> "CPR";
                default -> c.endsWith("_decimales") ? decimales : trunca;
            };
            pares[2 * i] = c;
            pares[2 * i + 1] = v;
        }
        return JdbcGrabador.fila(pares);
    }

    @Test
    @DisplayName("LOGIN-PG2 ConsultarFormatos: SELECT * FROM fn_pla_consultar_formatos() through prepareStatement (no CALL, no OUT parameter, nothing bound); the rows come back under P_OUT_MONEDAFORMATO with the HANA labels and text values")
    void formatos_es_una_funcion_que_devuelve_filas() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.filas(List.of(filaFormato("OPE", "CLP", 0, "T"), filaFormato("OPE", "USD", 2, "R"))));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.FORMATOS, new JsonObject());
        Sentencia s = g.unica();
        assertEquals("prepareStatement", s.via);
        assertEquals("select * from fn_pla_consultar_formatos()", norm(s.sql).toLowerCase());
        assertThat(s.enlaces).isEmpty();
        assertThat(s.salidas).isEmpty();
        assertTrue(s.ejecutada);
        assertTrue(s.cerrada);
        s.conjuntos.forEach(rs -> assertTrue(rs.cerrado));
        assertEquals(Set.of("P_OUT_MONEDAFORMATO"), out.keySet());
        JsonArray filas = out.getAsJsonArray("P_OUT_MONEDAFORMATO");
        assertEquals(2, filas.size());
        JsonObject f0 = filas.get(0).getAsJsonObject();
        assertEquals(18, f0.size());
        assertEquals("OPE", f0.get("IdUsoMonedaFormato").getAsString());
        assertEquals("CLP", f0.get("IdMoneda").getAsString());
        assertEquals("RF", f0.get("IdTipoInstrumento").getAsString());
        assertEquals("CPR", f0.get("IdTipoOperacion").getAsString());
        assertEquals("0", f0.get("CantidadDecimales").getAsString());
        assertEquals("T", f0.get("PuntosTrucaRedondea").getAsString());
        assertEquals("2", filas.get(1).getAsJsonObject().get("TipoCambioDecimales").getAsString());
        assertEquals("R", filas.get(1).getAsJsonObject().get("ParidadTrucaRedondea").getAsString());
        assertThat(f0.get("CantidadDecimales").getAsJsonPrimitive().isString()).as("values are text, as every HANA result set value was (getString)").isTrue();
        for (String esperada : List.of("PrecioDecimales", "PrecioTrucaRedondea", "TasaDecimales", "TasaTrucaRedondea", "MontoDecimales", "MontoTrucaRedondea", "ParidadDecimales", "PuntosDecimales")) {
            assertTrue(f0.has(esperada), esperada);
        }
        assertThat(g.otrasLlamadas).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 ConsultarFormatos: no rows is an empty array (not a missing key); a NULL value is JSON null; the input JSON is ignored")
    void formatos_vacio_y_nulos() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.filas(List.of()));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.FORMATOS, json("{\"cualquiera\":1}"));
        assertEquals(json("{\"P_OUT_MONEDAFORMATO\":[]}"), out);
        Map<String, Object> fila = filaFormato("OPE", "CLP", 0, "T");
        fila.put("id_moneda", null);
        JdbcGrabador g2 = new JdbcGrabador().responder(Respuesta.filas(List.of(fila)));
        JsonObject out2 = pg().llamar(g2.conexion(), LoginPg2Recursos.FORMATOS, new JsonObject());
        assertTrue(out2.getAsJsonArray("P_OUT_MONEDAFORMATO").get(0).getAsJsonObject().get("IdMoneda").isJsonNull());
    }

    // ------------------------------------------------------------------------------------------------ refcursors

    @Test
    @DisplayName("LOGIN-PG2 RecuperarClaveUsuario with errors: the INOUT refcursors are read with getObject after execute as result sets and come back as arrays under P_OUT_ADVERTENCIASDETALLE / P_OUT_ERRORESDETALLE (labels as the cursor gives them, values as text), and every cursor is closed")
    void cursores_con_filas() throws Exception {
        Rutina r = RUT.rutina("sp_pla_acc_recuperar_clave_usuario");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(porIndice(r,
                mapa("p_out_mail", null, "p_out_advertencias_contador", 1, "p_out_advertencias_detalle", List.of(JdbcGrabador.fila("codigo", "ADV1", "descripcion", "Aviso uno")),
                        "p_out_errores_contador", 2, "p_out_errores_detalle", List.of(JdbcGrabador.fila("codigo", "E1", "descripcion", "Error uno"), JdbcGrabador.fila("codigo", "E2", "descripcion", "Error dos"))))));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.RECUPERAR, json("{\"p_IdEmpresa\":1,\"p_Username\":\"JPEREZ\",\"p_Token\":\"A1B2C\"}"));
        assertEquals(json("{\"P_OUT_MAIL\":null,\"P_OUT_ADVERTENCIASCONTADOR\":\"1\",\"P_OUT_ADVERTENCIASDETALLE\":[{\"codigo\":\"ADV1\",\"descripcion\":\"Aviso uno\"}],"
                + "\"P_OUT_ERRORESCONTADOR\":\"2\",\"P_OUT_ERRORESDETALLE\":[{\"codigo\":\"E1\",\"descripcion\":\"Error uno\"},{\"codigo\":\"E2\",\"descripcion\":\"Error dos\"}]}"), out);
        Sentencia s = g.unica();
        assertEquals(2, s.conjuntos.size(), "two cursors were opened");
        s.conjuntos.forEach(rs -> assertTrue(rs.cerrado));
        assertThat(s.lecturas).contains("getObject(" + r.indice("p_out_advertencias_detalle") + ")", "getObject(" + r.indice("p_out_errores_detalle") + ")");
        assertThat(out.get("P_OUT_MAIL").isJsonNull()).isTrue();
    }

    @Test
    @DisplayName("LOGIN-PG2 ConfirmarCambioClaveUsuario: the status code and the cursors are read from their own positions (5 status, 6 and 8 counters, 7 and 9 cursors): distinct values do not get mixed")
    void confirmar_no_mezcla_posiciones() throws Exception {
        Rutina r = RUT.rutina("sp_pla_acc_confirmar_cambio_clave_usuario");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(porIndice(r,
                mapa("p_status_code", 401, "p_out_advertencias_contador", 3, "p_out_advertencias_detalle", List.of(JdbcGrabador.fila("c", "a")), "p_out_errores_contador", 4,
                        "p_out_errores_detalle", List.of(JdbcGrabador.fila("c", "e"))))));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.CONFIRMAR, json(casos().get(5).entrada()));
        assertEquals("401", out.get("P_STATUSCODE").getAsString());
        assertEquals("3", out.get("P_OUT_ADVERTENCIASCONTADOR").getAsString());
        assertEquals("4", out.get("P_OUT_ERRORESCONTADOR").getAsString());
        assertEquals("a", out.getAsJsonArray("P_OUT_ADVERTENCIASDETALLE").get(0).getAsJsonObject().get("c").getAsString());
        assertEquals("e", out.getAsJsonArray("P_OUT_ERRORESDETALLE").get(0).getAsJsonObject().get("c").getAsString());
    }

    @Test
    @DisplayName("LOGIN-PG2 ObtenerFechas: the six OUT values keep their own names (a swapped index would swap two dates)")
    void fechas_no_mezcla_posiciones() throws Exception {
        Caso c = casos().get(2);
        Rutina r = RUT.rutina(c.pgNombre());
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(porIndice(r, c.salidas())));
        JsonObject out = pg().llamar(g.conexion(), c.hana(), json(c.entrada()));
        assertEquals("2026-11-02 09:00:00", out.get("P_OUT_FECHAHORASISTEMALOCAL").getAsString());
        assertEquals("2026-11-03 12:00:00", out.get("P_OUT_FECHAHORASISTEMAUTC").getAsString());
        assertEquals(Types.VARCHAR, g.unica().salidas.get(r.indice("p_out_fecha_hora_sistema_local")));
    }

    // ------------------------------------------------------------------------------------------------ IN parameters

    @Test
    @DisplayName("LOGIN-PG2 IN keys are found ignoring case and underscores, as HANA found them ignoring case: p_IdEmpresa, P_IDEMPRESA, p_id_empresa and p_idempresa are the same parameter; unknown keys are ignored")
    void claves_de_entrada_sin_distinguir_mayusculas_ni_guiones_bajos() throws Exception {
        for (String entrada : List.of("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}", "{\"P_IDEMPRESA\":1,\"P_IDUSUARIO\":7}", "{\"p_id_empresa\":1,\"p_id_usuario\":7}",
                "{\"p_idempresa\":1,\"p_idusuario\":7,\"otra\":\"x\"}")) {
            JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
            pg().llamar(g.conexion(), LoginPg2Recursos.ADMIN, json(entrada));
            assertEquals(1L, ((Number) g.unica().valor(1)).longValue(), entrada);
            assertEquals(7L, ((Number) g.unica().valor(2)).longValue(), entrada);
        }
    }

    @Test
    @DisplayName("LOGIN-PG2 an IN parameter that is absent or JSON null is bound as NULL with its own JDBC type (BIGINT, VARCHAR), never skipped and never as a string 'null'")
    void entradas_ausentes_van_como_null_tipado() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        pg().llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1}"));
        JdbcGrabador.Enlace e2 = g.unica().enlace(2);
        assertNull(e2.valor());
        assertEquals(Types.BIGINT, e2.tipoSql());
        JdbcGrabador g2 = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        pg().llamar(g2.conexion(), LoginPg2Recursos.CONFIG, json("{\"p_IdEmpresa\":null,\"p_CodigoParametro\":null}"));
        assertNull(g2.unica().enlace(1).valor());
        assertEquals(Types.BIGINT, g2.unica().enlace(1).tipoSql());
        assertNull(g2.unica().enlace(2).valor());
        assertEquals(Types.VARCHAR, g2.unica().enlace(2).tipoSql());
        JdbcGrabador g3 = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        pg().llamar(g3.conexion(), LoginPg2Recursos.ADMIN, new JsonObject());
        assertNull(g3.unica().enlace(1).valor());
        assertNull(g3.unica().enlace(2).valor());
    }

    @Test
    @DisplayName("LOGIN-PG2 a bigint IN accepts a JSON number or a numeric text and is bound as a number; a varchar IN accepts a JSON number or text and is bound as text")
    void conversion_de_entradas() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "x")));
        pg().llamar(g.conexion(), LoginPg2Recursos.CONFIG, json("{\"p_IdEmpresa\":\"5\",\"p_CodigoParametro\":12345}"));
        assertTrue(JdbcGrabador.iguales(5L, g.unica().valor(1)));
        assertThat(g.unica().valor(1)).isInstanceOf(Number.class);
        assertEquals("12345", g.unica().valor(2));
    }

    @Test
    @DisplayName("LOGIN-PG2 a bigint IN that is not a number fails with an IllegalArgumentException BEFORE anything is executed (as HANA's Long.parseLong did)")
    void entrada_no_numerica() {
        ProcedimientoAlmacenado p = pg();
        JdbcGrabador g = new JdbcGrabador();
        assertThatThrownBy(() -> p.llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":\"abc\",\"p_IdUsuario\":7}"))).isInstanceOf(IllegalArgumentException.class);
        assertThat(g.sentencias.stream().filter(s -> s.ejecutada)).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ null OUT values and state

    @Test
    @DisplayName("LOGIN-PG2 a NULL OUT value is JSON null under its legacy name (as HANA's getString gave), not a missing key and not the text 'null'")
    void salida_nula() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of()));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.CONFIG, json("{\"p_IdEmpresa\":1,\"p_CodigoParametro\":\"X\"}"));
        assertTrue(out.has("P_OUT_VALOR"));
        assertTrue(out.get("P_OUT_VALOR").isJsonNull());
    }

    @Test
    @DisplayName("LOGIN-PG2 every call prepares and closes its own statement; the implementation keeps no statement between calls and never touches the transaction of the caller")
    void una_sentencia_por_llamada() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        ProcedimientoAlmacenado p = pg();
        p.llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}"));
        p.llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":2,\"p_IdUsuario\":8}"));
        assertEquals(2, g.sentencias.size());
        g.sentencias.forEach(s -> assertTrue(s.cerrada));
        assertEquals(2L, ((Number) g.sentencias.get(1).valor(1)).longValue());
        assertThat(g.otrasLlamadas).isEmpty();
    }

    // ================================================================================================ D. 503 and errors

    static Stream<String> nombresRechazados() {
        return Stream.of(LoginPg2Recursos.OTRO, "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario", "BSCL.Nada.BD::Nada", "sp_pla_obtener_fechas",
                "bscl.plataforma.bd.utilitarios::obtenerfechas", " " + LoginPg2Recursos.FECHAS, "");
    }

    @ParameterizedTest(name = "LOGIN-PG2 a procedure that is not one of the eight answers 503 (UnsupportedOperationException naming it) with ZERO calls on the connection: [{0}]")
    @MethodSource("nombresRechazados")
    void otros_procedimientos_503(String nombre) {
        ProcedimientoAlmacenado p = pg();
        JdbcGrabador g = new JdbcGrabador();
        assertThatThrownBy(() -> p.llamar(g.conexion(), nombre, new JsonObject())).isInstanceOf(UnsupportedOperationException.class)
                .satisfies(e -> assertThat(String.valueOf(e.getMessage())).contains(nombre.isEmpty() ? "" : nombre.strip()));
        assertTrue(g.intacta(), "the connection was used: " + g.sentencias + g.otrasLlamadas);
    }

    @Test
    @DisplayName("LOGIN-PG2 a null procedure name answers 503 too, without touching the connection")
    void nombre_nulo() {
        ProcedimientoAlmacenado p = pg();
        JdbcGrabador g = new JdbcGrabador();
        assertThatThrownBy(() -> p.llamar(g.conexion(), null, new JsonObject())).isInstanceOfAny(UnsupportedOperationException.class, IllegalArgumentException.class);
        assertTrue(g.intacta());
    }

    @Test
    @DisplayName("A3-5 a rejection does not stop the other procedures (no state is kept: a 503 for one name, then the eight still work, then the 503 again)")
    void un_rechazo_no_detiene_a_los_demas() throws Exception {
        ProcedimientoAlmacenado p = pg();
        JdbcGrabador vacio = new JdbcGrabador();
        assertThatThrownBy(() -> p.llamar(vacio.conexion(), LoginPg2Recursos.OTRO, new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        assertEquals("S", p.llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}")).get("P_OUT_ESADMIN").getAsString());
        assertThatThrownBy(() -> p.llamar(vacio.conexion(), LoginPg2Recursos.OTRO, new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
        assertTrue(vacio.intacta());
    }

    static Stream<String> procedimientosDelOraculoFueraDeLosOcho() {
        LoginPgRecursos.Oraculo o = LoginPgRecursos.Oraculo.cargar();
        java.util.Set<String> ocho = new TreeSet<>(LoginPg2Recursos.HANA.values());
        java.util.List<String> r = new ArrayList<>();
        for (LoginPgRecursos.Objeto x : o.objetos.values()) {
            if ((x.tipo().equals("procedimiento") || x.tipo().equals("funcion")) && !ocho.contains(x.hana())) {
                r.add(x.hana());
                r.add(x.postgres());
            }
        }
        return r.stream();
    }

    @ParameterizedTest(name = "A3-5 [{0}] (a procedure or function of the catalog that is NOT one of the eight, by its HANA or Postgres name) answers 503 naming it, with ZERO calls on the connection")
    @MethodSource("procedimientosDelOraculoFueraDeLosOcho")
    void los_demas_procedimientos_del_catalogo_responden_503(String nombre) {
        JdbcGrabador g = new JdbcGrabador();
        assertThatThrownBy(() -> pg().llamar(g.conexion(), nombre, new JsonObject())).isInstanceOf(UnsupportedOperationException.class)
                .satisfies(e -> assertThat(String.valueOf(e.getMessage())).contains(nombre));
        assertTrue(g.intacta(), "the connection was used: " + g.sentencias + g.otrasLlamadas);
    }

    @Test
    @DisplayName("A3-5 the oracle has 11 procedures and functions of which exactly the eight of the login are served: the others are rejected above (the guard of that stream: it is not empty)")
    void el_oraculo_trae_otros_procedimientos() {
        assertThat(procedimientosDelOraculoFueraDeLosOcho().count()).isEqualTo(6L);
    }

    @Test
    @DisplayName("LOGIN-PG2 a SQLException at execute reaches the caller as the SAME exception (the services roll back on it), the statement is closed and nothing else is done to the connection")
    void error_al_ejecutar_se_propaga() {
        ProcedimientoAlmacenado p = pg();
        SQLException boom = new SQLException("simulated failure", "XX000");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.falla(boom));
        assertThatThrownBy(() -> p.llamar(g.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}"))).isSameAs(boom);
        assertTrue(g.unica().cerrada);
        assertThat(g.otrasLlamadas).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 a SQLException while reading an OUT value or a cursor reaches the caller unchanged and the statement and the cursors already opened are closed")
    void error_al_leer_se_propaga() {
        ProcedimientoAlmacenado p = pg();
        Rutina r = RUT.rutina("sp_pla_acc_recuperar_clave_usuario");
        SQLException boom = new SQLException("cursor failed", "34000");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(porIndice(r,
                mapa("p_out_mail", "a@b.cl", "p_out_advertencias_contador", 0, "p_out_advertencias_detalle", List.of(JdbcGrabador.fila("c", "x")), "p_out_errores_contador", 1,
                        "p_out_errores_detalle", boom))));
        assertThatThrownBy(() -> p.llamar(g.conexion(), LoginPg2Recursos.RECUPERAR, json(casos().get(4).entrada()))).isSameAs(boom);
        Sentencia s = g.unica();
        assertTrue(s.cerrada);
        s.conjuntos.forEach(rs -> assertTrue(rs.cerrado, "a cursor already opened is closed"));
        assertThat(g.otrasLlamadas).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 a null connection fails fast with an exception (no NullPointerException from deep inside is the contract: any RuntimeException or SQLException, nothing is sent)")
    void conexion_nula() {
        ProcedimientoAlmacenado p = pg();
        assertThatThrownBy(() -> p.llamar(null, LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}"))).isInstanceOf(Exception.class);
    }

    // ================================================================================================ E. misc

    @Test
    @DisplayName("LOGIN-PG2 the Postgres implementation is a ProcedimientoAlmacenado (the interface the login services take) and two instances behave the same (no state)")
    void es_un_procedimiento_almacenado() throws Exception {
        ProcedimientoAlmacenado a = pg();
        ProcedimientoAlmacenado b = pg();
        assertFalse(a == b);
        JdbcGrabador g1 = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        JdbcGrabador g2 = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        assertEquals(a.llamar(g1.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}")),
                b.llamar(g2.conexion(), LoginPg2Recursos.ADMIN, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}")));
        assertSame(g1.unica().sql.getClass(), g2.unica().sql.getClass());
    }
}
