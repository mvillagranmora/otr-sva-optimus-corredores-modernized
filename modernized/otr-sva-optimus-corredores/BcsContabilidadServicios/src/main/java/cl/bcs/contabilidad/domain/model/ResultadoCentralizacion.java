package cl.bcs.contabilidad.domain.model;

import java.math.BigDecimal;

/**
 * Represents one accounting movement result row returned after centralisation.
 *
 * Derived from legacy cl.bcs.contabilidad.pojo.ResultadoCentralizacion.
 * Note: Ejercicio is String here (converted from Long via String.valueOf in legacy
 * BcsCentralizacion.llenarResultado()), matching the legacy field type exactly.
 */
public record ResultadoCentralizacion(
        Long       idTipoCentralizacion,
        Long       idMovimiento,
        String     idTipoMovimiento,
        Long       idEmpresa,
        Long       numero,
        Long       linea,
        String     fecha,
        String     documentoIdentidad,
        Long       idPortafolio,
        /** String because legacy llenarResultado() converts Long via String.valueOf(). */
        String     ejercicio,
        String     periodo,
        BigDecimal monto,
        String     idMoneda,
        String     estado
) {}
