package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ServicioDistribucionDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ProcesarDistribucion} (typed entry {@code logica}; the production {@code call} serialises with a plain Gson whose date format depends
 * on the platform locale, so the oracle records the typed {@code logica} output) as {@code ProcesarDistribucionService#execute(JsonObject)}: distribution of the PAC rendition
 * into Tesoreria movements, investment-account entries, fund contributions or stock orders.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-277..308, 319..321}. The request carries no data: everything is read from the file load and the mandates
 * (PAC.LogMandatoPAC pending rows). Collaborators (CalcularParidad, RevisarRequisitosBasicos, CrearIngresoEgreso, LiquidarComprobante, CrearCargoAbono, CalcularMontoNumeroCuota,
 * CrearSolicitudAporteRescateNormal, ConsultarInstrumentoPrecioBolsa, CalcularMontoYCobrosOrden, CrearOrdenes) are recorded fakes.
 * <p>Order: the four result lists are compared as sets (P_OUT_MENSAJES, P_OUT_SOLICITUDES_*) because the legacy iterates a HashMap of IdentServicio groups; IdTraspaso (500, 501, ...) and
 * the collaborator call order still depend on that iteration (cases 306 and 308 are order-sensitive: a modern implementation must reproduce the String-hash order).
 * <p>PAC.LogMandatoPAC.FechaProceso (database CURRENT_TIMESTAMP) is masked as {@code <TS>} in the table dump (Dump.TIMESTAMP).
 */
