package cl.bcs.cartolas.domain;

import java.time.LocalDateTime;

/**
 * Registro de trazabilidad de proceso por nómina (tabla CAR.Traza).
 */
public class Traza {

    private long          id;
    private long          idNomina;
    private String        mensaje;
    private LocalDateTime timestamp;

    public static Traza of(final long idNomina, final String mensaje) {
        final Traza t = new Traza();
        t.idNomina = idNomina;
        t.mensaje  = mensaje;
        return t;
    }

    public long getId()                       { return id; }
    public void setId(final long v)            { this.id = v; }

    public long getIdNomina()                 { return idNomina; }
    public void setIdNomina(final long v)      { this.idNomina = v; }

    public String getMensaje()                { return mensaje; }
    public void setMensaje(final String v)    { this.mensaje = v; }

    public LocalDateTime getTimestamp()       { return timestamp; }
    public void setTimestamp(final LocalDateTime v){ this.timestamp = v; }
}
