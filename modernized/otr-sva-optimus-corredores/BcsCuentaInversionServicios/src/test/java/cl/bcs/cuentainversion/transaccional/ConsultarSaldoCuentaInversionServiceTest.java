package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ConsultarSaldoCuentaInversionService;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion} (entry point {@code call(conn, json)}) as
 * {@link ConsultarSaldoCuentaInversionService#execute(com.google.gson.JsonObject)}: resolves the accounts to consult (client / portfolio /
 * list / executive's portfolios), calls SaldoCuentaInversion and enriches every row with account data, other balances (guarantee, retained,
 * treasury ...), requests in transit, last-movement date and CLP truncation.
 *
 * <p>Collaborators of the legacy (ObtenerPortafoliosEjecutivo, CalcularParidad) are recorded fakes whose inputs are part of each golden
 * ("colaboradores", verified in {@code src/test-pending/transaccional/ConsultarSaldoColaboradoresPendingTest}). NOT characterised: the
 * money-market valuation path (needs FMM series/fund tables, EntregarPreciosTasas and CalcularParidad rates).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-91..107}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarSaldoCuentaInversionServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-91_lista-de-portafolios-con-ejecutivo-en-primer-elemento",
            "CTA-A-92_un-portafolio-ejecutivo-desde-usuario",
            "CTA-A-93_cliente-sin-portafolio-usa-todos-sus-portafolios",
            "CTA-A-94_lista-sin-portafolios-usa-portafolios-del-ejecutivo",
            "CTA-A-95_entrada-sin-cliente-portafolio-ni-lista-es-invalida",
            "CTA-A-96_usuario-desconocido-sin-ejecutivo-es-error",
            "CTA-A-97_usuario-cliente-no-filtra-portafolios",
            "CTA-A-98_filtro-de-portafolios-permitidos-del-ejecutivo",
            "CTA-A-99_otros-saldos-retenido-dap-garantia-tesoreria",
            "CTA-A-100_tipos-de-saldo-solo-disponible",
            "CTA-A-101_clp-trunca-decimales-usd-no",
            "CTA-A-102_solicitudes-de-aporte-en-transito",
            "CTA-A-103_fecha-ultimo-movimiento",
            "CTA-A-104_filtro-por-producto-agrupador-apv",
            "CTA-A-105_relacion-cuenta-propia-y-tercero",
            "CTA-A-106_usuario-empleado-sin-ejecutivo-explicito-no-filtra-portafolios",
            "CTA-A-107_solicitudes-en-transito-ignoradas-con-producto-agrupador-por-defecto");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final String[] SIN_ORDEN = {"P_OUT_SALDOINICIALFINAL", "P_OUT_SALDOPORFECHAEFECTIVA", "P_OUT_SALDOPORFECHAMOVIMIENTO",
            "P_OUT_ERRORESDETALLE"};

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colaboradores = new ColaboradoresDeCaso(caso);
        ConsultarSaldoCuentaInversionService service = new ConsultarSaldoCuentaInversionService(caso.jdbc(),
                new SaldoCuentaInversionService(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colaboradores);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> {
            try {
                return service.execute(caso.entrada().deepCopy());
            } catch (Exception e) {
                // the legacy throws plain java.lang.Exception in some cases: keep its type, Verifica compares it
                throw sneaky(e);
            }
        });
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colaboradores.traza(), SIN_ORDEN);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneaky(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarSaldoCuentaInversion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultarSaldoCuentaInversion.java:347-349,422-441: case 3a, executive taken from the first list element, accounts of the listed portfolios
    @Test
    void ctaA91_listaDePortafoliosConEjecutivoEnPrimerElemento() {
        verificar(CASOS.get(0));
    }

    // ConsultarSaldoCuentaInversion.java:407-419: case 2 (p_IdPortafolio), executive from the user
    @Test
    void ctaA92_unPortafolioEjecutivoDesdeUsuario() {
        verificar(CASOS.get(1));
    }

    // ConsultarSaldoCuentaInversion.java:393-405 + Insert_Cuenta_Inversion_Cliente_Tabla_Paso.sql: case 1, every portfolio of the client
    @Test
    void ctaA93_clienteSinPortafolioUsaTodosSusPortafolios() {
        verificar(CASOS.get(2));
    }

    // ConsultarSaldoCuentaInversion.java:443-471: case 3b, portfolios from ObtenerPortafoliosEjecutivo
    @Test
    void ctaA94_listaSinPortafoliosUsaPortafoliosDelEjecutivo() {
        verificar(CASOS.get(3));
    }

    // ConsultarSaldoCuentaInversion.java:473-481: ENTRADA_INVALIDA, counter 1, returns at once
    @Test
    void ctaA95_entradaSinClientePortafolioNiListaEsInvalida() {
        verificar(CASOS.get(4));
    }

    // ConsultarSaldoCuentaInversion.java:359-366: no executive and unknown user -> UI_ENTRADA_SIN_ID_EJECUTIVO_EMPLEADO
    @Test
    void ctaA96_usuarioDesconocidoSinEjecutivoEsError() {
        verificar(CASOS.get(5));
    }

    // ConsultarSaldoCuentaInversion.java:368-370,486: CLI user -> executive -1, the permitted-portfolio filter is skipped
    @Test
    void ctaA97_usuarioClienteNoFiltraPortafolios() {
        verificar(CASOS.get(6));
    }

    // ConsultarSaldoCuentaInversion.java:486-517: only portfolios returned by ObtenerPortafoliosEjecutivo(filtrar S) survive
    @Test
    void ctaA98_filtroDePortafoliosPermitidosDelEjecutivo() {
        verificar(CASOS.get(7));
    }

    // ConsultarSaldoCuentaInversion.java:605-704 + Saldos_Retenidos / Pend_Liberacion_DAP / En_Garantia / Tesoreria SQL
    @Test
    void ctaA99_otrosSaldosRetenidoDapGarantiaTesoreria() {
        verificar(CASOS.get(8));
    }

    // ConsultarSaldoCuentaInversion.java:612-652: p_TipoSaldoConsultar restricts which other balances are queried
    @Test
    void ctaA100_tiposDeSaldoSoloDisponible() {
        verificar(CASOS.get(9));
    }

    // ConsultarSaldoCuentaInversion.java:874-887: CLP amounts setScale(0, DOWN); other currencies keep decimals
    @Test
    void ctaA101_clpTruncaDecimalesUsdNo() {
        verificar(CASOS.get(10));
    }

    // ConsultarSaldoCuentaInversion.java:584-602 + Select_SolicitudesFFMM.sql: MontoTransSolicitudesAporte
    @Test
    void ctaA102_solicitudesDeAporteEnTransito() {
        verificar(CASOS.get(11));
    }

    // ConsultarSaldoCuentaInversion.java:816-839,902-904 + Select_Ultimo_Movimiento.sql; accounts without movement render 0001-01-03
    @Test
    void ctaA103_fechaUltimoMovimiento() {
        verificar(CASOS.get(12));
    }

    // ConsultarSaldoCuentaInversion.java:521-532 + Select_Info_Cuenta_Inversion.sql: APV product filter / 9999 -> 0
    @Test
    void ctaA104_filtroPorProductoAgrupadorApv() {
        verificar(CASOS.get(13));
    }

    // Select_Info_Cuenta_Inversion.sql: Relacion CPRO (broker's own client) / REL / TER
    @Test
    void ctaA105_relacionCuentaPropiaYTercero() {
        verificar(CASOS.get(14));
    }

    // Select_Id_Empleado.sql: IdEntidad column not aliased -> p_IdEjecutivo null for employees -> filter skipped (LEGACY DEFECT)
    @Test
    void ctaA106_usuarioEmpleadoSinEjecutivoExplicitoNoFiltraPortafolios() {
        verificar(CASOS.get(15));
    }

    // ConsultarSaldoCuentaInversion.java:546-550: requests query compared with the DEFAULT 9999 product id finds nothing (LEGACY QUIRK)
    @Test
    void ctaA107_solicitudesEnTransitoIgnoradasConProductoAgrupadorPorDefecto() {
        verificar(CASOS.get(16));
    }
}
