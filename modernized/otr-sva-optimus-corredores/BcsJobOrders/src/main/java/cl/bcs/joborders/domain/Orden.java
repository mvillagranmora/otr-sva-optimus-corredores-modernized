package cl.bcs.joborders.domain;

// B-JOB-02..10: Orden en memoria para el algoritmo de asignación cronológica
public record Orden(
    long idJob,
    int idOrdenPaso,
    String idClasificacionTipoInstrumento,
    String idTipoInstrumento,
    String idBolsa,
    String idTipoOperacion,
    String idCondicionDeLiquidacion,
    double precio,
    double cantidadDisponible,
    double cantidadAsignada,
    double monto,
    String tipoRuteo,
    String fechaIngreso,
    String fechaOperacion,
    int idLibroOrdenes,
    String idCodigoTipoPrecio,
    String especificaBolsa,
    int idInstrumento
) {}
