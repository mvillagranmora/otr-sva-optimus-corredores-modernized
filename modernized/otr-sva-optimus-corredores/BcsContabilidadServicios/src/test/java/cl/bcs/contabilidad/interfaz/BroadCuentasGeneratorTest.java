package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadCuenta;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadCuentasGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code CreaBroadCuentas} (B1_BROAD_CTAS.TXT + B1_CTRL_BROAD_CTAS.TXT).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaBroadCuentas.java
 */
class BroadCuentasGeneratorTest {

    @TempDir
    Path base;

    private Path fi() {
        return base.resolve("Transmisiones/Salida/FI");
    }

    private FakeSapSource generar(Clock clock) throws Exception {
        FakeSapSource sap = new FakeSapSource();
        new BroadCuentasGenerator(sap, clock).generar(contexto(base));
        return sap;
    }

    @Test
    @DisplayName("CONT-A-06: file names are B1_BROAD_CTAS.TXT / B1_CTRL_BROAD_CTAS.TXT")
    void cont_a_06_broadCuentasFileNames() {
        // CreaInterfazDiaria.java:120-121
        assertThat(BroadCuentasGenerator.ARCHIVO).isEqualTo("B1_BROAD_CTAS.TXT");
        assertThat(BroadCuentasGenerator.ARCHIVO_CONTROL).isEqualTo("B1_CTRL_BROAD_CTAS.TXT");
    }

    @Test
    @DisplayName("CONT-A-10: B1_BROAD_CTAS.TXT is byte-identical to the legacy golden")
    void cont_a_10_broadCuentasDataFileMatchesLegacyGolden() throws Exception {
        // CreaBroadCuentas.java:92-128 (golden produced by executing those statements)
        generar(CLOCK);
        assertThat(leer(fi().resolve("B1_BROAD_CTAS.TXT"))).isEqualTo(golden("FI/B1_BROAD_CTAS.TXT"));
    }

    @Test
    @DisplayName("CONT-A-10: header line literal, space separated, mislabelled COD-CUENTA-CONTABLE kept")
    void cont_a_10_broadCuentasHeaderLiteral() throws Exception {
        // CreaBroadCuentas.java:92-103
        generar(CLOCK);
        assertThat(lineas(fi().resolve("B1_BROAD_CTAS.TXT")).get(0)).isEqualTo(
                "COD-ENTIDAD COD-CTA-CONTABLE DESCRIPCION TIPO-CUENTA CUENTA-GRUPO COD-CUENTA-CONTABLE "
                        + "IND_CTA_CONFIDENCIAL IND_CTA-ASOCIADA IND-CTA-MONETARIA IND_RELEVANTE_FLUJO-CAJA "
                        + "IND-CTA-AFILIADA");
    }

    @Test
    @DisplayName("CONT-A-10: detail widths 7/15/100/20/10/4/1/1/1/1/1 = 161 chars, no separators")
    void cont_a_10_broadCuentasFieldPositions() throws Exception {
        // CreaBroadCuentas.java:111-121
        generar(CLOCK);
        String l = lineas(fi().resolve("B1_BROAD_CTAS.TXT")).get(3); // query row #1 (written last)
        assertThat(l).hasSize(161);
        assertThat(l.substring(0, 7)).isEqualTo("20100  ");
        assertThat(l.substring(7, 22)).isEqualTo("1101001" + " ".repeat(8));
        assertThat(l.substring(22, 122)).isEqualTo("Caja moneda nacional" + " ".repeat(80));
        assertThat(l.substring(122, 142)).isEqualTo("1" + " ".repeat(19));
        assertThat(l.substring(142, 152)).isEqualTo("110100    ");
        assertThat(l.substring(152, 156)).isEqualTo("CLP ");
        assertThat(l.substring(156, 161)).isEqualTo("NNYYN");
    }

    @Test
    @DisplayName("CONT-A-24: rows are written in reverse query order")
    void cont_a_24_broadCuentasRowsReversed() throws Exception {
        // CreaBroadCuentas.java:105-109 (listIterator(size) + hasPrevious)
        generar(CLOCK);
        List<String> l = lineas(fi().resolve("B1_BROAD_CTAS.TXT"));
        assertThat(l).hasSize(5); // header + 3 rows + "" after the final LF
        assertThat(l.get(1)).startsWith("20100  3101001");
        assertThat(l.get(2)).startsWith("20100  2101005");
        assertThat(l.get(3)).startsWith("20100  1101001");
        assertThat(l.get(4)).isEmpty();
    }

    @Test
    @DisplayName("CONT-A-25: a null field is written as the literal text 'null', padded")
    void cont_a_25_broadCuentasNullFieldWrittenAsNull() throws Exception {
        // CreaBroadCuentas.java:114 String.format("%1$-20s", null) -> "null" + 16 spaces
        generar(CLOCK);
        String l = lineas(fi().resolve("B1_BROAD_CTAS.TXT")).get(1);
        assertThat(l.substring(122, 142)).isEqualTo("null" + " ".repeat(16));
    }

    @Test
    @DisplayName("CONT-A-25: values longer than the width are NOT truncated (line grows, columns shift)")
    void cont_a_25_broadCuentasNoTruncation() throws Exception {
        // CreaBroadCuentas.java:113 %-100s pads but never truncates
        FakeSapSource sap = new FakeSapSource();
        String larga = "D".repeat(105);
        sap.cuentasBroad = new ArrayList<>(List.of(
                new BroadCuenta("20100", "1101001", larga, "1", "110100", "CLP", "N", "N", "N", "N", "N")));
        new BroadCuentasGenerator(sap, CLOCK).generar(contexto(base));
        String l = lineas(fi().resolve("B1_BROAD_CTAS.TXT")).get(1);
        assertThat(l).hasSize(166);
        assertThat(l.substring(22, 127)).isEqualTo(larga);
        assertThat(l.substring(127, 147)).isEqualTo("1" + " ".repeat(19));
    }

