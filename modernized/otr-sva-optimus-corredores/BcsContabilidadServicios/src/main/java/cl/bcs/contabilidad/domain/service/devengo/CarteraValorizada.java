package cl.bcs.contabilidad.domain.service.devengo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One raw row of {@code "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"} for a valuation report, the input of the three
 * Devengo* accrual centralizations (CONT-C-01..18).
 *
 * <p>Legacy read movements and events through SQL that already derived them (CentralizaDevengo*_SelectMovimientos.sql /
 * _SelectEventos.sql); here the row is read as-is and {@link DevengoCalculator} applies those derivations.
 * {@code documentoIdentidad} and {@code idMonedaSistema} come from the joins of the movement query (client document,
 * PLA.MonedaSistema) and are null in rows of the event query, which legacy never joined.
 */
public record CarteraValorizada(
        Long idValorizacion,
        Long idCartera,
        Long idEmpresa,
        String idTipoOperacion,
        String idClasificacionTipoInstrumento,
        Long folioOperacion,
        Long folioCartera,
        String documentoIdentidad,
        Long idPortafolio,
        LocalDate fechaOperacion,
        String idMonedaInstrumento,
        String idMonedaSistema,
        BigDecimal montoCompra,
        BigDecimal montoPactado,
        BigDecimal montoActual,
        BigDecimal montoProximo,
        BigDecimal montoInteres,
        BigDecimal montoInteresCorrido,
        BigDecimal montoReajuste) {
}
