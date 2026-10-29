package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.MovimientoBpc;
import cl.bcs.contabilidad.infrastructure.interfaz.MovimientoBpcGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization of legacy {@code CreaMovimientoBPC} (B1_BROAD_BPC.TXT + B1_CTRL_BROAD_BPC.TXT).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaMovimientoBPC.java
 */
class MovimientoBpcGeneratorTest {

    private static final String HEADER =
            "SOCIEDAD CUENTA-DE-MAYOR VARIANTE-DE-EJERCICIO EJERCICIO-PERIODO NRO-DOCUMENTO-CONTABLE "
                    + "NRO-APUNTE-CONTABLE SEGMENTO LEDGER SOC-GL-ASOCIADA TOTAL-CONTAB-DEBE TOTAL-CONTB-HABER "
                    + "CUENTA-OPERATIVA CLASE-DE-REGISTRO TIPO-CUENTA INDICADOR-ANULACION ";

    @TempDir
    Path base;

    private Path bpc() {
        return base.resolve("Transmisiones/Salida/BPC");
    }

    @Test
    @DisplayName("CONT-A-06: file names are B1_BROAD_BPC.TXT / B1_CTRL_BROAD_BPC.TXT (BPC directory)")
    void cont_a_06_movimientoBpcFileNames() {
        // CreaInterfazDiaria.java:185-186
        assertThat(MovimientoBpcGenerator.ARCHIVO).isEqualTo("B1_BROAD_BPC.TXT");
        assertThat(MovimientoBpcGenerator.ARCHIVO_CONTROL).isEqualTo("B1_CTRL_BROAD_BPC.TXT");
    }

