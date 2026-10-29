package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.Date;

/**
 * Datos generales del documento a presentar a la entidad reguladora.
 *
 * @author fpino@email.com
 *
 */
public final class Documento implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Comuna de la declaraci&oacute;n.
     */
    private String comunaDeclaracion;

    /**
     * Direcci&oacute;n de declaraci&oacute;n.
     */
    private String direccionDeclaracion;

    /**
     * Email de declaraci&oacute;n.
     */
    private String emailDeclaracion;

    /**
     * Indica si el documento est&aacute; en proceso.
     */
    private boolean enProceso;

    /**
     * Fecha de creaci&oacute;n del documento.
     */
    private Date fechaCreacion;

    /**
     * Fecha/Hora del inicio del &uacute;ltimo proceso.
     */
    private Date fechaInicioUltimoProceso;

    /**
     * Fecha l&iacute;mite de presentaci&oacute;n del documento.
     */
    private Date fechaLimite;

    /**
     * Fecha de presentaci&oacute;n del documento.
     */
    private Date fechaPresentacion;

    /**
     * Folio de declaraci&oacute;n del documento.
     */
    private String folioDeclaracion;

    /**
     * Id del documento.
     */
    private long id;

    /**
     * Id de la empresa del documento.
     */
    private long idEmpresa;

    /**
     * Id del estado del documento.
     */
    private String idEstadoDocumento;

    /**
     * Id del tipo de documento.
     */
    private String idTipoDocumento;

    /**
     * Nombre del esquema del documento.
     */
    private String nombreEsquema;

    /**
     * RUT del declarante.
     */
    private String rutDeclarante;

    /**
     * RUT del representante legal.
     */
    private String rutRepresentante;

    /**
     * N&uacute;mero de tel&eacute;fono de declaraci&oacute;n.
     */
    private String telefonoDeclaracion;

    /**
     * Versi&oacute;n del esquema del documento.
     */
    private String versionEsquema;

    /**
     * Obtiene la comuna de la declaraci&oacute;n.
     *
     * @return La comuna de la declaraci&oacute;n.
     */
    public String getComunaDeclaracion() {
        return comunaDeclaracion;
    }

    /**
     * Obtiene la direcci&oacute;n de declaraci&oacute;n.
     *
     * @return La direcci&oacute;n de declaraci&oacute;n.
     */
    public String getDireccionDeclaracion() {
        return direccionDeclaracion;
    }

    /**
     * Obtiene el email de declaraci&oacute;n.
     *
     * @return El email de declaraci&oacute;n.
     */
    public String getEmailDeclaracion() {
        return emailDeclaracion;
    }

    /**
     * Obtiene la fecha de creaci&oacute;n del documento.
     *
     * @return La fecha de creaci&oacute;n del documento.
     */
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha/hora inicio del &uacute;ltimo proceso.
     *
     * @return La fecha/hora inicio del &uacute;ltimo proceso.
     */
    public Date getFechaInicioUltimoProceso() {
        return fechaInicioUltimoProceso;
    }

    /**
     * Obtiene la fecha l&iacute;mite de presentaci&oacute;n del documento.
     *
     * @return La fecha l&iacute;mite de presentaci&oacute;n del documento.
     */
    public Date getFechaLimite() {
        return fechaLimite;
    }

    /**
     * Obtiene la fecha de presentaci&oacute;n del documento.
     *
     * @return La fecha de presentaci&oacute;n del documento.
     */
    public Date getFechaPresentacion() {
        return fechaPresentacion;
    }

    /**
     * Obtiene el folio de declaraci&oacute;n del documento.
     *
     * @return El folio de declaraci&oacute;n del documento.
     */
    public String getFolioDeclaracion() {
        return folioDeclaracion;
    }

    /**
     * Obtiene el id del documento.
     *
     * @return El id del documento.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtiene el id de la empresa del documento.
     *
     * @return El id de la empresa del documento.
     */
    public long getIdEmpresa() {
        return idEmpresa;
    }

    /**
     * Obtiene el id del estado del documento.
     *
     * @return El id del estado del documento.
     */
    public String getIdEstadoDocumento() {
        return idEstadoDocumento;
    }

    /**
     * Obtiene el id del tipo de documento.
     *
     * @return El id del tipo de documento.
     */
    public String getIdTipoDocumento() {
        return idTipoDocumento;
    }

    /**
     * Obtiene el nombre del esquema del documento.
     *
     * @return El nombre del esquema del documento.
     */
    public String getNombreEsquema() {
        return nombreEsquema;
    }

    /**
     * Obtiene el RUT del declarante.
     *
     * @return El RUT del declarante.
     */
    public String getRutDeclarante() {
        return rutDeclarante;
    }

    /**
     * Obtiene el RUT del representante legal.
     *
     * @return El RUT del representante legal.
     */
    public String getRutRepresentante() {
        return rutRepresentante;
    }

    /**
     * Obtiene el n&uacute;mero de tel&eacute;fono de declaraci&oacute;n.
     *
     * @return El n&uacute;mero de tel&eacute;fono de declaraci&oacute;n.
     */
    public String getTelefonoDeclaracion() {
        return telefonoDeclaracion;
    }

    /**
     * Obtiene la versi&oacute;n del esquema del documento.
     *
     * @return La versi&oacute;n del esquema del documento.
     */
    public String getVersionEsquema() {
        return versionEsquema;
    }

    /**
     * Indica si el documento est&aacute; en proceso.
     *
     * @return <code>true</code> si el documento est&aacute; en proceso,
     *         <code>false</code> en caso contrario.
     */
    public boolean isEnProceso() {
        return enProceso;
    }

    /**
     * Asigna la comuna de la declaraci&oacute;n.
     *
     * @param comunaDeclaracion
     *            La comuna de la declaraci&oacute;n.
     */
    public void setComunaDeclaracion(final String comunaDeclaracion) {
        this.comunaDeclaracion = comunaDeclaracion;
    }

    /**
     * Asigna la direcci&oacute;n de declaraci&oacute;n.
     *
     * @param direccionDeclaracion
     *            La direcci&oacute;n f&iacute;sica de declaraci&oacute;n.
     */
    public void setDireccionDeclaracion(final String direccionDeclaracion) {
        this.direccionDeclaracion = direccionDeclaracion;
    }

    /**
     * Asigna el email de declaraci&oacute;n.
     *
     * @param emailDeclaracion
     *            El email de declaraci&oacute;n.
     */
    public void setEmailDeclaracion(final String emailDeclaracion) {
        this.emailDeclaracion = emailDeclaracion;
    }

    /**
     * Asigna si el documento est&aacute; en proceso.
     *
     * @param enProceso
     *            Si el documento est&aacute; en proceso.
     */
    public void setEnProceso(final boolean enProceso) {
        this.enProceso = enProceso;
    }

    /**
     * Asigna la fecha de creaci&oacute;n del documento.
     *
     * @param fechaCreacion
     *            La fecha de creaci&oacute;n del documento.
     */
    public void setFechaCreacion(final Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Asigna la fecha/hora inicio del &uacute;ltimo proceso.
     *
     * @param fechaInicioUltimoProceso
     *            La fecha/hora inicio del &uacute;ltimo proceso.
     */
    public void setFechaInicioUltimoProceso(final Date fechaInicioUltimoProceso) {
        this.fechaInicioUltimoProceso = fechaInicioUltimoProceso;
    }

    /**
     * Asigna la fecha l&iacute;mite de presentaci&oacute;n del documento.
     *
     * @param fechaLimite
     *            La fecha l&iacute;mite de presentaci&oacute;n del documento.
     */
    public void setFechaLimite(final Date fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    /**
     * Asigna la fecha de presentaci&oacute;n del documento.
     *
     * @param fechaPresentacion
     *            La fecha de presentaci&oacute;n del documento.
     */
    public void setFechaPresentacion(final Date fechaPresentacion) {
        this.fechaPresentacion = fechaPresentacion;
    }

    /**
     * Asigna el folio de presentaci&oacute;n del documento.
     *
     * @param folioPresentacionDocumento
     *            El folio de presentaci&oacute;n del documento.
     */
    public void setFolioDeclaracion(final String folioPresentacionDocumento) {
        this.folioDeclaracion = folioPresentacionDocumento;
    }

    /**
     * Asigna el id del documento.
     *
     * @param id
     *            El id del documento.
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Asigna el id de la empresa del documento.
     *
     * @param idEmpresa
     *            El id de la empresa del documento.
     */
    public void setIdEmpresa(final long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    /**
     * Asigna el id del estado del documento.
     *
     * @param idEstadoDocumento
     *            El id del estado del documento.
     */
    public void setIdEstadoDocumento(final String idEstadoDocumento) {
        this.idEstadoDocumento = idEstadoDocumento;
    }

    /**
     * Asigna el id del tipo de documento.
     *
     * @param idTipoDocumento
     *            El id del tipo de documento.
     */
    public void setIdTipoDocumento(final String idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    /**
     * Asigna el nombre del esquema del documento.
     *
     * @param nombreEsquema
     *            El nombre del esquema del documento.
     */
    public void setNombreEsquema(final String nombreEsquema) {
        this.nombreEsquema = nombreEsquema;
    }

    /**
     * Asigna el RUT del declarante.
     *
     * @param rutDeclarante
     *            El RUT del declarante.
     */
    public void setRutDeclarante(final String rutDeclarante) {
        this.rutDeclarante = rutDeclarante;
    }

    /**
     * Asigna el RUT del representante legal.
     *
     * @param rutRepresentante
     *            El RUT del representante legal.
     */
    public void setRutRepresentante(final String rutRepresentante) {
        this.rutRepresentante = rutRepresentante;
    }

    /**
     * Asigna el n&uacute;mero de tel&eacute;fono de declaraci&oacute;n.
     *
     * @param telefonoPresentacion
     *            El n&uacute;mero de tel&eacute;fono de declaraci&oacute;n.
     */
    public void setTelefonoDeclaracion(final String telefonoPresentacion) {
        this.telefonoDeclaracion = telefonoPresentacion;
    }

    /**
     * Asigna la versi&oacute;n del esquema del documento.
     *
     * @param versionEsquema
     *            La versi&oacute;n del esquema del documento.
     */
    public void setVersionEsquema(final String versionEsquema) {
        this.versionEsquema = versionEsquema;
    }

    @Override
    public String toString() {
        return "Documento [comunaDeclaracion=" + comunaDeclaracion + ", direccionDeclaracion="
                + direccionDeclaracion + ", emailDeclaracion=" + emailDeclaracion + ", enProceso="
                + enProceso + ", fechaCreacion=" + fechaCreacion + ", fechaInicioUltimoProceso="
                + fechaInicioUltimoProceso + ", fechaLimite=" + fechaLimite + ", fechaPresentacion="
                + fechaPresentacion + ", folioDeclaracion=" + folioDeclaracion + ", id=" + id
                + ", idEmpresa=" + idEmpresa + ", idEstadoDocumento=" + idEstadoDocumento
                + ", idTipoDocumento=" + idTipoDocumento + ", nombreEsquema=" + nombreEsquema
                + ", rutDeclarante=" + rutDeclarante + ", rutRepresentante=" + rutRepresentante
                + ", telefonoDeclaracion=" + telefonoDeclaracion + ", versionEsquema="
                + versionEsquema + "]";
    }

}
