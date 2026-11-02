package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.PerfilesPortafolioUtilsService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.ArrayList;
import java.util.List;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

/**
 * SQL-level characterization of the CURRENT PerfilesPortafolioUtilsService (this seam exists today, so these run green):
 * which SQL text goes out with which named parameters, and that nothing is concatenated (RULE-068 pattern).
 * Row mapping and HANA semantics are NOT covered here (needs HANA; H2 cannot run the HANA views).
 */
class PerfilesPortafolioUtilsJdbcTest {

    static final String PAYLOAD = "x'); DROP TABLE \"BSCL.Portafolio.BD::POR.Portafolio\"; --";

    NamedParameterJdbcTemplate named;
    JdbcTemplate plain;
    PerfilesPortafolioUtilsService utils;

    @BeforeEach
    void setUp() {
        named = mock(NamedParameterJdbcTemplate.class);
        plain = mock(JdbcTemplate.class);
        when(named.getJdbcTemplate()).thenReturn(plain);
        utils = new PerfilesPortafolioUtilsService(new cl.bcs.sura.adapter.out.LegacyHanaSuraPort(named));
    }

    private static List<Invocation> inv(Object mock, String method) {
        return mockingDetails(mock).getInvocations().stream().filter(i -> i.getMethod().getName().equals(method)).toList();
    }

    @Test
    void bSura20_consultarIds_sqlEsElRecursoYElDocumentoViajaComoParametroNombrado() {
        utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(List.of(PAYLOAD, "11111111-1"));
        Invocation i = inv(named, "query").get(0);
        String sql = (String) i.getArgument(0);
        MapSqlParameterSource p = (MapSqlParameterSource) i.getArgument(1);
        assertThat(sql).contains("WHERE DOCI.\"DocumentoIdentidad\" IN (:DocumentoIdentidad)");
        assertThat(sql).doesNotContain("DROP TABLE");
        assertThat(p.getValue("DocumentoIdentidad")).isEqualTo(List.of(PAYLOAD, "11111111-1"));
    }

    /** Found against HANA QA: (VALUES ...) in FROM is a syntax error there, and H2 accepts it, so the SQL text itself is pinned. */
    @Test
    void bSura20_perfilesFiltrados_semillaComoSelectFromDummyUnionAll_sinValuesYSinConcatenar() {
        IdsEntidadYPerfiles a = perfilRow(10, 1, PAYLOAD, 0, "Opci\u00f3n 2", "", "", "", "");
        IdsEntidadYPerfiles b = perfilRow(11, 2, "22222222-2", 0, "opci\u00f3n 4", "", "", "", "");
        new cl.bcs.sura.adapter.out.LegacyHanaSuraPort(named).consultarPerfilesFiltrados(List.of(a, b));
        Invocation i = inv(plain, "query").get(0);
        String sql = (String) i.getArgument(0);
        assertThat(sql).contains("FROM (SELECT CAST(? AS BIGINT) AS \"IdCliente\"").contains(" FROM DUMMY UNION ALL SELECT CAST(? AS BIGINT)")
                .contains(") AS TEMP").doesNotContain("VALUES").doesNotContain("DROP TABLE");
        List<Object> args = java.util.Arrays.asList(i.getArguments()).subList(2, i.getArguments().length);   // varargs come expanded
        assertThat(args).containsExactly(1L, "Opci\u00f3n 2", PAYLOAD, 10L, 2L, "opci\u00f3n 4", "22222222-2", 11L);
    }

