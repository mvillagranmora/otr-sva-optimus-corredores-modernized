package cl.bcs.plataforma.jobs.domain;

import java.sql.Timestamp;

/**
 * Represents a pauta execution record.
 * Legacy: cl.bcs.plataforma.jobs.LaPauta.Ejecucion
 */
public class Ejecucion {
    public Long idEmpresa;
    public String fechaSistema;
    public Timestamp fechaInicio;
    public Timestamp fechaFin;
    public String rutaLog;
    public String estado;

    @Override
    public String toString() {
        return "Ejecucion{idEmpresa=" + idEmpresa + ", fechaSistema=" + fechaSistema
                + ", estado=" + estado + "}";
    }
}