@ExtendWith(ZonaUtc.class)
class ProcesarDistribucionServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-277_csc-ffm-exitoso-recorre-tesoreria-cuenta-inversion-y-fondos",
            "TES-B-278_csc-en-uf-convierte-con-la-paridad-de-la-uf",
            "TES-B-279_csc-fondo-en-dolares-calcula-monto-y-forma-de-pago-spot",
            "TES-B-280_csc-fondo-en-dolares-sin-resultado-de-calculo-deja-monto-cero",
            "TES-B-281_saapv-con-fondo-mutuo-crea-solicitud-de-aporte",
            "TES-B-282_saapv-con-acciones-crea-ordenes",
            "TES-B-283_saapv-con-acciones-y-error-de-precio-no-calcula-ordenes",
            "TES-B-284_saapv-con-acciones-y-orden-con-error-rechaza-la-solicitud",
            "TES-B-285_saapv-sin-distribucion-informa-contratos-sin-distribucion",
            "TES-B-286_saapv-mixto-fondo-y-acciones-usa-el-fondo-y-las-ordenes",
            "TES-B-287_requisitos-basicos-con-error-sale-con-un-error",
            "TES-B-288_corredor-inexistente-lanza-IndexOutOfBoundsException",
            "TES-B-289_sin-log-pendiente-el-archivo-no-coincide-con-mandatos",
            "TES-B-290_archivo-con-mandato-inexistente-informa-y-procesa-el-resto",
            "TES-B-291_archivo-con-mandato-inactivo-informa-estado-cargo-rechazado",
            "TES-B-292_mandato-activo-sin-log-pendiente-informa-mal-configurado",
            "TES-B-293_monto-del-archivo-distinto-al-del-contrato-rechaza",
            "TES-B-294_mandato-con-estado-distinto-de-ING-es-rechazado-por-el-archivo",
            "TES-B-295_saapv-revocado-rechaza-con-el-estado-largo",
            "TES-B-296_csc-revocado-no-entra-en-el-join-y-se-rechaza-por-monto",
            "TES-B-297_contrato-csc-bloqueado-no-pasa-a-cuenta-de-inversion",
            "TES-B-298_portafolio-bloqueado-sin-clientes-activos-informa-error",
            "TES-B-299_portafolio-inactivo-no-se-procesa-pero-el-resto-si",
            "TES-B-300_cliente-inactivo-no-se-procesa-pero-el-resto-si",
            "TES-B-301_ingreso-egreso-con-error-rechaza-la-solicitud-asociada",
            "TES-B-302_liquidar-comprobante-con-error-no-informa-liquidacion-exitosa",
            "TES-B-303_cargo-abono-con-error-rechaza-la-solicitud",
            "TES-B-304_solicitud-de-aporte-con-error-rechaza-la-solicitud",
            "TES-B-305_simulacion-S-no-liquida-ni-registra-movimientos",
            "TES-B-306_dos-servicios-csc-el-orden-de-los-grupos-sigue-el-hash",
            "TES-B-307_sin-empleado-caja-ni-cuenta-de-distribucion-envia-ceros",
            "TES-B-308_carga-mixta-de-siete-servicios-con-exitos-y-rechazos",
            "TES-B-319_log-de-mandato-rechazado-hoy-se-informa-como-mensaje-de-error",
            "TES-B-320_log-rechazado-en-otra-fecha-no-se-informa",
            "TES-B-321_solicitud-de-rescate-informa-el-codigo-de-rescate");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES", "P_OUT_SOLICITUDES_PROCESADAS", "P_OUT_SOLICITUDES_NO_PROCESADAS", "P_OUT_SOLICITUDES_A_PROCESAR"};

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return ServicioDistribucionDeCaso.de(caso, col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ProcesarDistribucion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ProcesarDistribucion.java:333-776: CSC fund path: Tesoreria (3 movements per request + liquidation), Cuenta de Inversion (cargo/abono), FFMM (aporte); MovimientoGenerado stays empty (solicitud.equals(id) compares a register with a Long)
    @Test
    void tesB277_cscFfmExitosoRecorreTesoreriaCuentaInversionYFondos() {
        verificar(CASOS.get(0));
    }

    // ProcesarDistribucion.java:2000-2008,716: CSC contract in UF: the deposit amount is the UF amount times the paridad
    @Test
    void tesB278_cscEnUfConvierteConLaParidadDeLaUf() {
        verificar(CASOS.get(1));
    }

    // ProcesarDistribucion.java:1050-1117: fund in USD: CalcularMontoNumeroCuota + two spot payment legs, amount replaced by the calculated one
    @Test
    void tesB279_cscFondoEnDolaresCalculaMontoYFormaDePagoSpot() {
        verificar(CASOS.get(2));
    }

    // ProcesarDistribucion.java:1114-1116: USD fund without calculation result: the request amount becomes 0
    @Test
    void tesB280_cscFondoEnDolaresSinResultadoDeCalculoDejaMontoCero() {
        verificar(CASOS.get(3));
    }

    // ProcesarDistribucion.java:1003-1043: SAAPV contract with an FFM instrument: one fund request per distribution line
    @Test
    void tesB281_saapvConFondoMutuoCreaSolicitudDeAporte() {
        verificar(CASOS.get(4));
    }

    // ProcesarDistribucion.java:1235-1442,2116-2158: SAAPV with an ACN instrument: price, quantity calculation and orders
    @Test
    void tesB282_saapvConAccionesCreaOrdenes() {
        verificar(CASOS.get(5));
    }

    // ProcesarDistribucion.java:1256-1258: price query with errors: no orders are calculated
    @Test
    void tesB283_saapvConAccionesYErrorDePrecioNoCalculaOrdenes() {
        verificar(CASOS.get(6));
    }

    // ProcesarDistribucion.java:1398-1423: an order creation error rejects the request
    @Test
    void tesB284_saapvConAccionesYOrdenConErrorRechazaLaSolicitud() {
        verificar(CASOS.get(7));
    }

    // ProcesarDistribucion.java:527-540: SAAPV folio without distribution: error 'Contratos sin distribucion' and early return
    @Test
    void tesB285_saapvSinDistribucionInformaContratosSinDistribucion() {
        verificar(CASOS.get(8));
    }

    // ProcesarDistribucion.java:723-743: mixed FFM + ACN distribution: the request goes to FFMM once (break at the first FFM line, Nemotecnico order) and the ACN lines listed before it go to orders
    @Test
    void tesB286_saapvMixtoFondoYAccionesUsaElFondoYLasOrdenes() {
        verificar(CASOS.get(9));
    }

    // ProcesarDistribucion.java:386-389: basic requirements with errors: counter 1 and nothing else
    @Test
    void tesB287_requisitosBasicosConErrorSaleConUnError() {
        verificar(CASOS.get(10));
    }

    // ProcesarDistribucion.java:383-384: no own-account portfolio: IndexOutOfBoundsException (read before the requirements are checked)
    @Test
    void tesB288_corredorInexistenteLanzaIndexOutOfBoundsException() {
        verificar(CASOS.get(11));
    }

    // ProcesarDistribucion.java:408-412: no pending log rows: 'EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE'
    @Test
    void tesB289_sinLogPendienteElArchivoNoCoincideConMandatos() {
        verificar(CASOS.get(12));
    }

    // ProcesarDistribucion.java:2374-2387: file row without mandate: 'Mandato informado no existe.' (error) and the rest is processed
    @Test
    void tesB290_archivoConMandatoInexistenteInformaYProcesaElResto() {
        verificar(CASOS.get(13));
    }

    // ProcesarDistribucion.java:2401-2413: file row of an inactive mandate (state != ING)
    @Test
    void tesB291_archivoConMandatoInactivoInformaEstadoCargoRechazado() {
        verificar(CASOS.get(14));
    }

    // ProcesarDistribucion.java:2510-2531: file row whose mandate has no pending log: 'mal configurado'
    @Test
    void tesB292_mandatoActivoSinLogPendienteInformaMalConfigurado() {
        verificar(CASOS.get(15));
    }

    // ProcesarDistribucion.java:1951-1965: file amount differs from the contract amount: rejected with IdMandatoPAC -1
    @Test
    void tesB293_montoDelArchivoDistintoAlDelContratoRechaza() {
        verificar(CASOS.get(16));
    }

    // ProcesarDistribucion.java:1873-1885: mandate with state != ING
    @Test
    void tesB294_mandatoConEstadoDistintoDeINGEsRechazadoPorElArchivo() {
        verificar(CASOS.get(17));
    }

    // ProcesarDistribucion.java:1898-1909: SAAPV contract in REV state
    @Test
    void tesB295_saapvRevocadoRechazaConElEstadoLargo() {
        verificar(CASOS.get(18));
    }

    // ProcesarDistribucion.java:1910-1920: CSC in RVC state never reaches the branch: the join excludes it, the amount is 0 and the request is rejected by amount
    @Test
    void tesB296_cscRevocadoNoEntraEnElJoinYSeRechazaPorMonto() {
        verificar(CASOS.get(19));
    }

    // ProcesarDistribucion.java:571-583: CSC contract not ING: 'Contrato bloqueado, Anulado o Revocado'
    @Test
    void tesB297_contratoCscBloqueadoNoPasaACuentaDeInversion() {
        verificar(CASOS.get(20));
    }

    // ProcesarDistribucion.java:603-625: blocked portfolio and no active clients: 'EL ARCHIVO DE CARGA NO PRESENTA CLIENTE(S) ACTIVO(S)'
    @Test
    void tesB298_portafolioBloqueadoSinClientesActivosInformaError() {
        verificar(CASOS.get(21));
    }

    // ProcesarDistribucion.java:605-607: inactive portfolio: not processed, the rest is
    @Test
    void tesB299_portafolioInactivoNoSeProcesaPeroElRestoSi() {
        verificar(CASOS.get(22));
    }

    // ProcesarDistribucion.java:608-610: inactive client: not processed, the rest is
    @Test
    void tesB300_clienteInactivoNoSeProcesaPeroElRestoSi() {
        verificar(CASOS.get(23));
    }

    // ProcesarDistribucion.java:1680-1711: CrearIngresoEgreso error with and without matching reference
    @Test
    void tesB301_ingresoEgresoConErrorRechazaLaSolicitudAsociada() {
        verificar(CASOS.get(24));
    }

    // ProcesarDistribucion.java:1750-1770: LiquidarComprobante error: no 'UI_LIQUIDACION_EXITOSA' message
    @Test
    void tesB302_liquidarComprobanteConErrorNoInformaLiquidacionExitosa() {
        verificar(CASOS.get(25));
    }

    // ProcesarDistribucion.java:890-923: CrearCargoAbono error rejects the request
    @Test
    void tesB303_cargoAbonoConErrorRechazaLaSolicitud() {
        verificar(CASOS.get(26));
    }

    // ProcesarDistribucion.java:1137-1168: CrearSolicitudAporteRescateNormal error rejects the request
    @Test
    void tesB304_solicitudDeAporteConErrorRechazaLaSolicitud() {
        verificar(CASOS.get(27));
    }

    // ProcesarDistribucion.java:1480,1713: simulation S: positive references, nothing liquidated, no movement messages
    @Test
    void tesB305_simulacionSNoLiquidaNiRegistraMovimientos() {
        verificar(CASOS.get(28));
    }

    // ProcesarDistribucion.java:1933-1988: two services: groups follow the HashMap order of IdentServicio (IdTraspaso 500, 501 depend on it)
    @Test
    void tesB306_dosServiciosCscElOrdenDeLosGruposSigueElHash() {
        verificar(CASOS.get(29));
    }

    // ProcesarDistribucion.java:429-448,466-469: no employee, no cash box, no distribution account: zeros are sent
    @Test
    void tesB307_sinEmpleadoCajaNiCuentaDeDistribucionEnviaCeros() {
        verificar(CASOS.get(30));
    }

    // ProcesarDistribucion.java:333-776: largest input: seven services (3 CSC, 3 SAAPV, 1 rejected) with funds in CLP/USD, orders and rejections
    @Test
    void tesB308_cargaMixtaDeSieteServiciosConExitosYRechazos() {
        verificar(CASOS.get(31));
    }

    // ProcesarDistribucion.java:2534-2560: a pre-rendition log row rejected today produces MANDATO_RECHAZADO_PRERENDICION (error message)
    @Test
    void tesB319_logDeMandatoRechazadoHoySeInformaComoMensajeDeError() {
        verificar(CASOS.get(32));
    }

    // ProcesarDistribucion.java:2534-2560: the same row rejected on another date is not reported
    @Test
    void tesB320_logRechazadoEnOtraFechaNoSeInforma() {
        verificar(CASOS.get(33));
    }

    // ProcesarDistribucion.java:1174-1178: RESNORM movement type: rescate success code
    @Test
    void tesB321_solicitudDeRescateInformaElCodigoDeRescate() {
        verificar(CASOS.get(34));
    }

}
