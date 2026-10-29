package cl.bcs.mesajobs.domain;

// Parámetros de entrada comunes a todos los jobs de BcsMesaJobs
public record JobParams(
    long idEmpresa,
    String fechaDesde,
    String fechaHasta,
    String rutaArchivo,
    String nombreArchivo,
    String formatoArchivo,
    String servicio
) {}
