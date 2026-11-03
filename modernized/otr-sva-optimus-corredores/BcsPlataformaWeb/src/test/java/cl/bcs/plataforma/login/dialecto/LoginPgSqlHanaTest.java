package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * T-31 / T-35 HANA regression, the safety net of the whole change: in HANA mode the production code must send byte-identical SQL.
 * <ul>
 *   <li>Part A (GREEN today, must stay green): the goldens in {@code loginpg/golden-hana} equal what the CURRENT code holds in its constants and sends
 *       to JDBC in five flows. They were recorded from the code BEFORE the dialect layer; this proves the goldens are the code's behaviour.</li>
 *   <li>Part B (RED until T-31): the statement resources of the HANA dialect produce exactly those texts, one by one.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginPgSqlHanaTest {

    private Map<String, String> constantes;
    private Map<String, List<String>> flujos;

    @BeforeAll
    void capturar() throws Exception {
        LoginPgGrabar.siHaySolicitud();
        constantes = LoginPgFlujos.capturarConstantes();
        flujos = LoginPgFlujos.capturarFlujos();
    }

    Stream<String> ids() {
        return LoginPgRecursos.goldenSql().keySet().stream();
    }

    Stream<String> idsSentencias() {
        return LoginPgRecursos.goldenSentencias().keySet().stream();
    }

    Stream<String> nombresFlujo() {
        return LoginPgRecursos.goldenFlujos().keySet().stream();
    }

    // ------------------------------------------------------------------------------------------------ Part A: goldens vs current code (green)

    @Test
    @DisplayName("T-31 golden covers exactly the SQL constants of the six classes: none added, none removed")
    void golden_cubre_exactamente_las_constantes() {
        Set<String> golden = LoginPgRecursos.goldenSql().keySet();
        assertThat(constantes.keySet()).containsExactlyInAnyOrderElementsOf(golden);
        assertEquals(55, golden.size(), "number of SQL / object-name constants pinned (measured on the code before T-31)");
    }

    @ParameterizedTest(name = "golden text of {0} equals the constant of the current code")
    @MethodSource("ids")
    void golden_igual_a_la_constante(String id) {
        assertEquals(LoginPgRecursos.goldenSql().get(id), constantes.get(id), "text of " + id);
    }

    @ParameterizedTest(name = "flow {0}: the SQL the code sends equals the golden, text by text and in order")
    @MethodSource("nombresFlujo")
    void flujo_envia_lo_mismo_que_el_golden(String flujo) {
        assertThat(flujos.get(flujo)).as("texts sent in flow " + flujo).containsExactlyElementsOf(LoginPgRecursos.goldenFlujos().get(flujo));
    }

    @Test
    @DisplayName("T-31 the flows exercise at least the pinned number of constants (the floor cannot shrink silently)")
    void los_flujos_cubren_un_piso_de_sentencias() {
        Set<String> cubiertas = new LinkedHashSet<>();
        Set<String> enviadas = new LinkedHashSet<>();
        flujos.values().forEach(enviadas::addAll);
        for (Map.Entry<String, String> e : LoginPgRecursos.goldenSentencias().entrySet()) {
            if (enviadas.contains(comoSeEnvia(e.getValue()))) cubiertas.add(e.getKey());
        }
        System.out.println("LOGINPG statements exercised by the flows: " + cubiertas.size() + " of " + LoginPgRecursos.goldenSentencias().size()
                + "; not exercised: " + sinCubrir(cubiertas));
        assertThat(cubiertas.size()).as("statements reached by the five flows").isGreaterThanOrEqualTo(PISO_FLUJOS);
    }

    static final int PISO_FLUJOS = 33;

    private static List<String> sinCubrir(Set<String> cubiertas) {
        List<String> l = new ArrayList<>(LoginPgRecursos.goldenSentencias().keySet());
        l.removeAll(cubiertas);
        return l;
    }

    /** the text as SqlNombrado hands it to JDBC: {@code :name} -> {@code ?}, trailing ';' removed (comments never occur in these statements) */
    static String comoSeEnvia(String sql) {
        StringBuilder sb = new StringBuilder();
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
                sb.append('?');
                i = j - 1;
                continue;
            }
            sb.append(c);
        }
        String t = sb.toString().stripTrailing();
        return t.endsWith(";") ? t.substring(0, t.length() - 1) : t;
    }

    // ------------------------------------------------------------------------------------------------ Part B: the HANA dialect resources (red)

    @ParameterizedTest(name = "T-31 hana dialect: texto({0}) is byte-identical to the golden")
    @MethodSource("ids")
    void hana_envia_texto_identico(String id) {
        Object catalogo = ApiLoginPg.catalogoDeRecursos();
        Object sentencias = ApiLoginPg.sentencias(ApiLoginPg.hana(catalogo));
        assertEquals(LoginPgRecursos.goldenSql().get(id), ApiLoginPg.texto(sentencias, id), "HANA text of " + id);
    }

    @Test
    @DisplayName("T-31 the statement catalog holds exactly the golden ids (every statement of the six classes lives in a resource) in both dialects")
    void ids_de_sentencias_son_los_del_golden() {
        Object catalogo = ApiLoginPg.catalogoDeRecursos();
        Set<String> golden = LoginPgRecursos.goldenSql().keySet();
        assertThat(ApiLoginPg.ids(ApiLoginPg.sentencias(ApiLoginPg.hana(catalogo)))).containsExactlyInAnyOrderElementsOf(golden);
        assertThat(ApiLoginPg.ids(ApiLoginPg.sentencias(ApiLoginPg.postgres(catalogo)))).containsExactlyInAnyOrderElementsOf(golden);
    }

    @ParameterizedTest(name = "T-31 {0} lives in a statement resource (sql/comun or sql/hana)")
    @MethodSource("idsSentencias")
    void cada_sentencia_esta_en_un_recurso(String id) {
        boolean comun = LoginPgRecursos.hay("/sql/comun/" + id + ".sql");
        boolean hana = LoginPgRecursos.hay("/sql/hana/" + id + ".sql");
        if (!comun && !hana) {
            throw new AssertionError("LOGIN-PG not implemented: no statement resource /sql/comun/" + id + ".sql or /sql/hana/" + id + ".sql yet");
        }
        assertTrue(comun ^ hana || (comun && hana), "resource present");
    }

    @Test
    @DisplayName("T-31 the inline NEXTVAL statement of AuditoriaSesion.siguiente is the HANA golden when composed with the dialect functions")
    void select_de_secuencia_compuesto_con_el_dialecto_hana() {
        Object d = ApiLoginPg.hana(ApiLoginPg.catalogoDeRecursos());
        String compuesto = ApiLoginPg.f(d, "seleccionarSinTabla", ApiLoginPg.f(d, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::Log") + " AS \"Id\"");
        assertNotNull(flujos.get("login-exito-dos-veces-y-filtro"));
        String enviado = "SELECT \"BSCL.Plataforma.BD.Secuencias::Log\".NEXTVAL AS \"Id\" FROM DUMMY";
        assertTrue(LoginPgRecursos.goldenFlujos().get("login-exito-dos-veces-y-filtro").contains(enviado), "the golden flow contains the inline statement");
        assertEquals(enviado, compuesto);
    }

    @Test
    @DisplayName("T-31 QUERY_RELOJ_UTC composed with the dialect functions is the golden text in HANA")
    void reloj_utc_compuesto() {
        Object d = ApiLoginPg.hana(ApiLoginPg.catalogoDeRecursos());
        String compuesto = ApiLoginPg.f(d, "seleccionarSinTabla", ApiLoginPg.f(d, "ahoraUtc") + " as \"UTC\"");
        assertEquals(LoginPgRecursos.goldenSql().get("CrearSesionServicio.QUERY_RELOJ_UTC"), compuesto);
    }

    @Test
    @DisplayName("T-31 stored procedure call syntax (ProcedureDispatcherService) is pinned in the golden: CALL with BSCL.\"ruta\" and a ? per parameter")
    void sintaxis_de_llamada_de_procedimiento() {
        List<String> llamada = LoginPgRecursos.goldenFlujos().get("procedimiento-llamada");
        assertNotNull(llamada);
        assertThat(llamada).contains("{CALL BSCL.\"BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin\"(?,?,?,?)}",
                "{CALL BSCL.\"BSCL.Plataforma.BD.Utilitarios::Sin\"()}");
        if (llamada.stream().noneMatch(s -> s.contains("SYS.PROCEDURE_PARAMETERS"))) fail("introspection query missing in the golden");
    }
}
