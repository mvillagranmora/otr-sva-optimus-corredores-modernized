package cl.bcs.normativa.cartolas.entidades;

import java.io.Serializable;
import java.sql.Timestamp;

public class Traza implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long idNomina;
    private String mensaje;
    private Timestamp timestamp;

    public long getId() {
        return id;
    }

    public long getIdNomina() {
        return idNomina;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdNomina(
            final long idNomina) {
        this.idNomina = idNomina;
    }

    public void setMensaje(
            final String mensaje) {
        this.mensaje = mensaje;
    }

    public void setTimestamp(
            final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
