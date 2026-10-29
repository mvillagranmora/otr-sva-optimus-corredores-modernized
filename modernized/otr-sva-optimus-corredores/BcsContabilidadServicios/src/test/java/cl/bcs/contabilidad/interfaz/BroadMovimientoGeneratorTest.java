package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadMovimiento;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadMovimientoGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization of legacy {@code CreaBroadMovimiento} (B1_BROAD_MOV_CONT.TXT + B1_CTRL_BROAD_MOV_CONT.TXT).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaBroadMovimiento.java
 */
class BroadMovimientoGeneratorTest {

    private static final String HEADER =
            "COD-INSTITUCION COD-APLICATIVO COD-ENTIDAD FEC-DIA HRA-TRASACCION NUM-OPERACION NUM-POS_OPERACION "
                    + "NUM-REGISTRO COD-CTA-CONTABLE COD-TIPO-CUENTA COD-PRODUCTO-CONTABLE COD-CENTRO-COSTO "
                    + "FEC-DOCUMENTO NRO-SERIE-DOC NRO-DOC COD-AFILIADA TIP-CARGO-ABONO COD-MONEDA-CONTABLE "
                    + "MTO-TRANSACCION-CONTABLE IND-ANULADO IND-OPE-AFILIADA FILLER-01 FILLER-02";

    private static final String CTRL_HEADER =
            "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCTURA DES-CAMPOVALIDAR POS-INICIAL POS-FINAL "
                    + "MTO-SUMCAMPO MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n";

    @TempDir
    Path base;

    private Path fi() {
        return base.resolve("Transmisiones/Salida/FI");
    }

    private static BroadMovimiento mov(Integer hra, String numRegistro, BigDecimal monto) {
        return new BroadMovimiento("1", "B1", "20100", LocalDate.of(2026, 10, 12), hra, "5001", "1", numRegistro,
                "1101001", "1", "PD01", "", LocalDate.of(2026, 10, 12), "13", "F-1001", "", "D", "CLP",
                monto, "N", "", "", "");
    }

    @Test
    @DisplayName("CONT-A-06: file names are B1_BROAD_MOV_CONT.TXT / B1_CTRL_BROAD_MOV_CONT.TXT")
    void cont_a_06_broadMovimientoFileNames() {
        // CreaInterfazDiaria.java:132-133
        assertThat(BroadMovimientoGenerator.ARCHIVO).isEqualTo("B1_BROAD_MOV_CONT.TXT");
        assertThat(BroadMovimientoGenerator.ARCHIVO_CONTROL).isEqualTo("B1_CTRL_BROAD_MOV_CONT.TXT");
    }

