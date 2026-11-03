package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * T-30 / T-35: the functions of {@code SqlDialecto}, per dialect, with LITERAL expected text. HANA expectations are the fragments of the current
 * HANA statements (see the goldens); Postgres expectations are the Postgres constructs chosen in API_ASSUMED_LOGINPG.md (A-8..A-18).
 */
class LoginPgDialectoFuncionesTest {

    private Object hana;
    private Object pg;

    @BeforeEach
    void dialectos() {
        Object catalogo = ApiLoginPg.catalogoDeRecursos();
        hana = ApiLoginPg.hana(catalogo);
        pg = ApiLoginPg.postgres(catalogo);
    }

    // ------------------------------------------------------------------------------------------------ name

    @Test
    @DisplayName("T-30 each dialect names itself with the value of the property: hana / postgres")
    void nombres() {
        assertEquals("hana", ApiLoginPg.f(hana, "nombre"));
        assertEquals("postgres", ApiLoginPg.f(pg, "nombre"));
    }

    // ------------------------------------------------------------------------------------------------ UTC now

    @Test
    @DisplayName("T-30 UTC now: CURRENT_UTCTIMESTAMP in HANA, (now() AT TIME ZONE 'UTC') in Postgres (a timestamp without zone in UTC)")
    void ahora_utc() {
        assertEquals("CURRENT_UTCTIMESTAMP", ApiLoginPg.f(hana, "ahoraUtc"));
        assertEquals("(now() AT TIME ZONE 'UTC')", ApiLoginPg.f(pg, "ahoraUtc"));
    }

    // ------------------------------------------------------------------------------------------------ first N

    @ParameterizedTest(name = "T-30 first-N n={2}: hana [{0}]")
    @CsvSource(delimiter = '|', value = {
            "SELECT \"A\" FROM \"T\" WHERE \"B\" = :b|SELECT TOP 1 \"A\" FROM \"T\" WHERE \"B\" = :b|1",
            "SELECT \"A\" FROM \"T\" WHERE \"B\" = :b ORDER BY \"A\"|SELECT TOP 25 \"A\" FROM \"T\" WHERE \"B\" = :b ORDER BY \"A\"|25" })
    void primeros_hana(String entrada, String esperado, int n) {
        assertEquals(esperado, ApiLoginPg.f(hana, "primeros", entrada, n));
    }

    @ParameterizedTest(name = "T-30 first-N n={2}: postgres [{0}]")
    @CsvSource(delimiter = '|', value = {
            "SELECT \"A\" FROM \"T\" WHERE \"B\" = :b|SELECT \"A\" FROM \"T\" WHERE \"B\" = :b LIMIT 1|1",
            "SELECT \"A\" FROM \"T\" WHERE \"B\" = :b ORDER BY \"A\"|SELECT \"A\" FROM \"T\" WHERE \"B\" = :b ORDER BY \"A\" LIMIT 25|25",
            "select \"A\" from \"T\"|select \"A\" from \"T\" LIMIT 1|1" })
    void primeros_postgres(String entrada, String esperado, int n) {
        assertEquals(esperado, ApiLoginPg.f(pg, "primeros", entrada, n));
    }

    @Test
    @DisplayName("T-30 first-N keeps the named parameters as :name in both dialects (the SqlNombrado binding does not change)")
    void primeros_conserva_parametros() {
        String sql = "SELECT \"A\" FROM \"T\" WHERE \"X\" = :IdUsuario AND \"Y\" = :IdCanal";
        for (Object d : new Object[] { hana, pg }) {
            String r = ApiLoginPg.f(d, "primeros", sql, 1);
            assertThat(LoginPgRecursos.parametros(r)).containsExactly("IdUsuario", "IdCanal");
            assertThat(r).doesNotContain("?");
        }
    }

