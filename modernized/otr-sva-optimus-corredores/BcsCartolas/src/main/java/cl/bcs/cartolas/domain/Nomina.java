package cl.bcs.cartolas.domain;

import java.time.LocalDate;

/**
 * Entidad Nómina — agrupador de cartolas a generar para un período.
 *
 * <p>Equivalente a {@code cl.bcs.normativa.cartolas.entidades.Nomina} del legacy.</p>
 */
public class Nomina {

    private long      id;
    private long      idEmpresa;
    private long      idUsuario;
    private long      idPlantilla;
    private String    periodo;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String    comentario;
    private String    certificada       = Opcion.NO;
    private LocalDate fechaCertificacion;
    private long      idUsuarioCertificador;
    private String    nombreUsuarioCertificador;
    private String    notaCertificacion;
    private String    tipoProceso       = TipoProceso.NORMAL;
    private String    estadoProceso     = EstadoProceso.INGRESADO;
    private LocalDate fechaProgramadaProceso;
    private LocalDate fechaInicioProceso;
    private LocalDate fechaTerminoProceso;
    private String    mensajeProceso;

    // -------------------------------------------------------------------------
    // Computed helpers (pure logic)
    // -------------------------------------------------------------------------

    /**
     * Retorna el primer día del período si fechaDesde es null.
     */
    public static LocalDate startOfMonth(final String periodo) {
        if (periodo == null) {
            return null;
        }
        return LocalDate.parse(periodo + "-01");
    }

    /**
     * Retorna el último día del período si fechaHasta es null.
     */
    public static LocalDate endOfMonth(final String periodo) {
        if (periodo == null) {
            return null;
        }
        final LocalDate start = LocalDate.parse(periodo + "-01");
        return start.withDayOfMonth(start.lengthOfMonth());
    }

    public LocalDate getFechaDesdeEfectiva() {
        return fechaDesde != null ? fechaDesde : startOfMonth(periodo);
    }

    public LocalDate getFechaHastaEfectiva() {
        return fechaHasta != null ? fechaHasta : endOfMonth(periodo);
    }

    public boolean isCertificada() {
        return Opcion.SI.equals(certificada);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public long getId()                          { return id; }
    public void setId(final long id)              { this.id = id; }

    public long getIdEmpresa()                   { return idEmpresa; }
    public void setIdEmpresa(final long v)        { this.idEmpresa = v; }

    public long getIdUsuario()                   { return idUsuario; }
    public void setIdUsuario(final long v)        { this.idUsuario = v; }

    public long getIdPlantilla()                 { return idPlantilla; }
    public void setIdPlantilla(final long v)      { this.idPlantilla = v; }

    public String getPeriodo()                   { return periodo; }
    public void setPeriodo(final String v)        { this.periodo = v; }

    public LocalDate getFechaDesde()             { return fechaDesde; }
    public void setFechaDesde(final LocalDate v)  { this.fechaDesde = v; }

    public LocalDate getFechaHasta()             { return fechaHasta; }
    public void setFechaHasta(final LocalDate v)  { this.fechaHasta = v; }

    public String getComentario()                { return comentario; }
    public void setComentario(final String v)     { this.comentario = v; }

    public String getCertificada()               { return certificada; }
    public void setCertificada(final String v)    { this.certificada = v; }

    public LocalDate getFechaCertificacion()     { return fechaCertificacion; }
    public void setFechaCertificacion(final LocalDate v) { this.fechaCertificacion = v; }

    public long getIdUsuarioCertificador()       { return idUsuarioCertificador; }
    public void setIdUsuarioCertificador(final long v)   { this.idUsuarioCertificador = v; }

    public String getNombreUsuarioCertificador() { return nombreUsuarioCertificador; }
    public void setNombreUsuarioCertificador(final String v) { this.nombreUsuarioCertificador = v; }

    public String getNotaCertificacion()         { return notaCertificacion; }
    public void setNotaCertificacion(final String v)     { this.notaCertificacion = v; }

    public String getTipoProceso()               { return tipoProceso; }
    public void setTipoProceso(final String v)    { this.tipoProceso = v; }

    public String getEstadoProceso()             { return estadoProceso; }
    public void setEstadoProceso(final String v)  { this.estadoProceso = v; }

    public LocalDate getFechaProgramadaProceso() { return fechaProgramadaProceso; }
    public void setFechaProgramadaProceso(final LocalDate v) { this.fechaProgramadaProceso = v; }

    public LocalDate getFechaInicioProceso()     { return fechaInicioProceso; }
    public void setFechaInicioProceso(final LocalDate v)     { this.fechaInicioProceso = v; }

    public LocalDate getFechaTerminoProceso()    { return fechaTerminoProceso; }
    public void setFechaTerminoProceso(final LocalDate v)    { this.fechaTerminoProceso = v; }

    public String getMensajeProceso()            { return mensajeProceso; }
    public void setMensajeProceso(final String v) { this.mensajeProceso = v; }
}
