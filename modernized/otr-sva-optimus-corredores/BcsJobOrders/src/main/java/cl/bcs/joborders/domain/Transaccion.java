package cl.bcs.joborders.domain;

// B-JOB-02..10: Transacción (palo) para el algoritmo de asignación cronológica
public record Transaccion(
    int idTransaccionPaso,
    String idClasificacionTipoInstrumento,
    String idTipoInstrumento,
    String idBolsa,
    String idTipoOperacion,
    String idCondicionDeLiquidacion,
    double precio,
    double cantidadTotal,
    double cantidadDisponible,
    double cantidadAsignada,
    double monto,
    String fechaIngresoRegistro,
    String fechaOperacionTransaccion,
    int idInstrumento
) {}