    @Test
    @DisplayName("CONT-A-26: ISO-8859-1 (ñ = 0xF1, unmappable € = '?'), LF terminators, no CR")
    void cont_a_26_broadCuentasEncodingAndTerminators() throws Exception {
        // CreaBroadCuentas.java:128 writeStringToFile(..., "ISO-8859-1", true); lines end with "\n" (103,122)
        generar(CLOCK);
        byte[] b = leer(fi().resolve("B1_BROAD_CTAS.TXT"));
        String s = texto(fi().resolve("B1_BROAD_CTAS.TXT"));
        assertThat(s).contains("Proveedores Compañía Ñuñoa");
        assertThat(s).contains("Capital ?uro");
        assertThat(b).contains((byte) 0xF1).doesNotContain((byte) '\r');
        assertThat(b[b.length - 1]).isEqualTo((byte) '\n');
    }

    @Test
    @DisplayName("CONT-A-11: B1_CTRL_BROAD_CTAS.TXT is byte-identical to the legacy golden")
    void cont_a_11_broadCuentasControlMatchesLegacyGolden() throws Exception {
        // CreaBroadCuentas.java:155-199
        generar(CLOCK);
        assertThat(leer(fi().resolve("B1_CTRL_BROAD_CTAS.TXT"))).isEqualTo(golden("FI/B1_CTRL_BROAD_CTAS.TXT"));
    }

    @Test
    @DisplayName("CONT-A-11: control = header + C|SCRIPT(30)|''(30)|name(30)|count(15)|fec(8)|hra(10), no final LF")
    void cont_a_11_broadCuentasControlLayout() throws Exception {
        // CreaBroadCuentas.java:155-194
        generar(CLOCK);
        String s = texto(fi().resolve("B1_CTRL_BROAD_CTAS.TXT"));
        assertThat(s).isEqualTo(
                "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n"
                        + "C"
                        + "SCRIPT" + " ".repeat(24)
                        + " ".repeat(30)
                        + "B1_BROAD_CTAS.TXT" + " ".repeat(13)
                        + "3" + " ".repeat(14)
                        + "2012    "
                        + "9:0:0     ");
    }

    @Test
    @DisplayName("CONT-A-28: CTAS control timestamp is epoch 0 (stat of nonexistent path) in the JVM zone")
    void cont_a_28_broadCuentasControlTimestampIsEpochRegardlessOfClockInstant() throws Exception {
        // CreaBroadCuentas.java:166,179-186: File(rutaDescarga+nombreArchivoCtr+nombreArchivo).lastModified()==0
        // Epoch in UTC: 1970+1+1 = 1972, HOUR=0 -> "0:0:0"
        Clock utc2030 = Clock.fixed(Instant.parse("2030-05-20T13:14:15Z"), ZoneOffset.UTC);
        generar(utc2030);
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_CTAS.TXT"))).endsWith("3" + " ".repeat(14) + "1972    0:0:0     ");
    }

    @Test
    @DisplayName("CONT-A-27: re-running appends to existing files (append=true), never truncates")
    void cont_a_27_broadCuentasRerunAppends() throws Exception {
        // CreaBroadCuentas.java:128,199 FileUtils.writeStringToFile(file, linea, "ISO-8859-1", true)
        generar(CLOCK);
        generar(CLOCK);
        assertThat(leer(fi().resolve("B1_BROAD_CTAS.TXT")))
                .isEqualTo(concat(golden("FI/B1_BROAD_CTAS.TXT"), golden("FI/B1_BROAD_CTAS.TXT")));
        assertThat(leer(fi().resolve("B1_CTRL_BROAD_CTAS.TXT")))
                .isEqualTo(concat(golden("FI/B1_CTRL_BROAD_CTAS.TXT"), golden("FI/B1_CTRL_BROAD_CTAS.TXT")));
    }

    @Test
    @DisplayName("CONT-A-36: no rows -> header-only data file and count '0' in control")
    void cont_a_36_broadCuentasEmptyResult() throws Exception {
        // CreaBroadCuentas.java:92-103,176 (out.size() == 0)
        FakeSapSource sap = new FakeSapSource();
        sap.cuentasBroad = new ArrayList<>();
        new BroadCuentasGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(texto(fi().resolve("B1_BROAD_CTAS.TXT"))).isEqualTo(
                "COD-ENTIDAD COD-CTA-CONTABLE DESCRIPCION TIPO-CUENTA CUENTA-GRUPO COD-CUENTA-CONTABLE "
                        + "IND_CTA_CONFIDENCIAL IND_CTA-ASOCIADA IND-CTA-MONETARIA IND_RELEVANTE_FLUJO-CAJA "
                        + "IND-CTA-AFILIADA\n");
        assertThat(texto(fi().resolve("B1_CTRL_BROAD_CTAS.TXT"))).endsWith("0" + " ".repeat(14) + "2012    9:0:0     ");
    }

    @Test
    @DisplayName("CONT-A-02: cuentas query takes no fecha (legacy passes empty params)")
    void cont_a_02_broadCuentasQueryHasNoFecha() throws Exception {
        // CreaBroadCuentas.java:75-76 parmsIN empty although fecha is a parameter of logica()
        FakeSapSource sap = generar(CLOCK);
        assertThat(sap.llamadas).containsExactly("CUENTAS_BROAD");
    }
}
