package cl.bcs.ordenes.domain;

/**
 * Orden en proceso de validación — contiene el contador de errores de negocio.
 * Mutable por diseño: el algoritmo de limpieza incrementa `errores` a medida
 * que evalúa cada regla de validación.
 */
public class OrdenProceso {
    public final String id;
    public long errores;

    public OrdenProceso(String id, long errores) {
        this.id = id;
        this.errores = errores;
    }
}
