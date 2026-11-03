package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * T-32 the equivalence catalog. The ORACLE (loginpg/oraculo-*.csv) is derived from resumen_exportacion.xlsx by
 * analysis/otr-sva-optimus-corredores/equivalence/BcsPlataformaWeb/xlsx_a_oraculo.py, independent of the production CSV.
 * Part A (green, self-check of the oracle against the code and itself); Part B (red until T-32): the production CSV resources and
 * {@code CatalogoEquivalencias} against the oracle; Part C (red until T-32): the behaviour on CSV text built in the test (NO_DISPONIBLE, duplicates,
 * unknown names, format).
 */
class LoginPgCatalogoTest {

    private static final LoginPgRecursos.Oraculo ORACULO = LoginPgRecursos.Oraculo.cargar();
    private static final Pattern PG_PLANO = Pattern.compile("[a-z][a-z0-9_]*");

    static Stream<String> tablasLogin() {
        return ORACULO.alcance.stream().filter(o -> o.tipo().equals("tabla")).map(LoginPgRecursos.Objeto::hana);
    }

    static Stream<String> objetosLogin() {
        return ORACULO.alcance.stream().map(LoginPgRecursos.Objeto::hana);
    }

    // ================================================================================================ Part A: the oracle (green)

    @Test
    @DisplayName("T-32 oracle integrity: the CSV fixtures hash to the values the generator recorded (line endings ignored)")
    void integridad_del_oraculo() throws Exception {
        JsonObject m = JsonParser.parseString(LoginPgRecursos.texto("/loginpg/ORACULO_MANIFEST.json")).getAsJsonObject();
        JsonObject hashes = m.getAsJsonObject("sha256");
        for (String f : List.of("oraculo-objetos.csv", "oraculo-columnas.csv", "login-alcance.txt")) {
            String t = LoginPgRecursos.texto("/loginpg/" + f).replace("\r", "");
            String h = java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(t.getBytes(StandardCharsets.UTF_8)));
            assertEquals(hashes.get(f).getAsString(), h, "sha256 of " + f);
        }
        assertEquals(365, m.get("filas_objetos").getAsInt());
        assertEquals(1722, m.get("filas_columnas").getAsInt());
    }

    @Test
    @DisplayName("T-32 oracle shape: 365 objects (142 tables, 21 views, 10 procedures, 1 function, 8 sequences, FKs, triggers), 142 tables with columns, 1722 columns")
    void forma_del_oraculo() {
        assertEquals(365, ORACULO.objetos.size());
        Map<String, Integer> porTipo = new java.util.TreeMap<>();
        ORACULO.objetos.values().forEach(o -> porTipo.merge(o.tipo(), 1, Integer::sum));
        assertEquals(Map.of("tabla", 142, "vista", 21, "procedimiento", 10, "funcion", 1, "secuencia", 8, "fk", 102, "trigger", 81), porTipo);
        assertEquals(142, ORACULO.columnas.size());
        assertEquals(1722, ORACULO.columnas.values().stream().mapToInt(Map::size).sum());
    }

    @Test
    @DisplayName("T-32 the 55 HANA names the production code cites are exactly the scope file: 38 tables, 7 views, 8 procedures, 2 sequences (TODO_MVP 10.5 says 57 counting procedures built in parts)")
    void alcance_igual_a_lo_que_cita_el_codigo() {
        Set<String> enCodigo = LoginPgRecursos.nombresHanaEnProduccion();
        Set<String> enAlcance = new TreeSet<>();
        ORACULO.alcance.forEach(o -> enAlcance.add(o.hana()));
        assertThat(enCodigo).containsExactlyElementsOf(enAlcance);
        Map<String, Integer> porTipo = new java.util.TreeMap<>();
        ORACULO.alcance.forEach(o -> porTipo.merge(o.tipo(), 1, Integer::sum));
        assertEquals(Map.of("tabla", 38, "vista", 7, "procedimiento", 8, "secuencia", 2), porTipo);
    }

    @ParameterizedTest(name = "T-32 oracle: {0} is in the xlsx with the type and Postgres name of the scope file")
    @MethodSource("objetosLogin")
    void alcance_esta_en_el_oraculo(String hana) {
        LoginPgRecursos.Objeto alcance = ORACULO.alcance.stream().filter(o -> o.hana().equals(hana)).findFirst().orElseThrow();
        assertEquals(alcance, ORACULO.objeto(hana));
    }

    @Test
    @DisplayName("T-32 oracle format: every Postgres name is flat lower snake_case without schema, with the prefix of its type (t_ v_ sq_; sp_ or fn_ for procedures)")
    void formato_de_nombres_postgres() {
        Map<String, String> prefijo = Map.of("tabla", "t_", "vista", "v_", "secuencia", "sq_");
        Set<String> vistos = new HashSet<>();
        for (LoginPgRecursos.Objeto o : ORACULO.objetos.values()) {
            if (!Set.of("tabla", "vista", "secuencia", "procedimiento", "funcion").contains(o.tipo())) continue;
            assertTrue(PG_PLANO.matcher(o.postgres()).matches(), "flat snake_case: " + o);
            assertTrue(vistos.add(o.postgres()), "unique Postgres name: " + o);
            if (prefijo.containsKey(o.tipo())) {
                assertTrue(o.postgres().startsWith(prefijo.get(o.tipo())), "prefix of its type: " + o);
            } else {
                assertTrue(o.postgres().startsWith("sp_") || o.postgres().startsWith("fn_"), "procedure/function prefix: " + o);
            }
        }
    }

    @Test
    @DisplayName("T-32 finding: ConsultarFormatos is typed 'procedimiento' in the sheet but its Postgres name is a FUNCTION (fn_pla_consultar_formatos); the other 7 login procedures are sp_")
    void consultar_formatos_es_funcion_aunque_la_hoja_diga_procedimiento() {
        LoginPgRecursos.Objeto o = ORACULO.objeto("BSCL.Plataforma.BD.Servicios::ConsultarFormatos");
        assertEquals("procedimiento", o.tipo());
        assertEquals("fn_pla_consultar_formatos", o.postgres());
        long sp = ORACULO.alcance.stream().filter(x -> x.tipo().equals("procedimiento") && x.postgres().startsWith("sp_")).count();
        assertEquals(7, sp);
    }

    @Test
    @DisplayName("T-32 the xlsx has NO duplicate keys (no repeated nombre_hana, no repeated table+column, no repeated Postgres name) and no NO_DISPONIBLE mark")
    void el_xlsx_no_trae_duplicados_ni_marcas() {
        List<List<String>> obj = LoginPgRecursos.csv(LoginPgRecursos.texto("/loginpg/oraculo-objetos.csv"));
        Set<String> claves = new HashSet<>();
        for (List<String> f : obj.subList(1, obj.size())) {
            assertTrue(claves.add(f.get(1)), "duplicate nombre_hana " + f);
            assertFalse(f.get(2).equals("NO_DISPONIBLE") || f.get(2).isEmpty(), "unexpected mark " + f);
        }
        List<List<String>> col = LoginPgRecursos.csv(LoginPgRecursos.texto("/loginpg/oraculo-columnas.csv"));
        Set<String> ck = new HashSet<>();
        for (List<String> f : col.subList(1, col.size())) {
            assertTrue(ck.add(f.get(0) + "|" + f.get(2)), "duplicate column key " + f);
        }
        JsonObject m = JsonParser.parseString(LoginPgRecursos.texto("/loginpg/ORACULO_MANIFEST.json")).getAsJsonObject();
        assertThat(m.getAsJsonArray("hallazgos").toString()).doesNotContain("repetida");
    }

    @Test
    @DisplayName("T-32 both sheets agree: the Postgres table name of every column row is the one of its object row; each of the 142 tables has its columns, no view has any")
    void las_dos_hojas_coinciden() {
        for (Map.Entry<String, String> e : ORACULO.tablaPgDeColumnas.entrySet()) {
            assertEquals(ORACULO.objeto(e.getKey()).postgres(), e.getValue(), "table name in the column sheet for " + e.getKey());
            assertEquals("tabla", ORACULO.objeto(e.getKey()).tipo());
        }
        ORACULO.objetos.values().stream().filter(o -> o.tipo().equals("tabla")).forEach(o -> assertTrue(ORACULO.columnas.containsKey(o.hana()), "columns of " + o.hana()));
        ORACULO.objetos.values().stream().filter(o -> o.tipo().equals("vista")).forEach(o -> assertFalse(ORACULO.columnas.containsKey(o.hana()), "a view has columns: " + o.hana()));
        for (Map<String, String> cols : ORACULO.columnas.values()) {
            assertEquals(cols.size(), new HashSet<>(cols.values()).size(), "Postgres column names unique within a table");
            cols.values().forEach(c -> assertTrue(PG_PLANO.matcher(c).matches(), "column format " + c));
        }
    }

    @Test
    @DisplayName("T-32 a fixture that is missing FAILS (message FIXTURE MISSING), it is never skipped")
    void fixture_ausente_falla() {
        assertThatThrownBy(() -> LoginPgRecursos.texto("/loginpg/no-existe.csv")).isInstanceOf(AssertionError.class).hasMessageContaining("FIXTURE MISSING");
        assertThatThrownBy(() -> LoginPgRecursos.texto("/loginpg/golden-hana/Clase.NoExiste.sql")).isInstanceOf(AssertionError.class).hasMessageContaining("FIXTURE MISSING");
    }

    // ================================================================================================ Part B: production CSV and catalog vs oracle (red)

    private static List<Map<String, String>> filasPorCabecera(String csv, List<String> obligatorias) {
        List<List<String>> filas = LoginPgRecursos.csv(csv.startsWith("﻿") ? csv.substring(1) : csv);
        List<String> cab = filas.get(0);
        for (String c : obligatorias) assertThat(cab).as("header of the CSV").contains(c);
        List<Map<String, String>> r = new ArrayList<>();
        for (List<String> f : filas.subList(1, filas.size())) {
            Map<String, String> m = new LinkedHashMap<>();
            for (int i = 0; i < cab.size(); i++) m.put(cab.get(i), i < f.size() ? f.get(i) : "");
            r.add(m);
        }
        return r;
    }

    private static List<Map<String, String>> objetosProduccion() {
        return filasPorCabecera(LoginPgRecursos.textoProduccion(ApiLoginPg.RECURSO_OBJETOS), List.of("tipo_objeto", "nombre_hana", "nombre_postgres"));
    }

    private static List<Map<String, String>> columnasProduccion() {
        return filasPorCabecera(LoginPgRecursos.textoProduccion(ApiLoginPg.RECURSO_COLUMNAS),
                List.of("nombre_tabla_hana", "nombre_tabla_postgresql", "nombre_columna_hana", "nombre_columna_postgresql"));
    }

    @Test
    @DisplayName("T-32 production objects CSV: every one of the 55 objects the code cites has the oracle's type and Postgres name (none marked NO_DISPONIBLE today)")
    void csv_de_objetos_cubre_el_alcance() {
        Map<String, Map<String, String>> porHana = new HashMap<>();
        for (Map<String, String> f : objetosProduccion()) porHana.put(f.get("nombre_hana"), f);
        List<String> distintos = new ArrayList<>();
        for (LoginPgRecursos.Objeto o : ORACULO.alcance) {
            Map<String, String> f = porHana.get(o.hana());
            if (f == null) {
                distintos.add("MISSING " + o.hana());
            } else if (!f.get("tipo_objeto").equals(o.tipo()) || !f.get("nombre_postgres").equals(o.postgres())) {
                distintos.add("DIFFERENT " + o.hana() + " production=" + f.get("tipo_objeto") + "/" + f.get("nombre_postgres") + " oracle=" + o.tipo() + "/" + o.postgres());
            }
        }
        assertThat(distintos).as("login objects missing or different in equivalencias-objetos.csv").isEmpty();
    }

    @Test
    @DisplayName("T-32 production objects CSV invents nothing: every row equals the oracle's, except that a name may be NO_DISPONIBLE; no repeated key")
    void csv_de_objetos_no_inventa_nada() {
        Set<String> claves = new HashSet<>();
        for (Map<String, String> f : objetosProduccion()) {
            assertTrue(claves.add(f.get("nombre_hana")), "duplicate key " + f.get("nombre_hana"));
            LoginPgRecursos.Objeto o = ORACULO.objetos.get(f.get("nombre_hana"));
            assertThat(o).as("object not in the xlsx: " + f.get("nombre_hana")).isNotNull();
            assertEquals(o.tipo(), f.get("tipo_objeto"), "type of " + o.hana());
            if (!f.get("nombre_postgres").equals("NO_DISPONIBLE")) {
                assertEquals(o.postgres(), f.get("nombre_postgres"), "Postgres name of " + o.hana());
            }
        }
    }

    @Test
    @DisplayName("T-32 production columns CSV: for each of the 38 login tables the table name and every column pair equal the oracle's, no missing and no extra; no repeated key")
    void csv_de_columnas_de_las_tablas_del_login() {
        Map<String, Map<String, String>> porTabla = new LinkedHashMap<>();
        Map<String, String> tablaPg = new HashMap<>();
        Set<String> claves = new HashSet<>();
        for (Map<String, String> f : columnasProduccion()) {
            assertTrue(claves.add(f.get("nombre_tabla_hana") + "|" + f.get("nombre_columna_hana")), "duplicate key " + f);
            porTabla.computeIfAbsent(f.get("nombre_tabla_hana"), k -> new LinkedHashMap<>()).put(f.get("nombre_columna_hana"), f.get("nombre_columna_postgresql"));
            tablaPg.put(f.get("nombre_tabla_hana"), f.get("nombre_tabla_postgresql"));
        }
        List<String> problemas = new ArrayList<>();
        tablasLogin().forEach(t -> {
            if (!porTabla.containsKey(t)) {
                problemas.add("NO COLUMNS " + t);
                return;
            }
            if (!ORACULO.tablaPgDeColumnas.get(t).equals(tablaPg.get(t))) problemas.add("TABLE NAME " + t + " " + tablaPg.get(t));
            if (!ORACULO.columnas.get(t).equals(porTabla.get(t))) problemas.add("COLUMNS DIFFER " + t);
        });
        assertThat(problemas).isEmpty();
        // and nothing outside the xlsx
        porTabla.forEach((t, cols) -> assertThat(ORACULO.columnas.get(t)).as("table " + t + " must be in the xlsx").isNotNull().containsAllEntriesOf(cols));
    }

    @ParameterizedTest(name = "T-32 CatalogoEquivalencias: every column of {0} resolves to exactly the xlsx value")
    @MethodSource("tablasLogin")
    void catalogo_resuelve_las_columnas_de_cada_tabla(String tabla) {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        assertEquals(ORACULO.objeto(tabla).postgres(), ApiLoginPg.nombrePostgres(cat, tabla));
        Map<String, String> esperado = ORACULO.columnas.get(tabla);
        assertThat(esperado).isNotEmpty();
        esperado.forEach((hana, pg) -> assertEquals(pg, ApiLoginPg.columnaPostgres(cat, tabla, hana), tabla + "." + hana));
    }

    @ParameterizedTest(name = "T-32 CatalogoEquivalencias: {0} resolves to its type and Postgres name")
    @MethodSource("objetosLogin")
    void catalogo_resuelve_cada_objeto_del_login(String hana) {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        LoginPgRecursos.Objeto o = ORACULO.objeto(hana);
        assertEquals(o.postgres(), ApiLoginPg.nombrePostgres(cat, hana));
        assertEquals(o.tipo(), ApiLoginPg.tipo(cat, hana));
        assertTrue(ApiLoginPg.disponible(cat, hana));
        assertThat(ApiLoginPg.objetosHana(cat)).contains(hana);
    }

    @Test
    @DisplayName("T-32 tokens: the object token {ACC.Usuario} finds the full HANA name for each of the 45 tables and views of the scope (the part after :: is unique)")
    void fichas_de_objeto() {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        int n = 0;
        for (LoginPgRecursos.Objeto o : ORACULO.alcance) {
            if (!o.tipo().equals("tabla") && !o.tipo().equals("vista")) continue;
            String ficha = o.hana().substring(o.hana().indexOf("::") + 2);
            assertEquals(o.hana(), ApiLoginPg.hanaDeFicha(cat, ficha), "token " + ficha);
            n++;
        }
        assertEquals(45, n);
        assertThatThrownBy(() -> ApiLoginPg.hanaDeFicha(cat, "ACC.NoExiste")).isInstanceOf(RuntimeException.class).hasMessageContaining("ACC.NoExiste");
    }

    @Test
    @DisplayName("T-32 names are matched exactly: another letter case, surrounding spaces, no schema or a Postgres name are NOT the HANA name (answer: object not available)")
    void formato_y_mayusculas_de_los_nombres() {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        for (String malo : List.of("bscl.plataforma.bd::acc.usuario", " BSCL.Plataforma.BD::ACC.Usuario", "BSCL.Plataforma.BD::ACC.Usuario ", "ACC.Usuario", "t_pla_acc_usuario",
                "\"BSCL.Plataforma.BD::ACC.Usuario\"", "")) {
            assertThatThrownBy(() -> ApiLoginPg.nombrePostgres(cat, malo)).as("name [" + malo + "]").satisfies(e -> assertTrue(ApiLoginPg.esNoDisponible(e), e.toString()));
            assertFalse(ApiLoginPg.disponible(cat, malo), "[" + malo + "]");
        }
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.Plataforma.BD::ACC.Usuario", "idempresa")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("idempresa");
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.Plataforma.BD::ACC.Usuario", "id_empresa")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("T-32 the columns of a view are not in the catalog (the sheet has none): asking for one fails with a message that names the view (gap Q-LOGINPG-1)")
    void columnas_de_vistas_aun_no_existen() {
        Object cat = ApiLoginPg.catalogoDeRecursos();
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa", "IdEmpresa")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PLA.ConsultarEmpresa");
    }

    // ================================================================================================ Part C: behaviour on CSV text built here (red)

    private static final String OBJ_CAB = "tipo_objeto,nombre_hana,nombre_postgres,dominio,modulo_backend\n";
    private static final String COL_CAB = "nombre_tabla_hana,nombre_tabla_postgresql,nombre_columna_hana,nombre_columna_postgresql\n";
    private static final String COLS_TABLA = COL_CAB + "BSCL.X.BD::A.Uno,t_x_a_uno,IdUno,id_uno\nBSCL.X.BD::A.Uno,t_x_a_uno,Nombre,nombre\n";

    @Test
    @DisplayName("T-32 NO_DISPONIBLE: the object exists in the catalog, is not available, and asking its Postgres name answers the 503 exception naming the HANA object; its HANA name still resolves")
    void no_disponible() {
        Object cat = ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,NO_DISPONIBLE,X,M\ntabla,BSCL.X.BD::A.Dos,t_x_a_dos,X,M\n", COLS_TABLA);
        assertFalse(ApiLoginPg.disponible(cat, "BSCL.X.BD::A.Uno"));
        assertTrue(ApiLoginPg.disponible(cat, "BSCL.X.BD::A.Dos"));
        assertEquals("tabla", ApiLoginPg.tipo(cat, "BSCL.X.BD::A.Uno"));
        assertThatThrownBy(() -> ApiLoginPg.nombrePostgres(cat, "BSCL.X.BD::A.Uno")).satisfies(e -> {
            assertTrue(ApiLoginPg.esNoDisponible(e), e.toString());
            assertThat(e.getMessage()).contains("BSCL.X.BD::A.Uno");
        });
        assertEquals("t_x_a_dos", ApiLoginPg.nombrePostgres(cat, "BSCL.X.BD::A.Dos"));
        assertThat(ApiLoginPg.objetosHana(cat)).containsExactlyInAnyOrder("BSCL.X.BD::A.Uno", "BSCL.X.BD::A.Dos");
        assertEquals("tabla", ApiLoginPg.tipo(cat, "BSCL.X.BD::A.Dos"));
        assertThat(ApiLoginPg.hanaDeFicha(cat, "A.Uno")).isEqualTo("BSCL.X.BD::A.Uno");
    }

    @Test
    @DisplayName("T-32 an object absent from the catalog and an object with an empty Postgres name are as unavailable as NO_DISPONIBLE (A-6)")
    void ausente_y_vacio_son_no_disponibles() {
        Object cat = ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Vacio,,X,M\n", COLS_TABLA);
        assertFalse(ApiLoginPg.disponible(cat, "BSCL.X.BD::A.Vacio"));
        assertFalse(ApiLoginPg.disponible(cat, "BSCL.X.BD::A.Ausente"));
        for (String n : List.of("BSCL.X.BD::A.Vacio", "BSCL.X.BD::A.Ausente")) {
            assertThatThrownBy(() -> ApiLoginPg.nombrePostgres(cat, n)).satisfies(e -> assertTrue(ApiLoginPg.esNoDisponible(e), e.toString()));
        }
    }

    @Test
    @DisplayName("T-32 duplicate keys in the source table: an identical repeated row is tolerated once; the same HANA name with another Postgres name or type is refused, naming the key (A-7)")
    void duplicados() {
        Object cat = ApiLoginPg.catalogoDeCsv(OBJ_CAB + "secuencia,BSCL.X.BD.Secuencias::S,sq_x_s,X,M\nsecuencia,BSCL.X.BD.Secuencias::S,sq_x_s,X,M\n", COL_CAB);
        assertEquals("sq_x_s", ApiLoginPg.nombrePostgres(cat, "BSCL.X.BD.Secuencias::S"));
        assertThat(ApiLoginPg.objetosHana(cat)).hasSize(1);
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv(OBJ_CAB + "secuencia,BSCL.X.BD.Secuencias::S,sq_x_s,X,M\nsecuencia,BSCL.X.BD.Secuencias::S,sq_x_otra,X,M\n", COL_CAB))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("BSCL.X.BD.Secuencias::S").hasMessageContaining("uplic");
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,t_a,X,M\nvista,BSCL.X.BD::A.Uno,t_a,X,M\n", COL_CAB))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("BSCL.X.BD::A.Uno");
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,t_a,X,M\n",
                COLS_TABLA + "BSCL.X.BD::A.Uno,t_x_a_uno,IdUno,otro_id\n")).isInstanceOf(RuntimeException.class).hasMessageContaining("IdUno");
        // two HANA objects must not claim the same Postgres name either
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,t_igual,X,M\ntabla,BSCL.X.BD::A.Dos,t_igual,X,M\n", COL_CAB))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("t_igual");
    }

    @Test
    @DisplayName("T-32 format of the CSV text: CRLF line endings, a UTF-8 byte order mark and quoted cells are accepted; columns are found by header name, extra columns ignored")
    void formato_del_csv() {
        String objetos = "﻿dominio,nombre_postgres,tipo_objeto,nombre_hana\r\nX,\"t_x_a_uno\",tabla,\"BSCL.X.BD::A.Uno\"\r\n";
        Object cat = ApiLoginPg.catalogoDeCsv(objetos, COLS_TABLA.replace("\n", "\r\n"));
        assertEquals("t_x_a_uno", ApiLoginPg.nombrePostgres(cat, "BSCL.X.BD::A.Uno"));
        assertEquals("id_uno", ApiLoginPg.columnaPostgres(cat, "BSCL.X.BD::A.Uno", "IdUno"));
    }

    @Test
    @DisplayName("T-32 a CSV without a required header column is refused, naming the column")
    void cabecera_incompleta() {
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv("tipo_objeto,nombre_hana\ntabla,BSCL.X.BD::A.Uno\n", COL_CAB)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nombre_postgres");
        assertThatThrownBy(() -> ApiLoginPg.catalogoDeCsv(OBJ_CAB, "nombre_tabla_hana,nombre_columna_hana\n")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nombre_columna_postgresql");
    }

    @Test
    @DisplayName("T-32 unknown column and unknown table of a column lookup fail with both names in the message; they are configuration errors, not the 503 exception")
    void columnas_desconocidas() {
        Object cat = ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,t_x_a_uno,X,M\n", COLS_TABLA);
        assertEquals("id_uno", ApiLoginPg.columnaPostgres(cat, "BSCL.X.BD::A.Uno", "IdUno"));
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.X.BD::A.Uno", "NoExiste")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NoExiste").hasMessageContaining("A.Uno").satisfies(e -> assertFalse(ApiLoginPg.esNoDisponible(e)));
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.X.BD::A.Otra", "IdUno")).isInstanceOf(RuntimeException.class).hasMessageContaining("A.Otra");
    }

    @Test
    @DisplayName("T-32 the columns of a table whose object is NO_DISPONIBLE are not served either: the 503 exception, so no statement is built over it")
    void columnas_de_tabla_no_disponible() {
        Object cat = ApiLoginPg.catalogoDeCsv(OBJ_CAB + "tabla,BSCL.X.BD::A.Uno,NO_DISPONIBLE,X,M\n", COLS_TABLA);
        assertThatThrownBy(() -> ApiLoginPg.columnaPostgres(cat, "BSCL.X.BD::A.Uno", "IdUno")).satisfies(e -> assertTrue(ApiLoginPg.esNoDisponible(e), e.toString()));
    }
}
