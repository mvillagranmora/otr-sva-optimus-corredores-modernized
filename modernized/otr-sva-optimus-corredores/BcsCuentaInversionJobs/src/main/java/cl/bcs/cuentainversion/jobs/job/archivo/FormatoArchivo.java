package cl.bcs.cuentainversion.jobs.job.archivo;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Format of a generated file, read from {@code ARC.ConsultarFormatoArchivo} and its detail (legacy
 * {@code generarArchivo/FormatoArchivo.java}). Bug for bug: a failure while loading (no format, no detail, a SQL error) is only logged and the
 * object stays partly filled, so {@link #getCantidadDeTiposDeRegistro()} is {@code null} (CIJ-DEF-08); the format SQL is built with
 * {@code String.format} (the legacy injection shape, kept: the code comes from the job parameter).
 */
public final class FormatoArchivo {

    private static final Logger log = LoggerFactory.getLogger(FormatoArchivo.class);

    private Long idEmpresa;
    private Long idFormatoArchivo;
    private String formatoSalida;
    private String descripcion;
    private String servicio;
    private String rutaModulo;
    private String codigoFormatoArchivo;
    private String separadorCampos;
    private String separadorDecimales;
    private String rutaCompleta;
    private Integer cantidadDeTiposDeRegistro;
    private String nombreArchivoSalida;
    private String rellenarCampo;
    private String agregoNombreColumna;

    private final List<FormatoArchivoDetalle> formatoArchivoDetalle = new ArrayList<>();

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public String getRutaModulo() {
        return rutaModulo;
    }

    public String getSeparadorCampos() {
        return separadorCampos;
    }

    public String getSeparadorDecimales() {
        return separadorDecimales;
    }

    public String getFormatoSalida() {
        return formatoSalida;
    }

    public String getServicio() {
        return servicio;
    }

    public String getRutaCompleta() {
        return rutaCompleta;
    }

    public String getNombreArchivoSalida() {
        return nombreArchivoSalida;
    }

    public String getRellenarCampo() {
        return rellenarCampo;
    }

    public Integer getCantidadDeTiposDeRegistro() {
        return cantidadDeTiposDeRegistro;
    }

    public String getAgregoNombreColumna() {
        return agregoNombreColumna;
    }

    List<FormatoArchivoDetalle> detalle() {
        return formatoArchivoDetalle;
    }

    /** Legacy constructor (FormatoArchivo.java:103-148): the last matching row wins; nothing escapes. */
    public FormatoArchivo(JdbcTemplate jdbc, Long idEmpresa, String codigoFormatoArchivo) {
        int[] cantidadRegistros = {0};
        try {
            String query = String.format("SELECT \"IdFormatoArchivo\",\"FormatoSalida\",\"Servicio\",\"CodigoFormatoArchivo\",\"SeparadorCampos\","
                    + "\"Descripcion\",\"RutaModulo\",\"RutaCompleta\",\"NombreArchivoSalida\",\"CantidadDeTiposDeRegistro\",\"RellenarCampo\","
                    + "\"SeparadorDecimales\",\"AgregoNombreColumna\" FROM BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivo\" "
                    + "WHERE \"IdEmpresa\" = %s AND \"CodigoFormatoArchivo\" = '%s' AND \"IdEstado\" = 'ACT' ", idEmpresa, codigoFormatoArchivo);
            ResultSetExtractor<Object> lector = rs -> {
                while (rs.next()) {
                    cantidadRegistros[0]++;
                    this.idFormatoArchivo = rs.getLong("IdFormatoArchivo");
                    this.descripcion = rs.getString("Descripcion");
                    this.formatoSalida = rs.getString("FormatoSalida");
                    this.servicio = rs.getString("Servicio");
                    this.rutaModulo = rs.getString("RutaModulo");
                    this.separadorCampos = rs.getString("SeparadorCampos");
                    this.separadorDecimales = rs.getString("SeparadorDecimales");
                    this.rellenarCampo = rs.getString("RellenarCampo");
                    this.rutaCompleta = rs.getString("RutaCompleta");
                    this.nombreArchivoSalida = rs.getString("NombreArchivoSalida");
                    this.cantidadDeTiposDeRegistro = rs.getInt("CantidadDeTiposDeRegistro");
                    this.codigoFormatoArchivo = codigoFormatoArchivo;
                    this.agregoNombreColumna = rs.getString("AgregoNombreColumna");
                    this.idEmpresa = idEmpresa;
                }
                return null;
            };
            jdbc.query(query, lector);
            if (cantidadRegistros[0] == 0) {
                throw new Exception("NO EXISTE FORMATO PARA EL ARCHIVO A CREAR");
            }
            cargarFormatoArchivoDetalle(jdbc);
        } catch (Exception e) {
            log.error("FormatoArchivo: {}", e.toString(), e);
        }
    }

    /** Legacy {@code cargarFormatoArchivoDetalle} (:156-196). */
    private void cargarFormatoArchivoDetalle(JdbcTemplate jdbc) {
        int[] cantidadRegistros = {0};
        try {
            String query = String.format("SELECT \"IdFormatoArchivo\", \"IdFormatoArchivoDetalle\",\"TipoRegistro\", \"Campo\", \"TipoDato\", \"Largo\", "
                    + "\"Decimales\", \"ValorDefecto\", \"Orden\", \"DescripcionFormatoDetalle\",\"Formato\" "
                    + "FROM BSCL.\"BSCL.CuentaInversion.BD.Vistas::ARC.ConsultarFormatoArchivoDetalle\" WHERE \"IdFormatoArchivo\" = %s ", idFormatoArchivo);
            ResultSetExtractor<Object> lector = rs -> {
                while (rs.next()) {
                    cantidadRegistros[0]++;
                    formatoArchivoDetalle.add(fila(rs));
                }
                return null;
            };
            jdbc.query(query, lector);
            if (cantidadRegistros[0] == 0) {
                throw new Exception("NO EXISTE FORMATO DETALLE PARA EL ARCHIVO A CREAR");
            }
        } catch (Exception e) {
            log.error("FormatoArchivo detalle: {}", e.toString(), e);
        }
    }

    private static FormatoArchivoDetalle fila(ResultSet rs) throws java.sql.SQLException {
        FormatoArchivoDetalle item = new FormatoArchivoDetalle();
        item.setIdFormatoArchivoDetalle(rs.getLong("IdFormatoArchivoDetalle"));
        item.setTipoRegistro(rs.getInt("TipoRegistro"));
        item.setCampo(rs.getString("Campo"));
        item.setTipoDato(rs.getString("TipoDato"));
        item.setLargo(rs.getInt("Largo"));
        item.setDecimales(rs.getInt("Decimales"));
        item.setValorDefecto(rs.getString("ValorDefecto"));
        item.setOrden(rs.getInt("Orden"));
        item.setDescripcion(rs.getString("DescripcionFormatoDetalle"));
        item.setFormato(rs.getString("Formato"));
        return item;
    }

    /** Legacy {@code formatear} (:242-354): see {@link FormateadorCampo}. */
    public String formatear(int tipoRegistro, String nombreCampo, String valor) {
        return FormateadorCampo.formatear(this, tipoRegistro, nombreCampo, valor);
    }
}
