package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Resumen del proceso de carga de datos.
 * Migrado desde legacy cl.bcs.normativa.esquema.ResumenCarga.
 */
public class ResumenCarga implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date fecha;
    private String glosa;
    private long id;
    private long idDocumento;
    private long idProceso;
    private long idUsuario;
    private String nombreArchivo;
    private long totalRegistrosCargados;
    private long totalRegistrosErroneos;
    private long totalRegistrosLeidos;

    public void contarRegistrosErroneos() { this.totalRegistrosErroneos++; }

    public Date getFecha() { return fecha; }
    public String getGlosa() { return glosa; }
    public long getId() { return id; }
    public long getIdDocumento() { return idDocumento; }
    public long getIdProceso() { return idProceso; }
    public long getIdUsuario() { return idUsuario; }
    public String getNombreArchivo() { return nombreArchivo; }
    public long getTotalRegistrosCargados() { return totalRegistrosCargados; }
    public long getTotalRegistrosErroneos() { return totalRegistrosErroneos; }
    public long getTotalRegistrosLeidos() { return totalRegistrosLeidos; }

    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setGlosa(String glosa) { this.glosa = glosa; }
    public void setId(long id) { this.id = id; }
    public void setIdDocumento(long idDocumento) { this.idDocumento = idDocumento; }
    public void setIdProceso(long idProceso) { this.idProceso = idProceso; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public void setTotalRegistrosCargados(long totalRegistrosCargados) { this.totalRegistrosCargados = totalRegistrosCargados; }
    public void setTotalRegistrosErroneos(long totalRegistrosErroneos) { this.totalRegistrosErroneos = totalRegistrosErroneos; }
    public void setTotalRegistrosLeidos(long totalRegistrosLeidos) { this.totalRegistrosLeidos = totalRegistrosLeidos; }
}
