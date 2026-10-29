package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GuardarPatrimonioCaja} (legacy/.../cuentaInversion/GuardarPatrimonioCaja.java) against the modern {@code GuardarPatrimonioCajaService}.
 *
 * <p>The collaborators go through the port {@code cl.bcs.cuentainversion.port.out.ColaboradoresReportes} (JSON contract of the legacy services).
 * <p>Constructor: new GuardarPatrimonioCajaService(NamedParameterJdbcTemplate, ColaboradoresReportes)
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class GuardarPatrimonioCajaServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-124_guardar-patrimonio-moneda-sistema-liq-y-adq",
            "CTA-B-125_guardar-patrimonio-monto-cero-queda-inactivo",
            "CTA-B-126_guardar-patrimonio-moneda-extranjera-se-suma-convertida-al-cliente",
            "CTA-B-127_guardar-patrimonio-cuenta-solo-en-moneda-extranjera-se-pierde",
            "CTA-B-128_guardar-patrimonio-sin-cuentas-activas-solo-borra-el-dia",
            "CTA-B-129_guardar-patrimonio-con-calculo-de-comision",
            "CTA-B-130_guardar-patrimonio-pide-saldos-por-moneda-con-los-filtros-fijos");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GuardarPatrimonioCajaService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (GuardarPatrimonioCaja): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-124: system currency: LIQ and ADQ patrimony rows")
    void ctaB124_guardarPatrimonioMonedaSistemaLiqYAdq() {
        // GuardarPatrimonioCaja.java:151-241 (delete of the day, insert)
        // Only the system currency has accounts: one ConsultarSaldo call; a LIQ row (from the by-effective-date balances) and an
        // ADQ row (by movement date) per account. MontoPatrimonio = + SaldoDisponible + SaldoEnGarantia - SaldoRetiro (operators
        // + / - of PLA.ConfiguracionPatrimonioCaja, only ACT items; the unknown item SaldoInexistente is ignored and the '*'
        // operator item is not read); amounts formatted by MonedaFormato (CLP truncate 0 decimals); rows of the date are deleted
        // first (the 03-12 row stays); ids from the PatrimonioCaja sequence; commission fields stay 0/INA
        // (CobroComisionPatrimonio = N).
        Contexto x = ejecutar("CTA-B-124_guardar-patrimonio-moneda-sistema-liq-y-adq");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(8);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-125: zero amount stays inactive (INA)")
    void ctaB125_guardarPatrimonioMontoCeroQuedaInactivo() {
        // GuardarPatrimonioCaja.java:228, :312, :360
        // A patrimony that nets to exactly 0 is stored with IdEstado INA, otherwise ACT.
        Contexto x = ejecutar("CTA-B-125_guardar-patrimonio-monto-cero-queda-inactivo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(5);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-126: foreign currency is added converted to the client")
    void ctaB126_guardarPatrimonioMonedaExtranjeraSeSumaConvertidaAlCliente() {
        // GuardarPatrimonioCaja.java:187-235 (CalcularParidad)
        // USD patrimony (formatted to 2 decimals, HALF_UP) is converted with CalcularParidad (USD>CLP 950.5 in the fake) and
        // ADDED to the system-currency row of the same client, portfolio and date type; only system-currency rows are inserted
        // (the USD rows themselves are not stored). Accounts of USD with no CLP row for that client/portfolio are lost.
        Contexto x = ejecutar("CTA-B-126_guardar-patrimonio-moneda-extranjera-se-suma-convertida-al-cliente");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(6);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-127: an account only in foreign currency is lost (defect)")
    void ctaB127_guardarPatrimonioCuentaSoloEnMonedaExtranjeraSePierde() {
        // GuardarPatrimonioCaja.java:180-235
        // LEGACY DEFECT: client 1003 only has a USD account: its converted patrimony has no CLP row to be added to and is
        // dropped (nothing is stored for it).
        Contexto x = ejecutar("CTA-B-127_guardar-patrimonio-cuenta-solo-en-moneda-extranjera-se-pierde");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(4);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion", "ConsultarSaldoCuentaInversion", "CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-128: no active accounts: only deletes the day")
    void ctaB128_guardarPatrimonioSinCuentasActivasSoloBorraElDia() {
        // GuardarPatrimonioCaja.java:151-158
        // No active account at all: the rows of the date are deleted and nothing is inserted; ConsultarSaldo is never called.
        Contexto x = ejecutar("CTA-B-128_guardar-patrimonio-sin-cuentas-activas-solo-borra-el-dia");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(4);
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-129: with commission calculation (parametro S)")
    void ctaB129_guardarPatrimonioConCalculoDeComision() {
        // GuardarPatrimonioCaja.java:237 + calculoPatrimonioComision :381
        // CTA.CobroComisionPatrimonio = S: the ADQ rows get MontoPatrimonioAfectoComision / MontoComision /
        // PorcentajeCobroComision / IdEstadoComision from GuardarPatrimonioCajaCalculoComision (previous business day 1,000,000
        // + 50,000 -> 105, VIG for portfolio 2001; 2,000,000 -> 200 for portfolio 2002); LIQ rows keep 0 / INA.
        Contexto x = ejecutar("CTA-B-129_guardar-patrimonio-con-calculo-de-comision");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(8);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-130: asks ConsultarSaldo per currency with fixed filters")
    void ctaB130_guardarPatrimonioPideSaldosPorMonedaConLosFiltrosFijos() {
        // GuardarPatrimonioCaja.java:251-360
        // Pins the request to ConsultarSaldoCuentaInversion: module/user from the input, UsaReglasNegocio S, IncluyeOtrosSaldos
        // S, dates = p_Fecha, ProductoAgrupador null and the account list of the currency (IdPortafolio, IdMoneda).
        Contexto x = ejecutar("CTA-B-130_guardar-patrimonio-pide-saldos-por-moneda-con-los-filtros-fijos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")).as("rows of PatrimonioCaja").hasSize(4);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
