package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Relaci&oacute;n de la validaci&oacute;n y el detalle de un documento.
 *
 * @author fpino@email.com
 *
 */
public class DetalleValidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id del detalle de la validaci&oacute;n.
     */
    private long id;

    /**
     * Id del detalle del documento.
     */
    private long idDetalleDocumento;

    /**
     * Id del resumen de la validaci&oacute;n.
     */
    private long idResumenValidacion;

    /**
     * Obtiene el id del detalle de la validaci&oacute;n.
     *
     * @return El id del detalle de la validaci&oacute;n.
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
     * Obtiene el id del resumen de la validaci&oacute;n.
     *
     * @return El id del resumen de la validaci&oacute;n.
     */
    public long getIdResumenValidacion() {
        return idResumenValidacion;
    }

    /**
     * Asigna el id del detalle de la validaci&oacute;n.
     *
     * @param id
     *            El id del detalle de la validaci&oacute;n.
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
     * Asigna el id del resumen de la validaci&oacute;n.
     *
     * @param idResumenValidacion
     *            El id del resumen de la validaci&oacute;n.
     */
    public void setIdResumenValidacion(final long idResumenValidacion) {
        this.idResumenValidacion = idResumenValidacion;
    }

    @Override
    public String toString() {
        return "DetalleValidacion [id=" + id + ", idDetalleDocumento=" + idDetalleDocumento
                + ", idResumenValidacion=" + idResumenValidacion + "]";
    }

}
