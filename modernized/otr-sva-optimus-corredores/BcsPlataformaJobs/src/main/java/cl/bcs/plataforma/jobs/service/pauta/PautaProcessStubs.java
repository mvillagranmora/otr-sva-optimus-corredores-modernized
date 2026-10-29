package cl.bcs.plataforma.jobs.service.pauta;

import org.springframework.stereotype.Service;

/**
 * Stub implementations for all Pauta* process classes.
 *
 * These classes delegate to HANA stored procedures and cannot be
 * executed without a live HANA connection.  Each stub's execute()
 * method throws UnsupportedOperationException.  Integration tests
 * that call these are annotated {@code @Disabled("requires HANA")}.
 *
 * Legacy classes stubbed here (one inner class per legacy class):
 *   PautaAbrirSistema, PautaEntregarSistema, PautaTerminarPauta,
 *   PautaIniciarPauta, PautaCambioFechaSistema,
 *   PautaBloqueoUsuariosPorVigencia, PautaBloqueoTemporalClientePorDesactualizacionDatos,
 *   PautaActualizarParametrosFinancieros, PautaActualizarFlagRendicion,
 *   PautaActualizarOrderIdHT, PautaActualizarSaldoCuentaBancoCorredora,
 *   PautaAbrirFondosMutuos, PautaAbrirModuloMesaSpot,
 *   PautaAdministracionLiquidezFFMM, PautaCambioNemosSD,
 *   PautaCargarMarketMakerDesdeBursatil, PautaCoberturaPatrimonio,
 *   PautaConsolidarClientes, PautaCopiarPatrimonioConsolidadoParaInhabil,
 *   PautaCustodiaCarteraCircular, PautaDesactivarLineaOperacionalMasivo,
 *   PautaEjecutarAnulacionIngresoEgreso, PautaEjecutarProcesoDiaInhabil,
 *   PautaEjecutarReplicaBatch, PautaEjecutarReplicaPostPauta,
 *   PautaEnvioArchivosCasilla, PautaEstadisticaOperacionCliente,
 *   PautaFacturacionMensual, PautaFatca,
 *   PautaGenerar1064FR, PautaGenerar1064R, PautaGenerar1720,
 *   PautaGenerarCuadraturaCustodiaCircular1518, PautaGenerarEstadisticaCliente,
 *   PautaGenerarInfoDerechos, PautaGenerarInterfazP40SBIF_Scotia,
 *   PautaGenerarInterfazP40SBIF_ScotiaFinDeMes,
 *   PautaGenerarNCG365Masivo, PautaGenerarNCG380,
 *   PautaGestionDataHistorica, PautaGuardarHistoricoCartolaConsolidada,
 *   PautaGuardarPatrimonioConsolidado, PautaIniciarReplicaDelta,
 *   PautaIniciarReplicaFull, PautaInterfazBasilea,
 *   PautaInterfazLiquidezIntradia, PautaLecturaEjecucionOrdenes,
 *   PautaLimpiarDataHistorico, PautaNetearCarterasNegativas,
 *   PautaNotificacionCargoGananciaCapital, PautaNotificacionEjecucionOrdenesDiaria,
 *   PautaNotificacionStockCriticoFolioDte, PautaNotificarNCG365FFMM,
 *   PautaProcesarCupones, PautaProcesoEvaluacionMontoSoi,
 *   PautaReniciarFolioOperacionOmnibusEmisor, PautaRespaldarCustodia,
 *   PautaRespaldoReplica, PautaRevalorizarMovimientosFFMM,
 *   PautaValidacionPreventiva, PautaValorizarCarteraAdcicional,
 *   PautaValorizarCarteraAdicionalPactosSub, PautaValorizarCarteraFuturos,
 *   PautaValorizarCarteraIF, PautaValorizarCarteraOpciones,
 *   PautaValorizarCarteraRFIF, PautaValorizarCarteraRF,
 *   PautaValorizarCarteraRVPrecioMercado, PautaValorizarCarteraSubPrestamosEmprestitos,
 *   PautaValorizarCupones, PautaValorizarCuponFeriado,
 *   PautaValorizarFFMM, PautaValorizarGarantiasRVRFIF,
 *   PautaValorizarPactos, PautaValorizarPrestamosEmprestitos,
 *   PautaValorizarReportes, PautaValorizarSimultaneas,
 *   PautaVencimientosIF, PautaVencimientosOperacionesOpciones,
 *   PautaVencimientosPactos, PautaVencimientosPrestamosEmprestitos,
 *   PautaVencimientosReportes, PautaVencimientosRF,
 *   PautaVencimientosSimultanea
 */