    @Test
    @DisplayName("CONT-A-12: B1_BROAD_MOV_CONT.TXT is byte-identical to the legacy golden (contador 3)")
    void cont_a_12_broadMovimientoDataFileMatchesLegacyGolden() throws Exception {
        // CreaBroadMovimiento.java:113-177
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(fi().resolve("B1_BROAD_MOV_CONT.TXT"))).isEqualTo(golden("FI/B1_BROAD_MOV_CONT.TXT"));
    }

    @Test
    @DisplayName("CONT-A-12: header literal (HRA-TRASACCION, NUM-POS_OPERACION typos kept)")
    void cont_a_12_broadMovimientoHeaderLiteral() throws Exception {
        // CreaBroadMovimiento.java:113-136
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).get(0)).isEqualTo(HEADER);
    }

    @Test
    @DisplayName("CONT-A-12: 23 fixed-width columns, 180 chars, dates yyyyMMdd, amount at 151-170")
    void cont_a_12_broadMovimientoFieldPositions() throws Exception {
        // CreaBroadMovimiento.java:144-170
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        String l = lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).get(2); // query row #2 (reverse order)
        assertThat(l).hasSize(180);
        assertThat(l.substring(0, 1)).isEqualTo("1");
        assertThat(l.substring(1, 3)).isEqualTo("B1");
        assertThat(l.substring(3, 10)).isEqualTo("20100  ");
        assertThat(l.substring(10, 18)).isEqualTo("20261012");
        assertThat(l.substring(18, 26)).isEqualTo("1435    ");
        assertThat(l.substring(26, 36)).isEqualTo("5001      ");
        assertThat(l.substring(36, 46)).isEqualTo("2         ");
        assertThat(l.substring(46, 56)).isEqualTo("3         ");
        assertThat(l.substring(56, 76)).isEqualTo("2101005" + " ".repeat(13));
        assertThat(l.substring(76, 96)).isEqualTo("2" + " ".repeat(19));
        assertThat(l.substring(96, 104)).isEqualTo(" ".repeat(8));
        assertThat(l.substring(104, 112)).isEqualTo("CC10    ");
        assertThat(l.substring(112, 120)).isEqualTo("20261011");
        assertThat(l.substring(120, 130)).isEqualTo("13        ");
        assertThat(l.substring(130, 141)).isEqualTo("F-1001     ");
        assertThat(l.substring(141, 145)).isEqualTo("0001");
        assertThat(l.substring(145, 146)).isEqualTo("C");
        assertThat(l.substring(146, 150)).isEqualTo("CLP ");
        assertThat(l.substring(150, 170)).isEqualTo("1500.0000" + " ".repeat(11));
        assertThat(l.substring(170, 172)).isEqualTo("N ");
        assertThat(l.substring(172, 180)).isEqualTo(" ".repeat(8));
    }

    @Test
    @DisplayName("CONT-A-04: NUM-REGISTRO column (47-56) carries the contador, never the row's NumRegistro")
    void cont_a_04_broadMovimientoNumRegistroIsContador() throws Exception {
        // CreaBroadMovimiento.java:153 String.format("%1$-10s", contador)
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base, 42L));
        String s = texto(fi().resolve("B1_BROAD_MOV_CONT.TXT"));
        assertThat(s).doesNotContain("IGNORED");
        for (String l : lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).subList(1, 4)) {
            assertThat(l.substring(46, 56)).isEqualTo("42        ");
        }
    }

    @Test
    @DisplayName("CONT-A-25: null HRA-TRASACCION renders as 'null' padded to 8")
    void cont_a_25_broadMovimientoNullHora() throws Exception {
        // CreaBroadMovimiento.java:150 String.format("%1$-8s", (Short) null)
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBroad = new ArrayList<>(List.of(mov(null, "", new BigDecimal("1.0000"))));
        new BroadMovimientoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).get(1).substring(18, 26)).isEqualTo("null    ");
    }

    @Test
    @DisplayName("CONT-A-35: amounts use BigDecimal.toString() (scientific form kept, not toPlainString)")
    void cont_a_35_broadMovimientoAmountUsesBigDecimalToString() throws Exception {
        // CreaBroadMovimiento.java:165 %-20s on BigDecimal; :230 totalMtoSumCampo.toString(). The total starts as
        // new BigDecimal(0) (:90, scale 0), and 0 + 1E+3 has scale max(0,-3) = 0, so the control total prints "1000".
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBroad = new ArrayList<>(List.of(mov(1435, "", new BigDecimal("1E+3"))));
        new BroadMovimientoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).get(1).substring(150, 170))
                .isEqualTo("1E+3" + " ".repeat(16));
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_MOV_CONT.TXT"))).contains("15117010001" + " ".repeat(14));
    }

    @Test
    @DisplayName("CONT-A-13: B1_CTRL_BROAD_MOV_CONT.TXT is byte-identical to the legacy golden")
    void cont_a_13_broadMovimientoControlMatchesLegacyGolden() throws Exception {
        // CreaBroadMovimiento.java:202-257
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(fi().resolve("B1_CTRL_BROAD_MOV_CONT.TXT")))
                .isEqualTo(golden("FI/B1_CTRL_BROAD_MOV_CONT.TXT"));
    }

    @Test
    @DisplayName("CONT-A-13: control carries unpadded campo/pos 151/170 and the unpadded total 3250.5000")
    void cont_a_13_broadMovimientoControlLayoutAndTotal() throws Exception {
        // CreaBroadMovimiento.java:223-252; total = 1500.0000 + 1500.0000 + 250.5000
        new BroadMovimientoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_MOV_CONT.TXT"))).isEqualTo(
                CTRL_HEADER
                        + "C"
                        + "SCRIPT" + " ".repeat(24)
                        + " ".repeat(30)
                        + "B1_BROAD_MOV_CONT.TXT" + " ".repeat(9)
                        + "MTO-TRANSACCION-CONTABLE"
                        + "151"
                        + "170"
                        + "3250.5000"
                        + "3" + " ".repeat(14)
                        + "2012    "    // epoch quirk, see CONT-A-28
                        + "9:0:0     ");
    }

    @Test
    @DisplayName("CONT-A-36: no rows -> header-only data file, total '0', count '0'")
    void cont_a_36_broadMovimientoEmptyResult() throws Exception {
        // CreaBroadMovimiento.java:90 new BigDecimal(0).toString() == "0"
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBroad = new ArrayList<>();
        new BroadMovimientoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_BROAD_MOV_CONT.TXT"))).isEqualTo(HEADER + "\n");
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_MOV_CONT.TXT")))
                .endsWith("MTO-TRANSACCION-CONTABLE1511700" + "0" + " ".repeat(14) + "2012    9:0:0     ");
    }

    @Test
    @DisplayName("CONT-A-29: a null amount aborts the family: generator throws, neither file is written")
    void cont_a_29_broadMovimientoNullAmountAbortsFamily() {
        // CreaBroadMovimiento.java:172 totalMtoSumCampo.add(null) -> NPE, not caught (catch IOException only)
        FakeSapSource sap = new FakeSapSource();
        sap.movimientosBroad = new ArrayList<>(List.of(mov(1435, "", new BigDecimal("1.0000")), mov(1435, "", null)));
        assertThatThrownBy(() -> new BroadMovimientoGenerator(sap, CLOCK).generar(contexto(base)))
                .isInstanceOf(RuntimeException.class);
        assertThat(fi().resolve("B1_BROAD_MOV_CONT.TXT")).doesNotExist();
        assertThat(fi().resolve("B1_CTRL_BROAD_MOV_CONT.TXT")).doesNotExist();
    }

    @Test
    @DisplayName("CONT-A-02: movimiento query receives fechaEjecucion")
    void cont_a_02_broadMovimientoQueryReceivesFechaEjecucion() throws Exception {
        // CreaBroadMovimiento.java:94-96 parmsIN.put("FechaEjecucion", fecha)
        FakeSapSource sap = new FakeSapSource();
        new BroadMovimientoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(sap.llamadas).containsExactly("MOVIMIENTOS_BROAD@2026-10-12");
    }
}
