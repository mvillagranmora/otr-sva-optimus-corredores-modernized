package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A3-3 {@code SentenciasSql(SqlDialecto)}: {@code ids()}, {@code texto(id)}, {@code de(hanaText)}, {@code exigirDisponibles(ids)}, {@code dialecto()}, static
 * {@code hana(id)} and {@code predeterminadas()}; {@code objetosDe} is gone; {@code texto} and {@code exigirDisponibles} are public NON-final. Postgres
 * {@code texto(id)} reads {@code /sql/postgres/<id>.sql}; a missing file is {@link UnsupportedOperationException} naming the id and postgres (this replaces the
 * NO_DISPONIBLE mark: no text = 503, nothing is sent). For the ten ids that are an object NAME ({@code SP_*}, {@code SECUENCIA_*}) the file holds just the
 * Postgres name on one line. The "reads the file" and "missing file" behaviour is proven on the REAL class loaded a second time in a class loader that hides
 * or replaces one resource (LoginPg3Aislado), not with a double.
 */
class LoginPg3SentenciasTest {

    private static final String ID_SQL = "CrearSesionServicio.UPDATE_INTENTOS";
    private static final String ID_NOMBRE = "BootstrapServicio.SP_FECHAS";
    private static final String ID_OTRO = "FiltroSesion.SQL_VALIDA_TOKEN";

    static Map<String, String> nombresEsperados() {
        Map<String, String> m = new LinkedHashMap<>();
        for (String l : LoginPgRecursos.texto("/loginpg3/nombres-postgres-esperados.tsv").split("\n")) {
            if (l.isBlank()) continue;
            String[] p = l.split("\t");
            m.put(p[0], p[1]);
        }
        return m;
    }

    static Stream<String> idsDeNombres() {
        return nombresEsperados().keySet().stream();
    }

    static Stream<String> idsDeSql() {
        return LoginPgRecursos.goldenSentencias().keySet().stream();
    }

    private static String ruta(String id) {
        return "sql/postgres/" + id + ".sql";
    }

    // ------------------------------------------------------------------------------------------------ shape

