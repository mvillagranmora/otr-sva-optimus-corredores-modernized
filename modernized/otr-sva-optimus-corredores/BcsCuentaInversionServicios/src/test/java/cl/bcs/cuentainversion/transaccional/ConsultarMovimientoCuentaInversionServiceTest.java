package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.infrastructure.adapter.HanaRepository;
import cl.bcs.cuentainversion.service.ConsultarMovimientoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion}: movements of portfolio/currency from the view
 * ConsultarCartolaDeMovimientos (running balance SaldoSimulado, Cargo/Abono, matrix and ADC filters), split by PLA.FechaGoLive into a historic part
 * (CTA.CargoAbonoMovimientoHistorico), a current part and a hybrid. The modern service is built with the real {@code HanaRepository} over the case database and a
 * {@code ConfiguracionSistemaPort} fed with the case configuration ({@link ConfiguracionDelCaso}).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-159..188}. Legacy: {@code ConsultarMovimientoCuentaInversion}; modern: {@link ConsultarMovimientoCuentaInversionService}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarMovimientoCuentaInversionServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-159_saldo-simulado-acumulado-por-fecha-de-movimiento-anulado-suma-cero",
            "CTA-A-160_saldo-por-fecha-efectiva-ordena-y-filtra-por-fecha-efectiva",
            "CTA-A-161_filtro-por-estado",
            "CTA-A-162_conceptos-de-migracion-y-ajuste-se-excluyen",
            "CTA-A-163_tipo-de-movimiento-desconocido-no-tiene-cargo-ni-abono-ni-suma",
            "CTA-A-164_saldo-inicial-nulo-cuenta-como-cero",
            "CTA-A-165_lista-de-consultas-vacia-informa-exito",
            "CTA-A-166_dos-consultas-de-portafolios-distintos",
            "CTA-A-167_moneda-distinta-solo-trae-su-cuenta",
            "CTA-A-168_filtro-por-matriz-agrupadora-apv",
            "CTA-A-169_matriz-que-no-corresponde-no-trae-movimientos",
            "CTA-A-170_filtro-es-adc-coincide",
            "CTA-A-171_filtro-es-adc-no-coincide",
            "CTA-A-172_portafolio-sin-matriz-usa-matriz-no-apv",
            "CTA-A-173_incluye-saldo-nulo-lanza-NullPointerException",
            "CTA-A-174_fecha-desde-invalida-lanza-excepcion",
            "CTA-A-175_fecha-desde-nula-lanza-excepcion",
            "CTA-A-176_historico-puro-sin-saldos",
            "CTA-A-177_historico-puro-con-saldo-inicial-y-final",
            "CTA-A-178_historico-sin-filas-con-saldos-no-agrega-filas",
            "CTA-A-179_hibrido-con-saldos-usa-saldo-final-historico-como-inicial",
            "CTA-A-180_hibrido-sin-saldos-descarta-el-segundo-movimiento-historico",
            "CTA-A-181_hibrido-sin-saldos-con-un-solo-movimiento-historico-lanza-IndexOutOfBounds",
            "CTA-A-182_hibrido-sin-movimientos-historicos-consulta-solo-lo-actual",
            "CTA-A-183_hasta-igual-a-la-fecha-de-corte-es-hibrido",
            "CTA-A-184_desde-igual-a-la-fecha-de-corte-es-solo-actual",
            "CTA-A-185_dos-consultas-historicas-acumulan-la-lista-y-duplican-filas",
            "CTA-A-186_fecha-de-corte-invalida-se-ignora",
            "CTA-A-187_fecha-de-corte-no-configurada-se-ignora",
            "CTA-A-188_historico-ignora-el-filtro-de-estado");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ConsultarMovimientoCuentaInversionService service = new ConsultarMovimientoCuentaInversionService(new HanaRepository(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarMovimientoCuentaInversion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-159: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: SaldoSimulado = SaldoInicial + running sum, ANU counts 0; ConsultarMovimientoCuentaInversion.java:253-257
    @Test
    void ctaA159_saldoSimuladoAcumuladoPorFechaDeMovimientoAnuladoSumaCero() {
        verificar(CASOS.get(0));
    }

    // CTA-A-160: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: SaldoPorFechaEfectiva S selects/orders by FechaEfectivaMovimiento
    @Test
    void ctaA160_saldoPorFechaEfectivaOrdenaYFiltraPorFechaEfectiva() {
        verificar(CASOS.get(1));
    }

    // CTA-A-161: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: CM.IdEstado = COALESCE(CMP.IdEstado, CM.IdEstado); balance over the filtered rows
    @Test
    void ctaA161_filtroPorEstado() {
        verificar(CASOS.get(2));
    }

    // CTA-A-162: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: concepts ABMIGI/CAMIGI/ABMIGR/ABAJUS/CAAJUS excluded
    @Test
    void ctaA162_conceptosDeMigracionYAjusteSeExcluyen() {
        verificar(CASOS.get(3));
    }

    // CTA-A-163: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: Cargo/Abono NULL for a type other than MOVCAR/MOVABN, contributes nothing to the balance
    @Test
    void ctaA163_tipoDeMovimientoDesconocidoNoTieneCargoNiAbonoNiSuma() {
        verificar(CASOS.get(4));
    }

    // CTA-A-164: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: COALESCE(SaldoInicial, 0)
    @Test
    void ctaA164_saldoInicialNuloCuentaComoCero() {
        verificar(CASOS.get(5));
    }

    // CTA-A-165: ConsultarMovimientoCuentaInversion.java:253-264: no consultas -> success message, no movements
    @Test
    void ctaA165_listaDeConsultasVaciaInformaExito() {
        verificar(CASOS.get(6));
    }

    // CTA-A-166: ConsultarMovimientoCuentaInversion.java:253-257: two portfolios in one call
    @Test
    void ctaA166_dosConsultasDePortafoliosDistintos() {
        verificar(CASOS.get(7));
    }

    // CTA-A-167: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: join on IdPortafolio AND IdMoneda
    @Test
    void ctaA167_monedaDistintaSoloTraeSuCuenta() {
        verificar(CASOS.get(8));
    }

    // CTA-A-168: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: IdMatrizAgrupador filter against the portfolio APV matrix
    @Test
    void ctaA168_filtroPorMatrizAgrupadoraApv() {
        verificar(CASOS.get(9));
    }

    // CTA-A-169: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: matrix that does not match -> no rows
    @Test
    void ctaA169_matrizQueNoCorrespondeNoTraeMovimientos() {
        verificar(CASOS.get(10));
    }

    // CTA-A-170: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: EsADC filter coincides with the matrix
    @Test
    void ctaA170_filtroEsAdcCoincide() {
        verificar(CASOS.get(11));
    }

    // CTA-A-171: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: EsADC filter that does not coincide
    @Test
    void ctaA171_filtroEsAdcNoCoincide() {
        verificar(CASOS.get(12));
    }

    // CTA-A-172: ConsultarMovimientoCuentaInversion_SelectConsulta.sql: portfolio without APV matrix uses the NOAPV matrix by CodigoPortafolio
    @Test
    void ctaA172_portafolioSinMatrizUsaMatrizNoApv() {
        verificar(CASOS.get(13));
    }

    // CTA-A-173: ConsultarMovimientoCuentaInversion.java:215 (LEGACY DEFECT): IncluyeSaldo null -> NullPointerException
    @Test
    void ctaA173_incluyeSaldoNuloLanzaNullPointerException() {
        verificar(CASOS.get(14));
    }

    // CTA-A-174: ConsultarMovimientoCuentaInversion.java:216: invalid FechaDesde -> exception from Fecha.toDate
    @Test
    void ctaA174_fechaDesdeInvalidaLanzaExcepcion() {
        verificar(CASOS.get(15));
    }

    // CTA-A-175: ConsultarMovimientoCuentaInversion.java:216: null FechaDesde -> exception
    @Test
    void ctaA175_fechaDesdeNulaLanzaExcepcion() {
        verificar(CASOS.get(16));
    }

    // CTA-A-176: ConsultarMovimientoCuentaInversion.java:216-224,290-296: both dates before the go-live date -> historic table only
    @Test
    void ctaA176_historicoPuroSinSaldos() {
        verificar(CASOS.get(17));
    }

    // CTA-A-177: ConsultarMovimientoCuentaInversion.java:297-306: IncluyeSaldo S adds the Saldo Inicial / Saldo Final rows to the historic result
    @Test
    void ctaA177_historicoPuroConSaldoInicialYFinal() {
        verificar(CASOS.get(18));
    }

    // CTA-A-178: ConsultarMovimientoCuentaInversion.java:297-298: historic without rows -> no balance rows
    @Test
    void ctaA178_historicoSinFilasConSaldosNoAgregaFilas() {
        verificar(CASOS.get(19));
    }

    // CTA-A-179: ConsultarMovimientoCuentaInversion.java:226-247: hybrid with IncluyeSaldo S: Saldo Final of the historic part seeds the current balance and is removed from the output
    @Test
    void ctaA179_hibridoConSaldosUsaSaldoFinalHistoricoComoInicial() {
        verificar(CASOS.get(20));
    }

    // CTA-A-180: ConsultarMovimientoCuentaInversion.java:242-245 (LEGACY DEFECT): hybrid without IncluyeSaldo uses movimientos.get(1), i.e. the SECOND historic movement, and drops it from the output
    @Test
    void ctaA180_hibridoSinSaldosDescartaElSegundoMovimientoHistorico() {
        verificar(CASOS.get(21));
    }

    // CTA-A-181: ConsultarMovimientoCuentaInversion.java:242 (LEGACY DEFECT): hybrid, IncluyeSaldo N, one historic movement -> IndexOutOfBoundsException
    @Test
    void ctaA181_hibridoSinSaldosConUnSoloMovimientoHistoricoLanzaIndexOutOfBounds() {
        verificar(CASOS.get(22));
    }

    // CTA-A-182: ConsultarMovimientoCuentaInversion.java:239-241: hybrid without historic movements keeps the requested SaldoInicial
    @Test
    void ctaA182_hibridoSinMovimientosHistoricosConsultaSoloLoActual() {
        verificar(CASOS.get(23));
    }

    // CTA-A-183: ConsultarMovimientoCuentaInversion.java:226: FechaHasta equal to the go-live date is hybrid
    @Test
    void ctaA183_hastaIgualALaFechaDeCorteEsHibrido() {
        verificar(CASOS.get(24));
    }

    // CTA-A-184: ConsultarMovimientoCuentaInversion.java:248-250: FechaDesde equal to the go-live date is current only
    @Test
    void ctaA184_desdeIgualALaFechaDeCorteEsSoloActual() {
        verificar(CASOS.get(25));
    }

    // CTA-A-185: ConsultarMovimientoCuentaInversion.java:218-219 (LEGACY DEFECT): the historic consultas list is not cleared, the second consulta repeats the first one's rows
    @Test
    void ctaA185_dosConsultasHistoricasAcumulanLaListaYDuplicanFilas() {
        verificar(CASOS.get(26));
    }

    // CTA-A-186: ConsultarMovimientoCuentaInversion.java:205-212: an unparseable PLA.FechaGoLive is replaced by 0001-01-01 (everything current)
    @Test
    void ctaA186_fechaDeCorteInvalidaSeIgnora() {
        verificar(CASOS.get(27));
    }

    // CTA-A-187: ConsultarMovimientoCuentaInversion.java:205-212: missing PLA.FechaGoLive is replaced by 0001-01-01
    @Test
    void ctaA187_fechaDeCorteNoConfiguradaSeIgnora() {
        verificar(CASOS.get(28));
    }

    // CTA-A-188: ConsultarMovimientoCuentaInversion_SelectConsultaHistorico.sql: the historic query ignores the IdEstado filter
    @Test
    void ctaA188_historicoIgnoraElFiltroDeEstado() {
        verificar(CASOS.get(29));
    }

}
