package cl.bcs.plataforma.cre;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.cre.ConsultarEstadoModuloService.Modulo;
import cl.bcs.plataforma.port.out.ModuloRepository;

/** One test per rule card of RULES_PLATAFORMA_ESM.md (legacy ConsultarEstadoModulo.java line numbers in the comments). */
class ConsultarEstadoModuloReglasTest {

    private static JsonObject salida(String prefijoCaso) throws Exception {
        OraculoCasos.Resultado r = OraculoCasos.ejecutar(OraculoCasos.caso(prefijoCaso));
        assertThat(r.excepcion()).as(prefijoCaso).isNull();
        return JsonParser.parseString(r.salidaCruda()).getAsJsonObject();
    }

    private static Throwable falla(String prefijoCaso) throws Exception {
        OraculoCasos.Resultado r = OraculoCasos.ejecutar(OraculoCasos.caso(prefijoCaso));
        assertThat(r.excepcion()).as(prefijoCaso).isNotNull();
        return r.excepcion();
    }

    private static JsonArray lista(JsonObject o) {
        return o.getAsJsonArray("p_out_ListaModulos");
    }

    @Test
    void plaEsm01_estadoNuloUsaABT_ESM22_casoHipotetico26() throws Exception {
        JsonArray l = lista(salida("PLA-ESM-26"));
        assertThat(l.toString()).contains("\"IdEstadoModulo\":\"ABT\"");
    }

    @Test
    void plaEsm02_nombreEsCodigoModuloSinTransformar_ESM22() throws Exception {
        assertThat(lista(salida("PLA-ESM-24")).toString()).contains("\"NombreModulo\":\" X \"").contains("\"NombreModulo\":\"\"");
        assertThat(lista(salida("PLA-ESM-22")).toString()).contains("Ñandú");
    }

    @Test
    void plaEsm03_soloFiltraPorEmpresa_ESM23() throws Exception {
        assertThat(lista(salida("PLA-ESM-01")).size()).isEqualTo(5);
        assertThat(lista(salida("PLA-ESM-03")).size()).isEqualTo(3);
        assertThat(lista(salida("PLA-ESM-02")).size()).isEqualTo(1);
    }

