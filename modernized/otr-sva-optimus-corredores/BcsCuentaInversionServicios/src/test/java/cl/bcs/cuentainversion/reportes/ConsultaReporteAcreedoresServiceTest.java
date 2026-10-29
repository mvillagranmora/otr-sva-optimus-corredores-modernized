package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.ConsultaReporteAcreedoresService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Activated pending test (see reportes/README.md "Pending tests").
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultaReporteAcreedores} (legacy/.../cuentaInversion/ConsultaReporteAcreedores.java) against the modern {@code ConsultaReporteAcreedoresService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class ConsultaReporteAcreedoresServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-83_acreedores-consolidado-distribuye-el-haber-por-tramos",
            "CTA-B-84_acreedores-no-consolidado-por-cuenta",
            "CTA-B-85_acreedores-sin-cuentas-con-saldo-positivo",
            "CTA-B-86_acreedores-sin-abonos",
            "CTA-B-87_acreedores-moneda-extranjera-se-formatea-con-la-moneda-del-sistema",
            "CTA-B-88_acreedores-cliente-sin-abonos-se-omite",
            "CTA-B-89_acreedores-abono-que-cancela-exactamente-el-saldo",
            "CTA-B-90_acreedores-cliente-desconocido-para-la-bd-falla-con-error-generico",
            "CTA-B-91_acreedores-ningun-cliente-conocido",
            "CTA-B-92_acreedores-empresa-inexistente-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new ConsultaReporteAcreedoresService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (ConsultaReporteAcreedores): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-83: consolidated: distributes the credit by tranche")
    void ctaB83_acreedoresConsolidadoDistribuyeElHaberPorTramos() {
        // ConsultaReporteAcreedores.java:284-307, :622 obtenerCuentasInversionPositivas
        // Default Consolidar=S and empty currency (= system currency CLP): the positive client 1001 (150) is covered walking the
        // certified MOVABN credits from the newest: 30 (A), 50 (B) and 70 of the 80 credit (C, intermediation); the negative
        // client is not reported. Intermediation row first, then general.
        Contexto x = ejecutar("CTA-B-83_acreedores-consolidado-distribuye-el-haber-por-tramos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-84: not consolidated: by account")
    void ctaB84_acreedoresNoConsolidadoPorCuenta() {
        // ConsultaReporteAcreedores.java:357-380
        // Consolidar=N: one report row group per positive ACCOUNT, balance = available + retained (150 for 4001); account 4005
        // has a single 25 credit but 60 balance.
        Contexto x = ejecutar("CTA-B-84_acreedores-no-consolidado-por-cuenta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(3);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-85: no accounts with a positive balance")
    void ctaB85_acreedoresSinCuentasConSaldoPositivo() {
        // ConsultaReporteAcreedores.java:234 (SIN_CUENTAS...)
        // No positive balance -> ERROR SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO.
        Contexto x = ejecutar("CTA-B-85_acreedores-sin-cuentas-con-saldo-positivo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-86: no credits")
    void ctaB86_acreedoresSinAbonos() {
        // ConsultaReporteAcreedores.java:284, :357 (continue)
        // Positive balance but no certified MOVABN in the DB -> ERROR_AL_OBTENER_ABONOS (the staging rows of this run are
        // removed in the finally block).
        Contexto x = ejecutar("CTA-B-86_acreedores-sin-abonos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_OBTENER_ABONOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-87: foreign currency formatted with the system currency (defect)")
    void ctaB87_acreedoresMonedaExtranjeraSeFormateaConLaMonedaDelSistema() {
        // ConsultaReporteAcreedores.java:307, :380, :832 darFormatoMontos(idMonedaSistema)
        // LEGACY DEFECT: the amounts are formatted with the SYSTEM currency (CLP: 0 decimals, DOWN) even when the report is for
        // USD, so 12.345 USD is reported as 12. The USD balance is still tested with 2-decimals HALF_EVEN.
        Contexto x = ejecutar("CTA-B-87_acreedores-moneda-extranjera-se-formatea-con-la-moneda-del-sistema");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-88: client without credits is omitted")
    void ctaB88_acreedoresClienteSinAbonosSeOmite() {
        // ConsultaReporteAcreedores.java:284, :357
        // A positive client without credits in the DB (1001) is skipped silently; client 1002 is reported, classified as non
        // related even though Relacionado is SI (same 'S' vs 'SI' defect as the bolsa report).
        Contexto x = ejecutar("CTA-B-88_acreedores-cliente-sin-abonos-se-omite");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-89: a credit that cancels the balance exactly")
    void ctaB89_acreedoresAbonoQueCancelaExactamenteElSaldo() {
        // ConsultaReporteAcreedores.java:284-307
        // Balance 80 with credits 30 and 50: the running balance reaches exactly 0 and the walk stops.
        Contexto x = ejecutar("CTA-B-89_acreedores-abono-que-cancela-exactamente-el-saldo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-90: client unknown to the DB fails with the generic error")
    void ctaB90_acreedoresClienteDesconocidoParaLaBdFallaConErrorGenerico() {
        // ConsultaReporteAcreedores.java:406-411
        // A client returned by the balance service that has no portfolio row in the DB makes the loop dereference null: the
        // exception is swallowed and reported as ERROR_AL_GENERAR_REPORTE (the output table stays empty).
        Contexto x = ejecutar("CTA-B-90_acreedores-cliente-desconocido-para-la-bd-falla-con-error-generico");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_GENERAR_REPORTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-91: no known client")
    void ctaB91_acreedoresNingunClienteConocido() {
        // ConsultaReporteAcreedores.java:420-426
        // None of the positive accounts has client data -> ERROR_AL_OBTENER_DATOS_CLIENTE.
        Contexto x = ejecutar("CTA-B-91_acreedores-ningun-cliente-conocido");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_OBTENER_DATOS_CLIENTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-92: unknown company throws")
    void ctaB92_acreedoresEmpresaInexistenteLanzaExcepcion() {
        // ConsultaReporteAcreedores.java:197 (ConsultarEmpresa: Empresa no existe)
        // ConsultarEmpresa fails with 'Empresa no existe'; unlike the in-flow errors it propagates as an exception.
        Contexto x = ejecutar("CTA-B-92_acreedores-empresa-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.Exception").isEqualTo("java.lang.Exception");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
