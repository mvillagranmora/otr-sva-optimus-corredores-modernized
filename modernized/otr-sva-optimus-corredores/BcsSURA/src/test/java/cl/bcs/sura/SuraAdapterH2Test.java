package cl.bcs.sura;

import cl.bcs.sura.adapter.out.SuraRepositoryAdapter;
import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static cl.bcs.sura.support.SuraTestSupport.idRow;
import static cl.bcs.sura.support.SuraTestSupport.perfilRow;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL-level behaviour of the JDBC adapter on H2, extending SuraIntegrationTest without touching its seed rows
 * (these tests use portfolio ids 9xx and document numbers 9xxxxxxx-x and clean up after themselves).
 *
 * H2 vs HANA, what these tests CANNOT show:
 *  - the adapter works on flat tables (PORTAFOLIOS_T, PERFILES_COMERCIALES_T, INDICADORES_PORTAFOLIO_T), not on the HANA
 *    views/joins of the legacy SQL; Proposito/IdFormaCobro columns that legacy defaults to "" come back from the seed;
 *  - legacy INSERT/UPDATE resolve ValorIndicador through IND.DetalleIndicador with UPPER() on both sides and store the
 *    IdDetalleIndicador; H2 stores the raw string (no catalogue, no case folding);
 *  - legacy UPDATE of profiles targets POR.Portafolio; the adapter updates PERFILES_COMERCIALES_T;
 *  - IdIndicador is a sequence NEXTVAL in HANA, an identity column in H2.
 * What they DO pin: the batch/filter semantics and that every value travels as a bound parameter (RULE-068 pattern).
 */
@SpringBootTest
@ActiveProfiles("test")
class SuraAdapterH2Test {

    static final String PAYLOAD = "x'); DROP TABLE PORTAFOLIOS_T; --";

