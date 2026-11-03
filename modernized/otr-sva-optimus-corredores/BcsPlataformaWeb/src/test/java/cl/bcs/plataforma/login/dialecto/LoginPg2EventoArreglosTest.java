package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.ArregloGrabado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Respuesta;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Sentencia;

/**
 * LOGIN-PG round 2, part B (T-33): how the FOUR table parameters of EVT.AgregarEvento become four arrays of composite types in Postgres. HANA received local
 * temporary tables filled with the JSON rows; Postgres gets {@code sp_pla_evt_agregar_evento(p_eventos ty_..._eventos[], p_valores ..., p_archivos_adjuntos ...,
 * p_id_direcciones ...)}. Pinned at the JDBC seam (no real Postgres here): {@code Connection.createArrayOf(typeName, Object[])} with the UNQUALIFIED type name
 * (the flat-name convention; the schema comes from the connection's search path, Q-LOGINPG2-5) and one {@code String} per row, the text form of the composite
 * (a record literal): attributes in the order of the type, a JSON key is found ignoring case and underscores, a missing or null value is an empty field, a
 * field is double-quoted when it is empty text or has a comma, parenthesis, double quote, backslash or whitespace (inside quotes {@code "} and {@code \}
 * are doubled), bytea as {@code \x} plus hex from the base64 of the JSON. The array is bound with {@code setArray}. Whether pgjdbc accepts it is Q-LOGINPG2-4.
 */
class LoginPg2EventoArreglosTest {

    private static final LoginPg2Recursos.Rutinas RUT = LoginPg2Recursos.Rutinas.cargar();
    private static final String[] TIPOS = { "ty_pla_evt_agregar_evento_eventos", "ty_pla_evt_agregar_evento_valores", "ty_pla_evt_agregar_evento_archivos_adjuntos",
            "ty_pla_evt_agregar_evento_id_direcciones" };
    private static final String[] PARAMETROS = { "p_eventos", "p_valores", "p_archivos_adjuntos", "p_id_direcciones" };
    private static final String[] CLAVES = { "P_EVENTOS", "P_VALORES", "P_ARCHIVOSADJUNTOS", "P_IDDIRECCIONES" };

    private static JsonObject json(String t) {
        return LoginPg2ProcedimientosTest.json(t);
    }

    private static JdbcGrabador llamar(String entrada) throws Exception {
        JdbcGrabador g = new JdbcGrabador();
        ProcedimientoAlmacenado p = ApiLoginPg3.procedimientosPostgres();
        p.llamar(g.conexion(), LoginPg2Recursos.EVENTO, json(entrada));
        return g;
    }

    /** a call whose four table parameters are the given JSON arrays (or "" to leave one out) */
    private static String entrada(String eventos, String valores, String archivos, String direcciones) {
        StringBuilder sb = new StringBuilder("{\"P_IDEMPRESA\":1,\"P_IDUSUARIO\":7,\"P_IDMODULO\":\"PLA\"");
        String[] v = { eventos, valores, archivos, direcciones };
        for (int i = 0; i < 4; i++) if (!v[i].isEmpty()) sb.append(",\"").append(CLAVES[i]).append("\":").append(v[i]);
        return sb.append("}").toString();
    }

    private static ArregloGrabado arreglo(JdbcGrabador g, int cual) {
        return (ArregloGrabado) g.unica().valor(RUT.rutina("sp_pla_evt_agregar_evento").indice(PARAMETROS[cual]));
    }

    private static List<Object> elementos(JdbcGrabador g, int cual) {
        return arreglo(g, cual).elementos();
    }

    // ------------------------------------------------------------------------------------------------ oracle (green)