    @Test
    void bSura20_modificarPerfiles_batchConLosCuatroParametrosDelLegacy() {
        IdsEntidadYPerfiles a = perfilRow(10, 1, "D", 0, "F", "PC-1", "PCO-1", "PP-1", "");
        IdsEntidadYPerfiles b = perfilRow(11, 1, "D", 0, "F", PAYLOAD, "PCO-2", "PP-2", "");
        utils.modificarPerfilesPortafolio(List.of(a, b));
        Invocation i = inv(named, "batchUpdate").get(0);
        String sql = (String) i.getArgument(0);
        SqlParameterSource[] batch = (SqlParameterSource[]) i.getArgument(1);
        assertThat(sql).contains("PORT.\"IdPerfilComercialBaseOrigen\" = :IdPerfilComercial")
                .contains("PORT.\"IdPerfilProductos\" = :IdPerfilProductos")
                .contains("PORT.\"IdPerfilCobros\" = :IdPerfilCobros")
                .contains("WHERE PORT.\"IdPortafolio\" = :IdPortafolio")
                .doesNotContain("DROP TABLE");
        assertThat(batch).hasSize(2);
        assertThat(batch[0].getValue("IdPortafolio")).isEqualTo(10L);
        assertThat(batch[0].getValue("IdPerfilComercial")).isEqualTo("PC-1");
        assertThat(batch[0].getValue("IdPerfilProductos")).isEqualTo("PP-1");
        assertThat(batch[0].getValue("IdPerfilCobros")).isEqualTo("PCO-1");
        assertThat(batch[1].getValue("IdPerfilComercial")).isEqualTo(PAYLOAD);
    }

    @Test
    void bSura20_ingresarYModificarIndicadores_parametrosEntidadYValor() {
        utils.ingresarIndicadoresCobroCustodia(List.of(idRow(10, 1, "D", 0, ""), idRow(11, 1, "D", 3, "")), PAYLOAD);
        Invocation ins = inv(named, "batchUpdate").get(0);
        SqlParameterSource[] b1 = (SqlParameterSource[]) ins.getArgument(1);
        assertThat((String) ins.getArgument(0)).contains(":IdEntidad").contains("UPPER(:ValorIndicador)").doesNotContain("DROP TABLE");
        assertThat(b1).hasSize(1);                                    // only IdIndicador == 0
        assertThat(b1[0].getValue("IdEntidad")).isEqualTo(10L);
        assertThat(b1[0].getValue("ValorIndicador")).isEqualTo(PAYLOAD);

        IdsEntidadYPerfiles u = idRow(11, 1, "D", 3, "");
        u.idFormaCobro = "OPCION 1";
        utils.modificarIndicadoresCobroCustodia(List.of(u, idRow(12, 1, "D", 0, "")));
        Invocation upd = inv(named, "batchUpdate").get(1);
        SqlParameterSource[] b2 = (SqlParameterSource[]) upd.getArgument(1);
        assertThat(b2).hasSize(1);                                    // IdIndicador != 0 and "S"
        assertThat(b2[0].getValue("IdEntidad")).isEqualTo(3L);        // NOTE: IdEntidad carries the IdIndicador (legacy quirk)
        assertThat(b2[0].getValue("ValorIndicador")).isEqualTo("OPCION 1");
    }

    @Test
    void bSura20_obtenerEntidadesParaInsertar_valoresPorPlaceholder_nuncaConcatenados() {
        List<IdsEntidadYPerfiles> ids = new ArrayList<>(List.of(idRow(101, 1, PAYLOAD, 0, ""), idRow(102, 2, PAYLOAD, 0, "")));
        JsonObject ind = new JsonObject();
        ind.addProperty("CodigoTipoIndicador", "COBROCUST");
        ind.addProperty("ValorIndicador", PAYLOAD);
        com.google.gson.JsonArray indicadores = new com.google.gson.JsonArray();
        indicadores.add(ind);
        JsonObject socio = new JsonObject();
        socio.addProperty("TipoEntidad", "CLIENTE");
        socio.addProperty("DocumentoIdentidad", PAYLOAD);
        socio.add("IndicadorPortafolio", indicadores);
        utils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(List.of(socio), ids);

        Invocation q = inv(plain, "query").get(0);
        String sql = (String) q.getArgument(0);
        Object[] args = (Object[]) q.getRawArguments()[2];
        assertThat(sql).doesNotContain("DROP TABLE");
        assertThat(sql.chars().filter(c -> c == '?').count()).isEqualTo(8);   // 2 portfolios x 4 columns
        assertThat(args).containsExactly(1L, PAYLOAD, PAYLOAD, 101L, 2L, PAYLOAD, PAYLOAD, 102L);
        // same join semantics as legacy ConsultarPerfilesComercialesFiltradosPorTablaTemporal
        assertThat(sql).contains("COALESCE(PERF.\"IdFormaCobro\", TEMP.\"IdFormaCobro\", '')")
                .contains("UPPER(PERF.\"IdFormaCobro\") = UPPER(TEMP.\"IdFormaCobro\")");
    }
}
