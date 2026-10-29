package cl.bcs.normativa.cartolas.batch;

import java.io.Serializable;

public class BatchParams implements Serializable {

    private static final long serialVersionUID = 1L;

    private String creaParam;
    private long idAgenda;
    private long idEmpresa;
    private String idModulo;
    private String idTipoTarea;
    private long idUsuario;
    private long segundos;

    public String getCreaParam() {
        return creaParam;
    }

    public long getIdAgenda() {
        return idAgenda;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public String getIdModulo() {
        return idModulo;
    }

    public String getIdTipoTarea() {
        return idTipoTarea;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public long getSegundos() {
        return segundos;
    }

    public void setCreaParam(String creaParam) {
        this.creaParam = creaParam;
    }

    public void setIdAgenda(long idAgenda) {
        this.idAgenda = idAgenda;
    }

    public void setIdEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setIdModulo(String idModulo) {
        this.idModulo = idModulo;
    }

    public void setIdTipoTarea(String idTipoTarea) {
        this.idTipoTarea = idTipoTarea;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setSegundos(long segundos) {
        this.segundos = segundos;
    }

    @Override
    public String toString() {
        return "BatchParams{" + "creaParam=" + creaParam + ", idAgenda=" + idAgenda + ", idEmpresa="
                + idEmpresa + ", idModulo=" + idModulo + ", idTipoTarea=" + idTipoTarea
                + ", idUsuario=" + idUsuario + ", segundos=" + segundos + '}';
    }

}
