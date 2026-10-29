package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.infrastructure.interfaz.DescriptivasBroadGenerator;
import cl.bcs.contabilidad.interfaz.InterfazFixtures.Consulta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Characterization of legacy {@code CreaDescriptivasBroad}
 * (B1_DES_ENTIDAD.TXT, B1_DES_TIPO_CTA.TXT, B1_DES_CC_PROD.TXT — no control files).
 * Oracle: legacy/.../BcsContabilidadServicios/src/cl/bcs/contabilidad/CreaDescriptivasBroad.java
 */
class DescriptivasBroadGeneratorTest {

    @TempDir
    Path base;

    private Path fi() {
        return base.resolve("Transmisiones/Salida/FI");
    }

    private FakeSapSource generar(FakeSapSource sap) throws IOException {
        new DescriptivasBroadGenerator(sap, CLOCK).generar(contexto(base));
        return sap;
    }

    @Test
    @DisplayName("CONT-A-06: file names B1_DES_ENTIDAD.TXT / B1_DES_TIPO_CTA.TXT / B1_DES_CC_PROD.TXT")
    void cont_a_06_descriptivasFileNames() {
        // CreaInterfazDiaria.java:158,160,162
        assertThat(DescriptivasBroadGenerator.ARCHIVO_ENTIDAD).isEqualTo("B1_DES_ENTIDAD.TXT");
        assertThat(DescriptivasBroadGenerator.ARCHIVO_TIPO_CUENTA).isEqualTo("B1_DES_TIPO_CTA.TXT");
        assertThat(DescriptivasBroadGenerator.ARCHIVO_CENTRO_COSTO).isEqualTo("B1_DES_CC_PROD.TXT");
    }

    @Test
    @DisplayName("CONT-A-16: B1_DES_ENTIDAD.TXT matches golden: header + 20/100 columns, reverse order, ISO-8859-1")
    void cont_a_16_entidadMatchesLegacyGolden() throws Exception {
        // CreaDescriptivasBroad.java:149-167
        generar(new FakeSapSource());
        Path f = fi().resolve("B1_DES_ENTIDAD.TXT");
        assertThat(leer(f)).isEqualTo(golden("FI/B1_DES_ENTIDAD.TXT"));
        List<String> l = lineas(f);
        assertThat(l.get(0)).isEqualTo("COD-ENTIDAD DESCRIPCION ");
        assertThat(l.get(1)).isEqualTo("20200" + " ".repeat(15) + "Agencia Valores" + " ".repeat(85));
        assertThat(l.get(2)).hasSize(120).startsWith("20100" + " ".repeat(15) + "Corredora Ñuñoa S.A.");
        assertThat(leer(f)).contains((byte) 0xD1, (byte) 0xF1);
    }

    @Test
    @DisplayName("CONT-A-17: B1_DES_TIPO_CTA.TXT matches golden: header + 20/100 columns, reverse order")
    void cont_a_17_tipoCuentaMatchesLegacyGolden() throws Exception {
        // CreaDescriptivasBroad.java:203-219
        generar(new FakeSapSource());
        Path f = fi().resolve("B1_DES_TIPO_CTA.TXT");
        assertThat(leer(f)).isEqualTo(golden("FI/B1_DES_TIPO_CTA.TXT"));
        List<String> l = lineas(f);
        assertThat(l.get(0)).isEqualTo("TIPO-CUENTA DESCRIPCION ");
        assertThat(l.get(1)).isEqualTo("3" + " ".repeat(19) + "Patrimonio" + " ".repeat(90));
        assertThat(l.get(3)).isEqualTo("1" + " ".repeat(19) + "Activo" + " ".repeat(94));
        assertThat(l.get(4)).isEmpty();
    }

    @Test
    @DisplayName("CONT-A-18: B1_DES_CC_PROD.TXT matches golden: header + 8/100/3 columns, reverse order")
    void cont_a_18_centroCostoMatchesLegacyGolden() throws Exception {
        // CreaDescriptivasBroad.java:253-271 (ASCII fixture: legacy uses the platform default charset here)
        generar(new FakeSapSource());
        Path f = fi().resolve("B1_DES_CC_PROD.TXT");
        assertThat(leer(f)).isEqualTo(golden("FI/B1_DES_CC_PROD.TXT"));
        List<String> l = lineas(f);
        assertThat(l.get(0)).isEqualTo("COD-CC-PRD DESCRIPCION TIPO ");
        assertThat(l.get(1)).isEqualTo("CC10    " + "Centro costo operaciones" + " ".repeat(76) + "CC ");
        assertThat(l.get(2)).hasSize(111);
    }

    @Test
    @DisplayName("CONT-A-19: a failing Entidad query does not stop TipoCuenta/CentroCosto; generator does not throw")
    void cont_a_19_entidadFailureIsIsolated() throws Exception {
        // CreaDescriptivasBroad.java:137-174 each logicaX wraps everything in catch (Exception) and only logs
        FakeSapSource sap = new FakeSapSource();
        sap.fallar.add(Consulta.ENTIDADES);
        assertThatCode(() -> generar(sap)).doesNotThrowAnyException();
        assertThat(fi().resolve("B1_DES_ENTIDAD.TXT")).doesNotExist();
        assertThat(leer(fi().resolve("B1_DES_TIPO_CTA.TXT"))).isEqualTo(golden("FI/B1_DES_TIPO_CTA.TXT"));
        assertThat(leer(fi().resolve("B1_DES_CC_PROD.TXT"))).isEqualTo(golden("FI/B1_DES_CC_PROD.TXT"));
        assertThat(sap.llamadas).containsExactly("ENTIDADES", "TIPOS_CUENTA", "CENTROS_COSTO");
    }

    @Test
    @DisplayName("CONT-A-19: a failing TipoCuenta query does not stop CentroCosto")
    void cont_a_19_tipoCuentaFailureIsIsolated() throws Exception {
        // CreaDescriptivasBroad.java:191-225
        FakeSapSource sap = new FakeSapSource();
        sap.fallar.add(Consulta.TIPOS_CUENTA);
        assertThatCode(() -> generar(sap)).doesNotThrowAnyException();
        assertThat(leer(fi().resolve("B1_DES_ENTIDAD.TXT"))).isEqualTo(golden("FI/B1_DES_ENTIDAD.TXT"));
        assertThat(fi().resolve("B1_DES_TIPO_CTA.TXT")).doesNotExist();
        assertThat(leer(fi().resolve("B1_DES_CC_PROD.TXT"))).isEqualTo(golden("FI/B1_DES_CC_PROD.TXT"));
    }

    @Test
    @DisplayName("CONT-A-06: descriptivas write no control files")
    void cont_a_06_descriptivasHaveNoControlFiles() throws Exception {
        // CreaInterfazDiaria.java:155-164 (no nombreArchivoCtr passed)
        generar(new FakeSapSource());
        try (var s = Files.list(fi())) {
            assertThat(s.map(p -> p.getFileName().toString()).sorted().toList())
                    .containsExactly("B1_DES_CC_PROD.TXT", "B1_DES_ENTIDAD.TXT", "B1_DES_TIPO_CTA.TXT");
        }
    }
}