@Service
public class PautaProcessStubs {

    private static final String MSG =
            " requires a live HANA connection — annotate test with @Disabled(\"requires HANA\")";

    // ---- Sistema / apertura / cierre ----------------------------------------

    /** legacy: PautaAbrirSistema */
    public void pautaAbrirSistema(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("PautaAbrirSistema" + MSG);
    }

    /** legacy: PautaEntregarSistema */
    public void pautaEntregarSistema(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("PautaEntregarSistema" + MSG);
    }

    /** legacy: PautaTerminarPauta */
    public void pautaTerminarPauta(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("PautaTerminarPauta" + MSG);
    }

    /** legacy: PautaIniciarPauta */
    public void pautaIniciarPauta(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("PautaIniciarPauta" + MSG);
    }

    /** legacy: PautaCambioFechaSistema */
    public void pautaCambioFechaSistema(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("PautaCambioFechaSistema" + MSG);
    }

    // ---- Usuarios / bloqueos ------------------------------------------------

    /** legacy: PautaBloqueoUsuariosPorVigencia */
    public void pautaBloqueoUsuariosPorVigencia(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaBloqueoUsuariosPorVigencia" + MSG);
    }

    /** legacy: PautaBloqueoTemporalClientePorDesactualizacionDatos */
    public void pautaBloqueoTemporalClientePorDesactualizacionDatos(Long idEmpresa) {
        throw new UnsupportedOperationException(
                "PautaBloqueoTemporalClientePorDesactualizacionDatos" + MSG);
    }

    // ---- Parametros financieros / mercado -----------------------------------

    /** legacy: PautaActualizarParametrosFinancieros */
    public void pautaActualizarParametrosFinancieros(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaActualizarParametrosFinancieros" + MSG);
    }

    /** legacy: PautaActualizarFlagRendicion */
    public void pautaActualizarFlagRendicion(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaActualizarFlagRendicion" + MSG);
    }

    /** legacy: PautaActualizarOrderIdHT */
    public void pautaActualizarOrderIdHT(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaActualizarOrderIdHT" + MSG);
    }

    /** legacy: PautaActualizarSaldoCuentaBancoCorredora */
    public void pautaActualizarSaldoCuentaBancoCorredora(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaActualizarSaldoCuentaBancoCorredora" + MSG);
    }

    /** legacy: PautaCambioNemosSD */
    public void pautaCambioNemosSD(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaCambioNemosSD" + MSG);
    }

    /** legacy: PautaCargarMarketMakerDesdeBursatil */
    public void pautaCargarMarketMakerDesdeBursatil(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaCargarMarketMakerDesdeBursatil" + MSG);
    }

    // ---- Módulos / apertura ------------------------------------------------

    /** legacy: PautaAbrirFondosMutuos */
    public void pautaAbrirFondosMutuos(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaAbrirFondosMutuos" + MSG);
    }

    /** legacy: PautaAbrirModuloMesaSpot */
    public void pautaAbrirModuloMesaSpot(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaAbrirModuloMesaSpot" + MSG);
    }

    // ---- Patrimonio / clientes ---------------------------------------------

    /** legacy: PautaCoberturaPatrimonio */
    public void pautaCoberturaPatrimonio(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaCoberturaPatrimonio" + MSG);
    }

    /** legacy: PautaConsolidarClientes */
    public void pautaConsolidarClientes(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaConsolidarClientes" + MSG);
    }

