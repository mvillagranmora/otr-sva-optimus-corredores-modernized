package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.ConsultaReporteAntiguedadBolsaService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa} (legacy/.../cuentaInversion/ConsultaReporteAntiguedadBolsa.java) against the modern {@code ConsultaReporteAntiguedadBolsaService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores).
 */
class ConsultaReporteAntiguedadBolsaServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-69_bolsa-separa-intermediacion-asigna-deuda-por-tramos",
            "CTA-B-70_bolsa-sin-separar-intermediacion-devuelve-tabla-vacia",
            "CTA-B-71_bolsa-todas-las-monedas-formato-sin-decimales",
            "CTA-B-72_bolsa-consolidado-suma-las-cuentas-del-cliente",
            "CTA-B-73_bolsa-sin-cuentas-con-saldo-negativo",
            "CTA-B-74_bolsa-saldo-negativo-sin-cargos",
            "CTA-B-75_bolsa-clp-trunca-saldo-antes-de-decidir-si-es-negativo",
            "CTA-B-76_bolsa-moneda-extranjera-redondea-a-2-decimales-half-even",
            "CTA-B-77_bolsa-cliente-relacionado-queda-clasificado-como-no-relacionado",
            "CTA-B-78_bolsa-cargo-que-cancela-exactamente-el-saldo",
            "CTA-B-79_bolsa-tramos-por-dias-corridos-desde-la-fecha-de-consulta",
            "CTA-B-80_bolsa-orden-de-aplicacion-por-fecha-efectiva-certificacion-e-id",
            "CTA-B-81_bolsa-pide-saldos-con-el-ejecutivo-y-los-filtros-fijos",
            "CTA-B-82_bolsa-empresa-inexistente-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new ConsultaReporteAntiguedadBolsaService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (ConsultaReporteAntiguedadBolsa): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-69: intermediation split assigns the debt by tranche")
    void ctaB69_bolsaSeparaIntermediacionAsignaDeudaPorTramos() {
        // ConsultaReporteAntiguedadBolsa.java:351, :426, :807 asignarDeudaATramo
        // Debt -150 is covered walking the certified MOVCAR charges from the newest: 30 (tramo A), 50 (B) and the remaining 70
        // of the 80 charge (C, intermediation); only charges up to the query date count, only MOVCAR/CRT. With
        // SeparaIntermediacion=S the intermediation row comes first, then the general row.
        Contexto x = ejecutar("CTA-B-69_bolsa-separa-intermediacion-asigna-deuda-por-tramos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-70: SeparaIntermediacion = N returns an empty table (defect)")
    void ctaB70_bolsaSinSepararIntermediacionDevuelveTablaVacia() {
        // ConsultaReporteAntiguedadBolsa.java:351, :426
        // LEGACY DEFECT: with SeparaIntermediacion=N nothing is added to the output (no else branch): the report comes back
        // empty without error.
        Contexto x = ejecutar("CTA-B-70_bolsa-sin-separar-intermediacion-devuelve-tabla-vacia");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-71: all currencies: formatted without decimals")
    void ctaB71_bolsaTodasLasMonedasFormatoSinDecimales() {
        // ConsultaReporteAntiguedadBolsa.java:847 darFormatoMontos
        // 'TODAS' (or empty) currency = every currency of CTA.CuentaInversion; ConsultarSaldo is called ONCE with IdMoneda null
        // and its answer is split by currency. With more than one currency every amount is truncated to 0 decimals (setScale(0,
        // DOWN)). Row order across currencies follows the unordered DISTINCT query, so it is not compared.
        Contexto x = ejecutar("CTA-B-71_bolsa-todas-las-monedas-formato-sin-decimales");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(3);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-72: consolidated: sums the accounts of the client")
    void ctaB72_bolsaConsolidadoSumaLasCuentasDelCliente() {
        // ConsultaReporteAntiguedadBolsa.java:241, :292
        // Consolidar=S with one currency: ConsultarSaldo is asked for that currency, clients whose TOTAL (available + retained)
        // is negative are reported once (-150 = -100-20-30), using the first portfolio and all the client charges; the positive
        // client 1002 is excluded.
        Contexto x = ejecutar("CTA-B-72_bolsa-consolidado-suma-las-cuentas-del-cliente");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(2);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(2);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-73: no accounts with a negative balance")
    void ctaB73_bolsaSinCuentasConSaldoNegativo() {
        // ConsultaReporteAntiguedadBolsa.java:249 (SIN_SALDO_CUENTAS)
        // Zero or positive balances only -> ERROR SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO; nothing is written.
        Contexto x = ejecutar("CTA-B-73_bolsa-sin-cuentas-con-saldo-negativo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-74: negative balance without charges")
    void ctaB74_bolsaSaldoNegativoSinCargos() {
        // ConsultaReporteAntiguedadBolsa.java:317, :392
        // Negative balance but no certified charge in the DB -> ERROR_AL_OBTENER_LOS_CARGOS. The staging table keeps its rows
        // (it is only cleaned at the start of the next run).
        Contexto x = ejecutar("CTA-B-74_bolsa-saldo-negativo-sin-cargos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_AL_OBTENER_LOS_CARGOS");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(0);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-75: CLP truncates the balance before deciding if negative")
    void ctaB75_bolsaClpTruncaSaldoAntesDeDecidirSiEsNegativo() {
        // ConsultaReporteAntiguedadBolsa.java:625 (setScale(0, DOWN))
        // CLP: the balance is truncated to 0 decimals (DOWN) before the sign test: -0.99 becomes 0 (not negative), -1.00 stays
        // negative (only client 1003 is reported).
        Contexto x = ejecutar("CTA-B-75_bolsa-clp-trunca-saldo-antes-de-decidir-si-es-negativo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-76: foreign currency rounds to 2 decimals HALF_EVEN")
    void ctaB76_bolsaMonedaExtranjeraRedondeaA2DecimalesHalfEven() {
        // ConsultaReporteAntiguedadBolsa.java:631 (setScale(2, HALF_EVEN))
        // Non-CLP: balance rounded HALF_EVEN to 2 decimals before the sign test: -0.005 -> -0.00 and -0.004 -> -0.00 are not
        // negative, -0.015 -> -0.02 is. The reported amounts keep 2 decimals.
        Contexto x = ejecutar("CTA-B-76_bolsa-moneda-extranjera-redondea-a-2-decimales-half-even");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-77: related client is classified as not related (defect)")
    void ctaB77_bolsaClienteRelacionadoQuedaClasificadoComoNoRelacionado() {
        // ConsultaReporteAntiguedadBolsa.java:310, :384 (Relacionado.equals("S"))
        // LEGACY DEFECT: Relacionado is 'SI'/'NO' but the code compares with 'S', so a related-party client is still classified
        // 'Cuentas por Cobrar y Cuentas de Documentos'. The legal entity name comes from RazonSocial.
        Contexto x = ejecutar("CTA-B-77_bolsa-cliente-relacionado-queda-clasificado-como-no-relacionado");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-78: a charge that cancels the balance exactly")
    void ctaB78_bolsaCargoQueCancelaExactamenteElSaldo() {
        // ConsultaReporteAntiguedadBolsa.java:317-351
        // Balance -80 with charges 30 and 50: the running balance reaches exactly 0 and the walk stops (== 0 branch).
        Contexto x = ejecutar("CTA-B-78_bolsa-cargo-que-cancela-exactamente-el-saldo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-79: tranches by calendar days from the query date")
    void ctaB79_bolsaTramosPorDiasCorridosDesdeLaFechaDeConsulta() {
        // ConsultaReporteAntiguedadBolsa.java:771 buscarTramo
        // Tramo boundaries counted in calendar days from 2025-03-14: A = 0..2 days (03-12 .. 03-14), B = 3..10 (03-04 .. 03-11),
        // C = 11..30 (02-12 .. 03-03), D = 31 days or older (02-11 and before). Distinct amounts per day show which tramo each
        // belongs to: A 10, B 20+30, C 40+50, D 60+70.
        Contexto x = ejecutar("CTA-B-79_bolsa-tramos-por-dias-corridos-desde-la-fecha-de-consulta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-80: charges are applied by effective date, certification and id")
    void ctaB80_bolsaOrdenDeAplicacionPorFechaEfectivaCertificacionEId() {
        // ConsultaReporteAntiguedadBolsa.java:317, :392 (sort comparator)
        // Charges are applied newest first by (FechaEfectivaMovimiento, FechaCertificacion, IdCargoAbonoMovimiento) descending:
        // 20 (id 8071), 10 (8070) then 40 (8072, certified on 03-04); balance -45 is covered by 20 + 10 (tramo A) and 15 of the
        // 40 charge (tramo B, by its certification date 03-04).
        Contexto x = ejecutar("CTA-B-80_bolsa-orden-de-aplicacion-por-fecha-efectiva-certificacion-e-id");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-81: asks ConsultarSaldo with the executive and fixed filters")
    void ctaB81_bolsaPideSaldosConElEjecutivoYLosFiltrosFijos() {
        // ConsultaReporteAntiguedadBolsa.java:566-572 (llamada a ConsultarSaldoCuentaInversion)
        // Pins the request sent to ConsultarSaldoCuentaInversion (llamadasColaboradores): UsaReglasNegocio S, IncluyeOtrosSaldos
        // S, IncluyeProductoApv S, dates = query date, executive in CuentaInversionAConsultar.
        Contexto x = ejecutar("CTA-B-81_bolsa-pide-saldos-con-el-ejecutivo-y-los-filtros-fijos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_TABLA")).hasSize(1);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso")).as("rows of ConsultaReporteAntiguedadBolsa_Cuenta_Paso").hasSize(1);
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa", "ConsultarSaldoCuentaInversion");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-82: unknown company throws")
    void ctaB82_bolsaEmpresaInexistenteLanzaExcepcion() {
        // ConsultaReporteAntiguedadBolsa.java:205 (ConsultarEmpresa: Empresa no existe)
        // ConsultarEmpresa fails with 'Empresa no existe' and the exception propagates (no message to the caller).
        Contexto x = ejecutar("CTA-B-82_bolsa-empresa-inexistente-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.Exception").isEqualTo("java.lang.Exception");
        assertThat(servicios(x.colaboradores())).containsExactly("ConsultarEmpresa");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }
}
