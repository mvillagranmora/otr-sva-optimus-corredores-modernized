package cl.bcs.replica.domain;

/**
 * Output of initiating a replica operation.
 * Mutable: erroresContador defaults to 0L.
 */
public class SalidaIniciarReplica {

    private Long erroresContador = 0L;
    private String mensaje = "";

    public SalidaIniciarReplica() {
    }

    public SalidaIniciarReplica(Long erroresContador, String mensaje) {
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
}
