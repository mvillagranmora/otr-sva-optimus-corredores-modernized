package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Datos generales del documento a presentar a la entidad reguladora.
 * Migrado desde legacy cl.bcs.normativa.esquema.Documento.
 */
public final class Documento implements Serializable {

    private static final long serialVersionUID = 1L;

    private String comunaDeclaracion;
    private String direccionDeclaracion;
    private String emailDeclaracion;
    private boolean enProceso;
    private Date fechaCreacion;
    private Date fechaInicioUltimoProceso;
    private Date fechaLimite;
    private Date fechaPresentacion;
    private String folioDeclaracion;
    private long id;
    private long idEmpresa;
    private String idEstadoDocumento;
    private String idTipoDocumento;
    private String nombreEsquema;
    private String rutDeclarante;
    private String rutRepresentante;
    private String telefonoDeclaracion;
    private String versionEsquema;

    public String getComunaDeclaracion() { return comunaDeclaracion; }
    public String getDireccionDeclaracion() { return direccionDeclaracion; }
    public String getEmailDeclaracion() { return emailDeclaracion; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public Date getFechaInicioUltimoProceso() { return fechaInicioUltimoProceso; }
    public Date getFechaLimite() { return fechaLimite; }
    public Date getFechaPresentacion() { return fechaPresentacion; }
    public String getFolioDeclaracion() { return folioDeclaracion; }
    public long getId() { return id; }
    public long getIdEmpresa() { return idEmpresa; }
    public String getIdEstadoDocumento() { return idEstadoDocumento; }
    public String getIdTipoDocumento() { return idTipoDocumento; }
    public String getNombreEsquema() { return nombreEsquema; }
    public String getRutDeclarante() { return rutDeclarante; }
    public String getRutRepresentante() { return rutRepresentante; }
    public String getTelefonoDeclaracion() { return telefonoDeclaracion; }
    public String getVersionEsquema() { return versionEsquema; }
    public boolean isEnProceso() { return enProceso; }

    public void setComunaDeclaracion(String comunaDeclaracion) { this.comunaDeclaracion = comunaDeclaracion; }
    public void setDireccionDeclaracion(String direccionDeclaracion) { this.direccionDeclaracion = direccionDeclaracion; }
    public void setEmailDeclaracion(String emailDeclaracion) { this.emailDeclaracion = emailDeclaracion; }
    public void setEnProceso(boolean enProceso) { this.enProceso = enProceso; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaInicioUltimoProceso(Date fechaInicioUltimoProceso) { this.fechaInicioUltimoProceso = fechaInicioUltimoProceso; }
    public void setFechaLimite(Date fechaLimite) { this.fechaLimite = fechaLimite; }
    public void setFechaPresentacion(Date fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }
    public void setFolioDeclaracion(String folioDeclaracion) { this.folioDeclaracion = folioDeclaracion; }
    public void setId(long id) { this.id = id; }
    public void setIdEmpresa(long idEmpresa) { this.idEmpresa = idEmpresa; }
    public void setIdEstadoDocumento(String idEstadoDocumento) { this.idEstadoDocumento = idEstadoDocumento; }
    public void setIdTipoDocumento(String idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setRutDeclarante(String rutDeclarante) { this.rutDeclarante = rutDeclarante; }
    public void setRutRepresentante(String rutRepresentante) { this.rutRepresentante = rutRepresentante; }
    public void setTelefonoDeclaracion(String telefonoDeclaracion) { this.telefonoDeclaracion = telefonoDeclaracion; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "Documento[id=" + id + ", esquema=" + nombreEsquema + "-" + versionEsquema + "]";
    }
}