    @Autowired SuraRepositoryAdapter adapter;
    @Autowired NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD >= 900 AND ID_ENTIDAD < 1000", Map.of());
        jdbc.update("DELETE FROM PERFILES_COMERCIALES_T WHERE ID_PORTAFOLIO >= 900", Map.of());
        jdbc.update("DELETE FROM PORTAFOLIOS_T WHERE ID_PORTAFOLIO >= 900", Map.of());
    }

    private void seedPerfil(long id, String doc) {
        jdbc.update("INSERT INTO PERFILES_COMERCIALES_T (ID_PORTAFOLIO, ID_CLIENTE, DOCUMENTO_IDENTIDAD, ID_PERFIL_COMERCIAL, ID_PERFIL_COBROS, ID_PERFIL_PRODUCTOS) "
                + "VALUES (:id, 9, :doc, 'OLD-PC', 'OLD-PCO', 'OLD-PP')", Map.of("id", id, "doc", doc));
    }

    private long contar(long entidad) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = :e", Map.of("e", entidad), Long.class);
    }

    private String valorIndicadoresDe(long entidad) {
        return jdbc.queryForObject("SELECT VALOR_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = :e", Map.of("e", entidad), String.class);
    }

    @Test
    void bSura22_modificarPerfiles_batchActualizaTodosLosPerfilesEnviados_yNoToca_otros() {
        seedPerfil(901, "90000001-1");
        seedPerfil(902, "90000001-1");
        seedPerfil(903, "90000002-2");
        IdsEntidadYPerfiles a = perfilRow(901, 9, "90000001-1", 0, "F", "PC-1", "PCO-1", "PP-1", "");
        IdsEntidadYPerfiles b = perfilRow(902, 9, "90000001-1", 0, "F", "PC-2", "PCO-2", "PP-2", "");
        adapter.modificarPerfilesPortafolio(List.of(a, b));

        assertThat(jdbc.queryForList("SELECT ID_PORTAFOLIO, ID_PERFIL_COMERCIAL, ID_PERFIL_COBROS, ID_PERFIL_PRODUCTOS FROM PERFILES_COMERCIALES_T WHERE ID_PORTAFOLIO >= 900 ORDER BY ID_PORTAFOLIO", Map.of()))
                .extracting(r -> r.get("ID_PORTAFOLIO") + "|" + r.get("ID_PERFIL_COMERCIAL") + "|" + r.get("ID_PERFIL_COBROS") + "|" + r.get("ID_PERFIL_PRODUCTOS"))
                .containsExactly("901|PC-1|PCO-1|PP-1", "902|PC-2|PCO-2|PP-2", "903|OLD-PC|OLD-PCO|OLD-PP");
    }

    @Test
    void bSura08_modificarPerfiles_noFiltra_escribeTambienCamposVacios() {
        seedPerfil(904, "90000003-3");
        adapter.modificarPerfilesPortafolio(List.of(idRow(904, 9, "90000003-3", 0, "")));   // all three profile fields are ""
        assertThat(jdbc.queryForObject("SELECT ID_PERFIL_COMERCIAL FROM PERFILES_COMERCIALES_T WHERE ID_PORTAFOLIO = 904", Map.of(), String.class)).isEmpty();
    }

    @Test
    void bSura02_ingresar_listaMixta_insertaSoloIdIndicadorCero_unaFilaPorPortafolio() {
        adapter.ingresarIndicadoresCobroCustodia(List.of(
                idRow(910, 1, "d", 0, ""), idRow(911, 1, "d", 4, ""), idRow(912, 1, "d", 0, "")), "OPCION 1");
        assertThat(contar(910)).isEqualTo(1);
        assertThat(contar(911)).isZero();
        assertThat(contar(912)).isEqualTo(1);
        assertThat(valorIndicadoresDe(910)).isEqualTo("OPCION 1");   // H2: raw value; HANA: IdDetalleIndicador of the catalogue match
    }

    @Test
    void bSura02_ingresar_dosVeces_noEsIdempotente_duplicaFila() {
        // legacy INSERT has no existence check either; idempotency comes from the IdIndicador == 0 filter upstream
        IdsEntidadYPerfiles p = idRow(913, 1, "d", 0, "");
        adapter.ingresarIndicadoresCobroCustodia(List.of(p), "A");
        adapter.ingresarIndicadoresCobroCustodia(List.of(p), "A");
        assertThat(contar(913)).isEqualTo(2);
    }

    @Test
    void bSura03_modificar_listaMixta_actualizaSoloNoCeroConS() {
        adapter.ingresarIndicadoresCobroCustodia(List.of(idRow(920, 1, "d", 0, ""), idRow(921, 1, "d", 0, ""), idRow(922, 1, "d", 0, "")), "ORIGINAL");
        long i920 = jdbc.queryForObject("SELECT ID_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = 920", Map.of(), Long.class);
        long i921 = jdbc.queryForObject("SELECT ID_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = 921", Map.of(), Long.class);
        long i922 = jdbc.queryForObject("SELECT ID_INDICADOR FROM INDICADORES_PORTAFOLIO_T WHERE ID_ENTIDAD = 922", Map.of(), Long.class);

        IdsEntidadYPerfiles s = idRow(920, 1, "d", i920, "");  s.idFormaCobro = "NUEVO-S";
        IdsEntidadYPerfiles n = idRow(921, 1, "d", i921, "");  n.idFormaCobro = "NUEVO-N"; n.actualizarFormaCobro = "N";
        IdsEntidadYPerfiles cero = idRow(922, 1, "d", 0, "");  cero.idFormaCobro = "NUEVO-CERO";
        adapter.modificarIndicadoresCobroCustodia(List.of(s, n, cero));

        assertThat(valorIndicadoresDe(920)).isEqualTo("NUEVO-S");
        assertThat(valorIndicadoresDe(921)).isEqualTo("ORIGINAL");
        assertThat(valorIndicadoresDe(922)).isEqualTo("ORIGINAL");
        assertThat(i922).isNotEqualTo(0L);
    }

    @Test
    void bSura20_inyeccionEnValorIndicador_seGuardaLiteral_yLasTablasSiguenExistiendo() {
        adapter.ingresarIndicadoresCobroCustodia(List.of(idRow(930, 1, "d", 0, "")), PAYLOAD);
        assertThat(valorIndicadoresDe(930)).isEqualTo(PAYLOAD.length() <= 50 ? PAYLOAD : PAYLOAD.substring(0, 50));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM PORTAFOLIOS_T", Map.of(), Long.class)).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void bSura20_inyeccionEnDocumento_noDevuelveFilasNiDestruyeNada() {
        List<IdsEntidadYPerfiles> r = adapter.consultarIdsPortafolioIdCliente(List.of("' OR '1'='1", PAYLOAD));
        assertThat(r).isEmpty();
        assertThat(adapter.consultarPerfilesComerciales(List.of("' OR '1'='1"))).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM PORTAFOLIOS_T", Map.of(), Long.class)).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void bSura07_consultarIds_documentosRepetidosNoDuplicanFilas() {
        List<IdsEntidadYPerfiles> r = adapter.consultarIdsPortafolioIdCliente(List.of("22222222-2", "22222222-2"));
        assertThat(r).hasSize(1);
        assertThat(r.get(0).idPortafolio).isEqualTo(201L);
        assertThat(r.get(0).idCliente).isEqualTo(2L);
        assertThat(r.get(0).actualizarFormaCobro).isEqualTo("N");
    }
}
