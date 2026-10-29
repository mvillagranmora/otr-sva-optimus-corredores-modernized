package cl.bcs.custodia.jobs.domain;

/**
 * Mutable job result container.
 * Legacy source: multiple job classes (P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES pattern)
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

    public void incrementarErrores() {
        this.erroresContador++;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void agregarMensaje(String linea) {
        this.mensaje = this.mensaje + "\n" + linea;
    }

    public boolean tieneErrores() {
        return erroresContador > 0;
    }
}
