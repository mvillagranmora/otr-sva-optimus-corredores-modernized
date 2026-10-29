package cl.bcs.fondomutuo.jobs.domain;

/**
 * Traspaso row used by BcsJobConfirmarTraspaso.
 * Mirrors legacy cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso.
 */
public record RegistroTraspaso(
        Long   idTraspaso,
        Long   idEmpresa,
        String idEstado,
        String fechaIngreso,
        Long   diasTranscurridos
) {}
