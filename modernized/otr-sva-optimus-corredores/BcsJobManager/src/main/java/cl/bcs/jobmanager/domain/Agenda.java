package cl.bcs.jobmanager.domain;

/**
 * Domain entity representing a scheduled agenda (diary) entry in BAT.Agenda.
 * Migrated from legacy cl.bcs.scheduler.pojo.Agenda.
 */
public class Agenda {

    private int idAgenda;
    private String idTipoPrograma;
    private String esDemonio;
    private String habilitado;
    private String nombreClasePrograma;
    private String tipoClasePrograma;
    private String siguienteFechaEjecucion;
    private String ultimaFechaEjecucion;
    private String diaDeMes;
    private String mes;
    private String hora;
    private String minuto;
    private String soloDiasHabiles;
    private String soloFinDeMes;

    public Agenda() {}

    public int getIdAgenda() { return idAgenda; }
    public void setIdAgenda(int idAgenda) { this.idAgenda = idAgenda; }

    public String getIdTipoPrograma() { return idTipoPrograma; }
    public void setIdTipoPrograma(String idTipoPrograma) { this.idTipoPrograma = idTipoPrograma; }

    public String getEsDemonio() { return esDemonio; }
    public void setEsDemonio(String esDemonio) { this.esDemonio = esDemonio; }

    public String getHabilitado() { return habilitado; }
    public void setHabilitado(String habilitado) { this.habilitado = habilitado; }

    public String getNombreClasePrograma() { return nombreClasePrograma; }
    public void setNombreClasePrograma(String nombreClasePrograma) { this.nombreClasePrograma = nombreClasePrograma; }

    public String getTipoClasePrograma() { return tipoClasePrograma; }
    public void setTipoClasePrograma(String tipoClasePrograma) { this.tipoClasePrograma = tipoClasePrograma; }

    public String getSiguienteFechaEjecucion() { return siguienteFechaEjecucion; }
    public void setSiguienteFechaEjecucion(String siguienteFechaEjecucion) { this.siguienteFechaEjecucion = siguienteFechaEjecucion; }

    public String getUltimaFechaEjecucion() { return ultimaFechaEjecucion; }
    public void setUltimaFechaEjecucion(String ultimaFechaEjecucion) { this.ultimaFechaEjecucion = ultimaFechaEjecucion; }

    public String getDiaDeMes() { return diaDeMes; }
    public void setDiaDeMes(String diaDeMes) { this.diaDeMes = diaDeMes; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getMinuto() { return minuto; }
    public void setMinuto(String minuto) { this.minuto = minuto; }

    public String getSoloDiasHabiles() { return soloDiasHabiles; }
    public void setSoloDiasHabiles(String soloDiasHabiles) { this.soloDiasHabiles = soloDiasHabiles; }

    public String getSoloFinDeMes() { return soloFinDeMes; }
    public void setSoloFinDeMes(String soloFinDeMes) { this.soloFinDeMes = soloFinDeMes; }
}