    @Test
    @DisplayName("CONT-A-22: B1_BROAD_BPC.TXT is byte-identical to the legacy golden")
    void cont_a_22_movimientoBpcDataFileMatchesLegacyGolden() throws Exception {
        // CreaMovimientoBPC.java:107-154
        new MovimientoBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(bpc().resolve("B1_BROAD_BPC.TXT"))).isEqualTo(golden("BPC/B1_BROAD_BPC.TXT"));
    }

    @Test
    @DisplayName("CONT-A-22: header lists 15 labels that do NOT match the 14 detail columns (kept as legacy)")
    void cont_a_22_movimientoBpcHeaderLiteral() throws Exception {
        // CreaMovimientoBPC.java:107-122 vs 130-143 (header has CUENTA-OPERATIVA/TIPO-CUENTA, lacks MONEDA)
        new MovimientoBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(lineas(bpc().resolve("B1_BROAD_BPC.TXT")).get(0)).isEqualTo(HEADER);
    }

    @Test
    @DisplayName("CONT-A-22: widths 4/10/2/7/10/3/2/2/4/5/20/20/1/1 = 91 chars; negative debe kept with sign")
    void cont_a_22_movimientoBpcFieldPositions() throws Exception {
        // CreaMovimientoBPC.java:130-143
        new MovimientoBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        List<String> l = lineas(bpc().resolve("B1_BROAD_BPC.TXT"));
        String r3 = l.get(1); // query row #3 (reverse order)
        assertThat(r3).hasSize(91);
        assertThat(r3.substring(0, 4)).isEqualTo("2010");
        assertThat(r3.substring(4, 14)).isEqualTo("1101001   ");
        assertThat(r3.substring(14, 16)).isEqualTo("K4");
        assertThat(r3.substring(16, 23)).isEqualTo("2026010");
        assertThat(r3.substring(23, 33)).isEqualTo("5002      ");
        assertThat(r3.substring(33, 36)).isEqualTo("1  ");
        assertThat(r3.substring(36, 40)).isEqualTo("SB0L");
        assertThat(r3.substring(40, 44)).isEqualTo("    ");
        assertThat(r3.substring(44, 49)).isEqualTo("USD  ");
        assertThat(r3.substring(49, 69)).isEqualTo("-25.5000" + " ".repeat(12));
        assertThat(r3.substring(69, 89)).isEqualTo("0.0000" + " ".repeat(14));
        assertThat(r3.substring(89, 91)).isEqualTo(" X");
        assertThat(l.get(2).substring(40, 44)).isEqualTo("0001");
    }

    @Test
    @DisplayName("CONT-A-23: B1_CTRL_BROAD_BPC.TXT is byte-identical to the legacy golden")
    void cont_a_23_movimientoBpcControlMatchesLegacyGolden() throws Exception {
        // CreaMovimientoBPC.java:179-245
        new MovimientoBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(bpc().resolve("B1_CTRL_BROAD_BPC.TXT"))).isEqualTo(golden("BPC/B1_CTRL_BROAD_BPC.TXT"));
    }

    @Test
    @DisplayName("CONT-A-23: control carries padded debe/haber labels, blank positions, totals 1474.5000 / 1500.0000")
    void cont_a_23_movimientoBpcControlLayoutAndTotals() throws Exception {
        // CreaMovimientoBPC.java:203-240; debe = 1500.0000 + 0.0000 + (-25.5000); haber = 0 + 1500.0000 + 0
        new MovimientoBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(texto(bpc().resolve("B1_CTRL_BROAD_BPC.TXT"))).isEqualTo(
                "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA DES-CAMPOVALIDAR-1 POS-INICIAL-1 POS-FINAL-1 "
                        + "MTO-SUMCAMPO-1 DES-CAMPOVALIDAR-2 POS-INICIAL-2 POS-FINAL-2 MTO-SUMCAMPO-2 MTO-SUMCTAREG "
                        + "FEC-PROCESO HRA-PROCESO \n"
                        + "C"
                        + "SCRIPT" + " ".repeat(24)
                        + " ".repeat(30)
                        + "B1_BROAD_BPC.TXT" + " ".repeat(14)
                        + "TOTAL-CONTAB-DEBE" + " ".repeat(3)
                        + " ".repeat(3) + " ".repeat(3)
                        + "1474.5000" + " ".repeat(11)
                        + "TOTAL-CONTAB-HABER" + " ".repeat(2)
                        + " ".repeat(3) + " ".repeat(3)
                        + "1500.0000" + " ".repeat(11)
                        + "3" + " ".repeat(14)
                        + "2049    "
                        + "3:4:5     ");
    }

    @Test
    @DisplayName("CONT-A-36: no rows -> header-only data file, totals '0', count '0'")
    void cont_a_36_movimientoBpcEmptyResult() throws Exception {
        // CreaMovimientoBPC.java:86-87 new BigDecimal(0)
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBpc = new ArrayList<>();
        new MovimientoBpcGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(texto(bpc().resolve("B1_BROAD_BPC.TXT"))).isEqualTo(HEADER + "\n");
        assertThat(texto(bpc().resolve("B1_CTRL_BROAD_BPC.TXT"))).endsWith(
                "0" + " ".repeat(19) + "TOTAL-CONTAB-HABER  " + " ".repeat(6) + "0" + " ".repeat(19)
                        + "0" + " ".repeat(14) + "2049    3:4:5     ");
    }

    @Test
    @DisplayName("CONT-A-29: a null haber aborts the family: generator throws, neither file is written")
    void cont_a_29_movimientoBpcNullAmountAbortsFamily() {
        // CreaMovimientoBPC.java:146 totalhaber.add(null) -> NPE (only IOException is caught)
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBpc = new ArrayList<>(List.of(new MovimientoBpc("2010", "1101001", "K4", "2026010", "5001",
                "1", "SB", "0L", "", "CLP", new BigDecimal("1.0000"), null, "", "")));
        assertThatThrownBy(() -> new MovimientoBpcGenerator(sap, CLOCK).generar(contexto(base)))
                .isInstanceOf(RuntimeException.class);
        assertThat(bpc().resolve("B1_BROAD_BPC.TXT")).doesNotExist();
        assertThat(bpc().resolve("B1_CTRL_BROAD_BPC.TXT")).doesNotExist();
    }

    @Test
    @DisplayName("CONT-A-02: movimiento BPC query receives fechaEjecucion")
    void cont_a_02_movimientoBpcQueryReceivesFechaEjecucion() throws Exception {
        // CreaMovimientoBPC.java:90-92
        FakeSapSource sap = new FakeSapSource();
        new MovimientoBpcGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(sap.llamadas).containsExactly("MOVIMIENTOS_BPC@2026-10-12");
    }
}
