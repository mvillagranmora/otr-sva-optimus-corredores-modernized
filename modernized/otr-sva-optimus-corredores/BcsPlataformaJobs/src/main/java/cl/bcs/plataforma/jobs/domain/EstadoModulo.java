package cl.bcs.plataforma.jobs.domain;

/**
 * Module state record queried before pauta execution.
 * Legacy: cl.bcs.plataforma.jobs.LaPauta.EstadoModulo
 */
public class EstadoModulo {
    public String idModulo;
    public String idEstado;
    public Long idEmpresa;
    public String nombreModulo;

    @Override
    public String toString() {
        return "EstadoModulo{modulo=" + idModulo + ", estado=" + idEstado + "}";
    }
}
