package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.infrastructure.interfaz.CuentasBpcGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code CreaCuentasBPC} (B1_CUENTAS_BPC.TXT + B1_CTRL_CUENTAS_BPC.TXT).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaCuentasBPC.java
 */
class CuentasBpcGeneratorTest {

    private static final String HEADER = "CODIGO-SOCIEDAD CUENTA-OPERATIVA DESCRIPCION TIPO-CUENTA CUENTA-GRUPO ";

    @TempDir
    Path base;

    private Path bpc() {
        return base.resolve("Transmisiones/Salida/BPC");
    }

    @Test
    @DisplayName("CONT-A-06: file names are B1_CUENTAS_BPC.TXT / B1_CTRL_CUENTAS_BPC.TXT (BPC directory)")
    void cont_a_06_cuentasBpcFileNames() throws Exception {
        // CreaInterfazDiaria.java:173-176 (rutaDescargaBPC)
        assertThat(CuentasBpcGenerator.ARCHIVO).isEqualTo("B1_CUENTAS_BPC.TXT");
        assertThat(CuentasBpcGenerator.ARCHIVO_CONTROL).isEqualTo("B1_CTRL_CUENTAS_BPC.TXT");
        new CuentasBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(bpc().resolve("B1_CUENTAS_BPC.TXT")).exists();
        assertThat(base.resolve("Transmisiones/Salida/FI/B1_CUENTAS_BPC.TXT")).doesNotExist();
    }

    @Test
    @DisplayName("CONT-A-20: B1_CUENTAS_BPC.TXT is byte-identical to the legacy golden")
    void cont_a_20_cuentasBpcDataFileMatchesLegacyGolden() throws Exception {
        // CreaCuentasBPC.java:92-130
        new CuentasBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(bpc().resolve("B1_CUENTAS_BPC.TXT"))).isEqualTo(golden("BPC/B1_CUENTAS_BPC.TXT"));
    }

    @Test
    @DisplayName("CONT-A-20: header (with trailing space) and widths 4/10/60/3/10 = 87 chars, reverse order")
    void cont_a_20_cuentasBpcLayout() throws Exception {
        // CreaCuentasBPC.java:92-110
        new CuentasBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        List<String> l = lineas(bpc().resolve("B1_CUENTAS_BPC.TXT"));
        assertThat(l.get(0)).isEqualTo(HEADER);
        assertThat(l.get(2)).isEqualTo("2010" + "1101001   " + "Caja moneda nacional" + " ".repeat(40)
                + "1  " + "110100    ");
        assertThat(l.get(1)).hasSize(87).startsWith("20102101005   Proveedores Compañía ");
    }

    @Test
    @DisplayName("CONT-A-21: B1_CTRL_CUENTAS_BPC.TXT is byte-identical to the legacy golden")
    void cont_a_21_cuentasBpcControlMatchesLegacyGolden() throws Exception {
        // CreaCuentasBPC.java:155-197
        new CuentasBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(leer(bpc().resolve("B1_CTRL_CUENTAS_BPC.TXT"))).isEqualTo(golden("BPC/B1_CTRL_CUENTAS_BPC.TXT"));
    }

    @Test
    @DisplayName("CONT-A-21: control layout: sub-rutina CONSULTACUENTABPC, count, real write timestamp")
    void cont_a_21_cuentasBpcControlLayout() throws Exception {
        // CreaCuentasBPC.java:171-192 (File(ruta + nombreArchivo) is the real data file -> its mtime)
        new CuentasBpcGenerator(new FakeSapSource(), CLOCK).generar(contexto(base));
        assertThat(texto(bpc().resolve("B1_CTRL_CUENTAS_BPC.TXT"))).isEqualTo(
                "TIP-RUTINA DES-RUTINA DES-SUBRUTINA DES-ESTRUCUTRA MTO-SUMCTAREG FEC-PROCESO HRA-PROCESO \n"
                        + "C"
                        + "SCRIPT" + " ".repeat(24)
                        + "CONSULTACUENTABPC" + " ".repeat(13)
                        + "B1_CUENTAS_BPC.TXT" + " ".repeat(12)
                        + "2" + " ".repeat(14)
                        + "2049    "
                        + "3:4:5     ");
    }

    @Test
    @DisplayName("CONT-A-36: no rows -> header-only data file, count '0'")
    void cont_a_36_cuentasBpcEmptyResult() throws Exception {
        // CreaCuentasBPC.java:92-97,175
        FakeSapSource sap = new FakeSapSource();
        sap.cuentasBpc = new ArrayList<>();
        new CuentasBpcGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(texto(bpc().resolve("B1_CUENTAS_BPC.TXT"))).isEqualTo(HEADER + "\n");
        assertThat(texto(bpc().resolve("B1_CTRL_CUENTAS_BPC.TXT"))).endsWith("0" + " ".repeat(14) + "2049    3:4:5     ");
    }

    @Test
    @DisplayName("CONT-A-02: cuentas BPC query takes no fecha")
    void cont_a_02_cuentasBpcQueryHasNoFecha() throws Exception {
        // CreaCuentasBPC.java:78-79 parmsIN empty
        FakeSapSource sap = new FakeSapSource();
        new CuentasBpcGenerator(sap, CLOCK).generar(contexto(base));
        assertThat(sap.llamadas).containsExactly("CUENTAS_BPC");
    }
}
