package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.infrastructure.adapter.HanaRepository;
import cl.bcs.cuentainversion.service.ConsultarMovimientoCuentaInversionService;
import cl.bcs.cuentainversion.service.ConsultarMovimientosWSService;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.webservices.ConsultarMovimientos}: accounts of the portfolios, SaldoCuentaInversion
 * for the initial balance, ConsultarMovimientoCuentaInversion for the movements and the mapping to the web-service rows (dates shifted by the system time zone).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-226..240}. Legacy: {@code webservices.ConsultarMovimientos}; modern: {@link ConsultarMovimientosWSService}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarMovimientosWSServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-226_movimientos-de-un-portafolio-mapeados-a-la-salida-del-servicio-web",
            "CTA-A-227_portafolio-apv-informa-origen-apv",
            "CTA-A-228_portafolio-dc-informa-origen-apv",
            "CTA-A-229_moneda-nula-consulta-todas-las-cuentas-del-portafolio",
            "CTA-A-230_varios-portafolios",
            "CTA-A-231_portafolio-sin-cuentas-devuelve-lista-vacia",
            "CTA-A-232_filtro-de-estado",
            "CTA-A-233_tipo-de-fecha-LIQ-se-ignora-siempre-consulta-por-fecha-de-movimiento",
            "CTA-A-234_tipo-de-fecha-nulo",
            "CTA-A-235_matriz-agrupadora-se-propaga",
            "CTA-A-236_saldo-inicial-es-el-saldo-efectivo-final-de-la-cuenta",
            "CTA-A-237_sin-parametro-de-zona-horaria-lanza-excepcion",
            "CTA-A-238_zona-horaria-positiva-desplaza-la-hora-de-certificacion",
            "CTA-A-239_sin-fecha-de-certificacion-usa-hora-cero",
            "CTA-A-240_fecha-de-corte-historica-mezcla-origen-historico");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ConfiguracionDelCaso config = new ConfiguracionDelCaso(caso);
        ConsultarMovimientosWSService service = new ConsultarMovimientosWSService(caso.jdbc(), new SaldoCuentaInversionService(caso.jdbc()),
                new ConsultarMovimientoCuentaInversionService(new HanaRepository(caso.jdbc()), config, new DiccionarioDeCaso()), config);
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
    }

    /** Runs a service call that may throw a checked exception; it reaches Verifica unchanged (its type is pinned by the goldens). */
    @FunctionalInterface
    private interface Llamada {
        com.google.gson.JsonElement ejecutar() throws Exception;
    }

    private static Verifica.Ejecucion ejecutar(Llamada llamada) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return llamada.ejecutar();
            } catch (Exception e) {
                return ConsultarMovimientosWSServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarMovimientos");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-226: ConsultarMovimientos.java:184-190,278-362: portfolio movements mapped to the web-service rows (FechaMovimiento = date + shifted certification time)
    @Test
    void ctaA226_movimientosDeUnPortafolioMapeadosALaSalidaDelServicioWeb() {
        verificar(CASOS.get(0));
    }

    // CTA-A-227: ConsultarMovimientos.java:362-368: portfolio code APV-* -> Origen APV
    @Test
    void ctaA227_portafolioApvInformaOrigenApv() {
        verificar(CASOS.get(1));
    }

    // CTA-A-228: ConsultarMovimientos.java:362-368: portfolio code DC-* -> Origen APV
    @Test
    void ctaA228_portafolioDcInformaOrigenApv() {
        verificar(CASOS.get(2));
    }

    // CTA-A-229: ConsultarMovimientos_SelectCuentaInversion.sql: null currency -> every account of the portfolio
    @Test
    void ctaA229_monedaNulaConsultaTodasLasCuentasDelPortafolio() {
        verificar(CASOS.get(3));
    }

    // CTA-A-230: ConsultarMovimientos.java:184: several portfolios
    @Test
    void ctaA230_variosPortafolios() {
        verificar(CASOS.get(4));
    }

    // CTA-A-231: ConsultarMovimientos_SelectCuentaInversion.sql: portfolio without accounts -> empty result
    @Test
    void ctaA231_portafolioSinCuentasDevuelveListaVacia() {
        verificar(CASOS.get(5));
    }

    // CTA-A-232: ConsultarMovimientos.java:249: IdEstado filter passed to the movements query
    @Test
    void ctaA232_filtroDeEstado() {
        verificar(CASOS.get(6));
    }

    // CTA-A-233: ConsultarMovimientos.java:257 (LEGACY DEFECT): the condition can never be true, 'LIQ' still queries by movement date
    @Test
    void ctaA233_tipoDeFechaLIQSeIgnoraSiempreConsultaPorFechaDeMovimiento() {
        verificar(CASOS.get(7));
    }

    // CTA-A-234: ConsultarMovimientos.java:257: null p_TipoFechaConsulta behaves as ADQ
    @Test
    void ctaA234_tipoDeFechaNulo() {
        verificar(CASOS.get(8));
    }

    // CTA-A-235: ConsultarMovimientos.java:253: p_IdMatrizAgrupador is passed to the movements query
    @Test
    void ctaA235_matrizAgrupadoraSePropaga() {
        verificar(CASOS.get(9));
    }

    // CTA-A-236: ConsultarMovimientos.java:250 (LEGACY QUIRK): the initial balance of the consultation is the FINAL effective balance of the account
    @Test
    void ctaA236_saldoInicialEsElSaldoEfectivoFinalDeLaCuenta() {
        verificar(CASOS.get(10));
    }

    // CTA-A-237: ConsultarMovimientos.java:190: missing PLA.SegundosZonaHoraria -> exception
    @Test
    void ctaA237_sinParametroDeZonaHorariaLanzaExcepcion() {
        verificar(CASOS.get(11));
    }

    // CTA-A-238: ConsultarMovimientos.java:287-292: positive time-zone offset shifts the certification time
    @Test
    void ctaA238_zonaHorariaPositivaDesplazaLaHoraDeCertificacion() {
        verificar(CASOS.get(12));
    }

    // CTA-A-239: ConsultarMovimientos.java:287 (LEGACY DEFECT): null FechaCertificacion -> NullPointerException
    @Test
    void ctaA239_sinFechaDeCertificacionUsaHoraCero() {
        verificar(CASOS.get(13));
    }

    // CTA-A-240: ConsultarMovimientos.java:184-190 + ConsultarMovimientoCuentaInversion.java:226-247: historic rows have no account: empty DocumentoIdentidad/CodigoPortafolio/Origen
    @Test
    void ctaA240_fechaDeCorteHistoricaMezclaOrigenHistorico() {
        verificar(CASOS.get(14));
    }

}