    /** legacy: PautaCopiarPatrimonioConsolidadoParaInhabil */
    public void pautaCopiarPatrimonioConsolidadoParaInhabil(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaCopiarPatrimonioConsolidadoParaInhabil" + MSG);
    }

    /** legacy: PautaGuardarPatrimonioConsolidado */
    public void pautaGuardarPatrimonioConsolidado(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGuardarPatrimonioConsolidado" + MSG);
    }

    // ---- Custodia / circular -----------------------------------------------

    /** legacy: PautaCustodiaCarteraCircular */
    public void pautaCustodiaCarteraCircular(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaCustodiaCarteraCircular" + MSG);
    }

    /** legacy: PautaRespaldarCustodia */
    public void pautaRespaldarCustodia(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaRespaldarCustodia" + MSG);
    }

    // ---- Liquidez / interfaz -----------------------------------------------

    /** legacy: PautaAdministracionLiquidezFFMM */
    public void pautaAdministracionLiquidezFFMM(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaAdministracionLiquidezFFMM" + MSG);
    }

    /** legacy: PautaInterfazLiquidezIntradia */
    public void pautaInterfazLiquidezIntradia(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaInterfazLiquidezIntradia" + MSG);
    }

    /** legacy: PautaInterfazBasilea */
    public void pautaInterfazBasilea(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaInterfazBasilea" + MSG);
    }

    // ---- Línea operacional -------------------------------------------------

    /** legacy: PautaDesactivarLineaOperacionalMasivo */
    public void pautaDesactivarLineaOperacionalMasivo(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaDesactivarLineaOperacionalMasivo" + MSG);
    }

    // ---- Anulaciones / egreso ----------------------------------------------

    /** legacy: PautaEjecutarAnulacionIngresoEgreso */
    public void pautaEjecutarAnulacionIngresoEgreso(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaEjecutarAnulacionIngresoEgreso" + MSG);
    }

    /** legacy: PautaEjecutarProcesoDiaInhabil */
    public void pautaEjecutarProcesoDiaInhabil(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaEjecutarProcesoDiaInhabil" + MSG);
    }

    // ---- Réplica -----------------------------------------------------------

    /** legacy: PautaEjecutarReplicaBatch */
    public void pautaEjecutarReplicaBatch(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaEjecutarReplicaBatch" + MSG);
    }

    /** legacy: PautaEjecutarReplicaPostPauta */
    public void pautaEjecutarReplicaPostPauta(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaEjecutarReplicaPostPauta" + MSG);
    }

    /** legacy: PautaIniciarReplicaDelta */
    public void pautaIniciarReplicaDelta(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaIniciarReplicaDelta" + MSG);
    }

    /** legacy: PautaIniciarReplicaFull */
    public void pautaIniciarReplicaFull(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaIniciarReplicaFull" + MSG);
    }

    /** legacy: PautaRespaldoReplica */
    public void pautaRespaldoReplica(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaRespaldoReplica" + MSG);
    }

    // ---- Envío / archivos --------------------------------------------------

    /** legacy: PautaEnvioArchivosCasilla */
    public void pautaEnvioArchivosCasilla(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaEnvioArchivosCasilla" + MSG);
    }

    // ---- Estadísticas / operación ------------------------------------------

    /** legacy: PautaEstadisticaOperacionCliente */
    public void pautaEstadisticaOperacionCliente(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaEstadisticaOperacionCliente" + MSG);
    }

    /** legacy: PautaGenerarEstadisticaCliente */
    public void pautaGenerarEstadisticaCliente(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarEstadisticaCliente" + MSG);
    }

    /** legacy: PautaLecturaEjecucionOrdenes */
    public void pautaLecturaEjecucionOrdenes(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaLecturaEjecucionOrdenes" + MSG);
    }

    // ---- Facturación -------------------------------------------------------

    /** legacy: PautaFacturacionMensual */
    public void pautaFacturacionMensual(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaFacturacionMensual" + MSG);
    }

    // ---- FATCA / regulatorio -----------------------------------------------

    /** legacy: PautaFatca */
    public void pautaFatca(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaFatca" + MSG);
    }

    // ---- Generación de informes / documentos -------------------------------

