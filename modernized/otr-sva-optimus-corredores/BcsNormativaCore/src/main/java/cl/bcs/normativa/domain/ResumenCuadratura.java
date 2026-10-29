package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Resumen del proceso de cuadratura.
 * Migrado desde legacy cl.bcs.normativa.esquema.ResumenCuadratura.
 */
public class ResumenCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date anoMes;
    private long cantidadCasos;
    private Date fechaCuadratura;
    private String glosa;
    private long id;
    private final List<Long> idDetalles = new ArrayList<>();
    private long idDocumento;
    private long idProceso;
    private long idUsuario;
    private String tipoClasificador;
    private String tipoCuadratura;
    private String tipoDiferencia;

    public void contarCasos() { this.cantidadCasos++; }

    public Date getAnoMes() { return anoMes; }
    public long getCantidadCasos() { return cantidadCasos; }
    public Date getFechaCuadratura() { return fechaCuadratura; }
    public String getGlosa() { return glosa; }
    public long getId() { return id; }
    public List<Long> getIdDetalles() { return idDetalles; }
    public long getIdDocumento() { return idDocumento; }
    public long getIdProceso() { return idProceso; }
    public long getIdUsuario() { return idUsuario; }
    public String getTipoClasificador() { return tipoClasificador; }
    public String getTipoCuadratura() { return tipoCuadratura; }
    public String getTipoDiferencia() { return tipoDiferencia; }

    public void setAnoMes(Date anoMes) { this.anoMes = anoMes; }
    public void setCantidadCasos(long cantidadCasos) { this.cantidadCasos = cantidadCasos; }
    public void setFechaCuadratura(Date fechaCuadratura) { this.fechaCuadratura = fechaCuadratura; }
    public void setGlosa(String glosa) { this.glosa = glosa; }
    public void setId(long id) { this.id = id; }
    public void setIdDocumento(long idDocumento) { this.idDocumento = idDocumento; }
    public void setIdProceso(long idProceso) { this.idProceso = idProceso; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }
    public void setTipoClasificador(String tipoClasificador) { this.tipoClasificador = tipoClasificador; }
    public void setTipoCuadratura(String tipoCuadratura) { this.tipoCuadratura = tipoCuadratura; }
    public void setTipoDiferencia(String tipoDiferencia) { this.tipoDiferencia = tipoDiferencia; }

    @Override
    public String toString() {
        return "ResumenCuadratura[id=" + id + ", tipo=" + tipoCuadratura + "]";
    }
}
