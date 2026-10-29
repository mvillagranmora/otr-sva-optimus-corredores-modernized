package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Relaci&oacute;n de la cuadratura y el detalle de un documento.
 *
 * @author fpino@email.com
 *
 */
public class DetalleCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id del detalle de la cuadratura.
     */
    private long id;

    /**
     * Id del detalle del documento.
     */
    private long idDetalleDocumento;

    /**
     * Id del resumen de la cuadratura.
     */
    private long idResumenCuadratura;

    /**
     * Obtiene el id del detalle de la cuadratura.
     *
     * @return El id del detalle de la cuadratura.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtiene el id del detalle del documento.
     *
     * @return El id del detalle del documento.
     */
    public long getIdDetalleDocumento() {
        return idDetalleDocumento;
    }

    /**
     * Obtiene el id del resumen de la cuadratura.
     *
     * @return El id del resumen de la cuadratura.
     */
    public long getIdResumenCuadratura() {
        return idResumenCuadratura;
    }

    /**
     * Asigna el id del detalle de la cuadratura.
     *
     * @param id
     *            El id del detalle de la cuadratura.
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Asigna el id del detalle del documento.
     *
     * @param idDetalleDocumento
     *            El id del detalle del documento.
     */
    public void setIdDetalleDocumento(final long idDetalleDocumento) {
        this.idDetalleDocumento = idDetalleDocumento;
    }

    /**
     * Asigna el id del resumen de la cuadratura.
     *
     * @param idResumenCuadratura
     *            El id del resumen de la cuadratura.
     */
    public void setIdResumenCuadratura(final long idResumenCuadratura) {
        this.idResumenCuadratura = idResumenCuadratura;
    }

    @Override
    public String toString() {
        return "DetalleCuadratura [id=" + id + ", idDetalleDocumento=" + idDetalleDocumento
                + ", idResumenCuadratura=" + idResumenCuadratura + "]";
    }

}
