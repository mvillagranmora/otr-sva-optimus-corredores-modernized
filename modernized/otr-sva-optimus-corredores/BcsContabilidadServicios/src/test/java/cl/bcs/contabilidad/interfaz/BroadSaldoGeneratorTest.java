package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.infrastructure.interfaz.BroadSaldoGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code CreaBroadSaldo} (B1_BROAD_SALD_CONT.TXT + B1_CTRL_BROAD_SALD_CONT.TXT).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaBroadSaldo.java
 */
class BroadSaldoGeneratorTest {

    private static final String HEADER =
            "COD-INSTITUCION COD-APLICATIVO COD-ENTIDAD FEC-DIA NUM-REGISTRO COD-CTA-CONTABLE COD-TIPO-CUENTA "
                    + "COD-PRODUCTO-CONTABLE COD-CENTRO-COSTOS COD-MONEDA-CONTABLE MTO-SALDO-CONTABLE FILLER-01FILLER-02";

    @TempDir
    Path base;

    private Path fi() {
        return base.resolve("Transmisiones/Salida/FI");
    }

    @Test
    @DisplayName("CONT-A-06: file names are B1_BROAD_SALD_CONT.TXT / B1_CTRL_BROAD_SALD_CONT.TXT")
    void cont_a_06_broadSaldoFileNames() {
        // CreaInterfazDiaria.java:145-146
        assertThat(BroadSaldoGenerator.ARCHIVO).isEqualTo("B1_BROAD_SALD_CONT.TXT");
        assertThat(BroadSaldoGenerator.ARCHIVO_CONTROL).isEqualTo("B1_CTRL_BROAD_SALD_CONT.TXT");
    }

