package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.GenerarArchivoClientesPorPagarYCobrarService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar} (legacy/.../cuentaInversion/GenerarArchivoClientesPorPagarYCobrar.java) against the modern {@code GenerarArchivoClientesPorPagarYCobrarService}.
 *
 * Every case was RECORDED by executing the unmodified legacy class; the modern service runs on the same H2 data with the
 * collaborator fake {@link FakeColaboradores} (a mirror of the oracle shims) and must reproduce the JavaCall output, the changed tables,
 * the call log of its collaborators (llamadasColaboradores) and the generated files (names and cell contents / CSV lines, compared by ArchivoDump).
 */
class GenerarArchivoClientesPorPagarYCobrarServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-182_pagar-cobrar-busqueda-todos-ordenada-por-fecha-efectiva-desc",
            "CTA-B-183_pagar-cobrar-solo-saldos-negativos",
            "CTA-B-184_pagar-cobrar-solo-saldos-positivos-o-cero",
            "CTA-B-185_pagar-cobrar-filtra-por-moneda",
            "CTA-B-186_pagar-cobrar-desempata-el-ultimo-movimiento-por-ingreso-y-id",
            "CTA-B-187_pagar-cobrar-cliente-sin-indicador-relcorr-se-excluye",
            "CTA-B-188_pagar-cobrar-movimiento-del-dia-de-consulta-se-excluye",
            "CTA-B-189_pagar-cobrar-garantias-hacen-visible-una-cuenta-en-cero",
            "CTA-B-190_pagar-cobrar-saldo-retenido-con-fecha-efectiva-futura",
            "CTA-B-191_pagar-cobrar-simultaneas-a-plazo",
            "CTA-B-192_pagar-cobrar-money-market-clp",
            "CTA-B-193_pagar-cobrar-money-market-en-moneda-extranjera-usa-paridad",
            "CTA-B-194_pagar-cobrar-moneda-extranjera-paridad-no-disponible-deja-el-money-market-en-clp",
            "CTA-B-195_pagar-cobrar-archivo-xls-con-resumen-por-tramos",
            "CTA-B-196_pagar-cobrar-sin-datos-falta-data-reporte",
            "CTA-B-197_pagar-cobrar-sin-configuracion-de-ruta",
            "CTA-B-198_pagar-cobrar-ruta-inexistente-error-al-generar-archivo",
            "CTA-B-199_pagar-cobrar-sin-es-busqueda-lanza-excepcion",
            "CTA-B-200_pagar-cobrar-simultanea-duplicada-por-cuenta-lanza-excepcion",
            "CTA-B-224_pagar-cobrar-limites-de-los-tramos-de-antiguedad-en-el-resumen");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Contexto(CasoReporte caso, FakeColaboradores colaboradores, Resultado resultado) {
    }

    private static Contexto ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        Resultado r = caso.ejecutar(new GenerarArchivoClientesPorPagarYCobrarService(caso.jdbc(), colab)::execute);
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
        System.out.println("equivalence cases executed (GenerarArchivoClientesPorPagarYCobrar): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-182: search returns the latest movement per account ordered by effective date desc")
    void ctaB182_pagarCobrarBusquedaTodosOrdenadaPorFechaEfectivaDesc() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211 (SelectUltimosSaldos), :346-366 (zero and day-0 filters), :370 (EsBusqueda=S)
        // EsBusqueda=S returns the rows (no file): last certified movement per account (RANK by FechaCertificacion,
        // FechaIngresoRegistro, id), ordered by FechaEfectivaMovimiento DESC then account. Accounts with every amount at zero
        // (4007, no extras) are dropped; Relacion 'R' for the related client 1002; Pacto stays 0 (TODO in the legacy).
        // FechaEfectivaMovimiento must be before FechaSistema.
        Contexto x = ejecutar("CTA-B-182_pagar-cobrar-busqueda-todos-ordenada-por-fecha-efectiva-desc");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-183: TipoSaldo NEG keeps negative balances")
    void ctaB183_pagarCobrarSoloSaldosNegativos() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211 (SelectUltimosSaldos TipoSaldo)
        // TipoSaldo NEG keeps Saldo < 0.
        Contexto x = ejecutar("CTA-B-183_pagar-cobrar-solo-saldos-negativos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(4);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(4);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-184: TipoSaldo POS keeps balances >= 0")
    void ctaB184_pagarCobrarSoloSaldosPositivosOCero() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211
        // TipoSaldo POS keeps Saldo >= 0 (zero included), then zero rows are dropped by the all-amounts-zero filter.
        Contexto x = ejecutar("CTA-B-184_pagar-cobrar-solo-saldos-positivos-o-cero");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(2);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(3);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-185: IdMoneda filter")
    void ctaB185_pagarCobrarFiltraPorMoneda() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211
        // IdMoneda USD keeps USD accounts only.
        Contexto x = ejecutar("CTA-B-185_pagar-cobrar-filtra-por-moneda");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(2);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(3);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-186: ties on FechaCertificacion are broken by FechaIngresoRegistro then id")
    void ctaB186_pagarCobrarDesempataElUltimoMovimientoPorIngresoYId() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211 (RANK in SelectUltimosSaldos)
        // 4001: a second movement with the same FechaCertificacion but a later FechaIngresoRegistro wins (-999). 4003: same cert
        // and same ingreso -> the higher IdCargoAbonoMovimiento wins (1234).
        Contexto x = ejecutar("CTA-B-186_pagar-cobrar-desempata-el-ultimo-movimiento-por-ingreso-y-id");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-187: client without the RELCORR indicator disappears (defect)")
    void ctaB187_pagarCobrarClienteSinIndicadorRelcorrSeExcluye() {
        // GenerarArchivoClientesPorPagarYCobrar.java:211 (INNER JOIN indicadorCliente)
        // LEGACY DEFECT/QUIRK: the RELCORR indicator is an INNER JOIN, so a client without the indicator row disappears from the
        // report.
        Contexto x = ejecutar("CTA-B-187_pagar-cobrar-cliente-sin-indicador-relcorr-se-excluye");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(5);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(5);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-188: a movement effective on FechaSistema is excluded")
    void ctaB188_pagarCobrarMovimientoDelDiaDeConsultaSeExcluye() {
        // GenerarArchivoClientesPorPagarYCobrar.java:358-364
        // 4004 latest movement is effective on FechaSistema: 'fechaUnDiaAntes.after(FechaEfectivaMovimiento)' is false -> the
        // row is dropped (the Calendar.add(DATE, 0) adds nothing).
        Contexto x = ejecutar("CTA-B-188_pagar-cobrar-movimiento-del-dia-de-consulta-se-excluye");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(5);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-189: guarantees are never reported: :p_FechaHasta is not bound (defect)")
    void ctaB189_pagarCobrarGarantiasHacenVisibleUnaCuentaEnCero() {
        // GenerarArchivoClientesPorPagarYCobrar.java:229-236 (paramsOtrosSaldos has no p_FechaHasta)
        // LEGACY DEFECT: the guarantee SQL filters FechaOperacion <= :p_FechaHasta but the legacy only binds p_FechaDesde: the
        // parameter is NULL, the comparison is never true and guarantees are never reported (Garantias = 0 and account 4007,
        // which only has a guarantee of 1000, stays hidden).
        Contexto x = ejecutar("CTA-B-189_pagar-cobrar-garantias-hacen-visible-una-cuenta-en-cero");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-190: retained balance of movements effective after FechaSistema")
    void ctaB190_pagarCobrarSaldoRetenidoConFechaEfectivaFutura() {
        // GenerarArchivoClientesPorPagarYCobrar.java:238-239, :343-347
        // SaldoRetenido = abonos - cargos certified up to FechaSistema but effective after it (MOVABN +250.5, MOVCAR -40 =
        // 210.5). The two movements are also the latest certified, so they set Saldo = -1.
        Contexto x = ejecutar("CTA-B-190_pagar-cobrar-saldo-retenido-con-fecha-efectiva-futura");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(5);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-191: simultaneous operations (COMSIM +, VENSIM -)")
    void ctaB191_pagarCobrarSimultaneasAPlazo() {
        // GenerarArchivoClientesPorPagarYCobrar.java:241-242
        // Simultaneas = sum of INGMontoAPlazo of COMSIM (+) and VENSIM (-) operations settled up to FechaSistema: 500 - 120 =
        // 380 for 4007 (balance 0, visible because of it).
        Contexto x = ejecutar("CTA-B-191_pagar-cobrar-simultaneas-a-plazo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(7);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-192: money market in CLP = quantity x unit value")
    void ctaB192_pagarCobrarMoneyMarketClp() {
        // GenerarArchivoClientesPorPagarYCobrar.java:254-300
        // MoneyMarket = custody quantity x ValorCuota of the series whose fund is marked MoneyMarket = S: 10 x 1500.5 = 15005 on
        // 4001 (CLP). The non money market series (FMNO-A) is ignored.
        Contexto x = ejecutar("CTA-B-192_pagar-cobrar-money-market-clp");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-193: money market of a foreign currency account is converted with CalcularParidad")
    void ctaB193_pagarCobrarMoneyMarketEnMonedaExtranjeraUsaParidad() {
        // GenerarArchivoClientesPorPagarYCobrar.java:303-332
        // USD account 4002: the CLP money market value is converted with CalcularParidad (origin CLP -> destination USD,
        // IdInterno = IdCuentaInversion), 15005 x 0.001 = 15.005.
        Contexto x = ejecutar("CTA-B-193_pagar-cobrar-money-market-en-moneda-extranjera-usa-paridad");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-194: no rate: the CLP money market stays on the USD account")
    void ctaB194_pagarCobrarMonedaExtranjeraParidadNoDisponibleDejaElMoneyMarketEnClp() {
        // GenerarArchivoClientesPorPagarYCobrar.java:322-332
        // No rate configured for CLP>USD: the conversion answers MontoDestino null and the CalcularParidad shim marks the
        // calculation as failed; the legacy keeps what the answer holds.
        Contexto x = ejecutar("CTA-B-194_pagar-cobrar-moneda-extranjera-paridad-no-disponible-deja-el-money-market-en-clp");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(s.getAsJsonArray("P_OUT_RESULTADO")).hasSize(6);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-195: xls with Datos and Resumen sheets")
    void ctaB195_pagarCobrarArchivoXlsConResumenPorTramos() {
        // GenerarArchivoClientesPorPagarYCobrar.java:412-418 (generarArchivo), :426-480 (totales por rangos)
        // EsBusqueda=N writes <RutaBase><RutaCta>ClientesPorPagarYCobrar_<ts>.xls with sheets 'Datos' (one row per account) and
        // 'Resumen' (totals por pagar / por cobrar by age: 1-7, 8-9, 10-30, 31+ days, for related, not related and all).
        // P_OUT_RUTAARCHIVO is RutaCta + name; P_OUT_RESULTADO is null.
        Contexto x = ejecutar("CTA-B-195_pagar-cobrar-archivo-xls-con-resumen-por-tramos");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ClientesPorPagarYCobrar_<AHORA>.xls");
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-196: no data: FALTA_DATA_REPORTE")
    void ctaB196_pagarCobrarSinDatosFaltaDataReporte() {
        // GenerarArchivoClientesPorPagarYCobrar.java:385
        // No account of currency EUR -> FALTA_DATA_REPORTE.
        Contexto x = ejecutar("CTA-B-196_pagar-cobrar-sin-datos-falta-data-reporte");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("FALTA_DATA_REPORTE");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-197: no path configuration")
    void ctaB197_pagarCobrarSinConfiguracionDeRuta() {
        // GenerarArchivoClientesPorPagarYCobrar.java:403
        // No RutaArchivosCta / RutaBaseArchivos configured -> FALTA_CONFIGURACION_RUTA_ARCHIVO (checked after the data query).
        Contexto x = ejecutar("CTA-B-197_pagar-cobrar-sin-configuracion-de-ruta");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("FALTA_CONFIGURACION_RUTA_ARCHIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-198: unwritable path: ERROR_GENERAR_ARCHIVO")
    void ctaB198_pagarCobrarRutaInexistenteErrorAlGenerarArchivo() {
        // GenerarArchivoClientesPorPagarYCobrar.java:416-417
        // The directory of the file does not exist: FileNotFoundException is swallowed by generarArchivo, which answers false ->
        // ERROR_GENERAR_ARCHIVO. P_OUT_RUTAARCHIVO is already set.
        Contexto x = ejecutar("CTA-B-198_pagar-cobrar-ruta-inexistente-error-al-generar-archivo");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("ERROR_GENERAR_ARCHIVO");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(7);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-199: missing EsBusqueda throws NullPointerException (defect)")
    void ctaB199_pagarCobrarSinEsBusquedaLanzaExcepcion() {
        // GenerarArchivoClientesPorPagarYCobrar.java:370
        // LEGACY DEFECT: EsBusqueda absent -> in.EsBusqueda.equals('S') throws NullPointerException after all queries ran (and
        // its DB writes are rolled back).
        Contexto x = ejecutar("CTA-B-199_pagar-cobrar-sin-es-busqueda-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-200: duplicate portfolio-currency key throws IllegalStateException (defect)")
    void ctaB200_pagarCobrarSimultaneaDuplicadaPorCuentaLanzaExcepcion() {
        // GenerarArchivoClientesPorPagarYCobrar.java:241-242 (Collectors.toMap)
        // LEGACY DEFECT: two operations of different clients on the same portfolio and currency give two aggregated rows with
        // the same 'portfolio-currency' key and Collectors.toMap throws IllegalStateException (duplicate key).
        Contexto x = ejecutar("CTA-B-200_pagar-cobrar-simultanea-duplicada-por-cuenta-lanza-excepcion");
        Resultado r = x.resultado();
        assertThat(r.excepcion()).as("legacy threw java.lang.IllegalStateException").isEqualTo("java.lang.IllegalStateException");
        assertThat(servicios(x.colaboradores())).as("legacy called no collaborator").isEmpty();
        assertThat(PendienteSupport.nombresArchivos(x.caso())).as("legacy generated no file").isEmpty();
        verificar(x);
    }

    @Test
    @DisplayName("CTA-B-224: boundaries of the 1-7 / 8-9 / 10-30 / 31+ day ranges of the Resumen sheet")
    void ctaB224_pagarCobrarLimitesDeLosTramosDeAntiguedadEnElResumen() {
        // GenerarArchivoClientesPorPagarYCobrar.java:457-480 (sumarRango)
        // Boundaries of the age ranges of the Resumen sheet (days between FechaSistema 03-14 and the effective date): 7 days ->
        // 1-7, 8 -> 8-9, 9 -> 8-9, 10 -> 10-30, 30 -> 10-30, 31 -> 31+. Accounts 4001 (7d), 4003 (8d, related client), 4004
        // (9d), 4005 (10d), 4002 USD (30d), 4006 USD (31d, related client); balances are negative so they go to 'por pagar'.
        // Amounts of different currencies are added together.
        Contexto x = ejecutar("CTA-B-224_pagar-cobrar-limites-de-los-tramos-de-antiguedad-en-el-resumen");
        Resultado r = x.resultado();
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso");
        assertThat(r.tablas().getAsJsonArray("BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso")).as("rows of ConsultarSaldoCuentaInversion_Cuenta_Paso").hasSize(6);
        assertThat(servicios(x.colaboradores())).containsExactly("CalcularParidad");
        assertThat(PendienteSupport.nombresArchivos(x.caso())).containsExactlyInAnyOrder("CTA/ClientesPorPagarYCobrar_<AHORA>.xls");
        verificar(x);
    }
}
