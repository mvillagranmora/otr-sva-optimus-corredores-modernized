package cl.bcs.socionegocio.jobs.domain;

/**
 * Represents a client to be blocked automatically.
 * Ported from BloquearClientePorFechaVencimientoJob.DatosActualizar (legacy).
 */
public record ClienteABloquear(
        Long idCliente,
        String idTipoBloqueo,
        String comentarioBloqueo
) {}
