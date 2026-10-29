package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Resumen del proceso de validacion.
 * Migrado desde legacy cl.bcs.normativa.esquema.ResumenValidacion.
 */
public class ResumenValidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private long cantidadCasos;
    private String codigoValidacion;
    private String descripcionValidacion;
    private Date fechaValidacion;
    private long id;
    private final List<Long> idDetalles = new ArrayList<>();
    private long idDocumento;
    private long idProceso;
    private long idUsuario;

    public void contarCasos() { this.cantidadCasos++; }

    public long getCantidadCasos() { return cantidadCasos; }
    public String getCodigoValidacion() { return codigoValidacion; }
    public String getDescripcionValidacion() { return descripcionValidacion; }
    public Date getFechaValidacion() { return fechaValidacion; }
    public long getId() { return id; }
    public List<Long> getIdDetalles() { return idDetalles; }
    public long getIdDocumento() { return idDocumento; }
    public long getIdProceso() { return idProceso; }
    public long getIdUsuario() { return idUsuario; }

    public void setCantidadCasos(long cantidadCasos) { this.cantidadCasos = cantidadCasos; }
    public void setCodigoValidacion(String codigoValidacion) { this.codigoValidacion = codigoValidacion; }
    public void setDescripcionValidacion(String descripcionValidacion) { this.descripcionValidacion = descripcionValidacion; }
    public void setFechaValidacion(Date fechaValidacion) { this.fechaValidacion = fechaValidacion; }
    public void setId(long id) { this.id = id; }
    public void setIdDocumento(long idDocumento) { this.idDocumento = idDocumento; }
    public void setIdProceso(long idProceso) { this.idProceso = idProceso; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }

    @Override
    public String toString() {
        return "ResumenValidacion[id=" + id + ", codigo=" + codigoValidacion + "]";
    }
}
