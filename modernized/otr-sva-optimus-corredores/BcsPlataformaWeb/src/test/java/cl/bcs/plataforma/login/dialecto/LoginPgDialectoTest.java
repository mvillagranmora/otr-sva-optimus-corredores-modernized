package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A3-2 the MINIMAL {@code SqlDialecto}: {@code nombre()}, {@code siguienteValor}, {@code seleccionarSinTabla} and the default {@code exigirHana}, with LITERAL
 * expected text per dialect. Everything the earlier rounds pinned and production never used (objeto, columna, columnaSeleccion, nombreDe, ahoraUtc, primeros,
 * sumarSegundos, fechaATexto, aEntero, recortar, exigirDisponible) must be GONE, and the two implementations have a public no-arg constructor. Replaces
 * LoginPgDialectoFuncionesTest (see TRANSFORMATION_NOTES for what was removed and why).
 */
class LoginPgDialectoTest {

    static final String LOG = "BSCL.Plataforma.BD.Secuencias::Log";
    static final String DETALLE = "BSCL.Plataforma.BD.Secuencias::DetalleLog";

    private static SqlDialecto hana() {
        return ApiLoginPg3.hana();
    }

    private static SqlDialecto pg() {
        return ApiLoginPg3.postgres();
    }

    // ------------------------------------------------------------------------------------------------ shape (A3-2)

    @Test
    @DisplayName("A3-2 the interface has exactly four methods: nombre(), siguienteValor(String), seleccionarSinTabla(String) and the default exigirHana(String); nothing else is left")
    void la_interfaz_es_minima() throws Exception {
        Set<String> metodos = new TreeSet<>();
        for (Method m : SqlDialecto.class.getMethods()) metodos.add(m.getName() + "/" + m.getParameterCount());
        if (!metodos.equals(Set.of("nombre/0", "siguienteValor/1", "seleccionarSinTabla/1", "exigirHana/1"))) {
            throw ApiLoginPg3.catalogoEnProduccion("SqlDialecto still declares " + metodos + " (only nombre, siguienteValor, seleccionarSinTabla and exigirHana may remain)");
        }
        assertThat(SqlDialecto.class.getMethod("exigirHana", String.class).isDefault()).as("exigirHana is a default method").isTrue();
        assertThat(Modifier.isAbstract(SqlDialecto.class.getMethod("nombre").getModifiers())).as("nombre is abstract").isTrue();
        assertThat(Modifier.isAbstract(SqlDialecto.class.getMethod("siguienteValor", String.class).getModifiers())).as("siguienteValor is abstract").isTrue();
        assertThat(Modifier.isAbstract(SqlDialecto.class.getMethod("seleccionarSinTabla", String.class).getModifiers())).as("seleccionarSinTabla is abstract").isTrue();
    }