    @Test
    @DisplayName("T-30 first-N with n below 1 is refused (A-10); the legacy only uses n=1")
    void primeros_rechaza_n_menor_a_uno() {
        for (Object d : new Object[] { hana, pg }) {
            assertThatThrownBy(() -> ApiLoginPg.f(d, "primeros", "SELECT 1", 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ApiLoginPg.f(d, "primeros", "SELECT 1", -3)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------------------------------------ sequence

    @Test
    @DisplayName("T-30 next sequence value: NEXTVAL on the quoted HANA name vs nextval('sq_pla_log') / nextval('sq_pla_detalle_log')")
    void siguiente_valor() {
        assertEquals("\"BSCL.Plataforma.BD.Secuencias::Log\".NEXTVAL", ApiLoginPg.f(hana, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::Log"));
        assertEquals("\"BSCL.Plataforma.BD.Secuencias::DetalleLog\".NEXTVAL", ApiLoginPg.f(hana, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::DetalleLog"));
        assertEquals("nextval('sq_pla_log')", ApiLoginPg.f(pg, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::Log"));
        assertEquals("nextval('sq_pla_detalle_log')", ApiLoginPg.f(pg, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::DetalleLog"));
    }

    @Test
    @DisplayName("T-30 next sequence value of a sequence that is not in the catalog: Postgres answers the 503 exception (never invents a name)")
    void siguiente_valor_desconocido_en_postgres() {
        assertThatThrownBy(() -> ApiLoginPg.f(pg, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::NoExiste")).satisfies(e -> {
            assertTrue(ApiLoginPg.esNoDisponible(e), "ObjetoNoDisponibleException expected, got " + e);
            assertThat(e.getMessage()).contains("BSCL.Plataforma.BD.Secuencias::NoExiste");
        });
    }

    // ------------------------------------------------------------------------------------------------ select without FROM

    @Test
    @DisplayName("T-30 no-FROM select: SELECT x FROM DUMMY in HANA, SELECT x in Postgres")
    void select_sin_tabla() {
        assertEquals("SELECT 1 AS \"X\" FROM DUMMY", ApiLoginPg.f(hana, "seleccionarSinTabla", "1 AS \"X\""));
        assertEquals("SELECT 1 AS \"X\"", ApiLoginPg.f(pg, "seleccionarSinTabla", "1 AS \"X\""));
    }

    @Test
    @DisplayName("T-30 the clock statement and the sequence statement composed from the functions, per dialect (literal)")
    void composiciones() {
        assertEquals("SELECT CURRENT_UTCTIMESTAMP as \"UTC\" FROM DUMMY", ApiLoginPg.f(hana, "seleccionarSinTabla", ApiLoginPg.f(hana, "ahoraUtc") + " as \"UTC\""));
        assertEquals("SELECT (now() AT TIME ZONE 'UTC') as \"UTC\"", ApiLoginPg.f(pg, "seleccionarSinTabla", ApiLoginPg.f(pg, "ahoraUtc") + " as \"UTC\""));
        assertEquals("SELECT nextval('sq_pla_log') AS \"Id\"",
                ApiLoginPg.f(pg, "seleccionarSinTabla", ApiLoginPg.f(pg, "siguienteValor", "BSCL.Plataforma.BD.Secuencias::Log") + " AS \"Id\""));
    }

    // ------------------------------------------------------------------------------------------------ add seconds

    @Test
    @DisplayName("T-30 add seconds: ADD_SECONDS (ts, n) in HANA, (ts + (n) * INTERVAL '1' SECOND) in Postgres; parameters stay :name")
    void sumar_segundos() {
        assertEquals("ADD_SECONDS (CURRENT_UTCTIMESTAMP, to_int(:Param_ClaveUsuarioTiempoBloqueo)*60)",
                ApiLoginPg.f(hana, "sumarSegundos", "CURRENT_UTCTIMESTAMP", "to_int(:Param_ClaveUsuarioTiempoBloqueo)*60"));
        assertEquals("(now() + (CAST(:Param_ClaveUsuarioTiempoBloqueo AS integer)*60) * INTERVAL '1' SECOND)",
                ApiLoginPg.f(pg, "sumarSegundos", "now()", "CAST(:Param_ClaveUsuarioTiempoBloqueo AS integer)*60"));
        assertEquals("(:T + (:S) * INTERVAL '1' SECOND)", ApiLoginPg.f(pg, "sumarSegundos", ":T", ":S"));
        assertEquals("ADD_SECONDS (:T, :S)", ApiLoginPg.f(hana, "sumarSegundos", ":T", ":S"));
    }

    // ------------------------------------------------------------------------------------------------ date to text

    @Test
    @DisplayName("T-30 date to text: TO_VARCHAR (x,'YYYY-MM-DD') in HANA, to_char(x,'YYYY-MM-DD') in Postgres")
    void fecha_a_texto() {
        assertEquals("TO_VARCHAR (\"Fecha\",'YYYY-MM-DD')", ApiLoginPg.f(hana, "fechaATexto", "\"Fecha\"", "YYYY-MM-DD"));
        assertEquals("to_char(fecha,'YYYY-MM-DD')", ApiLoginPg.f(pg, "fechaATexto", "fecha", "YYYY-MM-DD"));
        assertEquals("to_char(t.fecha,'YYYY-MM-DD HH24:MI:SS')", ApiLoginPg.f(pg, "fechaATexto", "t.fecha", "YYYY-MM-DD HH24:MI:SS"));
    }

    // ------------------------------------------------------------------------------------------------ to integer

    @Test
    @DisplayName("T-30 to integer: to_int(x) in HANA, CAST(x AS integer) in Postgres")
    void a_entero() {
        assertEquals("to_int(:Param_ClaveUsuarioTiempoBloqueo)", ApiLoginPg.f(hana, "aEntero", ":Param_ClaveUsuarioTiempoBloqueo"));
        assertEquals("CAST(:Param_ClaveUsuarioTiempoBloqueo AS integer)", ApiLoginPg.f(pg, "aEntero", ":Param_ClaveUsuarioTiempoBloqueo"));
        assertEquals("CAST('15' AS integer)", ApiLoginPg.f(pg, "aEntero", "'15'"));
    }

    // ------------------------------------------------------------------------------------------------ trim

    @Test
    @DisplayName("T-30 trim both sides: rtrim(ltrim(x)) in both dialects (the same text, pinned so a change is deliberate)")
    void recortar() {
        assertEquals("rtrim(ltrim(\"Descripcion\"))", ApiLoginPg.f(hana, "recortar", "\"Descripcion\""));
        assertEquals("rtrim(ltrim(descripcion))", ApiLoginPg.f(pg, "recortar", "descripcion"));
    }

    // ------------------------------------------------------------------------------------------------ object and column tokens

    @Test
    @DisplayName("T-31 object token {ACC.Usuario}: the quoted full HANA name in HANA, the flat snake_case name in Postgres")
    void objeto_por_ficha() {
        assertEquals("\"BSCL.Plataforma.BD::ACC.Usuario\"", ApiLoginPg.f(hana, "objeto", "ACC.Usuario"));
        assertEquals("t_pla_acc_usuario", ApiLoginPg.f(pg, "objeto", "ACC.Usuario"));
        assertEquals("\"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\"", ApiLoginPg.f(hana, "objeto", "SOC.SocioDeNegocio"));
        assertEquals("t_socneg_soc_socio_de_negocio", ApiLoginPg.f(pg, "objeto", "SOC.SocioDeNegocio"));
    }

    @Test
    @DisplayName("T-31 column token {ACC.Usuario.IdEmpresa}: \"IdEmpresa\" in HANA, id_empresa in Postgres; the selection form carries the alias with the HANA name")
    void columna_por_ficha() {
        assertEquals("\"IdEmpresa\"", ApiLoginPg.f(hana, "columna", "ACC.Usuario.IdEmpresa"));
        assertEquals("id_empresa", ApiLoginPg.f(pg, "columna", "ACC.Usuario.IdEmpresa"));
        assertEquals("\"IdEmpresa\"", ApiLoginPg.f(hana, "columnaSeleccion", "ACC.Usuario.IdEmpresa"));
        assertEquals("id_empresa AS \"IdEmpresa\"", ApiLoginPg.f(pg, "columnaSeleccion", "ACC.Usuario.IdEmpresa"));
        assertEquals("codigo_usuario AS \"CodigoUsuario\"", ApiLoginPg.f(pg, "columnaSeleccion", "ACC.Usuario.CodigoUsuario"));
    }

    @Test
    @DisplayName("T-31 an unknown column token fails clearly (names the token) in both dialects; it is a configuration error, not a 503")
    void columna_desconocida() {
        for (Object d : new Object[] { hana, pg }) {
            assertThatThrownBy(() -> ApiLoginPg.f(d, "columna", "ACC.Usuario.NoExiste")).isInstanceOf(RuntimeException.class)
                    .satisfies(e -> assertThat(ApiLoginPg.esNoDisponible(e)).isFalse()).hasMessageContaining("ACC.Usuario.NoExiste");
        }
    }

    @Test
    @DisplayName("T-31 an unknown object token: Postgres answers the 503 exception, HANA fails clearly (names the token)")
    void objeto_desconocido() {
        assertThatThrownBy(() -> ApiLoginPg.f(pg, "objeto", "ACC.NoExiste")).satisfies(e -> {
            assertTrue(ApiLoginPg.esNoDisponible(e), "ObjetoNoDisponibleException expected, got " + e);
            assertThat(e.getMessage()).contains("ACC.NoExiste");
        });
        assertThatThrownBy(() -> ApiLoginPg.f(hana, "objeto", "ACC.NoExiste")).isInstanceOf(RuntimeException.class).hasMessageContaining("ACC.NoExiste");
    }
}
