package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.ProcesarCobroComisionPatrimonioService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Activated (was pending until the collaborator port and the services existed).
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ProcesarCobroComisionPatrimonio} (legacy/.../cuentaInversion/ProcesarCobroComisionPatrimonio.java) against the modern {@code ProcesarCobroComisionPatrimonioService}.
 *
 * <p>The collaborators go through the port {@code cl.bcs.cuentainversion.port.out.ColaboradoresReportes} (JSON contract of the legacy services).
 * <p>Constructor: new ProcesarCobroComisionPatrimonioService(DataSource, ColaboradoresReportes)
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class ProcesarCobroComisionPatrimonioServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-109_procesar-masivo-factura-solo-las-comisiones-vigentes",
            "CTA-B-110_procesar-solo-los-portafolios-seleccionados",
            "CTA-B-111_procesar-sin-portafolios-seleccionados",
            "CTA-B-112_procesar-sin-comisiones-vigentes",
            "CTA-B-113_procesar-error-del-servicio-de-facturacion-no-actualiza-estados",
            "CTA-B-114_procesar-sin-cobro-iva-lanza-excepcion",
            "CTA-B-115_procesar-solo-pasa-a-procesado-el-editado-en-estado-act",
            "CTA-B-116_procesar-registros-de-otro-periodo-no-se-tocan");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new ProcesarCobroComisionPatrimonioService(caso.dataSource(), colab)::execute);
        EJECUTADOS.incrementAndGet();
        return new Contexto(caso, colab, r);
    }

    private static void verificar(Contexto x) {
        x.caso().assertEquivalente(x.resultado());
        PendienteSupport.assertLlamadas(x.colaboradores(), x.caso());
        PendienteSupport.assertArchivos(x.caso());
    }

    private static List<String> servicios(FakeColaboradores f) {
        return f.llamadas().stream().map(o -> o.get("servicio").getAsString()).toList();
    }

    private static JsonObject salida(Resultado r) {
        assertThat(r.excepcion()).as("modern implementation threw").isNull();
        return r.salida();
    }

    private static long contador(JsonObject s, String campo) {
        return s.get(campo).getAsLong();
    }

    @AfterAll
    static void ningunCasoOmitido() {
        System.out.println("equivalence cases executed (ProcesarCobroComisionPatrimonio): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-109: mass run invoices only VIG commissions")
    void ctaB109_procesarMasivoFacturaSoloLasComisionesVigentes() {
        // ProcesarCobroComisionPatrimonio.java:148-177
        // Mass process: every VIG commission of the period/date range goes to CrearMovimientoComprobante (one movement per
        // portfolio: concept CMMNRV / CMTAPV for APV, voucher FOE / FOX, glosa from the dictionary, system date and currency,
        // IVA read from COM.Cobro); then the VIG custody and cash patrimony rows of the portfolio inside the period become PRO
        // and the OK message reports the count.
        Contexto x = ejecutar("CTA-B-109_procesar-masivo-factura-solo-las-comisiones-vigentes");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PATRIMONIOS_PROCESADOS_OK");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        assertThat(servicios(x.colaboradores())).containsExactly("CrearMovimientoComprobante");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-110: only the selected portfolios")
    void ctaB110_procesarSoloLosPortafoliosSeleccionados() {
        // ProcesarCobroComisionPatrimonio.java:151
        // EsProcesoMasivo=N: only the selected portfolios are billed and marked PRO; the other portfolio stays VIG.
        Contexto x = ejecutar("CTA-B-110_procesar-solo-los-portafolios-seleccionados");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PATRIMONIOS_PROCESADOS_OK");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        assertThat(servicios(x.colaboradores())).containsExactly("CrearMovimientoComprobante");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-111: no selected portfolios")
    void ctaB111_procesarSinPortafoliosSeleccionados() {
        // ProcesarCobroComisionPatrimonio.java:126 (UI_ERROR_ENTRADA_SIN_PORTAFOLIOS)
        // Not mass and nothing selected -> UI_ERROR_ENTRADA_SIN_PORTAFOLIOS; no collaborator is called.
        Contexto x = ejecutar("CTA-B-111_procesar-sin-portafolios-seleccionados");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_ENTRADA_SIN_PORTAFOLIOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-112: no VIG commissions")
    void ctaB112_procesarSinComisionesVigentes() {
        // ProcesarCobroComisionPatrimonio.java:155 (UI_ERROR_SIN_COMISIONES_A_PROCESAR)
        // The selected portfolio has no VIG commission -> UI_ERROR_SIN_COMISIONES_A_PROCESAR; billing service not called.
        Contexto x = ejecutar("CTA-B-112_procesar-sin-comisiones-vigentes");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_ERROR_SIN_COMISIONES_A_PROCESAR");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-113: billing service error: states are not updated")
    void ctaB113_procesarErrorDelServicioDeFacturacionNoActualizaEstados() {
        // ProcesarCobroComisionPatrimonio.java:160-162
        // The billing service reports an error: only its ERROR messages are returned (the warning is dropped), the error counter
        // is recomputed, and no state is changed.
        Contexto x = ejecutar("CTA-B-113_procesar-error-del-servicio-de-facturacion-no-actualiza-estados");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_FACTURACION");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("CrearMovimientoComprobante");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-114: missing IVA row throws (defect)")
    void ctaB114_procesarSinCobroIvaLanzaExcepcion() {
        // ProcesarCobroComisionPatrimonio.java:195 (cobros.get(0))
        // LEGACY DEFECT: the IVA row is read with cobros.get(0) without checking: absent IVA -> IndexOutOfBoundsException.
        Contexto x = ejecutar("CTA-B-114_procesar-sin-cobro-iva-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.IndexOutOfBoundsException").isEqualTo("java.lang.IndexOutOfBoundsException");
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-115: edited rows only go ACT -> PRO")
    void ctaB115_procesarSoloPasaAProcesadoElEditadoEnEstadoAct() {
        // ProcesarCobroComisionPatrimonio.java:171-173
        // LEGACY QUIRK: the edited-amount update only moves rows in state ACT of the period to PRO; rows in VIG (the state
        // EditarCobroComisionRegistros writes) stay VIG. The edited VIG amount of portfolio 2001 (999.99) is added to the billed
        // amount (see CTA-B-28).
        Contexto x = ejecutar("CTA-B-115_procesar-solo-pasa-a-procesado-el-editado-en-estado-act");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PATRIMONIOS_PROCESADOS_OK");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja", "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones", "BSCL.Custodia.BD::CUS.PatrimonioCustodia");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones")).as("rows of RegistrosEditadosComisiones").hasSize(3);
        assertThat(r.tablas().getAsJsonArray("BSCL.Custodia.BD::CUS.PatrimonioCustodia")).as("rows of PatrimonioCustodia").hasSize(5);
        assertThat(servicios(x.colaboradores())).containsExactly("CrearMovimientoComprobante");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-116: rows of another period are untouched")
    void ctaB116_procesarRegistrosDeOtroPeriodoNoSeTocan() {
        // ProcesarCobroComisionPatrimonio.java:148-173
        // Period 3 (March): only the March cash patrimony (portfolio 2001, 5.00) is billed and marked PRO; February rows are
        // untouched; the ACT edited row of period 3 becomes PRO.
        Contexto x = ejecutar("CTA-B-116_procesar-registros-de-otro-periodo-no-se-tocan");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PATRIMONIOS_PROCESADOS_OK");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(contador(s, "P_OUT_ADVERTENCIASCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(3);
        assertThat(servicios(x.colaboradores())).containsExactly("CrearMovimientoComprobante");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
