package cl.bcs.becb;

import cl.bcs.becb.domain.DataRiesgoFinanciero;
import cl.bcs.becb.domain.DataRiesgoFinancieroAgrupado;
import cl.bcs.becb.domain.SalidaGenerarInforme;
import cl.bcs.becb.domain.TipoInforme;
import cl.bcs.becb.service.RiesgoFinancieroService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiesgoFinancieroTest {

    // B-BECB-01: tabla determination
    @Test
    void tabla_mismaFecha_devuelveCUS() {
        assertThat(RiesgoFinancieroService.determinarTabla(
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 15))).isEqualTo("CUS");
    }

    @Test
    void tabla_fechaDiferente_devuelveHIS() {
        assertThat(RiesgoFinancieroService.determinarTabla(
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 14))).isEqualTo("HIS");
    }

    @Test
    void tabla_fechaFuturaInforme_devuelveHIS() {
        assertThat(RiesgoFinancieroService.determinarTabla(
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16))).isEqualTo("HIS");
    }

    // B-BECB-02: factory lookup
    @Test
    void resolveInforme_RIFI_valido() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("RIFI")).isEqualTo(TipoInforme.RIFI);
    }

    @Test
    void resolveInforme_rifi_minusculas_valido() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("rifi")).isEqualTo(TipoInforme.RIFI);
    }

    @Test
    void resolveInforme_DIFI_valido() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("DIFI")).isEqualTo(TipoInforme.DIFI);
    }

    @Test
    void resolveInforme_ALL_valido() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("ALL")).isEqualTo(TipoInforme.ALL);
    }

    @Test
    void resolveInforme_nulo_devuelveNull() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme(null)).isNull();
    }

    @Test
    void resolveInforme_vacio_devuelveNull() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("")).isNull();
    }

    @Test
    void resolveInforme_desconocido_devuelveNull() {
        assertThat(RiesgoFinancieroService.resolveCodigoInforme("XYZ")).isNull();
    }

    // B-BECB-03: grouping algorithm
    @Test
    void agrupar_listaVacia_devuelveVacia() {
        assertThat(RiesgoFinancieroService.agruparFilasRifi(List.of())).isEmpty();
    }

    @Test
    void agrupar_unElemento_devuelveUnGrupo_conVolatilidadCero() {
        // First group seed: Volatilidad=ZERO and Var=ZERO (legacy quirk preserved)
        DataRiesgoFinanciero f = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f));
        assertThat(grupos).hasSize(1);
        DataRiesgoFinancieroAgrupado g = grupos.get(0);
        assertThat(g.instrumentoEmisor).isEqualTo("BCI");
        assertThat(g.rango).isEqualByComparingTo(bd(1));
        assertThat(g.volatilidad).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(g.var).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(g.valorPresente).isEqualByComparingTo(bd(100));
        assertThat(g.duracion).isEqualByComparingTo(bd(3));
    }

    @Test
    void agrupar_mismoEmisorRango_acumulaValorPresente() {
        DataRiesgoFinanciero f1 = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("BCI", bd(1), bd(7), bd(4), bd(200), bd(3));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos).hasSize(1);
        assertThat(grupos.get(0).valorPresente).isEqualByComparingTo(bd(300));
    }

    @Test
    void agrupar_mismoEmisorRango_tomaMaxDuracion() {
        DataRiesgoFinanciero f1 = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("BCI", bd(1), bd(7), bd(4), bd(200), bd(3));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos.get(0).duracion).isEqualByComparingTo(bd(4));
    }

    @Test
    void agrupar_diferenteEmisor_creaGrupoNuevo() {
        DataRiesgoFinanciero f1 = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("BICE", bd(1), bd(7), bd(4), bd(200), bd(3));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos).hasSize(2);
        assertThat(grupos.get(0).instrumentoEmisor).isEqualTo("BCI");
        assertThat(grupos.get(1).instrumentoEmisor).isEqualTo("BICE");
    }

    @Test
    void agrupar_diferenteRango_creaGrupoNuevo() {
        DataRiesgoFinanciero f1 = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("BCI", bd(2), bd(7), bd(4), bd(200), bd(3));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos).hasSize(2);
        assertThat(grupos.get(1).volatilidad).isEqualByComparingTo(bd(7));
        assertThat(grupos.get(1).var).isEqualByComparingTo(bd(3));
    }

    @Test
    void agrupar_emisorCaseInsensitive_mismoGrupo() {
        DataRiesgoFinanciero f1 = row("BCI", bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("bci", bd(1), bd(7), bd(4), bd(200), bd(3));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos).hasSize(1);
        assertThat(grupos.get(0).valorPresente).isEqualByComparingTo(bd(300));
    }

    @Test
    void agrupar_tresGrupos_diferentesEmisores() {
        List<DataRiesgoFinanciero> filas = List.of(
                row("BCI",  bd(1), bd(5), bd(3), bd(100), bd(2)),
                row("BICE", bd(1), bd(6), bd(4), bd(200), bd(3)),
                row("BICE", bd(1), bd(7), bd(5), bd(150), bd(4)),
                row("ITAU", bd(2), bd(8), bd(6), bd(300), bd(5))
        );
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(filas);
        assertThat(grupos).hasSize(3);
        assertThat(grupos.get(1).instrumentoEmisor).isEqualTo("BICE");
        assertThat(grupos.get(1).valorPresente).isEqualByComparingTo(bd(350)); // 200+150
        assertThat(grupos.get(1).duracion).isEqualByComparingTo(bd(5)); // max(4,5)
        assertThat(grupos.get(2).instrumentoEmisor).isEqualTo("ITAU");
    }

    @Test
    void agrupar_segundoGrupo_tieneVolatilidadReal() {
        // Only second+ groups get actual volatilidad from the row
        DataRiesgoFinanciero f1 = row("BCI",  bd(1), bd(5), bd(3), bd(100), bd(2));
        DataRiesgoFinanciero f2 = row("BICE", bd(1), bd(9), bd(4), bd(200), bd(7));
        List<DataRiesgoFinancieroAgrupado> grupos = RiesgoFinancieroService.agruparFilasRifi(List.of(f1, f2));
        assertThat(grupos.get(0).volatilidad).isEqualByComparingTo(BigDecimal.ZERO); // first always ZERO
        assertThat(grupos.get(1).volatilidad).isEqualByComparingTo(bd(9));           // second gets actual
    }

    // B-BECB-04: tieneData
    @Test
    void tieneData_listaVacia_devuelveFalse() {
        assertThat(RiesgoFinancieroService.tieneData(List.of())).isFalse();
    }

    @Test
    void tieneData_conElementos_devuelveTrue() {
        assertThat(RiesgoFinancieroService.tieneData(List.of("x"))).isTrue();
    }

    // B-BECB-05: file naming
    @Test
    void nombreArchivo_formatoCorrecto() {
        assertThat(RiesgoFinancieroService.generarNombreArchivo("RIFI", "CUS", "20240115_123456"))
                .isEqualTo("Informe_RIFI_CUS_20240115_123456.xls");
    }

    @Test
    void nombreArchivoLog_formatoCorrecto() {
        assertThat(RiesgoFinancieroService.generarNombreArchivoLog("DIFI", "HIS", "20240115_123456"))
                .isEqualTo("Informe_DIFI_HIS_20240115_123456.log");
    }

    @Test
    void nombreArchivo_codigoALL() {
        assertThat(RiesgoFinancieroService.generarNombreArchivo("ALL", "HIS", "20240101_000000"))
                .isEqualTo("Informe_ALL_HIS_20240101_000000.xls");
    }

    // B-BECB-06: error output
    @Test
    void salidaError_setsCampos() {
        SalidaGenerarInforme s = RiesgoFinancieroService.salidaError("Interfaz no configurada");
        assertThat(s.erroresContador).isEqualTo(1L);
        assertThat(s.comentario).isEqualTo("Interfaz no configurada");
    }

    @Test
    void salidaError_sinData() {
        SalidaGenerarInforme s = RiesgoFinancieroService.salidaError("Archivo no tiene data");
        assertThat(s.erroresContador).isEqualTo(1L);
        assertThat(s.comentario).isEqualTo("Archivo no tiene data");
    }

    // HANA-deferred tests
    @Disabled("requires HANA: FactoryRiesgoFinanciero_GetIdCliente.sql")
    @Test
    void hana_obtenerIdCliente() {
    }

    @Disabled("requires HANA: FactoryRiesgoFinanciero_GetValorUF.sql")
    @Test
    void hana_obtenerValorUF() {
    }

    @Disabled("requires HANA: ConsultarInformeRiesgoFinanciero.sql + file system output")
    @Test
    void hana_generarInformeCompleto() {
    }

    // Helper methods
    private DataRiesgoFinanciero row(String emisor, BigDecimal rango, BigDecimal vol,
                                     BigDecimal dur, BigDecimal vp, BigDecimal var) {
        return new DataRiesgoFinanciero(emisor, rango, vol, dur, vp, var);
    }

    private BigDecimal bd(int v) {
        return new BigDecimal(v);
    }
}