    @Test
    @DisplayName("LOGIN-PG2 oracle: the four array parameters are at positions 4 to 7 of the event procedure, typed with the four composite types of the QA catalog (attribute order below is that of the catalog)")
    void oraculo_de_los_arreglos() {
        for (int i = 0; i < 4; i++) {
            assertEquals(4 + i, RUT.rutina("sp_pla_evt_agregar_evento").indice(PARAMETROS[i]));
            assertEquals(TIPOS[i] + "[]", RUT.rutina("sp_pla_evt_agregar_evento").parametro(PARAMETROS[i]).tipo());
        }
        assertEquals(List.of("codigo_tipo_evento", "id_secuencia", "fecha_creacion_sistema", "id_persona_remitente", "id_persona_destinatario", "id_portafolio"),
                RUT.tipos.get(TIPOS[0]).stream().map(LoginPg2Recursos.Atributo::nombre).toList());
        assertEquals(List.of("id_secuencia", "llave", "valor"), RUT.tipos.get(TIPOS[1]).stream().map(LoginPg2Recursos.Atributo::nombre).toList());
        assertEquals(List.of("id_secuencia", "archivo_adjunto", "nombre", "contrasenia"), RUT.tipos.get(TIPOS[2]).stream().map(LoginPg2Recursos.Atributo::nombre).toList());
        assertEquals(List.of("id_secuencia", "id_direccion", "tipo_destinatario"), RUT.tipos.get(TIPOS[3]).stream().map(LoginPg2Recursos.Atributo::nombre).toList());
        assertEquals("bytea", RUT.tipos.get(TIPOS[2]).get(1).tipo());
        assertEquals("timestamp(0) without time zone", RUT.tipos.get(TIPOS[0]).get(2).tipo());
    }

    // ------------------------------------------------------------------------------------------------ red