    @Test
    @DisplayName("A3-3 SentenciasSql keeps ids, texto, de, exigirDisponibles, dialecto, static hana and static predeterminadas; objetosDe is GONE")
    void forma_de_la_clase() throws Exception {
        Set<String> publicos = new TreeSet<>();
        for (Method m : SentenciasSql.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic()) publicos.add(m.getName() + "/" + m.getParameterCount() + (Modifier.isStatic(m.getModifiers()) ? "/static" : ""));
        }
        if (publicos.stream().anyMatch(x -> x.startsWith("objetosDe/"))) throw ApiLoginPg3.catalogoEnProduccion("SentenciasSql still has objetosDe (it read the HANA names to ask the catalog)");
        assertThat(publicos).contains("ids/0", "texto/1", "de/1", "exigirDisponibles/1", "dialecto/0", "hana/1/static", "predeterminadas/0/static");
        assertThat(SentenciasSql.class.getConstructors()).hasSize(1);
        assertThat(SentenciasSql.class.getConstructors()[0].getParameterTypes()).containsExactly(SqlDialecto.class);
    }

    @Test
    @DisplayName("A3-3 texto and exigirDisponibles are public and NOT final, and the class is not final: a test double may override them")
    void se_pueden_sobrescribir() throws Exception {
        assertThat(Modifier.isFinal(SentenciasSql.class.getModifiers())).as("class final").isFalse();
        Method texto = SentenciasSql.class.getMethod("texto", String.class);
        Method exigir = SentenciasSql.class.getMethod("exigirDisponibles", java.util.Collection.class);
        assertThat(Modifier.isFinal(texto.getModifiers())).as("texto final").isFalse();
        assertThat(Modifier.isFinal(exigir.getModifiers())).as("exigirDisponibles final").isFalse();
        // and a subclass that overrides them is honoured by de(): the 503 test double relies on it
        SentenciasSql doble = new LoginPgContexto.SinTextoPostgres(ApiLoginPg3.postgres(), ID_SQL);
        assertThatThrownBy(() -> doble.texto(ID_SQL)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> doble.de(SentenciasSql.hana(ID_SQL))).isInstanceOf(UnsupportedOperationException.class).hasMessageContaining(ID_SQL);
        assertThatThrownBy(() -> doble.exigirDisponibles(List.of(ID_OTRO, ID_SQL))).isInstanceOf(UnsupportedOperationException.class);
        doble.exigirDisponibles(List.of(ID_OTRO));
    }

    @Test
    @DisplayName("A3-3 dialecto() returns the dialect it was built with")
    void dialecto_es_el_dado() {
        SqlDialecto d = ApiLoginPg3.postgres();
        assertThat(ApiLoginPg3.sentencias(d).dialecto()).isSameAs(d);
        SqlDialecto h = ApiLoginPg3.hana();
        assertThat(ApiLoginPg3.sentencias(h).dialecto()).isSameAs(h);
    }

    // ------------------------------------------------------------------------------------------------ Postgres texto reads the file

    @Test
    @DisplayName("A3-3 postgres texto(id) is exactly the text of /sql/postgres/<id>.sql: with that resource replaced by other text in an isolated copy of the class, texto returns the other text")
    void texto_postgres_lee_el_archivo_del_id() throws Exception {
        String otro = "SELECT 1 /* texto de prueba */ FROM t_nada WHERE x = :X";
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(), Map.of(ruta(ID_SQL), otro))) {
            Object s = a.sentencias("postgres");
            assertEquals(otro, LoginPg3Aislado.texto(s, ID_SQL));
            // another statement is not affected by the replacement
            assertEquals(ApiLoginPg3.textoPg(ID_OTRO), LoginPg3Aislado.texto(s, ID_OTRO));
        }
    }

    @Test
    @DisplayName("A3-3 an object-name id in Postgres is the one-line name of its file: replaced by another name (with a line break after it), texto gives that name")
    void texto_de_un_nombre_lee_el_archivo() throws Exception {
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(), Map.of(ruta(ID_NOMBRE), "sp_otro_nombre\n"))) {
            assertEquals("sp_otro_nombre", LoginPg3Aislado.texto(a.sentencias("postgres"), ID_NOMBRE).strip());
        }
    }

    @Test
    @DisplayName("A3-3 a Postgres statement whose file does not exist: UnsupportedOperationException (503) naming the id and postgres; the other statements still answer")
    void archivo_ausente_es_503_con_el_id() throws Exception {
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(ruta(ID_SQL)), Map.of())) {
            Object s = a.sentencias("postgres");
            assertThatThrownBy(() -> LoginPg3Aislado.texto(s, ID_SQL)).isInstanceOf(UnsupportedOperationException.class).hasMessageContaining(ID_SQL)
                    .satisfies(e -> assertThat(e.getMessage().toLowerCase()).contains("postgres"));
            assertEquals(ApiLoginPg3.textoPg(ID_OTRO), LoginPg3Aislado.texto(s, ID_OTRO));
            // asking twice gives the same answer (nothing remembers a text that is not there)
            assertThatThrownBy(() -> LoginPg3Aislado.texto(s, ID_SQL)).isInstanceOf(UnsupportedOperationException.class).hasMessageContaining(ID_SQL);
        }
    }

    @Test
    @DisplayName("A3-3 a missing Postgres file of an object-name id (a procedure, a sequence) is the same 503: no text, nothing is sent")
    void nombre_ausente_es_503() throws Exception {
        for (String id : List.of(ID_NOMBRE, "AuditoriaSesion.SECUENCIA_LOG")) {
            try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(ruta(id)), Map.of())) {
                assertThatThrownBy(() -> LoginPg3Aislado.texto(a.sentencias("postgres"), id)).isInstanceOf(UnsupportedOperationException.class).hasMessageContaining(id);
            }
        }
    }

    @Test
    @DisplayName("A3-3 the HANA dialect never looks at sql/postgres: with every Postgres file hidden, every HANA text is still the golden")
    void hana_no_mira_los_archivos_de_postgres() throws Exception {
        Set<String> todos = new TreeSet<>();
        for (String id : LoginPgRecursos.goldenSql().keySet()) todos.add(ruta(id));
        try (LoginPg3Aislado a = new LoginPg3Aislado(todos, Map.of())) {
            Object s = a.sentencias("hana");
            for (Map.Entry<String, String> e : LoginPgRecursos.goldenSql().entrySet()) assertEquals(e.getValue(), LoginPg3Aislado.texto(s, e.getKey()), e.getKey());
        }
    }

    @Test
    @DisplayName("A3-3 the HANA text is read from /sql/hana/<id>.sql: replaced in an isolated copy, texto gives the replacement")
    void texto_hana_lee_su_archivo() throws Exception {
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(), Map.of("sql/hana/" + ID_SQL + ".sql", "UPDATE X SET Y = 1"))) {
            assertEquals("UPDATE X SET Y = 1", LoginPg3Aislado.texto(a.sentencias("hana"), ID_SQL));
        }
    }

    // ------------------------------------------------------------------------------------------------ the pre-flight

    @Test
    @DisplayName("A3-3 exigirDisponibles(ids) calls texto on each id: the first id with no Postgres text stops it with the 503 naming THAT id; ids not in the list are not asked")
    void exigir_disponibles_llama_a_texto_por_cada_id() throws Exception {
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(ruta(ID_SQL)), Map.of())) {
            Object s = a.sentencias("postgres");
            LoginPg3Aislado.exigirDisponibles(s, List.of(ID_OTRO, ID_NOMBRE));
            LoginPg3Aislado.exigirDisponibles(s, List.of());
            assertThatThrownBy(() -> LoginPg3Aislado.exigirDisponibles(s, List.of(ID_OTRO, ID_SQL, ID_NOMBRE))).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining(ID_SQL);
        }
        try (LoginPg3Aislado a = new LoginPg3Aislado(Set.of(ruta(ID_NOMBRE)), Map.of())) {
            assertThatThrownBy(() -> LoginPg3Aislado.exigirDisponibles(a.sentencias("postgres"), List.of(ID_OTRO, ID_NOMBRE))).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining(ID_NOMBRE);
        }
    }

    @Test
    @DisplayName("A3-3 an id that is not a statement of the index is an IllegalArgumentException from texto and from exigirDisponibles (a programming error, not a 503), in both dialects")
    void id_desconocido() {
        for (SentenciasSql s : List.of(ApiLoginPg3.sentenciasHana(), ApiLoginPg3.sentenciasPostgres())) {
            assertThatThrownBy(() -> s.texto("Clase.NoExiste")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Clase.NoExiste");
            assertThatThrownBy(() -> s.exigirDisponibles(List.of("Clase.NoExiste"))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------------------------------------ de(hanaText)

    @ParameterizedTest(name = "A3-3 de(HANA text of {0}) answers the text of the active dialect of that very statement, in Postgres and in HANA")
    @MethodSource("idsDeSql")
    void de_devuelve_el_texto_del_dialecto(String id) {
        String hana = LoginPgRecursos.goldenSql().get(id);
        assertEquals(hana, ApiLoginPg3.sentenciasHana().de(hana));
        assertEquals(ApiLoginPg3.sentenciasPostgres().texto(id), ApiLoginPg3.sentenciasPostgres().de(hana));
    }

    @Test
    @DisplayName("A3-3 de() also answers for the object-name constants (a procedure name, a sequence name): the Postgres name of that very id")
    void de_de_un_nombre() {
        for (Map.Entry<String, String> e : nombresEsperados().entrySet()) {
            String hana = LoginPgRecursos.goldenSql().get(e.getKey());
            assertEquals(e.getValue(), ApiLoginPg3.sentenciasPostgres().de(hana).strip(), e.getKey());
            assertEquals(hana, ApiLoginPg3.sentenciasHana().de(hana));
        }
    }

    @Test
    @DisplayName("A3-3 de() of a text that is not the HANA text of a statement fails (RuntimeException): no text goes to a database that is not a registered statement")
    void de_de_un_texto_desconocido() {
        assertThatThrownBy(() -> ApiLoginPg3.sentenciasPostgres().de("SELECT 1 FROM DUMMY")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> ApiLoginPg3.sentenciasHana().de("SELECT 1 FROM DUMMY")).isInstanceOf(RuntimeException.class);
    }

    // ------------------------------------------------------------------------------------------------ the ten object names

    @ParameterizedTest(name = "A3-3 sql/postgres/{0}.sql holds just the Postgres name on one line, and texto({0}) is that name")
    @MethodSource("idsDeNombres")
    void el_archivo_de_un_nombre_es_el_nombre(String id) {
        String esperado = nombresEsperados().get(id);
        String texto = ApiLoginPg3.textoPg(id);
        assertEquals(esperado, texto.strip(), "Postgres name of " + id);
        assertThat(texto.strip()).as("one line, no inner whitespace").doesNotContainPattern("\\s");
    }

    @ParameterizedTest(name = "A3-3 the name of {0} is the Postgres name of the xlsx oracle for the HANA name of that id (independent source)")
    @MethodSource("idsDeNombres")
    void el_nombre_es_el_del_oraculo(String id) {
        String hana = LoginPgRecursos.goldenSql().get(id).strip();
        assertEquals(LoginPgRecursos.Oraculo.cargar().objeto(hana).postgres(), ApiLoginPg3.textoPg(id).strip(), id);
    }

    @Test
    @DisplayName("A3-3 the ten object-name ids are exactly the SP_* and SECUENCIA_* ids of the index (8 procedures and 2 sequences), and the 45 others are SQL")
    void los_diez_nombres_son_los_sp_y_secuencia() {
        List<String> sp = new ArrayList<>();
        for (String id : LoginPgRecursos.goldenSql().keySet()) {
            String c = id.substring(id.indexOf('.') + 1);
            if (c.startsWith("SP_") || c.startsWith("SECUENCIA_")) sp.add(id);
        }
        assertThat(sp).containsExactlyInAnyOrderElementsOf(nombresEsperados().keySet());
        assertEquals(10, sp.size());
    }
}
