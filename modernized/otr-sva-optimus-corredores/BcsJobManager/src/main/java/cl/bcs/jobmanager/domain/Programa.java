package cl.bcs.jobmanager.domain;

/**
 * Domain entity representing a queued program (job instance) in BAT.Programa.
 * Migrated from legacy cl.bcs.scheduler.pojo.Programa.
 */
public class Programa {

    private long idPrograma;
    private String idEstado;
    private String idTipoPrograma;
    private int intento;
    private int maximoIntentos;
    private String nombreClasePrograma;
    private String tipoClasePrograma;
    private String minuto;
    private String fechaCreacion;
    private String fechaEjecucion;
    private String fechaFin;
    private String resultado;
    private int idAgenda;
    /** BAT.Agenda.EsDemonio = 'S' for the program's agenda (RULE-061: daemons are not dispatched). */
    private boolean demonio;

    public Programa() {}

    public boolean isDemonio() { return demonio; }
    public void setDemonio(boolean demonio) { this.demonio = demonio; }

    public long getIdPrograma() { return idPrograma; }
    public void setIdPrograma(long idPrograma) { this.idPrograma = idPrograma; }

    public String getIdEstado() { return idEstado; }
    public void setIdEstado(String idEstado) { this.idEstado = idEstado; }

    public String getIdTipoPrograma() { return idTipoPrograma; }
    public void setIdTipoPrograma(String idTipoPrograma) { this.idTipoPrograma = idTipoPrograma; }

    public int getIntento() { return intento; }
    public void setIntento(int intento) { this.intento = intento; }

    public int getMaximoIntentos() { return maximoIntentos; }
    public void setMaximoIntentos(int maximoIntentos) { this.maximoIntentos = maximoIntentos; }

    public String getNombreClasePrograma() { return nombreClasePrograma; }
    public void setNombreClasePrograma(String nombreClasePrograma) { this.nombreClasePrograma = nombreClasePrograma; }

    public String getTipoClasePrograma() { return tipoClasePrograma; }
    public void setTipoClasePrograma(String tipoClasePrograma) { this.tipoClasePrograma = tipoClasePrograma; }

    public String getMinuto() { return minuto; }
    public void setMinuto(String minuto) { this.minuto = minuto; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(String fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public int getIdAgenda() { return idAgenda; }
    public void setIdAgenda(int idAgenda) { this.idAgenda = idAgenda; }
}
