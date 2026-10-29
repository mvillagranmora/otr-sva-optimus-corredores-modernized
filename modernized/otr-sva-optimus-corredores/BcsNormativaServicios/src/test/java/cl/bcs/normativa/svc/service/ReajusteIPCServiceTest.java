package cl.bcs.normativa.svc.service;

import cl.bcs.normativa.svc.domain.declaracion.DetalleIPCDJ1871;
import cl.bcs.normativa.svc.domain.declaracion.ValorIPC;
import cl.bcs.normativa.svc.service.declaracion.ReajusteIPCService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de caracterización de la lógica de reajuste IPC.
 * No requieren Spring — todos los métodos bajo prueba son static.
 *
 * <p>Fórmula verificada contra el legacy ReajustarValorIPC:
 * <pre>
 *   variacion% = (ipcMesActual / ipcMesDetalle) * 100 - 100
 *   actualizado = valor * (1 + variacion%/100) = valor * ipcActual/ipcDetalle
 *   escala = 0, HALF_UP
 * </pre>
 */
class ReajusteIPCServiceTest {

    // ---- Caso 1: reajuste básico ----

    @Test
    @DisplayName("Reajuste básico con IPC conocido")
    void reajuste_basico() {
        /*
         * ipcMesActual  = 110
         * ipcMesDetalle = 100
         * variacion%    = (110/100)*100 - 100 = 10%
         * valorOriginal = 1000
         * actualizado   = 1000 * (1 + 10/100) = 1100
         */
        BigDecimal resultado = ReajusteIPCService.reajustar(
                new BigDecimal("110"),
                new BigDecimal("100"),
                new BigDecimal("1000"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("1100"));
    }

    // ---- Caso 2: sin variación (mismo IPC) ----

    @Test
    @DisplayName("Sin variación cuando IPC actual == IPC detalle")
    void reajuste_sinVariacion() {
        BigDecimal resultado = ReajusteIPCService.reajustar(
                new BigDecimal("105"),
                new BigDecimal("105"),
                new BigDecimal("5000"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("5000"));
    }

    // ---- Caso 3: IPC detalle cero → retorna valor original ----

    @Test
    @DisplayName("Denominador IPC cero retorna valor original sin excepción")
    void reajuste_ipcDetalleCero_retornaOriginal() {
        BigDecimal resultado = ReajusteIPCService.reajustar(
                new BigDecimal("105"),
                BigDecimal.ZERO,
                new BigDecimal("3000"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("3000"));
    }

    // ---- Caso 4: valor original null → retorna ZERO ----

    @Test
    @DisplayName("Valor original null retorna ZERO")
    void reajuste_valorOriginalNull_retornaCero() {
        BigDecimal resultado = ReajusteIPCService.reajustar(
                new BigDecimal("105"),
                new BigDecimal("100"),
                null);

        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- Caso 5: aplicarReajusteDJ1871 actualiza todos los campos ----

    @Test
    @DisplayName("aplicarReajusteDJ1871 actualiza todos los campos de ahorros y retiros")
    void aplicarReajusteDJ1871_actualizaTodosCampos() {
        ValorIPC ipc202201 = new ValorIPC();
        ipc202201.agnoMes = "202201";
        ipc202201.valorIpc = new BigDecimal("100");

        ValorIPC ipc202206 = new ValorIPC();
        ipc202206.agnoMes = "202206";
        ipc202206.valorIpc = new BigDecimal("120");

        Map<String, ValorIPC> mapaIPC = Map.of(
                "202201", ipc202201,
                "202206", ipc202206);

        DetalleIPCDJ1871 detalle = new DetalleIPCDJ1871();
        detalle.agnoMes = "202201";
        detalle.ahorrosTrabajador     = new BigDecimal("10000");
        detalle.ahorrosDeposito       = new BigDecimal("5000");
        detalle.ahorrosCotizaciones   = new BigDecimal("3000");
        detalle.retirosTrabajador     = new BigDecimal("2000");
        detalle.retirosDeposito       = new BigDecimal("1000");
        detalle.retirosCotizaciones   = new BigDecimal("500");

        // ipcReferencia = 120, ipcDetalle = 100 → variacion = 20%
        ReajusteIPCService.aplicarReajusteDJ1871(
                List.of(detalle), mapaIPC, new BigDecimal("120"));

        // 10000 * 1.20 = 12000
        assertThat(detalle.ahorrosTrabajadorActualizado).isEqualByComparingTo(new BigDecimal("12000"));
        // 5000 * 1.20 = 6000
        assertThat(detalle.ahorrosDepositoActualizado).isEqualByComparingTo(new BigDecimal("6000"));
        // 3000 * 1.20 = 3600
        assertThat(detalle.ahorrosCotizacionesActualizado).isEqualByComparingTo(new BigDecimal("3600"));
        // 2000 * 1.20 = 2400
        assertThat(detalle.retirosTrabajadorActualizado).isEqualByComparingTo(new BigDecimal("2400"));
        // 1000 * 1.20 = 1200
        assertThat(detalle.retirosDepositoActualizado).isEqualByComparingTo(new BigDecimal("1200"));
        // 500 * 1.20 = 600
        assertThat(detalle.retirosCotizacionesActualizado).isEqualByComparingTo(new BigDecimal("600"));
    }

    // ---- Caso 6: agnoMes no encontrado en mapa → skip ----

    @Test
    @DisplayName("Detalle con agnoMes no encontrado en mapa IPC no se modifica")
    void aplicarReajuste_agnoMesNotFound_skip() {
        Map<String, ValorIPC> mapaIPC = Map.of();

        DetalleIPCDJ1871 detalle = new DetalleIPCDJ1871();
        detalle.agnoMes = "202212";
        detalle.ahorrosTrabajador = new BigDecimal("1000");
        // ahorrosTrabajadorActualizado comienza en 0

        ReajusteIPCService.aplicarReajusteDJ1871(
                List.of(detalle), mapaIPC, new BigDecimal("120"));

        // No se modifica → sigue en 0
        assertThat(detalle.ahorrosTrabajadorActualizado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- Caso 7: indexarIPC ----

    @Test
    @DisplayName("indexarIPC construye mapa agnoMes → ValorIPC")
    void indexarIPC_mapaCorrecto() {
        ValorIPC v1 = new ValorIPC();
        v1.agnoMes = "202201";
        v1.valorIpc = new BigDecimal("100");

        ValorIPC v2 = new ValorIPC();
        v2.agnoMes = "202202";
        v2.valorIpc = new BigDecimal("101");

        Map<String, ValorIPC> mapa = ReajusteIPCService.indexarIPC(List.of(v1, v2));

        assertThat(mapa).containsKey("202201");
        assertThat(mapa).containsKey("202202");
        assertThat(mapa.get("202201").valorIpc).isEqualByComparingTo(new BigDecimal("100"));
    }
}
