package cl.bcs.plataforma.jobs.domain;

/**
 * Represents a process step within the pauta (daily closing batch).
 * Legacy: cl.bcs.plataforma.jobs.LaPauta.ProcesoPauta
 */
public class ProcesoPauta {
    public String codigoProceso;
    public String nombreClase;
    public Long orden;
    /** "S" = detiene pauta en caso de error, "N" = continua */
    public String detienePauta;
    public String codigoProcesoDependencia;
    public String estado;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProcesoPauta p)) return false;
        return this.codigoProceso.equals(p.codigoProceso)
                && this.nombreClase.equals(p.nombreClase);
    }

    @Override
    public int hashCode() {
        return codigoProceso.hashCode() * 31 + nombreClase.hashCode();
    }

    @Override
    public String toString() {
        return "ProcesoPauta{codigo=" + codigoProceso + ", orden=" + orden + ", estado=" + estado + "}";
    }
}
