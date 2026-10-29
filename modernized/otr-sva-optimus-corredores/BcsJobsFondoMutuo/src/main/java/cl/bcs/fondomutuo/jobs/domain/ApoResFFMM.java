package cl.bcs.fondomutuo.jobs.domain;

import java.math.BigDecimal;

/**
 * Aporte/Rescate FFMM record for IngresarOrdenBonificacionFiscalReinversion.
 * Mirrors legacy cl.bcs.fondomutuo.jobs.IngresarOrdenBonificacionFiscalReinversion.ApoResFFMM.
 */
public record ApoResFFMM(
        Long       idSolicitudAporteRescate,
        Long       idCliente,
        Long       idPortafolio,
        Long       idPortafolioOrigen,
        String     tipoSolicitud,
        String     idSerieFondo,
        String     calculoPorMontoCuota,
        BigDecimal monto,
        BigDecimal numeroCuotas,
        String     idMoneda,
        String     idMedioDePago,
        String     visado,
        String     solicitudBatch,
        String     origenOperacion,
        String     idRegimenTributario,
        String     indicadorMLT,
        String     idSerieFondoDestino,
        String     idEmisorDestino,
        Long       idAutorizadoOrigen,
        BigDecimal porcentaje,
        Long       folioContrato,
        BigDecimal porcentajeCalculado
) {}
