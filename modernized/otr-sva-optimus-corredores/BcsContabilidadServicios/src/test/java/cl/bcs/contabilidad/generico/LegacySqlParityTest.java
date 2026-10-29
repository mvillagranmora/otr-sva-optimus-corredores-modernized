package cl.bcs.contabilidad.generico;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CONT-C-90: SQL that WP-C keeps in SQL must be ported byte-for-byte (modulo line
 * endings and surrounding whitespace) from the legacy oracle files. CONT-C-91: the new
 * raw CarteraValorizada queries must exist as classpath resources.
 *
 * The legacy directory is read from system property {@code legacy.sql.dir}, defaulting to
 * the workspace layout relative to the module directory. If the legacy file is missing
 * the test FAILS (a comparison that cannot run is not a pass).
 */
class LegacySqlParityTest {

    static final String RES = "cl/bcs/contabilidad/sql/";
    static final Path LEGACY_DIR = Path.of(System.getProperty("legacy.sql.dir",
            "../../../legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/sql"));

    static final AtomicInteger EXECUTED = new AtomicInteger();

    @AfterAll
    static void report() {
        System.out.println("equivalence cases executed: " + EXECUTED.get() + " (LegacySqlParityTest)");
    }

    private static String normalize(String s) {
        return s.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    private static String classpath(String name) throws IOException {
        try (InputStream in = LegacySqlParityTest.class.getClassLoader().getResourceAsStream(RES + name)) {
            assertThat(in).as("classpath resource %s%s must exist", RES, name).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @ParameterizedTest(name = "CONT-C-90 {0} identical to legacy")
    @DisplayName("CONT-C-90: verbatim-ported SQL resources equal the legacy files")
    @ValueSource(strings = {
            // Datos queries kept in SQL (Devengo*.java load them at :22 / :22 / :290)
            "CentralizaDevengoATasaCompraCarteraPactada_SelectDatosMovimiento.sql",
            "CentralizaDevengoATasaPacto_SelectDatosMovimiento.sql",
            "CentralizaDevengoCarteraPropiaLibreYGarantia_SelectDatosMovimiento.sql",
            // ConsultarCondicionesConceptosNormativos.java:18
            "ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql",
            // CentralizaGenerico.java:37-51 (used by the CentralizaGenericoPort JDBC adapter)
            "CentralizaGenerico_SelectTipoCentralizacion.sql",
            "CentralizaGenerico_SelectCentralizacion.sql",
            "CentralizaGenerico_SelectMovimiento.sql",
            "CentralizaGenerico_SelectEventoTipo.sql",
            "CentralizaGenerico_SelectEventoTipoDato.sql",
            "CentralizaGenerico_SelectEventoTipoDatoValor.sql",
            "CentralizaGenerico_InsertCentralizacion.sql",
            "CentralizaGenerico_InsertMovimiento.sql",
            "CentralizaGenerico_InsertMovimientoDato.sql",
            "CentralizaGenerico_InsertEvento.sql",
            "CentralizaGenerico_InsertLog.sql",
            "CentralizaGenerico_InsertEventoTipo.sql",
            "CentralizaGenerico_InsertMovimientoPaso.sql",
            "CentralizaGenerico_InsertDatoValorPaso.sql"
    })
    void contC90_sqlVerbatim(String name) throws IOException {
        Path legacy = LEGACY_DIR.resolve(name);
        assertThat(Files.isRegularFile(legacy))
                .as("legacy oracle file %s must be readable (set -Dlegacy.sql.dir)", legacy.toAbsolutePath())
                .isTrue();

        assertThat(normalize(classpath(name))).isEqualTo(normalize(Files.readString(legacy, StandardCharsets.UTF_8)));
        EXECUTED.incrementAndGet();
    }

    @ParameterizedTest(name = "CONT-C-91 {0} exists and reads CarteraValorizadaIFRF by :p_IdEmpresa/:p_IdInforme")
    @DisplayName("CONT-C-91: raw CarteraValorizada queries exist (movement / event projections of the legacy SQL)")
    @ValueSource(strings = {
            "CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraMovimientos.sql",
            "CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraEventos.sql",
            "CentralizaDevengoATasaPacto_SelectCarteraMovimientos.sql",
            "CentralizaDevengoATasaPacto_SelectCarteraEventos.sql",
            "CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraMovimientos.sql",
            "CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraEventos.sql"
    })
    void contC91_rawQueriesExist(String name) throws IOException {
        String sql = classpath(name);
        assertThat(sql).contains("\"BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF\"")
                .contains(":p_IdEmpresa").contains(":p_IdInforme");
        EXECUTED.incrementAndGet();
    }
}
