package cl.bcs.contabilidad.domain.model.basilea;

import java.math.BigDecimal;

/**
 * One position read by the five ProcesarInterfazBasilea_Select*.sql queries (acciones, RF-IF, ventas cortas, pactos,
 * forward). Legacy {@code ProcesarInterfazBasilea.ObjDatosBusqueda} (ProcesarInterfazBasilea.java:211-238).
 */
public record DatoBusquedaBasilea(
        String idBolsa,
        String esVentaCorta,
        String tipoCarteraCorredor,
        String idClasificacionTipoInstrumento,
        String idTipoInstrumento,
        String idEmisor,
        String emisor,
        String nemotecnico,
        String grupo,
        String producto,
        String idMonedaInstrumento,
        BigDecimal monto,
        String documentoIdentidadEmisor,
        String idTipoReajuste,
        String fechaVencimiento,
        String fechaVencimientoOperacion,
        String fechaOperacion,
        String fechaLiquidacion,
        String plazo,
        String folio,
        BigDecimal tasa,
        String relacionadoCorredora,
        String idTipoOperacion,
        String idMonedaPrincipal,
        String idMonedaSecundaria,
        String nemoBasilea) {
}
