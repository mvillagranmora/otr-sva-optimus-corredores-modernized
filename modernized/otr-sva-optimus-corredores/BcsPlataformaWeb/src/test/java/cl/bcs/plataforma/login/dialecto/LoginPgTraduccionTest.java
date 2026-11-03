package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * T-31 translation of the statements that touch ONLY TABLES (the 6 statements over views wait for the columns of the views, Q-LOGINPG-1).
 * Structural checks on the Postgres text, derived from the HANA golden and the xlsx oracle; the values are compared in LoginPgResultadosH2Test.
 */
class LoginPgTraduccionTest {

    private static final LoginPgRecursos.Oraculo ORACULO = LoginPgRecursos.Oraculo.cargar();

    static final Set<String> CON_VISTAS = Set.of("BootstrapServicio.SQL_ACCESOS", "BootstrapServicio.SQL_EMPRESAS", "BootstrapServicio.SQL_IMAGEN",
            "BootstrapServicio.SQL_PREFERENCIAS", "BootstrapServicio.SQL_PREFERENCIAS_GRILLA", "GestionClaveServicio.SQL_DATOS_RECUPERACION");

    static boolean soloTablas(String sql) {
        for (String o : LoginPgRecursos.objetosHanaDe(sql)) {
            if (!ORACULO.objeto(o).tipo().equals("tabla")) return false;
        }
        return true;
    }

    static Stream<String> idsSoloTablas() {
        return LoginPgRecursos.goldenSentencias().entrySet().stream().filter(e -> soloTablas(e.getValue())).map(Map.Entry::getKey);
    }

    private static String pg(String id) {
        return ApiLoginPg3.textoPg(id);
    }

    // ------------------------------------------------------------------------------------------------ scope (green)

    @Test
    @DisplayName("T-31 scope: of the 45 SQL statements, 39 touch only tables and 6 touch views (listed); none touches a procedure or sequence by name")
    void alcance_de_la_traduccion() {
        Set<String> conVistas = new LinkedHashSet<>();
        LoginPgRecursos.goldenSentencias().forEach((id, sql) -> {
            if (!soloTablas(sql)) conVistas.add(id);
        });
        assertThat(conVistas).containsExactlyInAnyOrderElementsOf(CON_VISTAS);
        assertEquals(45, LoginPgRecursos.goldenSentencias().size());
        assertEquals(39, idsSoloTablas().count());
    }