    @Test
    @DisplayName("A3-2 HanaDialecto and PostgresDialecto: ONE public constructor, without parameters, and no public method beyond the interface (no catalog, no helper survives)")
    void las_implementaciones_no_traen_nada_mas() {
        for (String simple : new String[] { "HanaDialecto", "PostgresDialecto" }) {
            Class<?> c = ApiLoginPg3.clase(simple);
            if (c.getConstructors().length != 1 || c.getConstructors()[0].getParameterCount() != 0) {
                throw ApiLoginPg3.pendiente(simple + " must have exactly one public constructor and it takes no parameters, found " + java.util.Arrays.toString(c.getConstructors()));
            }
            Set<String> permitidos = Set.of("nombre", "siguienteValor", "seleccionarSinTabla", "exigirHana");
            for (Method m : c.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic() && !permitidos.contains(m.getName())) {
                    throw ApiLoginPg3.catalogoEnProduccion(simple + " still has the public method " + m.getName() + " (functions of the earlier rounds)");
                }
            }
            assertThat(SqlDialecto.class.isAssignableFrom(c)).isTrue();
        }
    }

    @Test
    @DisplayName("A3-2 the removed functions are really gone from the interface and from both implementations: objeto, columna, columnaSeleccion, nombreDe, ahoraUtc, primeros, sumarSegundos, fechaATexto, aEntero, recortar, exigirDisponible")
    void las_funciones_retiradas_no_existen() {
        Set<String> retiradas = Set.of("objeto", "columna", "columnaSeleccion", "nombreDe", "ahoraUtc", "primeros", "sumarSegundos", "fechaATexto", "aEntero", "recortar",
                "exigirDisponible");
        for (Class<?> c : new Class<?>[] { SqlDialecto.class, ApiLoginPg3.clase("HanaDialecto"), ApiLoginPg3.clase("PostgresDialecto") }) {
            for (Method m : c.getMethods()) {
                if (retiradas.contains(m.getName())) throw ApiLoginPg3.catalogoEnProduccion(c.getSimpleName() + " still has " + m.getName());
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ name

    @Test
    @DisplayName("A3-2 each dialect names itself with the value of the property: hana / postgres")
    void nombres() {
        assertEquals("hana", hana().nombre());
        assertEquals("postgres", pg().nombre());
    }

    // ------------------------------------------------------------------------------------------------ sequences

    @Test
    @DisplayName("A3-2 next sequence value, HANA unchanged: \"<name>\".NEXTVAL for the two sequences of the login")
    void siguiente_valor_hana() {
        assertEquals("\"BSCL.Plataforma.BD.Secuencias::Log\".NEXTVAL", hana().siguienteValor(LOG));
        assertEquals("\"BSCL.Plataforma.BD.Secuencias::DetalleLog\".NEXTVAL", hana().siguienteValor(DETALLE));
    }

    @Test
    @DisplayName("A3-2 next sequence value, HANA unchanged for any other name too: the quoted name and .NEXTVAL (HANA never judges a name)")
    void siguiente_valor_hana_de_cualquier_nombre() {
        assertEquals("\"BSCL.Plataforma.BD.Secuencias::Otra\".NEXTVAL", hana().siguienteValor("BSCL.Plataforma.BD.Secuencias::Otra"));
    }

    @Test
    @DisplayName("A3-2 next sequence value, Postgres: the two login sequences are Java constants: nextval('sq_pla_log') and nextval('sq_pla_detalle_log')")
    void siguiente_valor_postgres() {
        assertEquals("nextval('sq_pla_log')", pg().siguienteValor(LOG));
        assertEquals("nextval('sq_pla_detalle_log')", pg().siguienteValor(DETALLE));
    }

    @ParameterizedTest(name = "A3-2 next sequence value in Postgres of [{0}] (not one of the two login sequences) is UnsupportedOperationException naming it (503), never an invented name")
    @ValueSource(strings = { "BSCL.Plataforma.BD.Secuencias::NoExiste", "BSCL.Plataforma.BD.Secuencias::log", " BSCL.Plataforma.BD.Secuencias::Log", "BSCL.Plataforma.BD.Secuencias::Log ",
            "sq_pla_log", "Log", "" })
    void siguiente_valor_desconocido_en_postgres(String nombre) {
        assertThatThrownBy(() -> pg().siguienteValor(nombre)).isInstanceOf(UnsupportedOperationException.class)
                .satisfies(e -> assertThat(String.valueOf(e.getMessage())).contains(nombre));
    }

    @Test
    @DisplayName("A3-2 next sequence value in Postgres of a null name fails with an exception (never a text with 'null')")
    void siguiente_valor_nulo_en_postgres() {
        assertThatThrownBy(() -> pg().siguienteValor(null)).isInstanceOf(RuntimeException.class);
    }

    // ------------------------------------------------------------------------------------------------ select without FROM

    @Test
    @DisplayName("A3-2 no-FROM select, unchanged: SELECT x FROM DUMMY in HANA, SELECT x in Postgres")
    void select_sin_tabla() {
        assertEquals("SELECT 1 AS \"X\" FROM DUMMY", hana().seleccionarSinTabla("1 AS \"X\""));
        assertEquals("SELECT 1 AS \"X\"", pg().seleccionarSinTabla("1 AS \"X\""));
    }

    @Test
    @DisplayName("A3-2 the sequence statement composed with the two functions, per dialect (literal): the inline statement of AuditoriaSesion.siguiente")
    void composicion_de_la_secuencia() {
        assertEquals("SELECT \"BSCL.Plataforma.BD.Secuencias::Log\".NEXTVAL AS \"Id\" FROM DUMMY", hana().seleccionarSinTabla(hana().siguienteValor(LOG) + " AS \"Id\""));
        assertEquals("SELECT nextval('sq_pla_log') AS \"Id\"", pg().seleccionarSinTabla(pg().siguienteValor(LOG) + " AS \"Id\""));
        assertEquals("SELECT nextval('sq_pla_detalle_log') AS \"Id\"", pg().seleccionarSinTabla(pg().siguienteValor(DETALLE) + " AS \"Id\""));
    }

    // ------------------------------------------------------------------------------------------------ the guard (unchanged)

    @Test
    @DisplayName("A3-2 exigirHana, unchanged: HANA lets it pass; Postgres refuses with UnsupportedOperationException (503) naming what was asked and the active dialect")
    void exigir_hana() {
        hana().exigirHana("stored procedure X");
        assertThatThrownBy(() -> pg().exigirHana("stored procedure X")).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("stored procedure X").hasMessageContaining("hana mode").hasMessageContaining("postgres");
    }
}
