package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Resumen del proceso de generacion.
 * Migrado desde legacy cl.bcs.normativa.esquema.ResumenGeneracion.
 */
public class ResumenGeneracion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date fecha;
    private String glosa;
    private String nombreArchivo;
    private long id;
    private long idDocumento;
    private long idProceso;
    private long idUsuario;
    private long totalRegistrosGenerados;
    private long ultimoCorrelativo;

    public Date getFecha() { return fecha; }
    public String getGlosa() { return glosa; }
    public long getId() { return id; }
    public long getIdDocumento() { return idDocumento; }
    public long getIdProceso() { return idProceso; }
    public long getIdUsuario() { return idUsuario; }
    public String getNombreArchivo() { return nombreArchivo; }
    public long getTotalRegistrosGenerados() { return totalRegistrosGenerados; }
    public long getUltimoCorrelativo() { return ultimoCorrelativo; }

    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setGlosa(String glosa) { this.glosa = glosa; }
    public void setId(long id) { this.id = id; }
    public void setIdDocumento(long idDocumento) { this.idDocumento = idDocumento; }
    public void setIdProceso(long idProceso) { this.idProceso = idProceso; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public void setTotalRegistrosGenerados(long totalRegistrosGenerados) { this.totalRegistrosGenerados = totalRegistrosGenerados; }
    public void setUltimoCorrelativo(long ultimoCorrelativo) { this.ultimoCorrelativo = ultimoCorrelativo; }

    @Override
    public String toString() {
        return "ResumenGeneracion[id=" + id + ", archivo=" + nombreArchivo + "]";
    }
}
