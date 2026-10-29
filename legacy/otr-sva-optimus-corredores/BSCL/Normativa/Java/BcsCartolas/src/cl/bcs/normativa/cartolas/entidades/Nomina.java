package cl.bcs.normativa.cartolas.entidades;

import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cl.bcs.normativa.cartolas.dal.NominaService;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public class Nomina implements Serializable {

    public static final class Builder {

        public static Nomina newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Nomina nomina = new Nomina();
            nomina.setId(rs.getLong("IdNomina"));
            nomina.setIdEmpresa(rs.getLong("IdEmpresa"));
            nomina.setIdUsuario(rs.getLong("IdUsuario"));
            nomina.setIdPlantilla(rs.getLong("IdPlantilla"));
            nomina.setPeriodo(rs.getString("Periodo"));
            nomina.setFechaDesde(rs.getDate("FechaDesde"));
            nomina.setFechaHasta(rs.getDate("FechaHasta"));
            nomina.setComentario(rs.getString("Comentario"));
            nomina.setCertificada(rs.getString("Certificada"));
            nomina.setFechaCertificacion(rs.getDate("FechaCertificacion"));
            nomina.setIdUsuarioCertificador(rs.getLong("IdUsuarioCertificador"));
            nomina.setNombreUsuarioCertificador(rs.getString("NombreUsuarioCertificador"));
            nomina.setNotaCertificacion(rs.getString("NotaCertificacion"));
            nomina.setTipoProceso(rs.getString("TipoProceso"));
            nomina.setEstadoProceso(rs.getString("EstadoProceso"));
            nomina.setFechaProgramadaProceso(rs.getDate("FechaProgramadaProceso"));
            nomina.setFechaInicioProceso(rs.getDate("FechaInicioProceso"));
            nomina.setFechaTerminoProceso(rs.getDate("FechaTerminoProceso"));
            nomina.setMensajeProceso(rs.getString("MensajeProceso"));
            return nomina;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    private static Date endOfMonth(
            final String periodo)
                    throws ParseException {
        if (periodo == null) {
            return null;
        }

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final java.util.Date d = df.parse(periodo + "-01");
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        final int x = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, x);
        return new Date(cal.getTimeInMillis());
    }

    public static Nomina newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private static Date startOfMonth(
            final String periodo)
                    throws ParseException {
        if (periodo == null) {
            return null;
        }

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final java.util.Date d = df.parse(periodo + "-01");
        return new Date(d.getTime());
    }

    private String certificada;
    private String comentario;
    private String estadoProceso;
    private Date fechaCertificacion;
    private Date fechaDesde;
    private Date fechaHasta;
    private Date fechaInicioProceso;
    private Date fechaProgramadaProceso;
    private Date fechaTerminoProceso;
    private long id;
    private long idEmpresa;
    private long idPlantilla;
    private long idUsuario;
    private long idUsuarioCertificador;
    private String mensajeProceso;
    private String nombreUsuarioCertificador;
    private String notaCertificacion;
    private String periodo;
    private Plantilla plantilla;
    private String tipoProceso;

    public void abrir(
            final ContextoCartolas contexto)
                    throws ParseException {
        if (EstadoProceso.EN_PROCESO.equals(estadoProceso)) {
            throw new NominaException("La nomina esta en proceso: id=" + this.getId());
        }

        try {
            this.setEstadoProceso(EstadoProceso.EN_PROCESO);
            NominaService.save(contexto, this);
        } catch (SQLException ex) {
            throw new NominaException("Error al abrir nomina: id=" + this.getId(), ex);
        }
    }

    public void cerrar(
            final ContextoCartolas contexto)
                    throws ParseException {
        cerrar(contexto, null);
    }

    public void cerrar(
            final ContextoCartolas contexto,
            final Throwable t)
                    throws ParseException {
        try {
            String msg = null;
            String estado = EstadoProceso.TERMINADO;
            if (t != null) {
                estado = EstadoProceso.ERROR;
                if (t.getCause() != null) {
                    msg = t.getCause().getMessage();
                } else {
                    msg = t.getMessage();
                }
            }

            this.setEstadoProceso(estado);
            this.setMensajeProceso(msg);
            NominaService.save(contexto, this);
        } catch (SQLException ex) {
            throw new NominaException("Error al cerrar nomina: id=" + this.getId(), ex);
        }
    }

    public String getCertificada() {
        return certificada;
    }

    public String getComentario() {
        return comentario;
    }

    public String getEstadoProceso() {
        return estadoProceso;
    }

    public Date getFechaCertificacion() {
        return fechaCertificacion;
    }

    public Date getFechaDesde()
            throws ParseException {
        if (fechaDesde == null) {
            return startOfMonth(periodo);
        }

        return fechaDesde;
    }

    public Date getFechaHasta()
            throws ParseException {
        if (fechaHasta == null) {
            return endOfMonth(periodo);
        }

        return fechaHasta;
    }

    public Date getFechaInicioProceso() {
        return fechaInicioProceso;
    }

    public Date getFechaProgramadaProceso() {
        return fechaProgramadaProceso;
    }

    public Date getFechaTerminoProceso() {
        return fechaTerminoProceso;
    }

    public long getId() {
        return id;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public long getIdPlantilla() {
        return idPlantilla;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public long getIdUsuarioCertificador() {
        return idUsuarioCertificador;
    }

    public String getMensajeProceso() {
        return mensajeProceso;
    }

    public String getNombreUsuarioCertificador() {
        return nombreUsuarioCertificador;
    }

    public String getNotaCertificacion() {
        return notaCertificacion;
    }

    public String getPeriodo() {
        return periodo;
    }

    public Plantilla getPlantilla() {
        return plantilla;
    }

    public String getTipoProceso() {
        return tipoProceso;
    }

    public boolean isCertificada() {
        return Opcion.SI.equals(certificada);
    }

    public void setCertificada(
            final String certificada) {
        this.certificada = certificada;
    }

    public void setComentario(
            final String comentario) {
        this.comentario = comentario;
    }

    public void setEstadoProceso(
            final String estadoProceso) {
        this.estadoProceso = estadoProceso;
    }

    public void setFechaCertificacion(
            final Date fechaCertificacion) {
        this.fechaCertificacion = fechaCertificacion;
    }

    public void setFechaDesde(
            final Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public void setFechaHasta(
            final Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public void setFechaInicioProceso(
            final Date fechaInicioProceso) {
        this.fechaInicioProceso = fechaInicioProceso;

    }

    public void setFechaProgramadaProceso(
            final Date fechaProgramadaProceso) {
        this.fechaProgramadaProceso = fechaProgramadaProceso;

    }

    public void setFechaTerminoProceso(
            final Date fechaTerminoProceso) {
        this.fechaTerminoProceso = fechaTerminoProceso;

    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdEmpresa(
            final long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setIdPlantilla(
            final long idPlantilla) {
        this.idPlantilla = idPlantilla;
    }

    public void setIdUsuario(
            final long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setIdUsuarioCertificador(
            final long idUsuarioCertificador) {
        this.idUsuarioCertificador = idUsuarioCertificador;
    }

    public void setMensajeProceso(
            final String mensajeProceso) {
        this.mensajeProceso = mensajeProceso;

    }

    public void setNombreUsuarioCertificador(
            final String nombreUsuarioCertificador) {
        this.nombreUsuarioCertificador = nombreUsuarioCertificador;
    }

    public void setNotaCertificacion(
            final String notaCertificacion) {
        this.notaCertificacion = notaCertificacion;
    }

    public void setPeriodo(
            final String periodo) {
        this.periodo = periodo;
    }

    public void setPlantilla(
            final Plantilla plantilla) {
        this.plantilla = plantilla;
    }

    public void setTipoProceso(
            final String tipoProceso) {
        this.tipoProceso = tipoProceso;
    }

}
