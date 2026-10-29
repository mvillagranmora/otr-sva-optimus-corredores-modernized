package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.envio.support.CasoEnvio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the WP-B golden set: every recorded case is exercised by exactly one test class, every case has an
 * expected.json, and every SQL resource the services must load exists on the main classpath.
 */
class EnvioGoldenInventoryTest {

    /** SQL the modern services are expected to load verbatim from src/main/resources/cl/bcs/contabilidad/sql/. */
    static final List<String> SQL_REQUERIDOS = List.of(
            "EnvioContabilidad_SelectPreComprobante.sql",
            "EnvioContabilidad_SelectHomologacionCampos.sql",
            "EnvioContabilidad_SelectDefinicionAsientoDetallado.sql",
            "EnvioContabilidad_SelectDefinicionAsientoResumido.sql",
            "EnvioContabilidad_SelectAsientoProcesoDetallado.sql",
            "EnvioContabilidad_SelectAsientoProcesoResumido.sql",
            "EnvioContabilidad_SelectPrecomprobanteCreacion.sql",
            "EnvioContabilidad_SelectMovimientoDato.sql",
            "EnvioContabilidad_InsertPreComprobanteEnvioContabilidad.sql",
            "EnvioContabilidad_InsertPreComprobanteAsientoEnvioContabilidad.sql",
            "EnvioContabilidad_InsertPreComprobanteCentralizacionTipo.sql",
            "EnvioContabilidad_SelectOACT.sql",
            "InterfazContable_SelectPreComprobante.sql",
            "InterfazContable_SelectPrecomprobanteAsiento.sql",
            "ReversaPreComprobante_SelectReversaPrecomprobante.sql",
            "ReversaPreComprobante_SelectPrecomprobanteAnterior.sql",
            "ReversaPreComprobante_SelectPrecomprobanteAsiento.sql",
            "ReversaPreComprobante_SelectEventoPrecomprobanteAsiento.sql",
            "ReversaPreComprobante_InsertPrecomprobante.sql",
            "ReversaPreComprobante_InsertPrecomprobanteAsiento.sql",
            "ReversaPreComprobante_InsertEventoPrecomprobanteAsiento.sql");

    @Test
    @DisplayName("WP-B inventory: every case directory has expected.json and is run by exactly one test class")
    void cadaCasoGrabadoTieneTestYGolden() {
        List<String> declarados = new ArrayList<>();
        Stream.of(EnvioContabilidadServiceTest.CASOS, GenerarJsonAsientoSapServiceTest.CASOS,
                ReversaPreComprobanteServiceTest.CASOS, InterfazContableServiceTest.CASOS).forEach(declarados::addAll);

        assertThat(declarados).doesNotHaveDuplicates();
        assertThat(CasoEnvio.todos()).as("case directories under envio/cases").containsExactlyInAnyOrderElementsOf(declarados);
        for (String c : declarados) {
            assertThat(Files.exists(CasoEnvio.recurso(CasoEnvio.RAIZ + "/cases/" + c).resolve("expected.json")))
                    .as(c + " expected.json (record with legacy-oracle/runner/OracleRunner.java)").isTrue();
        }
        System.out.println("WP-B golden cases on disk: " + declarados.size());
    }

    @Test
    @DisplayName("WP-B SQL contract: every SQL resource used by EnvioContabilidad / GenerarJsonAsientoSap / ReversaPreComprobante exists and is non-blank")
    void recursosSqlPresentes() throws Exception {
        for (String n : SQL_REQUERIDOS) {
            var url = getClass().getClassLoader().getResource("cl/bcs/contabilidad/sql/" + n);
            assertThat(url).as("src/main/resources/cl/bcs/contabilidad/sql/" + n).isNotNull();
            try (var in = url.openStream()) {
                assertThat(new String(in.readAllBytes())).as(n).isNotBlank();
            }
        }
    }
}