    @Test
    @DisplayName("LOGIN-PG2 four createArrayOf calls on the caller's connection, one per table parameter, with the unqualified composite type names, each array bound with setArray at its own position; elements are Strings")
    void cuatro_arreglos_con_su_tipo() throws Exception {
        JdbcGrabador g = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA);
        assertEquals(4, g.arreglos.size());
        assertEquals(List.of(TIPOS), g.arreglos.stream().map(ArregloGrabado::tipo).toList());
        for (int i = 0; i < 4; i++) {
            JdbcGrabador.Enlace e = g.unica().enlace(4 + i);
            assertEquals("setArray", e.metodo(), PARAMETROS[i]);
            assertEquals(TIPOS[i], arreglo(g, i).tipo(), PARAMETROS[i]);
            elementos(g, i).forEach(x -> assertThat(x).isInstanceOf(String.class));
        }
        assertThat(g.arreglos.stream().map(ArregloGrabado::tipo)).noneMatch(t -> t.contains("."));
    }

    @Test
    @DisplayName("LOGIN-PG2 each row becomes the record literal of its type with the attributes IN THE ORDER OF THE TYPE (not the order of the JSON keys): one distinct value per attribute")
    void orden_de_atributos() throws Exception {
        JdbcGrabador g = llamar(entrada(
                "[{\"IdPortafolio\":4,\"IdPersonaDestinatario\":3,\"IdPersonaRemitente\":2,\"FechaCreacionSistema\":\"2026-11-03 10:15:30\",\"IdSecuencia\":1,\"CodigoTipoEvento\":\"EVT_A\"}]",
                "[{\"Valor\":\"V\",\"Llave\":\"K\",\"IdSecuencia\":1}]",
                "[{\"Contrasenia\":\"Zz9\",\"Nombre\":\"a.pdf\",\"ArchivoAdjunto\":\"Cv8=\",\"IdSecuencia\":1}]",
                "[{\"TipoDestinatario\":\"CC\",\"IdDireccion\":2,\"IdSecuencia\":1}]"));
        assertEquals(List.of("(EVT_A,1,\"2026-11-03 10:15:30\",2,3,4)"), elementos(g, 0));
        assertEquals(List.of("(1,K,V)"), elementos(g, 1));
        assertEquals(List.of("(1,\"\\\\x0aff\",a.pdf,Zz9)"), elementos(g, 2));
        assertEquals(List.of("(1,2,CC)"), elementos(g, 3));
    }

    static Stream<Arguments> citado() {
        return Stream.of(
                Arguments.of("\"simple\"", "(1,K,simple)"),
                Arguments.of("\"con espacio\"", "(1,K,\"con espacio\")"),
                Arguments.of("\"a,b\"", "(1,K,\"a,b\")"),
                Arguments.of("\"(x)\"", "(1,K,\"(x)\")"),
                Arguments.of("\"di\\\"ce\"", "(1,K,\"di\"\"ce\")"),
                Arguments.of("\"back\\\\slash\"", "(1,K,\"back\\\\slash\")"),
                Arguments.of("\"\"", "(1,K,\"\")"),
                Arguments.of("null", "(1,K,)"),
                Arguments.of("\"ñandú\"", "(1,K,ñandú)"),
                Arguments.of("\"tab\\there\"", "(1,K,\"tab\there\")"),
                Arguments.of("\"a'b\"", "(1,K,a'b)"),
                Arguments.of("12.5", "(1,K,12.5)"),
                Arguments.of("\"Xk3!q#$%&=?@\"", "(1,K,Xk3!q#$%&=?@)"));
    }

    @ParameterizedTest(name = "LOGIN-PG2 record literal quoting: JSON value {0} -> {1}")
    @MethodSource("citado")
    void comillas_y_escapes(String valorJson, String literal) throws Exception {
        JdbcGrabador g = llamar(entrada("", "[{\"IdSecuencia\":1,\"Llave\":\"K\",\"Valor\":" + valorJson + "}]", "", ""));
        assertEquals(List.of(literal), elementos(g, 1));
    }

    @Test
    @DisplayName("LOGIN-PG2 a key missing from the row is an empty field (NULL), as HANA's temporary table got NULL for it; row keys are found ignoring case and underscores; extra keys are ignored")
    void claves_de_fila() throws Exception {
        JdbcGrabador g = llamar(entrada("", "[{\"idsecuencia\":1,\"LLAVE\":\"K\"},{\"id_secuencia\":2,\"llave\":\"K2\",\"valor\":\"V2\",\"otra\":\"x\"}]", "", ""));
        assertEquals(List.of("(1,K,)", "(2,K2,V2)"), elementos(g, 1));
    }

    @Test
    @DisplayName("LOGIN-PG2 rows keep their order and every row is one element: three rows, three elements, in the JSON order")
    void orden_de_filas() throws Exception {
        JdbcGrabador g = llamar(entrada("", "[{\"IdSecuencia\":3,\"Llave\":\"C\",\"Valor\":\"3\"},{\"IdSecuencia\":1,\"Llave\":\"A\",\"Valor\":\"1\"},{\"IdSecuencia\":2,\"Llave\":\"B\",\"Valor\":\"2\"}]", "", ""));
        assertEquals(List.of("(3,C,3)", "(1,A,1)", "(2,B,2)"), elementos(g, 1));
    }

    @Test
    @DisplayName("LOGIN-PG2 an attachment: the base64 of the JSON becomes bytea hex (backslash-x then lower-case hex, the backslash doubled inside the quoted field); no attachment bytes is an empty field")
    void archivo_adjunto_bytea() throws Exception {
        JdbcGrabador g = llamar(entrada("", "", "[{\"IdSecuencia\":1,\"ArchivoAdjunto\":\"AAEC/w==\",\"Nombre\":\"x.bin\",\"Contrasenia\":null},{\"IdSecuencia\":2,\"Nombre\":\"vacio.txt\"}]", ""));
        assertEquals(List.of("(1,\"\\\\x000102ff\",x.bin,)", "(2,,vacio.txt,)"), elementos(g, 2));
    }

    @Test
    @DisplayName("LOGIN-PG2 a timestamp is passed through as given (a date, or a date and time, or ISO with T): Postgres parses them; the dash and colon need no quoting but a space does")
    void fechas_se_pasan_tal_cual() throws Exception {
        JdbcGrabador g = llamar(entrada("[{\"CodigoTipoEvento\":\"X\",\"IdSecuencia\":0,\"FechaCreacionSistema\":\"2026-11-03\"},{\"CodigoTipoEvento\":\"X\",\"IdSecuencia\":1,\"FechaCreacionSistema\":\"2026-11-03T10:15:30\"}]", "", "", ""));
        assertEquals(List.of("(X,0,2026-11-03,,,)", "(X,1,2026-11-03T10:15:30,,,)"), elementos(g, 0));
    }

    static Stream<Arguments> vacios() {
        return Stream.of(Arguments.of("[]"), Arguments.of("null"), Arguments.of("{\"no\":\"es arreglo\"}"), Arguments.of(""));
    }

    @ParameterizedTest(name = "LOGIN-PG2 a table parameter that is {0} (empty array, JSON null, not an array, absent) is an EMPTY array of its type, not NULL (HANA created an empty temporary table)")
    @MethodSource("vacios")
    void tabla_vacia_es_arreglo_vacio(String valor) throws Exception {
        JdbcGrabador g = llamar(entrada(valor, valor, valor, valor));
        assertEquals(4, g.arreglos.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(TIPOS[i], arreglo(g, i).tipo());
            assertEquals(List.of(), elementos(g, i));
        }
    }

    @Test
    @DisplayName("LOGIN-PG2 an element of a table parameter that is not a JSON object fails with a RuntimeException BEFORE the procedure is executed (as HANA's getAsJsonObject did)")
    void fila_que_no_es_objeto() {
        JdbcGrabador g = new JdbcGrabador();
        ProcedimientoAlmacenado p = ApiLoginPg3.procedimientosPostgres();
        assertThatThrownBy(() -> p.llamar(g.conexion(), LoginPg2Recursos.EVENTO, json(entrada("", "[1,2]", "", "")))).isInstanceOf(RuntimeException.class);
        assertThat(g.sentencias.stream().filter(s -> s.ejecutada)).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 p_aplica_origen_evento: the empty text of the login is bound as the empty text (not NULL: the legacy passes '' and the Postgres default is ''); a value is bound as is; absent or null is the empty text too")
    void aplica_origen_evento() throws Exception {
        int idx = RUT.rutina("sp_pla_evt_agregar_evento").indice("p_aplica_origen_evento");
        JdbcGrabador a = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA);
        assertEquals("", a.unica().valor(idx));
        JdbcGrabador b = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA.replace("\"P_APLICAORIGENEVENTO\":\"\"", "\"P_APLICAORIGENEVENTO\":\"WEB\""));
        assertEquals("WEB", b.unica().valor(idx));
        JdbcGrabador c = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA.replace(",\"P_APLICAORIGENEVENTO\":\"\"", ""));
        assertEquals("", c.unica().valor(idx));
        JdbcGrabador d = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA.replace("\"P_APLICAORIGENEVENTO\":\"\"", "\"P_APLICAORIGENEVENTO\":null"));
        assertEquals("", d.unica().valor(idx));
    }

    @Test
    @DisplayName("LOGIN-PG2 the event cursor p_out_id_evento: absent from the answer as an empty array when Postgres returns no cursor, rows as text when it does; registered OTHER, bound NULL")
    void cursor_del_evento() throws Exception {
        JdbcGrabador g = new JdbcGrabador();
        ProcedimientoAlmacenado p = ApiLoginPg3.procedimientosPostgres();
        JsonObject out = p.llamar(g.conexion(), LoginPg2Recursos.EVENTO, json(LoginPg2ProcedimientosTest.EVENTO_ENTRADA));
        assertEquals(json("{\"P_OUT_IDEVENTO\":[]}"), out);
        Sentencia s = g.unica();
        assertEquals(Map.of(9, java.sql.Types.OTHER), s.salidas);
        assertThat(s.enlace(9).valor()).isNull();
    }

    @Test
    @DisplayName("LOGIN-PG2 the other IN parameters of the event keep their types: company and user as numbers, module as text")
    void otros_parametros_del_evento() throws Exception {
        JdbcGrabador g = llamar(LoginPg2ProcedimientosTest.EVENTO_ENTRADA);
        Sentencia s = g.unica();
        assertTrue(JdbcGrabador.iguales(1L, s.valor(1)));
        assertTrue(JdbcGrabador.iguales(7L, s.valor(2)));
        assertEquals("PLA", s.valor(3));
        List<Integer> posiciones = new ArrayList<>(s.enlaces.keySet());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), posiciones);
    }
}
