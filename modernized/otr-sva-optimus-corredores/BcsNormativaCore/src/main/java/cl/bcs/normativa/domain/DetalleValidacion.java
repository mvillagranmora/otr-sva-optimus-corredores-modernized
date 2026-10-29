package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Detalle de una validacion.
 * Migrado desde legacy cl.bcs.normativa.esquema.DetalleValidacion.
 */
public class DetalleValidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long idDetalleDocumento;
    private long idResumenValidacion;

    public long getId() { return id; }
    public long getIdDetalleDocumento() { return idDetalleDocumento; }
    public long getIdResumenValidacion() { return idResumenValidacion; }

    public void setId(long id) { this.id = id; }
    public void setIdDetalleDocumento(long idDetalleDocumento) { this.idDetalleDocumento = idDetalleDocumento; }
    public void setIdResumenValidacion(long idResumenValidacion) { this.idResumenValidacion = idResumenValidacion; }
}
