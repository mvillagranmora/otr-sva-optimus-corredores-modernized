package cl.bcs.plataforma.jobs.domain;

import java.sql.Timestamp;

/**
 * Represents the execution detail for a single pauta process step.
 * Legacy: cl.bcs.plataforma.jobs.LaPauta.DetalleEjecucion
 */
public class DetalleEjecucion {
    public Long idEmpresa;
    public String fechaSistema;
    public String codigoProceso;
    public Timestamp fechaInicio;
    public Timestamp fechaFin;
    public String estado;
    public String mensaje;

    @Override
    public String toString() {
        return "DetalleEjecucion{codigo=" + codigoProceso + ", estado=" + estado + "}";
    }
}
