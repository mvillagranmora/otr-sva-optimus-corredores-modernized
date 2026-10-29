package cl.bcs.normativa.cartolas.entidades;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cl.bcs.normativa.cartolas.procesos.JasperUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

public class Plantilla implements Serializable {

    public static final class Builder {

        public static Plantilla newInstance(
                final long idEmpresa,
                final String nombre,
                final Date fecha,
                final boolean vigente) {
            final Plantilla plantilla = new Plantilla();
            plantilla.setIdEmpresa(idEmpresa);
            plantilla.setNombre(nombre);
            plantilla.setVigenteDesde(fecha);
            plantilla.setVigente(vigente);
            return plantilla;
        }

        public static Plantilla newInstance(
                final ResultSet rs)
                        throws SQLException {
            final Plantilla plantilla = new Plantilla();
            plantilla.setId(rs.getLong("IdPlantilla"));
            plantilla.setIdEmpresa(rs.getLong("IdEmpresa"));
            plantilla.setNombre(rs.getString("Nombre"));
            plantilla.setVigenteDesde(rs.getDate("VigenteDesde"));
            plantilla.setVigente(rs.getString("Vigente").equals(Opcion.SI));
            return plantilla;
        }

        private Builder() {
        }

    }

    private static final long serialVersionUID = 1L;

    public static Plantilla newInstance(
            final long idEmpresa,
            final String nombre,
            final Date fecha,
            final boolean vigente) {
        return Builder.newInstance(idEmpresa, nombre, fecha, vigente);
    }

    public static Plantilla newInstance(
            final ResultSet rs)
                    throws SQLException {
        return Builder.newInstance(rs);
    }

    private long id;
    private long idEmpresa;
    private final Object mutexReporte = new Object();
    private String nombre;
    private List<Recurso> recursos;
    private final Map<String, JasperReport> reports = new ConcurrentHashMap();
    private boolean vigente;
    private Date vigenteDesde;

    public Recurso buscarRecurso(
            final String nombre,
            final String tipo) {
        for (final Recurso recurso : recursos) {
            if (recurso.getTipo().compareTo(tipo) == 0
                    && recurso.getNombre().compareTo(nombre) == 0) {
                return recurso;
            }
        }

        final String msg = String.format("No existe recurso: tipo=%s, nombre=%s", tipo, nombre);
        throw new RecursoFaltanteException(msg);
    }

    public long getId() {
        return id;
    }

    public long getIdEmpresa() {
        return idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public JasperReport getReporte(
            final String nombre)
                    throws JRException,
                    IOException,
                    SQLException {
        synchronized (mutexReporte) {
            if (!reports.containsKey(nombre)) {
                reports.put(nombre, loadJasperReport(nombre));
            }

            return reports.get(nombre);
        }
    }

    public Date getVigenteDesde() {
        return vigenteDesde;
    }

    public boolean isVigente() {
        return vigente;
    }

    public JasperReport loadJasperReport(
            final String nombre)
                    throws JRException,
                    IOException,
                    SQLException {
        final Recurso recurso = buscarRecurso(nombre, TipoRecurso.REPORTE_EXE);
        return JasperUtils.loadJasperReport(recurso.getValor());
    }

    public Map<String, Object> newParametros()
            throws IOException {
        final Map<String, Object> map = new ConcurrentHashMap();
        for (final Recurso recurso : recursos) {
            if (recurso.getNombre() != null && recurso.getValor() != null) {
                final Object obj = recurso.getValorAsObject();
                if (obj != null) {
                    map.put(recurso.getNombre(), obj);
                }
            }
        }

        return map;
    }

    public void setId(
            final long id) {
        this.id = id;
    }

    public void setIdEmpresa(
            final long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setNombre(
            final String nombre) {
        this.nombre = nombre;
    }

    public void setRecursos(
            final List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public void setVigente(
            boolean vigente) {
        this.vigente = vigente;
    }

    public void setVigenteDesde(
            final Date vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    @Override
    public String toString() {
        return "Plantilla [id=" + id + ", nombre=" + nombre + ", vigenteDesde=" + vigenteDesde
                + "]";
    }

}
