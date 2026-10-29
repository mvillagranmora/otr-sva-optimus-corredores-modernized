package cl.bcs.socionegocio.jobs.domain;

/**
 * Mutable result container for job executions.
 * Ported from SalidaBloquearClientePorFechaVencimientoJob (legacy).
 */
public class ResultadoJob {

    private Long erroresContador = 0L;
    private String mensaje = "";

    public ResultadoJob() {}

    public ResultadoJob(Long erroresContador, String mensaje) {
        this.erroresContador = erroresContador;
        this.mensaje = mensaje;
    }

    public Long getErroresContador() {
        return erroresContador;
    }

    public void setErroresContador(Long erroresContador) {
        this.erroresContador = erroresContador;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean tieneErrores() {
        return erroresContador != null && erroresContador > 0;
    }
}
