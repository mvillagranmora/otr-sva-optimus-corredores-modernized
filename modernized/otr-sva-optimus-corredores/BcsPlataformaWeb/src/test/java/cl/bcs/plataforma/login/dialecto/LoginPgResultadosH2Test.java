package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.login.h2.LoginDb;

/**
 * T-35 result equivalence on H2: the table-only statements run in HANA shape (the schema of the existing login tests, HANA texts) and in Postgres shape
 * (flat snake_case schema generated from the xlsx oracle, Postgres texts, H2 MODE=PostgreSQL) over the SAME fake rows, and must give the same rows
 * under the HANA column names. Column TYPES of the Postgres schema are assumed equal to the HANA test schema (A-20). Limits of H2 (documented in
 * API_ASSUMED_LOGINPG.md): it returns TIMESTAMP WITH TIME ZONE for {@code now() AT TIME ZONE 'UTC'}, so the Postgres connection of this test rewrites
 * that literal to LOCALTIMESTAMP (the JVM runs in UTC) and the literal text is pinned in LoginPgDialectoFuncionesTest / LoginPgTraduccionTest; times
 * near "now" are compared as minutes from now.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginPgResultadosH2Test {

    private static final LoginPgRecursos.Oraculo ORACULO = LoginPgRecursos.Oraculo.cargar();
    private static final AtomicInteger NUM = new AtomicInteger();
    static final AtomicInteger EJECUTADOS = new AtomicInteger();

    private JsonObject cfg;
    private LoginDb hana;
    private Connection pgRaw;
    private Connection pgCon;
    private Connection hanaCon;
    private final Set<String> saltadas = new LinkedHashSet<>();

    @BeforeAll
    void montar() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
        cfg = JsonParser.parseString(LoginPgRecursos.texto("/loginpg/equivalencia-parametros.json")).getAsJsonObject();
        hana = LoginDb.nueva();
        for (String s : lineas(LoginPgRecursos.texto("/loginpg/seed-equivalencia-hana.sql"))) hana.sql(s);
        hanaCon = hana.con();
        pgRaw = DriverManager.getConnection("jdbc:h2:mem:lpg" + NUM.incrementAndGet() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1", "sa", "");
        construirPostgres(pgRaw);
        pgRaw.setAutoCommit(false);
        pgCon = puentePostgres(pgRaw);
    }

    @AfterAll
    void cerrar() throws Exception {
        System.out.println("LOGINPG equivalence cases executed: " + EJECUTADOS.get());
        pgRaw.close();
        hana.close();
    }

    // ------------------------------------------------------------------------------------------------ Postgres-shaped H2 schema from the oracle

    private static List<String> lineas(String texto) {
        List<String> r = new ArrayList<>();
        for (String l : texto.split("\n")) if (!l.isBlank() && !l.strip().startsWith("--")) r.add(l);
        return r;
    }

    private static List<String> sentencias(String ruta) {
        StringBuilder limpio = new StringBuilder();
        for (String l : LoginPgRecursos.texto(ruta).split("\n")) if (!l.strip().startsWith("--")) limpio.append(l).append('\n');
        List<String> r = new ArrayList<>();
        for (String s : limpio.toString().split(";\\s*\n")) if (!s.isBlank()) r.add(s);
        return r;
    }

    /** replaces the double-quoted names outside string literals: the first is the object, the next ones its columns; null when the object is not a table/sequence of the oracle */
    String traducir(String sql) {
        StringBuilder sb = new StringBuilder();
        String objeto = null;
        boolean simple = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (simple) {
                sb.append(c);
                if (c == '\'') simple = false;
            } else if (c == '\'') {
                simple = true;
                sb.append(c);
            } else if (c == '"') {
                int j = sql.indexOf('"', i + 1);
                String n = sql.substring(i + 1, j);
                if (objeto == null) {
                    objeto = n;
                    LoginPgRecursos.Objeto o = ORACULO.objetos.get(n);
                    if (o == null) fail("object not in the oracle: " + n);
                    if (!o.tipo().equals("tabla") && !o.tipo().equals("secuencia")) {
                        saltadas.add(n);
                        return null;
                    }
                    sb.append(o.postgres());
                } else {
                    String col = ORACULO.columnas.get(objeto) == null ? null : ORACULO.columnas.get(objeto).get(n);
                    if (col == null) fail("column not in the oracle: " + objeto + "." + n);
                    sb.append(col);
                }
                i = j;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void construirPostgres(Connection c) throws Exception {
        List<String> todas = new ArrayList<>();
        for (String r : List.of("/login/schema-login.sql", "/login/schema-bootstrap.sql")) todas.addAll(sentencias(r));
        todas.addAll(lineas(LoginPgRecursos.texto("/login/base-login.sql")));
        todas.addAll(lineas(LoginPgRecursos.texto("/loginpg/seed-equivalencia-hana.sql")));
        try (Statement st = c.createStatement()) {
            for (String s : todas) {
                String t = s.strip();
                if (t.startsWith("CREATE ALIAS") || t.contains("DUMMY")) continue;
                String pg = traducir(s);
                if (pg != null) st.execute(pg);
            }
        }
        c.commit();
    }

    private static Connection puentePostgres(Connection raw) {
        return (Connection) Proxy.newProxyInstance(LoginPgResultadosH2Test.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if ((m.getName().equals("prepareStatement") || m.getName().equals("nativeSQL")) && a != null && a.length > 0 && a[0] instanceof String s) {
                a = a.clone();
                a[0] = s.replace("(now() AT TIME ZONE 'UTC')", "LOCALTIMESTAMP");
            }
            try {
                return m.invoke(raw, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    // ------------------------------------------------------------------------------------------------ running a statement

    private Map<String, Object> parametros(String id, String sqlHana) {
        Map<String, Object> m = new LinkedHashMap<>();
        JsonObject comun = cfg.getAsJsonObject("comun");
        JsonObject extra = cfg.getAsJsonObject("por_sentencia").has(id) ? cfg.getAsJsonObject("por_sentencia").getAsJsonObject(id) : new JsonObject();
        for (String n : LoginPgRecursos.parametros(sqlHana)) {
            JsonElement e = extra.has(n) ? extra.get(n) : comun.get(n);
            if (e == null) fail("fixture has no value for parameter " + n + " of " + id);
            m.put(n, aJava(e));
        }
        return m;
    }

    private static Object aJava(JsonElement e) {
        if (e.isJsonNull()) return null;
        if (e.isJsonArray()) {
            List<Object> l = new ArrayList<>();
            for (JsonElement x : (JsonArray) e) l.add(aJava(x));
            return l;
        }
        if (e.getAsJsonPrimitive().isNumber()) return e.getAsLong();
        return e.getAsString();
    }

    private record Preparada(String sql, List<Object> valores) {
    }

    /** named :params -> ?, a List expands to one ? per element (as SqlNombrado does) */
    private static Preparada preparar(String sql, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        List<Object> valores = new ArrayList<>();
        boolean simple = false, doble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (simple) {
                if (c == '\'') simple = false;
            } else if (doble) {
                if (c == '"') doble = false;
            } else if (c == '\'') {
                simple = true;
            } else if (c == '"') {
                doble = true;
            } else if (c == ':' && i + 1 < sql.length() && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 2;
                while (j < sql.length() && Character.isJavaIdentifierPart(sql.charAt(j))) j++;
                Object v = params.get(sql.substring(i + 1, j));
                if (v instanceof List<?> l) {
                    sb.append("?,".repeat(l.size()), 0, 2 * l.size() - 1);
                    valores.addAll(l);
                } else {
                    sb.append('?');
                    valores.add(v);
                }
                i = j - 1;
                continue;
            }
            sb.append(c);
        }
        String t = sb.toString().stripTrailing();
        return new Preparada(t.endsWith(";") ? t.substring(0, t.length() - 1) : t, valores);
    }

    private record Salida(List<String> etiquetas, List<String> filas, int afectadas) {
    }

    private static void enlazar(PreparedStatement st, List<Object> valores) throws SQLException {
        for (int i = 0; i < valores.size(); i++) {
            Object v = valores.get(i);
            if (v == null) st.setNull(i + 1, Types.VARCHAR);
            else st.setObject(i + 1, v);
        }
    }

    private static String celda(ResultSet rs, int i, int tipo, long ahoraMs) throws SQLException {
        if (rs.getObject(i) == null) return "<null>";
        switch (tipo) {
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                Timestamp t = rs.getTimestamp(i);
                long delta = Math.round((t.getTime() - ahoraMs) / 60000.0);
                return Math.abs(delta) < 1440 ? "<now" + (delta >= 0 ? "+" : "") + delta + "m>" : t.toLocalDateTime().toString();
            }
            case Types.BINARY, Types.VARBINARY, Types.BLOB, Types.LONGVARBINARY -> {
                return java.util.HexFormat.of().formatHex(rs.getBytes(i));
            }
            default -> {
                return rs.getString(i);
            }
        }
    }

    private static List<String> filas(ResultSet rs, long ahora) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        List<String> r = new ArrayList<>();
        while (rs.next()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= md.getColumnCount(); i++) sb.append(i > 1 ? "|" : "").append(celda(rs, i, md.getColumnType(i), ahora));
            r.add(sb.toString());
        }
        return r;
    }

    private static List<String> etiquetas(ResultSetMetaData md) throws SQLException {
        List<String> e = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) e.add(md.getColumnLabel(i));
        return e;
    }

    private Salida ejecutar(Connection con, String sql, Map<String, Object> params, List<Object> posicionales, boolean consulta, List<String> volcados, boolean esPg) throws Exception {
        Preparada p = preparar(sql, params);
        List<Object> valores = posicionales != null ? posicionales : p.valores();
        long ahora = System.currentTimeMillis();
        try (PreparedStatement st = con.prepareStatement(p.sql())) {
            enlazar(st, valores);
            if (consulta) {
                try (ResultSet rs = st.executeQuery()) {
                    List<String> et = etiquetas(rs.getMetaData());
                    return new Salida(et, filas(rs, ahora), -1);
                }
            }
            int n = st.executeUpdate();
            List<String> todas = new ArrayList<>();
            List<String> et = new ArrayList<>();
            for (String t : volcados) {
                String nombre = esPg ? ORACULO.objeto(t).postgres() : "\"" + t + "\"";
                try (Statement s = con.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM " + nombre)) {
                    List<String> e2 = etiquetas(rs.getMetaData());
                    if (esPg) {
                        Map<String, String> inversa = new HashMap<>();
                        ORACULO.columnas.get(t).forEach((h, g) -> inversa.put(g, h));
                        e2.replaceAll(x -> inversa.get(x));
                    }
                    et.addAll(e2);
                    for (String f : filas(rs, ahora)) todas.add(t + "#" + f);
                }
            }
            return new Salida(et, todas, n);
        } finally {
            con.rollback();
        }
    }

    private List<Object> posicionales(String id) {
        return id.equals("FiltroSesion.SQL_VALIDA_TOKEN") ? List.of("TOKEN-A") : null;
    }

    // ------------------------------------------------------------------------------------------------ cases

    Stream<String> ids() {
        return LoginPgTraduccionTest.idsSoloTablas();
    }

    private Salida enHana(String id) throws Exception {
        String sql = LoginPgRecursos.goldenSentencias().get(id);
        boolean consulta = sql.stripLeading().toLowerCase().startsWith("select");
        List<String> v = volcados(id);
        return ejecutar(hanaCon, sql, parametros(id, sql), posicionales(id), consulta, v, false);
    }

    private List<String> volcados(String id) {
        JsonObject o = cfg.getAsJsonObject("volcar_tras_escribir");
        List<String> r = new ArrayList<>();
        if (o.has(id)) o.getAsJsonArray(id).forEach(e -> r.add(e.getAsString()));
        return r;
    }

    @ParameterizedTest(name = "T-35 fixture self-check (HANA H2): {0} runs on the HANA schema and is not vacuous")
    @MethodSource("ids")
    void hana_h2_ejecuta_y_no_es_vacio(String id) throws Exception {
        Salida s = enHana(id);
        String sql = LoginPgRecursos.goldenSentencias().get(id);
        if (sql.stripLeading().toLowerCase().startsWith("select")) {
            assertThat(s.filas()).as("HANA rows of " + id).isNotEmpty();
        } else {
            assertThat(s.afectadas()).as("rows affected in HANA by " + id).isGreaterThanOrEqualTo(1);
            assertThat(volcados(id)).as("tables dumped after " + id).isNotEmpty();
        }
    }

    @Test
    @DisplayName("T-35 the Postgres-shaped H2 schema builds from the oracle (38 login tables minus the views' tables), loads the same fake rows, and the 7 views are left out")
    void esquema_postgres_se_construye_desde_el_oraculo() throws Exception {
        assertThat(saltadas).as("objects of the H2 test schema that are views in the catalog (no columns known)").containsExactlyInAnyOrder(
                "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa", "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen",
                "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario", "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla",
                "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso", "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona",
                "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales");
        try (Statement st = pgRaw.createStatement()) {
            for (String t : List.of("BSCL.Plataforma.BD::ACC.Usuario", "BSCL.Plataforma.BD::ACC.Sesion", "BSCL.Plataforma.BD::PLA.Configuracion")) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + ORACULO.objeto(t).postgres())) {
                    rs.next();
                    int pg = rs.getInt(1);
                    assertEquals(Integer.parseInt(hana.valor("SELECT COUNT(*) FROM \"" + t + "\"")), pg, "rows of " + t);
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT nextval('sq_pla_log')")) {
                rs.next();
                assertThat(rs.getLong(1)).as("sequence starts at 1000 in the schema script").isGreaterThanOrEqualTo(1000L);
            }
        }
    }

    @ParameterizedTest(name = "T-35 result equivalence {0}: the Postgres text on H2-PostgreSQL gives the same labels and rows as the HANA text")
    @MethodSource("ids")
    void postgres_da_lo_mismo_que_hana(String id) throws Exception {
        Salida h = enHana(id);
        String sqlH = LoginPgRecursos.goldenSentencias().get(id);
        boolean consulta = sqlH.stripLeading().toLowerCase().startsWith("select");
        Object cat = ApiLoginPg.catalogoDeRecursos();
        String sqlP = ApiLoginPg.texto(ApiLoginPg.sentencias(ApiLoginPg.postgres(cat)), id);
        Salida p = ejecutar(pgCon, sqlP, parametros(id, sqlH), posicionales(id), consulta, volcados(id), true);
        EJECUTADOS.incrementAndGet();
        if (consulta) assertEquals(h.etiquetas(), p.etiquetas(), "column labels of " + id + " (must be the HANA names)");
        else assertEquals(h.afectadas(), p.afectadas(), "rows affected by " + id);
        List<String> fh = new ArrayList<>(h.filas());
        List<String> fp = new ArrayList<>(p.filas());
        if (!sqlH.toLowerCase().contains("order by")) {
            Collections.sort(fh);
            Collections.sort(fp);
        }
        assertEquals(fh, fp, "rows of " + id);
        if (!consulta) assertEquals(h.etiquetas(), p.etiquetas(), "dump labels of " + id);
    }

    @Test
    @DisplayName("T-35 equivalence cases executed: all 39 table-only statements ran in both dialects (zero would mean not proven)")
    void todos_los_casos_se_ejecutaron() throws Exception {
        int n = 0;
        for (String id : (Iterable<String>) ids()::iterator) {
            postgres_da_lo_mismo_que_hana(id);
            n++;
        }
        assertEquals(39, n, "equivalence cases executed");
    }

    // ------------------------------------------------------------------------------------------------ dialect functions evaluated by H2

    private String escalar(Connection con, String sql) throws Exception {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        } finally {
            con.rollback();
        }
    }

    @Test
    @DisplayName("T-35 each dialect function gives the same VALUE on H2 in both dialects: to integer, date to text, trim, add seconds, no-FROM select, next sequence value")
    void funciones_dan_el_mismo_valor() throws Exception {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        Object h = ApiLoginPg.hana(cat);
        Object p = ApiLoginPg.postgres(cat);
        record Caso(String funcion, Object[] args, String esperado) {
        }
        List<Caso> casos = List.of(
                new Caso("aEntero", new Object[] { "'15'" }, "15"),
                new Caso("fechaATexto", new Object[] { "DATE '2026-03-02'", "YYYY-MM-DD" }, "2026-03-02"),
                new Caso("recortar", new Object[] { "'  hola  '" }, "hola"),
                new Caso("sumarSegundos", new Object[] { "TIMESTAMP '2026-03-01 10:00:00'", "900" }, "2026-03-01 10:15:00"));
        for (Caso c : casos) {
            String eh = ApiLoginPg.f(h, c.funcion(), c.args());
            String ep = ApiLoginPg.f(p, c.funcion(), c.args());
            String vh = escalar(hanaCon, ApiLoginPg.f(h, "seleccionarSinTabla", eh + " AS \"v\""));
            String vp = escalar(pgCon, ApiLoginPg.f(p, "seleccionarSinTabla", ep + " AS \"v\""));
            assertTrue(vh.startsWith(c.esperado()), c.funcion() + " in HANA gave " + vh);
            assertTrue(vp.startsWith(c.esperado()), c.funcion() + " in Postgres gave " + vp);
        }
    }

    @Test
    @DisplayName("T-35 next sequence value on H2: Log and DetalleLog advance by one per call in both dialects, from the start value of the schema (1000 / 5000)")
    void siguiente_valor_avanza_de_a_uno() throws Exception {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        Object h = ApiLoginPg.hana(cat);
        Object p = ApiLoginPg.postgres(cat);
        for (String seq : List.of("BSCL.Plataforma.BD.Secuencias::Log", "BSCL.Plataforma.BD.Secuencias::DetalleLog")) {
            long minimo = seq.endsWith("::Log") ? 1000L : 5000L;
            long[] ant = new long[2];
            for (int i = 0; i < 2; i++) {
                long vh = Long.parseLong(escalar(hanaCon, ApiLoginPg.f(h, "seleccionarSinTabla", ApiLoginPg.f(h, "siguienteValor", seq) + " AS \"Id\"")));
                long vp = Long.parseLong(escalar(pgCon, ApiLoginPg.f(p, "seleccionarSinTabla", ApiLoginPg.f(p, "siguienteValor", seq) + " AS \"Id\"")));
                assertThat(vh).as("HANA " + seq).isGreaterThanOrEqualTo(minimo);
                assertThat(vp).as("Postgres " + seq).isGreaterThanOrEqualTo(minimo);
                if (i == 1) {
                    assertEquals(ant[0] + 1, vh);
                    assertEquals(ant[1] + 1, vp);
                }
                ant[0] = vh;
                ant[1] = vp;
            }
        }
    }

    @Test
    @DisplayName("T-35 first-N on H2: the first row of an ordered query is the same in both dialects")
    void primeros_da_la_misma_fila() throws Exception {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        Object h = ApiLoginPg.hana(cat);
        Object p = ApiLoginPg.postgres(cat);
        String qh = ApiLoginPg.f(h, "primeros", "SELECT \"IdUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" ORDER BY \"IdUsuario\" DESC", 1);
        String qp = ApiLoginPg.f(p, "primeros", "SELECT id_usuario AS \"IdUsuario\" FROM t_pla_acc_usuario ORDER BY id_usuario DESC", 1);
        assertEquals("2", escalar(hanaCon, qh));
        assertEquals("2", escalar(pgCon, qp));
    }
}
