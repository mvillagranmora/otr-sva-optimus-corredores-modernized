package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Resumen del resultado de aplicar una regla de validaci&oacute;n a un
 * documento.
 *
 * @author fpino@email.com
 *
 */
public class ResumenValidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Cantidad de casos.
     */
    private long cantidadCasos;

    /**
     * C&oacute;digo de la regla de validaci&oacute;n.
     */
    private String codigoValidacion;

    /**
     * Descripci&oacute;n de la regla de validaci&oacute;n.
     */
    private String descripcionValidacion;

    /**
     * Fecha/hora en que se aplic&oacute; la regla de validaci&oacute;n.
     */
    private Date fechaValidacion;

    /**
     * Id del resumen.
     */
    private long id;

    /**
     * Lista de los id de los detalles.
     */
    private final List<Long> idDetalles = new ArrayList<>();

    /**
     * Id del documento.
     */
    private long idDocumento;

    /**
     * Id del proceso.
     */
    private long idProceso;

    /**
     * Id del usuario.
     */
    private long idUsuario;

    /**
     * Incrementa en uno la cantidad de casos.
     */
    public void contarCasos() {
        this.cantidadCasos++;
    }

    /**
     * Obtiene la cantidad de casos.
     *
     * @return La cantidad de casos.
     */
    public long getCantidadCasos() {
        return cantidadCasos;
    }

    /**
     * Obtiene el c&oacute;digo de la regla de validaci&oacute;n.
     *
     * @return El c&oacute;digo de la regla de validaci&oacute;n.
     */
    public String getCodigoValidacion() {
        return codigoValidacion;
    }

    /**
     * Obtiene la descripci&oacute;n de la regla de validaci&oacute;n.
     *
     * @return La descripci&oacute;n de la regla de validaci&oacute;n.
     */
    public String getDescripcionValidacion() {
        return descripcionValidacion;
    }

    /**
     * Obtiene la fecha/hora en que se aplic&oacute; la regla de
     * validaci&oacute;n.
     *
     * @return La fecha/hora en que se aplic&oacute; la regla de
     *         validaci&oacute;n.
     */
    public Date getFechaValidacion() {
        return fechaValidacion;
    }

    /**
     * Obtiene el id del resumen.
     *
     * @return El id del resumen.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtiene la lista de los id de los detalles.
     *
     * @return La lista de los id de los detalles.
     */
    public List<Long> getIdDetalles() {
        return idDetalles;
    }

    /**
     * Obtiene el id del documento.
     *
     * @return El id del documento.
     */
    public long getIdDocumento() {
        return idDocumento;
    }

    /**
     * Obtiene el id del proceso.
     *
     * @return El id del proceso.
     */
    public long getIdProceso() {
        return idProceso;
    }

    /**
     * Obtiene el id del usuario.
     *
     * @return El id del usuario.
     */
    public long getIdUsuario() {
        return idUsuario;
    }

    /**
     * Asigna la cantidad de casos.
     *
     * @param cantidadCasos
     *            La cantidad de casos.
     */
    public void setCantidadCasos(final long cantidadCasos) {
        this.cantidadCasos = cantidadCasos;
    }

    /**
     * Asigna el c&oacute;digo de la regla de validaci&oacute;n.
     *
     * @param codigoValidacion
     *            El c&oacute;digo de la regla de validaci&oacute;n.
     */
    public void setCodigoValidacion(final String codigoValidacion) {
        this.codigoValidacion = codigoValidacion;
    }

    /**
     * Asigna la descripci&oacute;n de la regla de validaci&oacute;n.
     *
     * @param descripcionValidacion
     *            La descripci&oacute;n de la regla de validaci&oacute;n.
     */
    public void setDescripcionValidacion(final String descripcionValidacion) {
        this.descripcionValidacion = descripcionValidacion;
    }

    /**
     * Asigna la fecha/hora en que se aplic&oacute; la regla de
     * validaci&oacute;n.
     *
     * @param fechaValidacion
     *            La fecha/hora en que se aplic&oacute; la regla de
     *            validaci&oacute;n.
     */
    public void setFechaValidacion(final Date fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    /**
     * Asigna el id del resumen.
     *
     * @param id
     *            El id del resumen.
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Asigna el id del documento.
     *
     * @param idDocumento
     *            El id del documento.
     */
    public void setIdDocumento(final long idDocumento) {
        this.idDocumento = idDocumento;
    }

    /**
     * Asigna el id del proceso.
     *
     * @param idProceso
     *            El id del proceso.
     */
    public void setIdProceso(final long idProceso) {
        this.idProceso = idProceso;
    }

    /**
     * Asigna el id del usuario.
     *
     * @param idUsuario
     *            El id del usuario.
     */
    public void setIdUsuario(final long idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {
        return "ResumenValidacion [cantidadCasos=" + cantidadCasos + ", codigoValidacion="
                + codigoValidacion + ", descripcionValidacion=" + descripcionValidacion
                + ", fechaValidacion=" + fechaValidacion + ", id=" + id + ", idDocumento="
                + idDocumento + ", idProceso=" + idProceso + ", idUsuario=" + idUsuario + "]";
    }

}
