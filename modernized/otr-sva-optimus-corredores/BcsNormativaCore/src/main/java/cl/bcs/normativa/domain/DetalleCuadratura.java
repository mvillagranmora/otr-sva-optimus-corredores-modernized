package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Detalle de una cuadratura.
 * Migrado desde legacy cl.bcs.normativa.esquema.DetalleCuadratura.
 */
public class DetalleCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long idDetalleDocumento;
    private long idResumenCuadratura;

    public long getId() { return id; }
    public long getIdDetalleDocumento() { return idDetalleDocumento; }
    public long getIdResumenCuadratura() { return idResumenCuadratura; }

    public void setId(long id) { this.id = id; }
    public void setIdDetalleDocumento(long idDetalleDocumento) { this.idDetalleDocumento = idDetalleDocumento; }
    public void setIdResumenCuadratura(long idResumenCuadratura) { this.idResumenCuadratura = idResumenCuadratura; }
}
