package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/**
 * LOGIN-PG round 2, part A (T-35): result equivalence on H2 of the 6 statements over VIEWS. H2 stand-ins of the views (and of the two tables of
 * SQL_DATOS_RECUPERACION) are built twice from ONE fixture of synthetic rows ({@code loginpg2/vistas-h2.json}): in HANA shape (quoted full HANA names, HANA
 * column names, H2 MODE=LEGACY, the HANA golden text) and in Postgres shape (flat snake_case names and columns from the oracles, H2 MODE=PostgreSQL,
 * NULL sorted last as Postgres does, the production Postgres text). Same rows, so the same labels and rows must come out. Every Postgres text is thereby
 * also accepted by H2 MODE=PostgreSQL. Limits: column types of the Postgres stand-ins are assumed equal to the HANA ones; H2 is not Postgres (T-36).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginPg2VistasH2Test {

    private static final LoginPgRecursos.Oraculo XLSX = LoginPgRecursos.Oraculo.cargar();
    private static final LoginPg2Recursos.OraculoVistas VISTAS = LoginPg2Recursos.OraculoVistas.cargar();
    private static final AtomicInteger NUM = new AtomicInteger();
    static final AtomicInteger EJECUTADOS = new AtomicInteger();

    private JsonObject fixture;
    private Connection hana;
    private Connection pg;

    @BeforeAll
    void montar() throws Exception {
        fixture = JsonParser.parseString(LoginPg2Recursos.texto("vistas-h2.json")).getAsJsonObject();
        int n = NUM.incrementAndGet();
        hana = DriverManager.getConnection("jdbc:h2:mem:lpg2h" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        pg = DriverManager.getConnection("jdbc:h2:mem:lpg2p" + n + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1", "sa", "");
        for (JsonElement e : fixture.getAsJsonArray("objetos")) {
            JsonObject o = e.getAsJsonObject();
            String nombreHana = o.get("hana").getAsString();
            Map<String, String> tipos = new LinkedHashMap<>();
            o.getAsJsonObject("columnas").entrySet().forEach(c -> tipos.put(c.getKey(), c.getValue().getAsString()));
            Map<String, String> aPg = LoginPg2Recursos.columnasDe(nombreHana, XLSX, VISTAS);
            String nombrePg = XLSX.objeto(nombreHana).postgres();
            StringBuilder dh = new StringBuilder("CREATE TABLE \"" + nombreHana + "\" (");
            StringBuilder dp = new StringBuilder("CREATE TABLE " + nombrePg + " (");
            boolean primera = true;
            for (Map.Entry<String, String> c : tipos.entrySet()) {
                String colPg = aPg.get(c.getKey());
                if (colPg == null) fail("column " + c.getKey() + " of " + nombreHana + " is not in the oracle of columns");
                dh.append(primera ? "" : ", ").append('"').append(c.getKey()).append("\" ").append(c.getValue());
                dp.append(primera ? "" : ", ").append(colPg).append(' ').append(c.getValue());
                primera = false;
            }
            try (Statement st = hana.createStatement()) {
                st.execute(dh.append(')').toString());
            }
            try (Statement st = pg.createStatement()) {
                st.execute(dp.append(')').toString());
            }
            for (JsonElement f : o.getAsJsonArray("filas")) {
                insertar(hana, "\"" + nombreHana + "\"", f.getAsJsonArray(), tipos.size());
                insertar(pg, nombrePg, f.getAsJsonArray(), tipos.size());
            }
        }
        hana.setAutoCommit(false);
        pg.setAutoCommit(false);
    }

    private static void insertar(Connection c, String tabla, JsonArray fila, int n) throws SQLException {
        try (PreparedStatement st = c.prepareStatement("INSERT INTO " + tabla + " VALUES (" + "?,".repeat(n - 1) + "?)")) {
            for (int i = 0; i < n; i++) {
                JsonElement v = fila.get(i);
                if (v.isJsonNull()) st.setNull(i + 1, Types.NULL);
                else if (v.getAsJsonPrimitive().isNumber()) st.setLong(i + 1, v.getAsLong());
                else if (v.getAsString().startsWith("hex:")) st.setBytes(i + 1, HexFormat.of().parseHex(v.getAsString().substring(4)));
                else st.setString(i + 1, v.getAsString());
            }
            st.executeUpdate();
        }
    }

    @AfterAll
    void cerrar() throws Exception {
        System.out.println("LOGINPG2 view equivalence cases executed: " + EJECUTADOS.get());
        hana.close();
        pg.close();
    }

    // ------------------------------------------------------------------------------------------------ running

    private static Object aJava(JsonElement e) {
        return e.getAsJsonPrimitive().isNumber() ? (Object) e.getAsLong() : e.getAsString();
    }

    private List<Map<String, Object>> variantes(String id) {
        List<Map<String, Object>> r = new ArrayList<>();
        for (JsonElement e : fixture.getAsJsonObject("variantes").getAsJsonArray(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            e.getAsJsonObject().entrySet().forEach(x -> m.put(x.getKey(), aJava(x.getValue())));
            r.add(m);
        }
        return r;
    }

    private record Preparada(String sql, List<Object> valores) {
    }

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
                String n = sql.substring(i + 1, j);
                if (!params.containsKey(n)) fail("fixture has no value for parameter " + n);
                sb.append('?');
                valores.add(params.get(n));
                i = j - 1;
                continue;
            }
            sb.append(c);
        }
        String t = sb.toString().stripTrailing();
        return new Preparada(t.endsWith(";") ? t.substring(0, t.length() - 1) : t, valores);
    }

    private record Salida(List<String> etiquetas, List<List<String>> filas) {
    }

    private static Salida ejecutar(Connection con, String sql, Map<String, Object> params) throws Exception {
        Preparada p = preparar(sql, params);
        try (PreparedStatement st = con.prepareStatement(p.sql())) {
            for (int i = 0; i < p.valores().size(); i++) st.setObject(i + 1, p.valores().get(i));
            try (ResultSet rs = st.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<String> et = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) et.add(md.getColumnLabel(i));
                List<List<String>> filas = new ArrayList<>();
                while (rs.next()) {
                    List<String> f = new ArrayList<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if (rs.getObject(i) == null) f.add("<null>");
                        else if (md.getColumnType(i) == Types.VARBINARY || md.getColumnType(i) == Types.BINARY) f.add(HexFormat.of().formatHex(rs.getBytes(i)));
                        else f.add(rs.getString(i));
                    }
                    filas.add(f);
                }
                return new Salida(et, filas);
            }
        } finally {
            con.rollback();
        }
    }

    private static Salida proyectar(Salida s, List<String> etiquetas) {
        List<Integer> idx = new ArrayList<>();
        for (String e : etiquetas) {
            int i = s.etiquetas().indexOf(e);
            if (i < 0) fail("label " + e + " is not in " + s.etiquetas());
            idx.add(i);
        }
        List<List<String>> filas = new ArrayList<>();
        for (List<String> f : s.filas()) {
            List<String> n = new ArrayList<>();
            idx.forEach(i -> n.add(f.get(i)));
            filas.add(n);
        }
        return new Salida(etiquetas, filas);
    }

    static Stream<String> ids() {
        return LoginPg2TraduccionVistasTest.IDS.stream();
    }

    private static String hanaSql(String id) {
        return LoginPgRecursos.goldenSentencias().get(id);
    }

    // ------------------------------------------------------------------------------------------------ cases

    @ParameterizedTest(name = "LOGIN-PG2 fixture self-check (HANA H2): {0} returns rows for some variant and none for another (the filters are exercised)")
    @MethodSource("ids")
    void hana_h2_no_es_vacio(String id) throws Exception {
        int conFilas = 0, sinFilas = 0;
        for (Map<String, Object> v : variantes(id)) {
            if (ejecutar(hana, hanaSql(id), v).filas().isEmpty()) sinFilas++;
            else conFilas++;
        }
        assertThat(conFilas).as("variants of " + id + " with rows").isGreaterThan(0);
        if (variantes(id).size() > 1) assertThat(sinFilas).as("variants of " + id + " that return no row").isGreaterThan(0);
    }

    @ParameterizedTest(name = "LOGIN-PG2 fixture self-check (HANA H2): the filters of {0} leave rows OUT of the answer (a statement that lost its WHERE would show)")
    @MethodSource("ids")
    void el_filtro_deja_filas_fuera(String id) throws Exception {
        String vista = LoginPgRecursos.objetosHanaDe(hanaSql(id)).stream().filter(o -> XLSX.objeto(o).tipo().equals("vista")).findFirst().orElseThrow();
        if (id.equals("GestionClaveServicio.SQL_DATOS_RECUPERACION")) return;   // TOP 1 over joins: the variants with no match are checked by hana_h2_no_es_vacio
        int total;
        try (Statement st = hana.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"" + vista + "\"")) {
            rs.next();
            total = rs.getInt(1);
        }
        for (Map<String, Object> v : variantes(id)) {
            if (!ejecutar(hana, hanaSql(id), v).filas().isEmpty()) {
                assertThat(ejecutar(hana, hanaSql(id), v).filas().size()).as(id + " " + v + " rows returned vs rows in the view").isLessThan(total);
                return;
            }
        }
        fail("no variant of " + id + " returns rows");
    }

    @Test
    @DisplayName("LOGIN-PG2 fixture self-check: the Postgres-shaped stand-ins hold the same rows as the HANA-shaped ones, built from the oracles, and the views are named as in the catalog")
    void los_dos_esquemas_tienen_las_mismas_filas() throws Exception {
        for (JsonElement e : fixture.getAsJsonArray("objetos")) {
            JsonObject o = e.getAsJsonObject();
            String h = o.get("hana").getAsString();
            try (Statement a = hana.createStatement(); Statement b = pg.createStatement();
                    ResultSet ra = a.executeQuery("SELECT COUNT(*) FROM \"" + h + "\""); ResultSet rb = b.executeQuery("SELECT COUNT(*) FROM " + XLSX.objeto(h).postgres())) {
                ra.next();
                rb.next();
                assertEquals(o.getAsJsonArray("filas").size(), ra.getInt(1), h);
                assertEquals(ra.getInt(1), rb.getInt(1), h);
            }
        }
    }

    @Test
    @DisplayName("LOGIN-PG2 fixture sensitivity: with the ordering Postgres gives by default (NULL last) SQL_ACCESOS would NOT come out in the HANA order, so the NULLS FIRST of the Postgres text is what the comparison pins")
    void el_orden_de_nulos_distingue() throws Exception {
        Map<String, Object> p = Map.of("IdEmpresa", 1L, "IdUsuario", 10L);
        Salida h = ejecutar(hana, hanaSql("BootstrapServicio.SQL_ACCESOS"), p);
        Salida sinNulls = ejecutar(pg, "SELECT id_elemento_html AS \"IdElementoHtml\" FROM v_pla_acc_consultar_usuario_acceso WHERE id_empresa = :IdEmpresa AND id_usuario = :IdUsuario "
                + "AND mostrar_acceso = 'S' ORDER BY padre, orden", p);
        Salida conNulls = ejecutar(pg, "SELECT id_elemento_html AS \"IdElementoHtml\" FROM v_pla_acc_consultar_usuario_acceso WHERE id_empresa = :IdEmpresa AND id_usuario = :IdUsuario "
                + "AND mostrar_acceso = 'S' ORDER BY padre NULLS FIRST, orden NULLS FIRST", p);
        List<String> hanaOrden = proyectar(h, List.of("IdElementoHtml")).filas().stream().map(f -> f.get(0)).toList();
        assertEquals(List.of("menuSinOrden", "menuInicio", "menuOrdenes", "menuRaiz2", "btnCrear", "btnAnular"), hanaOrden);
        assertEquals(hanaOrden, conNulls.filas().stream().map(f -> f.get(0)).toList());
        assertThat(sinNulls.filas().stream().map(f -> f.get(0)).toList()).isNotEqualTo(hanaOrden);
    }

    @ParameterizedTest(name = "LOGIN-PG2 result equivalence {0}: the Postgres text on H2-PostgreSQL gives the same labels and rows as the HANA text, for every variant of the parameters")
    @MethodSource("ids")
    void postgres_da_lo_mismo_que_hana(String id) throws Exception {
        String sqlPg = ApiLoginPg3.textoPg(id);
        String sqlHana = hanaSql(id);
        for (Map<String, Object> v : variantes(id)) {
            Salida h = ejecutar(hana, sqlHana, v);
            Salida p = ejecutar(pg, sqlPg, v);
            EJECUTADOS.incrementAndGet();
            // SQL_ACCESOS: HANA's SELECT * returns every column of the view; the Postgres text lists the ten the POJO reads
            if (id.equals("BootstrapServicio.SQL_ACCESOS")) h = proyectar(h, p.etiquetas());
            assertEquals(h.etiquetas(), p.etiquetas(), "column labels of " + id + " " + v + " (must be the HANA names)");
            List<List<String>> fh = new ArrayList<>(h.filas());
            List<List<String>> fp = new ArrayList<>(p.filas());
            if (!sqlHana.toLowerCase().contains("order by")) {
                fh.sort(Collections.reverseOrder(java.util.Comparator.comparing(Object::toString)));
                fp.sort(Collections.reverseOrder(java.util.Comparator.comparing(Object::toString)));
            }
            assertEquals(fh, fp, "rows of " + id + " " + v);
        }
    }

    @Test
    @DisplayName("LOGIN-PG2 the Postgres text of every one of the six is accepted by H2 MODE=PostgreSQL (prepared against the stand-ins, parameters as ?)")
    void h2_postgresql_acepta_el_texto() throws Exception {
        Map<String, Integer> contados = new HashMap<>();
        for (String id : (Iterable<String>) ids()::iterator) {
            Preparada p = preparar(ApiLoginPg3.textoPg(id), variantes(id).get(0));
            try (PreparedStatement st = pg.prepareStatement(p.sql())) {
                contados.put(id, st.getParameterMetaData().getParameterCount());
            }
            assertEquals(p.valores().size(), contados.get(id), id);
        }
        assertEquals(6, contados.size());
    }

    @Test
    @DisplayName("LOGIN-PG2 view equivalence cases executed: 19 variants of the 6 statements ran in both dialects (zero would mean not proven)")
    void todos_los_casos_se_ejecutaron() throws Exception {
        int antes = EJECUTADOS.get();
        int n = 0;
        for (String id : (Iterable<String>) ids()::iterator) {
            postgres_da_lo_mismo_que_hana(id);
            n += variantes(id).size();
        }
        assertEquals(19, n, "variants");
        assertEquals(19, EJECUTADOS.get() - antes, "equivalence cases executed");
    }

    @Test
    @DisplayName("LOGIN-PG2 binary and text values survive: the logo bytes 0102ff and the label case of SQL_IMAGEN come out identical (bytea vs VARBINARY), and the lower-case name is not matched")
    void imagen_bytes() throws Exception {
        Salida p = ejecutar(pg, ApiLoginPg3.textoPg("BootstrapServicio.SQL_IMAGEN"), Map.of());
        assertEquals(List.of("NombreImagen", "Binario"), p.etiquetas());
        assertEquals(List.of(List.of("LOGO_CORREDOR_LOGIN", "0102ff")), p.filas());
    }
}