    @Test
    @DisplayName("CONT-A-14: B1_BROAD_SALD_CONT.TXT is byte-identical to the legacy golden")
    void cont_a_14_broadSaldoDataFileMatchesLegacyGolden() throws Exception {
        // CreaBroadSaldo.java:107-149
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(fi().resolve("B1_BROAD_SALD_CONT.TXT"))).isEqualTo(golden("FI/B1_BROAD_SALD_CONT.TXT"));
    }

    @Test
    @DisplayName("CONT-A-14: header literal (FILLER-01FILLER-02 run together, as legacy)")
    void cont_a_14_broadSaldoHeaderLiteral() throws Exception {
        // CreaBroadSaldo.java:107-120 (no space after "FILLER-01")
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(lineas(fi().resolve("B1_BROAD_SALD_CONT.TXT")).get(0)).isEqualTo(HEADER);
    }

    @Test
    @DisplayName("CONT-A-14: widths 1/2/7/8/10/20/20/8/8/4/20/6/6 = 120 chars; amount at 89-108")
    void cont_a_14_broadSaldoFieldPositions() throws Exception {
        // CreaBroadSaldo.java:129-143
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        String l = lineas(fi().resolve("B1_BROAD_SALD_CONT.TXT")).get(2); // query row #1 (reverse order)
        assertThat(l).hasSize(120);
        assertThat(l.substring(0, 10)).isEqualTo("1B120100  ");
        assertThat(l.substring(10, 18)).isEqualTo("20261012");
        assertThat(l.substring(18, 28)).isEqualTo("3         ");
        assertThat(l.substring(28, 48)).isEqualTo("1101001" + " ".repeat(13));
        assertThat(l.substring(48, 68)).isEqualTo("1" + " ".repeat(19));
        assertThat(l.substring(68, 76)).isEqualTo("PD01    ");
        assertThat(l.substring(76, 84)).isEqualTo(" ".repeat(8));
        assertThat(l.substring(84, 88)).isEqualTo("CLP ");
        assertThat(l.substring(88, 108)).isEqualTo("98765.4321" + " ".repeat(10));
        assertThat(l.substring(108, 120)).isEqualTo(" ".repeat(12));
        assertThat(lineas(fi().resolve("B1_BROAD_SALD_CONT.TXT")).get(1).substring(88, 108))
                .isEqualTo("0.0000" + " ".repeat(14));
    }

    @Test
    @DisplayName("CONT-A-04: NUM-REGISTRO column (19-28) carries the contador, never the row's NumRegistro")
    void cont_a_04_broadSaldoNumRegistroIsContador() throws Exception {
        // CreaBroadSaldo.java:134 String.format("%1$-10s", contador)
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base, 1234567L));
        assertThat(texto(fi().resolve("B1_BROAD_SALD_CONT.TXT"))).doesNotContain("IGNORED");
        assertThat(lineas(fi().resolve("B1_BROAD_SALD_CONT.TXT")).get(1).substring(18, 28)).isEqualTo("1234567   ");
    }

    @Test
    @DisplayName("CONT-A-15: B1_CTRL_BROAD_SALD_CONT.TXT is byte-identical to the legacy golden")
    void cont_a_15_broadSaldoControlMatchesLegacyGolden() throws Exception {
        // CreaBroadSaldo.java:173-227
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT")))
                .isEqualTo(golden("FI/B1_CTRL_BROAD_SALD_CONT.TXT"));
    }

    @Test
    @DisplayName("CONT-A-15: control has sub-rutina CONSULTABROADSALDO, pos 089/108 and an EMPTY sum")
    void cont_a_15_broadSaldoControlLayout() throws Exception {
        // CreaBroadSaldo.java:193-222 (setMtoSumcampo("") -> no total is ever written)
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT"))).isEqualTo(
                "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA DES-CAMPOVALIDAR POS-INICIAL POS-FINAL "
                        + "MTO-SUMCAMPO MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n"
                        + "C"
                        + "SCRIPT" + " ".repeat(24)
                        + "CONSULTABROADSALDO" + " ".repeat(12)
                        + "B1_BROAD_SALD_CONT.TXT" + " ".repeat(8)
                        + "MTO-SALDO-CONTABLE"
                        + "089"
                        + "108"
                        + "2" + " ".repeat(14)
                        + "2049    "     // 2026 + 10 + 13
                        + "3:4:5     "); // 15:04:05 -> HOUR(12h)=3, unpadded minute/second
    }

    @ParameterizedTest(name = "{0} @ {1} -> [{2}][{3}]")
    @CsvSource(delimiter = '|', value = {
            // instant              | zone             | FEC-PROCESO | HRA-PROCESO
            "2026-10-13T18:04:05Z   | UTC              | '2049    '  | '6:4:5     '",
            "2026-10-13T15:30:00Z   | America/Santiago | '2049    '  | '0:30:0    '",
            "2026-12-31T23:59:59Z   | UTC              | '2069    '  | '11:59:59  '",
            "2027-01-01T02:59:59Z   | America/Santiago | '2069    '  | '11:59:59  '",
            "2026-01-01T00:00:00Z   | UTC              | '2028    '  | '0:0:0     '"
    })
    @DisplayName("CONT-A-28: FEC-PROCESO = YEAR+MONTH+DAY as an integer SUM; HRA-PROCESO = 12h H:m:s unpadded")
    void cont_a_28_controlTimestampArithmetic(String instant, String zone, String fec, String hra) throws Exception {
        // CreaBroadSaldo.java:203-210: c.get(YEAR) + (c.get(MONTH)+1) + c.get(DAY_OF_MONTH) is int addition;
        // Calendar.HOUR is the 12-hour field. Data-file mtime == clock instant (file just written).
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneId.of(zone));
        new BroadSaldoGenerator(new FakeSapSource(), clock).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT"))).endsWith("2" + " ".repeat(14) + fec + hra);
    }

    @Test
    @DisplayName("CONT-A-36: no rows -> header-only data file, count '0'")
    void cont_a_36_broadSaldoEmptyResult() throws Exception {
        // CreaBroadSaldo.java:107-120,201
        FakeSapSource sap = new FakeSapSource();
        sap.saldosBroad = new ArrayList<>();
        new BroadSaldoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_BROAD_SALD_CONT.TXT"))).isEqualTo(HEADER + "\n");
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT")))
                .endsWith("MTO-SALDO-CONTABLE089108" + "0" + " ".repeat(14) + "2049    3:4:5     ");
    }

    @Test
    @DisplayName("CONT-A-26: control file has no trailing line terminator")
    void cont_a_26_broadSaldoControlHasNoTrailingNewline() throws Exception {
        // CreaBroadSaldo.java:212-227 (no "\n" after getHraProceso())
        new BroadSaldoGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        byte[] b = Files.readAllBytes(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT"));
        assertThat(b[b.length - 1]).isEqualTo((byte) ' ');
        assertThat(lineas(fi().resolve("B1_CTRL_BROAD_SALD_CONT.TXT"))).hasSize(2);
    }

    @Test
    @DisplayName("CONT-A-02: saldo query receives fechaEjecucion")
    void cont_a_02_broadSaldoQueryReceivesFechaEjecucion() throws Exception {
        // CreaBroadSaldo.java:88-90
        FakeSapSource sap = new FakeSapSource();
        new BroadSaldoGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(sap.llamadas).containsExactly("SALDOS_BROAD@2026-10-12");
    }
}
