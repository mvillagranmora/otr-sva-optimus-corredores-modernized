package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.ResumenCarga;

/**
 * Puerto de salida para gestion de ResumenCarga.
 */
public interface ResumenCargaPort {

    /** Inserta nuevo resumen de carga (asigna id internamente). */
    void insertar(ResumenCarga resumen);

    /** Actualiza glosa y totales de un resumen de carga existente. */
    void actualizar(ResumenCarga resumen);
}