    /** legacy: PautaGenerar1064FR */
    public void pautaGenerar1064FR(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerar1064FR" + MSG);
    }

    /** legacy: PautaGenerar1064R */
    public void pautaGenerar1064R(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerar1064R" + MSG);
    }

    /** legacy: PautaGenerar1720 */
    public void pautaGenerar1720(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerar1720" + MSG);
    }

    /** legacy: PautaGenerarCuadraturaCustodiaCircular1518 */
    public void pautaGenerarCuadraturaCustodiaCircular1518(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarCuadraturaCustodiaCircular1518" + MSG);
    }

    /** legacy: PautaGenerarInfoDerechos */
    public void pautaGenerarInfoDerechos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarInfoDerechos" + MSG);
    }

    /** legacy: PautaGenerarInterfazP40SBIF_Scotia */
    public void pautaGenerarInterfazP40SBIFScotia(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarInterfazP40SBIF_Scotia" + MSG);
    }

    /** legacy: PautaGenerarInterfazP40SBIF_ScotiaFinDeMes */
    public void pautaGenerarInterfazP40SBIFScotiaFinDeMes(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarInterfazP40SBIF_ScotiaFinDeMes" + MSG);
    }

    /** legacy: PautaGenerarNCG365Masivo */
    public void pautaGenerarNCG365Masivo(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarNCG365Masivo" + MSG);
    }

    /** legacy: PautaGenerarNCG380 */
    public void pautaGenerarNCG380(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGenerarNCG380" + MSG);
    }

    /** legacy: PautaGuardarHistoricoCartolaConsolidada */
    public void pautaGuardarHistoricoCartolaConsolidada(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGuardarHistoricoCartolaConsolidada" + MSG);
    }

    // ---- Gestión data histórica --------------------------------------------

    /** legacy: PautaGestionDataHistorica */
    public void pautaGestionDataHistorica(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaGestionDataHistorica" + MSG);
    }

    /** legacy: PautaLimpiarDataHistorico */
    public void pautaLimpiarDataHistorico(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaLimpiarDataHistorico" + MSG);
    }

    // ---- Carteras negativas ------------------------------------------------

    /** legacy: PautaNetearCarterasNegativas */
    public void pautaNetearCarterasNegativas(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaNetearCarterasNegativas" + MSG);
    }

    // ---- Notificaciones ----------------------------------------------------

    /** legacy: PautaNotificacionCargoGananciaCapital */
    public void pautaNotificacionCargoGananciaCapital(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaNotificacionCargoGananciaCapital" + MSG);
    }

    /** legacy: PautaNotificacionEjecucionOrdenesDiaria */
    public void pautaNotificacionEjecucionOrdenesDiaria(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaNotificacionEjecucionOrdenesDiaria" + MSG);
    }

    /** legacy: PautaNotificacionStockCriticoFolioDte */
    public void pautaNotificacionStockCriticoFolioDte(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaNotificacionStockCriticoFolioDte" + MSG);
    }

    /** legacy: PautaNotificarNCG365FFMM */
    public void pautaNotificarNCG365FFMM(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaNotificarNCG365FFMM" + MSG);
    }

    // ---- Cupones / valorización -------------------------------------------

    /** legacy: PautaProcesarCupones */
    public void pautaProcesarCupones(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaProcesarCupones" + MSG);
    }

    /** legacy: PautaValorizarCupones */
    public void pautaValorizarCupones(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCupones" + MSG);
    }

    /** legacy: PautaValorizarCuponFeriado */
    public void pautaValorizarCuponFeriado(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCuponFeriado" + MSG);
    }

    // ---- Evaluación / SOI --------------------------------------------------

    /** legacy: PautaProcesoEvaluacionMontoSoi */
    public void pautaProcesoEvaluacionMontoSoi(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaProcesoEvaluacionMontoSoi" + MSG);
    }

    // ---- Folio / ómnibus ---------------------------------------------------

    /** legacy: PautaReniciarFolioOperacionOmnibusEmisor */
    public void pautaReniciarFolioOperacionOmnibusEmisor(Long idEmpresa) {
        throw new UnsupportedOperationException("PautaReniciarFolioOperacionOmnibusEmisor" + MSG);
    }

    // ---- Revalorización FFMM -----------------------------------------------

    /** legacy: PautaRevalorizarMovimientosFFMM */
    public void pautaRevalorizarMovimientosFFMM(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaRevalorizarMovimientosFFMM" + MSG);
    }

    // ---- Validación preventiva --------------------------------------------

    /** legacy: PautaValidacionPreventiva */
    public void pautaValidacionPreventiva(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValidacionPreventiva" + MSG);
    }

    // ---- Valorización de carteras -----------------------------------------

    /** legacy: PautaValorizarCarteraAdcicional */
    public void pautaValorizarCarteraAdcicional(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraAdcicional" + MSG);
    }

    /** legacy: PautaValorizarCarteraAdicionalPactosSub */
    public void pautaValorizarCarteraAdicionalPactosSub(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraAdicionalPactosSub" + MSG);
    }

    /** legacy: PautaValorizarCarteraFuturos */
    public void pautaValorizarCarteraFuturos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraFuturos" + MSG);
    }

    /** legacy: PautaValorizarCarteraIF */
    public void pautaValorizarCarteraIF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraIF" + MSG);
    }

    /** legacy: PautaValorizarCarteraOpciones */
    public void pautaValorizarCarteraOpciones(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraOpciones" + MSG);
    }

    /** legacy: PautaValorizarCarteraRFIF */
    public void pautaValorizarCarteraRFIF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraRFIF" + MSG);
    }

    /** legacy: PautaValorizarCarteraRF */
    public void pautaValorizarCarteraRF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraRF" + MSG);
    }

    /** legacy: PautaValorizarCarteraRVPrecioMercado */
    public void pautaValorizarCarteraRVPrecioMercado(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraRVPrecioMercado" + MSG);
    }

    /** legacy: PautaValorizarCarteraSubPrestamosEmprestitos */
    public void pautaValorizarCarteraSubPrestamosEmprestitos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarCarteraSubPrestamosEmprestitos" + MSG);
    }

    /** legacy: PautaValorizarFFMM */
    public void pautaValorizarFFMM(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarFFMM" + MSG);
    }

    /** legacy: PautaValorizarGarantiasRVRFIF */
    public void pautaValorizarGarantiasRVRFIF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarGarantiasRVRFIF" + MSG);
    }

    /** legacy: PautaValorizarPactos */
    public void pautaValorizarPactos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarPactos" + MSG);
    }

    /** legacy: PautaValorizarPrestamosEmprestitos */
    public void pautaValorizarPrestamosEmprestitos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarPrestamosEmprestitos" + MSG);
    }

    /** legacy: PautaValorizarReportes */
    public void pautaValorizarReportes(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarReportes" + MSG);
    }

    /** legacy: PautaValorizarSimultaneas */
    public void pautaValorizarSimultaneas(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaValorizarSimultaneas" + MSG);
    }

    // ---- Vencimientos ------------------------------------------------------

    /** legacy: PautaVencimientosIF */
    public void pautaVencimientosIF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosIF" + MSG);
    }

    /** legacy: PautaVencimientosOperacionesOpciones */
    public void pautaVencimientosOperacionesOpciones(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosOperacionesOpciones" + MSG);
    }

    /** legacy: PautaVencimientosPactos */
    public void pautaVencimientosPactos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosPactos" + MSG);
    }

    /** legacy: PautaVencimientosPrestamosEmprestitos */
    public void pautaVencimientosPrestamosEmprestitos(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosPrestamosEmprestitos" + MSG);
    }

    /** legacy: PautaVencimientosReportes */
    public void pautaVencimientosReportes(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosReportes" + MSG);
    }

    /** legacy: PautaVencimientosRF */
    public void pautaVencimientosRF(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosRF" + MSG);
    }

    /** legacy: PautaVencimientosSimultanea */
    public void pautaVencimientosSimultanea(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("PautaVencimientosSimultanea" + MSG);
    }
}
