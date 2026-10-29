package cl.bcs.normativa.dal;

import java.io.Serializable;

public class LogRegistro implements Serializable {

    private static final long serialVersionUID = 1L;

    private String estadoProceso;
    private String fechaSistemaLog;
    private long idEmpresa;
    private String idModulo;
    private long idProceso;
    private String idTipoLog;
    private long idUsuario;
    private long idUsuarioAtendido;
    private String nombreProceso;

    public String getEstadoProceso() {
        return estadoProceso;
    }

    public String getFechaSistemaLog() {
        return fechaSistemaLog;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public String getIdModulo() {
        return idModulo;
    }

    public long getIdProceso() {
        return idProceso;
    }

    public String getIdTipoLog() {
        return idTipoLog;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public long getIdUsuarioAtendido() {
        return idUsuarioAtendido;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setEstadoProceso(final String estadoProceso) {
        this.estadoProceso = estadoProceso;
    }

    public void setFechaSistemaLog(final String fechaSistemaLog) {
        this.fechaSistemaLog = fechaSistemaLog;
    }

    public void setIdEmpresa(final long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setIdModulo(final String idModulo) {
        this.idModulo = idModulo;
    }

    public void setIdProceso(final long idProceso) {
        this.idProceso = idProceso;
    }

    public void setIdTipoLog(final String idTipoLog) {
        this.idTipoLog = idTipoLog;
    }

    public void setIdUsuario(final long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setIdUsuarioAtendido(final long idUsuarioAtendido) {
        this.idUsuarioAtendido = idUsuarioAtendido;
    }

    public void setNombreProceso(final String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    @Override
    public String toString() {
        return "LogRegistro [estadoProceso=" + estadoProceso + ", fechaSistemaLog="
                + fechaSistemaLog + ", idEmpresa=" + idEmpresa + ", idModulo=" + idModulo
                + ", idProceso=" + idProceso + ", idTipoLog=" + idTipoLog + ", idUsuario="
                + idUsuario + ", idUsuarioAtendido=" + idUsuarioAtendido + ", nombreProceso="
                + nombreProceso + "]";
    }

}
