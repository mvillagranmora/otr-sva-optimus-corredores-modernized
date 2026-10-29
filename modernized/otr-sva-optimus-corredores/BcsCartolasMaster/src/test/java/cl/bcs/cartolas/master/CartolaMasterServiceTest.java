package cl.bcs.cartolas.master;

import cl.bcs.cartolas.master.domain.GraficoMaster;
import cl.bcs.cartolas.master.domain.ImagenMaster;
import cl.bcs.cartolas.master.domain.PaletaColores;
import cl.bcs.cartolas.master.domain.ReporteMaster;
import cl.bcs.cartolas.master.service.CartolaMasterService;
import cl.bcs.cartolas.master.service.GraficadorPastelService;
import cl.bcs.cartolas.master.service.GraficadorSerieService;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de caracterización de la lógica pure-logic de BcsCartolasMaster.
 *
 * <p>Sin Spring — sin base de datos. Prueba únicamente la lógica de dominio y
 * los servicios de gráficos (JFreeChart) que no requieren HANA.</p>
 */
class CartolaMasterServiceTest {

    // =========================================================================
    // B-CM-01: CartolaMaster domain value object
    // =========================================================================

    @Test
    void cartolaMaster_constructorPersistsFields() {
        final byte[] bytes = new byte[]{1, 2, 3};
        final var cm = new cl.bcs.cartolas.master.domain.CartolaMaster("archivo.pdf", bytes);

        assertThat(cm.getNombreArchivo()).isEqualTo("archivo.pdf");
        assertThat(cm.getBytes()).isEqualTo(bytes);
        assertThat(cm.getSizeBytes()).isEqualTo(3);
    }

