package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.ResumenGeneracion;

/**
 * Puerto de salida para gestion de ResumenGeneracion.
 */
public interface ResumenGeneracionPort {

    /** Inserta nuevo resumen de generacion (asigna id internamente). */
    void insertar(ResumenGeneracion resumen);

    /** Actualiza glosa y total de registros generados. */
    void actualizar(ResumenGeneracion resumen);
}