    @Test
    void plaEsm04_empresaSinModulosOnulaDaListaVacia_ESM117() throws Exception {
        for (String c : List.of("PLA-ESM-04", "PLA-ESM-05", "PLA-ESM-06", "PLA-ESM-07", "PLA-ESM-12")) {
            JsonObject o = salida(c);
            assertThat(o.has("p_out_ListaModulos")).as(c).isTrue();
            assertThat(lista(o)).as(c).isEmpty();
            assertThat(o.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        }
    }

    @Test
    void plaEsm05_soloLectura_ESM22() throws Exception {
        Path dir = OraculoCasos.caso("PLA-ESM-01");
        try (var con = OraculoCasos.abrir(dir)) {
            String antes = instantanea(con);
            new cl.bcs.plataforma.adapter.in.javacall.ConsultarEstadoModuloHandler()
                    .call(con, OraculoCasos.json(dir.resolve("case.json")).getAsJsonObject("entrada"));
            assertThat(instantanea(con)).isEqualTo(antes);
        }
        assertThat(Files.readString(Path.of("src/main/java/cl/bcs/plataforma/adapter/out/jdbc/ModuloJdbcRepository.java")))
                .doesNotContain("executeUpdate").doesNotContain("executeBatch").doesNotContain("commit(");
    }

    private static String instantanea(java.sql.Connection con) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (var st = con.createStatement(); var rs = st.executeQuery(
                "SELECT * FROM \"BSCL.Plataforma.BD::PAU.Modulo\" ORDER BY \"IdEmpresa\",\"CodigoModulo\",\"IdEstado\"")) {
            int n = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= n; i++) sb.append(rs.getString(i)).append('|');
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Test
    void plaEsm06_formaFijaDeLaSalida_ESM51_57() throws Exception {
        OraculoCasos.Resultado r = OraculoCasos.ejecutar(OraculoCasos.caso("PLA-ESM-01"));
        assertThat(r.salidaCruda()).startsWith("{\"P_OUT_ADVERTENCIASCONTADOR\":0,\"P_OUT_ERRORESCONTADOR\":0,\"p_out_ListaModulos\":[{\"IdEstadoModulo\":\"ABT\",\"NombreModulo\":\"SIS\"}")
                .doesNotContain("p_out_ErroresDetalle").doesNotContain("p_out_AdvertenciasDetalle").doesNotContain("null");
    }

    @Test
    void plaEsm07_lecturaDeEntradaConGsonPlano_ESM64() throws Exception {
        assertThat(lista(salida("PLA-ESM-12"))).isEmpty();                       // keys are case sensitive
        assertThat(lista(salida("PLA-ESM-13"))).isNotEmpty();                    // "1" accepted as Long
        assertThat(lista(salida("PLA-ESM-14"))).isNotEmpty();                    // 1.0 accepted as Long
        assertThat(falla("PLA-ESM-15").getClass().getSimpleName()).isEqualTo("JsonSyntaxException");
        assertThat(falla("PLA-ESM-16").getClass().getSimpleName()).isEqualTo("JsonSyntaxException");
        assertThat(falla("PLA-ESM-17").getClass().getSimpleName()).isEqualTo("JsonSyntaxException");  // D-ESM-7
        assertThat(falla("PLA-ESM-18").getClass().getSimpleName()).isEqualTo("JsonSyntaxException");  // D-ESM-7
    }

    @Test
    void plaEsm08_ordenDeLaBaseSinOrderBy_ESM22_D_ESM3() throws Exception {
        JsonArray l = lista(salida("PLA-ESM-27"));
        assertThat(l).hasSize(25);
        assertThat(ModuloJdbcSql.texto()).doesNotContainIgnoringCase("order by");
    }

    @Test
    void plaEsm09_sinDeduplicar_ESM116_casoHipotetico25() throws Exception {
        JsonArray l = lista(salida("PLA-ESM-25"));
        assertThat(l.size()).isGreaterThanOrEqualTo(2);
        assertThat(l.get(0)).isEqualTo(l.get(1));
    }

    @Test
    void plaEsm10_errorSqlSePropagaComoExcepcion_D_ESM5() throws Exception {
        Throwable t = falla("PLA-ESM-29");
        assertThat(t.getCause()).isInstanceOf(java.sql.SQLException.class);
    }

    @Test
    void plaEsm11_sinColaboradoresNiEstado_ESM19_23() {
        // the service only needs a repository: p_IdModulo and p_IdUsuario (D-ESM-1) never reach it
        final Long[] recibido = new Long[1];
        final int[] llamadas = {0};
        ModuloRepository fake = id -> { recibido[0] = id; llamadas[0]++; return List.of(new Modulo("CER", "TES")); };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":7,\"p_IdModulo\":\"XYZ\",\"p_IdUsuario\":99}").getAsJsonObject();
        JsonObject out = new ConsultarEstadoModuloService(fake).call(in);
        assertThat(recibido[0]).isEqualTo(7L);
        assertThat(llamadas[0]).isEqualTo(1);
        assertThat(out.toString()).isEqualTo("{\"P_OUT_ADVERTENCIASCONTADOR\":0,\"P_OUT_ERRORESCONTADOR\":0,"
                + "\"p_out_ListaModulos\":[{\"IdEstadoModulo\":\"CER\",\"NombreModulo\":\"TES\"}]}");
    }

    /** Exposes the package-private SQL constant of the adapter without widening its visibility. */
    static final class ModuloJdbcSql {
        static String texto() throws Exception {
            var f = cl.bcs.plataforma.adapter.out.jdbc.ModuloJdbcRepository.class.getDeclaredField("SQL_SELECT_MODULOEMPRESA");
            f.setAccessible(true);
            return (String) f.get(null);
        }
    }
}