    @Test
    void cartolaMaster_nullNombreArchivo_throws() {
        assertThatThrownBy(() ->
                new cl.bcs.cartolas.master.domain.CartolaMaster(null, new byte[]{1}))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void cartolaMaster_nullBytes_throws() {
        assertThatThrownBy(() ->
                new cl.bcs.cartolas.master.domain.CartolaMaster("file.pdf", null))
                .isInstanceOf(NullPointerException.class);
    }

    // =========================================================================
    // B-CM-02: ReporteMaster enum — configuración completa
    // =========================================================================

    @Test
    void reporteMaster_infoCarteraMasterPag1_hasExpectedConfig() {
        final ReporteMaster r = ReporteMaster.InfoCarteraMasterPag1;

        assertThat(r.getNombreArchivo()).isEqualTo("InfoCarteraMasterPag1");
        assertThat(r.getNombreDataSource()).isEqualTo("InfoCarteraMaster");
        assertThat(r.getNombreParametroInclusion()).isNull();
        assertThat(r.tieneParametroInclusion()).isFalse();
    }

    @Test
    void reporteMaster_carteraRV_tieneParametroInclusion() {
        final ReporteMaster r = ReporteMaster.CarteraRV;

        assertThat(r.getNombreParametroInclusion()).isEqualTo("PRV_incCartera");
        assertThat(r.tieneParametroInclusion()).isTrue();
    }

    @Test
    void reporteMaster_enumContainsAllExpectedValues() {
        // Verifica que todos los reportes conocidos existen en el enum
        assertThat(ReporteMaster.values()).contains(
                ReporteMaster.InfoCarteraMasterPag0,
                ReporteMaster.InfoCarteraMasterPag1,
                ReporteMaster.InfoCarteraMasterPag2,
                ReporteMaster.InfoCarteraMasterPag4,
                ReporteMaster.CarteraRV,
                ReporteMaster.CarteraRF,
                ReporteMaster.MovimientosCaja,
                ReporteMaster.MovimientosTitulos,
                ReporteMaster.ProximosEventosCapital,
                ReporteMaster.ObservacionesAnotacionesCliente
        );
    }

    // =========================================================================
    // B-CM-03: GraficoMaster enum
    // =========================================================================

    @Test
    void graficoMaster_graficoActivoPorProducto_hasDataSource() {
        assertThat(GraficoMaster.GraficoActivoPorProducto.getNombreDataSource())
                .isEqualTo("GraficoActivoPorProducto");
        assertThat(GraficoMaster.GraficoActivoPorProducto.getReportePadre())
                .isEqualTo("InfoCarteraMasterPag1");
    }

    @Test
    void graficoMaster_graficoFM_nullDataSource() {
        assertThat(GraficoMaster.GraficoFM.getNombreDataSource()).isNull();
        assertThat(GraficoMaster.GraficoFM.getReportePadre()).isNull();
    }

    // =========================================================================
    // B-CM-04: ImagenMaster enum
    // =========================================================================

    @Test
    void imagenMaster_banner_hasCorrectNombreArchivo() {
        assertThat(ImagenMaster.Banner.getNombreArchivo()).isEqualTo("banner.png");
        assertThat(ImagenMaster.Banner.getGrupo()).isEqualTo("GENERAL");
    }

    @Test
    void imagenMaster_logo_hasCorrectNombreArchivo() {
        assertThat(ImagenMaster.Logo.getNombreArchivo()).isEqualTo("logo.gif");
    }

    // =========================================================================
    // B-CM-05: PaletaColores
    // =========================================================================

    @Test
    void paletaColores_rv_isOrange() {
        final var color = PaletaColores.get("RV");
        assertThat(color).isNotNull();
        assertThat(color.getRed()).isEqualTo(0xcc);
        assertThat(color.getGreen()).isEqualTo(0x66);
        assertThat(color.getBlue()).isEqualTo(0x00);
    }

    @Test
    void paletaColores_unknownKey_returnsNull() {
        assertThat(PaletaColores.get("UNKNOWN_PRODUCTO")).isNull();
    }

    @Test
    void paletaColores_keySet_containsExpectedProducts() {
        assertThat(PaletaColores.keySet())
                .contains("CAJA", "RV", "RF", "DV", "FWD", "FM", "OPL");
    }

    // =========================================================================
    // B-CM-06: CartolaMasterService.incluir (pure logic)
    // =========================================================================

    @Test
    void incluir_reporteSinParametro_siempreTrue() {
        // InfoCarteraMasterPag1 no tiene parámetro de inclusión → siempre incluir
        final var params = Map.<String, Object>of();
        assertThat(CartolaMasterService.incluir(ReporteMaster.InfoCarteraMasterPag1, params))
                .isTrue();
    }

    @Test
    void incluir_carteraRV_trueWhenParametroTrue() {
        final var params = Map.<String, Object>of("PRV_incCartera", Boolean.TRUE);
        assertThat(CartolaMasterService.incluir(ReporteMaster.CarteraRV, params)).isTrue();
    }

    @Test
    void incluir_carteraRV_falseWhenParametroFalse() {
        final var params = Map.<String, Object>of("PRV_incCartera", Boolean.FALSE);
        assertThat(CartolaMasterService.incluir(ReporteMaster.CarteraRV, params)).isFalse();
    }

    @Test
    void incluir_carteraRV_falseWhenParametroAbsente() {
        // Sin el parámetro en el mapa → no incluir (behavior seguro)
        final var params = Map.<String, Object>of();
        assertThat(CartolaMasterService.incluir(ReporteMaster.CarteraRV, params)).isFalse();
    }

    @Test
    void incluir_nullReporte_throws() {
        assertThatThrownBy(() ->
                CartolaMasterService.incluir(null, Map.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void incluir_nullParametros_throws() {
        assertThatThrownBy(() ->
                CartolaMasterService.incluir(ReporteMaster.CarteraRV, null))
                .isInstanceOf(NullPointerException.class);
    }

    // =========================================================================
    // B-CM-07: CartolaMasterService.construirNombreArchivo (pure logic)
    // =========================================================================

    @Test
    void construirNombreArchivo_formatoCorrecto() {
        final String nombre = CartolaMasterService.construirNombreArchivo(
                123570L,
                LocalDate.of(2017, 4, 1),
                LocalDate.of(2017, 6, 30));

        assertThat(nombre).isEqualTo("cartola_123570_2017-04-01_2017-06-30.pdf");
    }

    @Test
    void construirNombreArchivo_nullFechaDesde_throws() {
        assertThatThrownBy(() ->
                CartolaMasterService.construirNombreArchivo(1L, null, LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void construirNombreArchivo_nullFechaHasta_throws() {
        assertThatThrownBy(() ->
                CartolaMasterService.construirNombreArchivo(1L, LocalDate.now(), null))
                .isInstanceOf(NullPointerException.class);
    }

    // =========================================================================
    // B-CM-08: GraficadorPastelService (JFreeChart sin Spring)
    // =========================================================================

    @Test
    void graficadorPastel_generaImagenConDimensionesCorrectas() {
        final var data = Map.of(
                "RV", new BigDecimal("35.5"),
                "RF", new BigDecimal("20.0"),
                "FM", new BigDecimal("15.0")
        );

        final BufferedImage img = GraficadorPastelService.graficar(data, "Test Torta");

        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isEqualTo(217);
        assertThat(img.getHeight()).isEqualTo(152);
    }

    @Test
    void graficadorPastel_valoresCero_seExcluyen() {
        // Valor 0 debe excluirse del pie chart
        final var data = Map.of(
                "RV", new BigDecimal("0"),
                "RF", new BigDecimal("50.0")
        );

        // No debe lanzar excepción
        final BufferedImage img = GraficadorPastelService.graficar(data, "");
        assertThat(img).isNotNull();
    }

    @Test
    void graficadorPastel_mapaNulo_throws() {
        assertThatThrownBy(() -> GraficadorPastelService.graficar(null, "Titulo"))
                .isInstanceOf(NullPointerException.class);
    }

    // =========================================================================
    // B-CM-09: GraficadorSerieService (JFreeChart sin Spring)
    // =========================================================================

    @Test
    void graficadorSerie_generaImagenConDimensionesCorrectas() {
        final var serie = new TimeSeries("Patrimonio");
        serie.add(new Day(1, 4, 2017), 1_000_000.0);
        serie.add(new Day(1, 5, 2017), 1_050_000.0);
        serie.add(new Day(1, 6, 2017), 980_000.0);

        final BufferedImage img = GraficadorSerieService.graficar(serie, "Evolución Patrimonio");

        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isEqualTo(780);
        assertThat(img.getHeight()).isEqualTo(570);
    }

    @Test
    void graficadorSerie_serieNula_throws() {
        assertThatThrownBy(() -> GraficadorSerieService.graficar(null, "Titulo"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void graficadorSerie_serieSinDatos_generaImagenValida() {
        final var serie = new TimeSeries("Vacia");
        final BufferedImage img = GraficadorSerieService.graficar(serie, "Sin datos");

        // JFreeChart muestra "No hay datos" pero sigue retornando imagen válida
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isEqualTo(780);
        assertThat(img.getHeight()).isEqualTo(570);
    }
}
