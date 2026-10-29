package cl.bcs.custodia.jobs.domain;

import java.math.BigDecimal;

/**
 * Represents a custody position for a client.
 * Legacy source: GenerarResumenPosiciones.java (TarjetaExistenciaResumen / ResumenPosicionesRV)
 */
public record PosicionCustodia(
        Long idCliente,
        String idMoneda,
        BigDecimal cantidad,
        BigDecimal valorMercado
) {}
