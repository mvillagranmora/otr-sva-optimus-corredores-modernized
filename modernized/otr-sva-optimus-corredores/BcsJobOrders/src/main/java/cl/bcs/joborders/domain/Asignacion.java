package cl.bcs.joborders.domain;

// B-JOB-07..09: Resultado de una asignación entre orden y transacción
public record Asignacion(
    int idAsignacionPaso,
    String idEstadoFacturacion,
    int cantidadAsignada,
    String fechaAsignacion,
    int idTransaccionPaso,
    int idOrdenPaso,
    int montoTotal
) {}