    @ParameterizedTest(name = "T-31 golden {0}: every column the HANA text uses exists in a table it references, per the oracle")
    @MethodSource("idsSoloTablas")
    void columnas_del_golden_existen_en_el_oraculo(String id) {
        String sql = LoginPgRecursos.goldenSentencias().get(id);
        Set<String> tablas = LoginPgRecursos.objetosHanaDe(sql);
        List<String> sinDueno = new ArrayList<>();
        LoginPgRecursos.identificadoresHana(sql).forEach((n, c) -> {
            if (c[0] == 0) return;
            boolean esColumna = tablas.stream().anyMatch(t -> ORACULO.columnas.get(t).containsKey(n));
            if (!esColumna && c[1] == 0) sinDueno.add(n);
        });
        assertThat(sinDueno).as("identifiers that are neither a column of a referenced table nor an alias defined in " + id).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ Postgres text (red)

    @ParameterizedTest(name = "T-31 postgres {0}: no HANA token is left")
    @MethodSource("idsSoloTablas")
    void sin_fichas_de_hana(String id) {
        String t = pg(id);
        for (Pattern p : LoginPgRecursos.FICHAS_HANA) {
            assertThat(p.matcher(t).find()).as("HANA token " + p + " in " + t).isFalse();
        }
    }

    @ParameterizedTest(name = "T-31 postgres {0}: references exactly the Postgres names of the tables of the HANA statement")
    @MethodSource("idsSoloTablas")
    void tablas_exactas(String id) {
        String sql = LoginPgRecursos.goldenSentencias().get(id);
        Set<String> esperadas = new HashSet<>();
        LoginPgRecursos.objetosHanaDe(sql).forEach(h -> esperadas.add(ORACULO.objeto(h).postgres()));
        Set<String> todosLosObjetos = new HashSet<>();
        ORACULO.objetos.values().forEach(o -> todosLosObjetos.add(o.postgres()));
        Set<String> halladas = new HashSet<>();
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(pg(id));
        while (m.find()) if (todosLosObjetos.contains(m.group())) halladas.add(m.group());
        assertEquals(esperadas, halladas);
    }

    @ParameterizedTest(name = "T-31 postgres {0}: every column of the HANA statement appears under its Postgres name")
    @MethodSource("idsSoloTablas")
    void columnas_traducidas(String id) {
        String sql = LoginPgRecursos.goldenSentencias().get(id);
        Set<String> tablas = LoginPgRecursos.objetosHanaDe(sql);
        String t = pg(id);
        List<String> faltan = new ArrayList<>();
        LoginPgRecursos.identificadoresHana(sql).forEach((n, c) -> {
            if (c[0] == 0) return;
            for (String tabla : tablas) {
                String col = ORACULO.columnas.get(tabla).get(n);
                if (col != null && !Pattern.compile("\\b" + col + "\\b").matcher(t).find()) faltan.add(tabla + "." + n + " -> " + col);
            }
        });
        // a column that belongs to several referenced tables is only required once: report only names absent for ALL their tables
        List<String> realmenteFaltan = new ArrayList<>();
        for (String f : faltan) {
            String n = f.substring(f.indexOf('.', f.indexOf("::")) + 1, f.indexOf(" ->"));
            long duenos = tablas.stream().filter(x -> ORACULO.columnas.get(x).containsKey(n)).count();
            long faltantes = faltan.stream().filter(x -> x.contains("." + n + " ->")).count();
            if (faltantes >= duenos) realmenteFaltan.add(f);
        }
        assertThat(realmenteFaltan).isEmpty();
    }

    @ParameterizedTest(name = "T-31 postgres {0}: every double-quoted name is an alias defined with AS (columns carry the HANA name as alias)")
    @MethodSource("idsSoloTablas")
    void comillas_solo_en_alias(String id) {
        String t = pg(id);
        Map<String, int[]> ident = LoginPgRecursos.identificadoresHana(t);
        List<String> sinAlias = new ArrayList<>();
        ident.forEach((n, c) -> {
            if (c[1] == 0) sinAlias.add(n);
        });
        assertThat(sinAlias).as("quoted names never defined by AS in " + t).isEmpty();
        assertThat(t).doesNotContain("\"BSCL");
    }

    @ParameterizedTest(name = "T-31 postgres {0}: the :name parameters are the same as in HANA")
    @MethodSource("idsSoloTablas")
    void parametros_iguales(String id) {
        List<String> h = LoginPgRecursos.parametros(LoginPgRecursos.goldenSentencias().get(id));
        List<String> p = LoginPgRecursos.parametros(pg(id));
        assertThat(new HashSet<>(p)).isEqualTo(new HashSet<>(h));
    }

    @ParameterizedTest(name = "T-31 postgres {0}: TOP n becomes LIMIT n at the end")
    @MethodSource("idsSoloTablas")
    void top_a_limit(String id) {
        Matcher m = Pattern.compile("\\bTOP\\s+(\\d+)").matcher(LoginPgRecursos.goldenSentencias().get(id));
        if (!m.find()) return;
        String t = LoginPgRecursos.normalizar(pg(id));
        if (t.endsWith(";")) t = t.substring(0, t.length() - 1).trim();
        assertThat(t).endsWith(" LIMIT " + m.group(1));
        assertThat(t).doesNotContainPattern("(?i)\\bTOP\\b");
    }

    @ParameterizedTest(name = "T-31 postgres {0}: a derived table in FROM has an alias (Postgres before 16 refuses it, Q-LOGINPG-4)")
    @MethodSource("idsSoloTablas")
    void tabla_derivada_con_alias(String id) {
        String t = pg(id);
        Matcher m = Pattern.compile("(?i)\\bFROM\\s*\\(").matcher(t);
        while (m.find()) {
            int i = m.end(), prof = 1;
            while (i < t.length() && prof > 0) {
                char c = t.charAt(i++);
                if (c == '(') prof++;
                else if (c == ')') prof--;
            }
            String resto = t.substring(i).stripLeading();
            Matcher a = Pattern.compile("(?i)^(AS\\s+)?([A-Za-z_][A-Za-z0-9_]*)").matcher(resto);
            boolean tiene = a.find() && !Set.of("WHERE", "UNION", "ORDER", "GROUP", "JOIN", "LEFT", "INNER", "LIMIT", "ON").contains(a.group(2).toUpperCase());
            if (!tiene) fail("derived table without alias in " + t);
        }
    }

    // ------------------------------------------------------------------------------------------------ literal texts (red)

    @Test
    @DisplayName("T-31 literal postgres text of FiltroSesion.SQL_VALIDA_TOKEN (positional ?, alias OK quoted so the label stays OK)")
    void literal_valida_token() {
        assertEquals("SELECT COUNT(1) AS \"OK\" FROM t_pla_acc_sesion WHERE id_token_sesion = ? and token_valido = 'S'",
                LoginPgRecursos.normalizar(pg("FiltroSesion.SQL_VALIDA_TOKEN")));
    }

    @Test
    @DisplayName("T-31 literal postgres text of CrearSesionServicio.QUERY_BUSCA_TOKEN (TOP 1 -> LIMIT 1 at the end, selected column aliased)")
    void literal_busca_token() {
        assertEquals("SELECT id_token_sesion AS \"IdTokenSesion\" FROM t_pla_acc_sesion WHERE token_valido = 'S' AND id_usuario = :IdUsuario AND id_canal = :IdCanal LIMIT 1",
                LoginPgRecursos.normalizar(pg("CrearSesionServicio.QUERY_BUSCA_TOKEN")));
    }

    @Test
    @DisplayName("T-31 literal postgres text of CrearSesionServicio.UPDATE_INTENTOS")
    void literal_update_intentos() {
        assertEquals("UPDATE t_pla_acc_usuario SET intentos_conexion = :IntentosConexion WHERE id_usuario = :IdUsuario",
                LoginPgRecursos.normalizar(pg("CrearSesionServicio.UPDATE_INTENTOS")));
    }

    @Test
    @DisplayName("T-31 literal postgres text of CrearSesionServicio.QUERY_RELOJ_UTC")
    void literal_reloj_utc() {
        assertEquals("SELECT (now() AT TIME ZONE 'UTC') as \"UTC\"", LoginPgRecursos.normalizar(pg("CrearSesionServicio.QUERY_RELOJ_UTC")));
    }

    @Test
    @DisplayName("T-31 UPDATE_BLOQUEO_TEMPORAL in Postgres uses the interval form of add-seconds and the cast to integer, with the UTC clock")
    void literal_bloqueo_temporal() {
        String t = LoginPgRecursos.normalizar(pg("CrearSesionServicio.UPDATE_BLOQUEO_TEMPORAL"));
        assertThat(t).contains("fecha_bloqueo_temporal = (" + "(now() AT TIME ZONE 'UTC') + (CAST(:Param_ClaveUsuarioTiempoBloqueo AS integer)*60) * INTERVAL '1' SECOND)");
    }

    @Test
    @DisplayName("T-31 bootstrap feriados in Postgres formats the date with to_char and keeps the alias \"Fecha\"")
    void literal_feriados_to_char() {
        assertThat(LoginPgRecursos.normalizar(pg("BootstrapServicio.SQL_FERIADOS"))).containsIgnoringCase("to_char(fecha,'YYYY-MM-DD') AS \"Fecha\"");
    }

    @Test
    @DisplayName("T-31 / A3-3 the six statements over views have a Postgres text file: texto() must NOT raise and must carry no HANA token")
    void sentencias_con_vistas_no_devuelven_hana() {
        for (String id : CON_VISTAS) {
            String t = ApiLoginPg3.textoPg(id);
            assertThat(t).as(id).isNotBlank().doesNotContain("BSCL").doesNotContain("::");
        }
    }
}
