package cl.bcs.tesoreria.jobs.job.archivo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a payment file format (header and fields) read from the views {@code TES ConsultarFormatoArchivo} /
 * {@code ConsultarFormatoArchivoDetalle}. Legacy: {@code cl.bcs.tesoreria.generarArchivoPago.FormatoArchivo} (450 lines) and
 * {@code FormatoArchivoDetalle} (88 lines): lines 13-250 here, the formatter in {@link FormateadorPago}.
 *
 * <p>Legacy defects kept (README): TES-J-DEF-06 the constructor swallows every error (no row, SQL error), so the failure surfaces
 * later as a NullPointerException on {@code CantidadDeTiposDeRegistro}; TES-J-DEF-07 the format SQL is built with
 * {@code String.format} and the request parameters (SQL injection shape, NOT fixed on purpose: bug for bug).
 */
public final class FormatoPago {

    private static final Logger log = LoggerFactory.getLogger(FormatoPago.class);

    /** Legacy FormatoArchivoDetalle: one field of a record type. */
    public static final class Detalle {
        public Long idFormatoArchivoDetalle;
        public Integer tipoRegistro;
        public String campo;
        public String tipoDato;
        public Integer largo;
        public Integer decimales;
        public String valorDefecto;
        public Integer orden;
        public String descripcion;
        public String formato;
        public String codigoFormatoArchivo;
    }

    private Long idEmpresa;
    private Long idNominaTransferencia;
    private Long idFormatoArchivo;
    private String codigoFormatoArchivo;
    private Long idBanco;
    private String nombreBanco;
    private String idMoneda;
    private String idMedioDePago;
    private String formatoSalida;
    private String descripcion;
    private String servicio;
    private String rutaTesoreria;
    private String rutaCompleta;
    private Integer cantidadDeTiposDeRegistro;
    private String nombreArchivoSalida;
    private String separadorCampos;
    private String separadorDecimales;
    private String rellenarCampo;
    private String tipoSaltoLinea;
    private final List<Detalle> formatoArchivoDetalle = new ArrayList<>();
    private final JdbcTemplate jdbc;

    /** :155-200. The query is the legacy text, unmodified (HANA schema prefix included). */
    public FormatoPago(JdbcTemplate jdbc, Long idEmpresa, Long idNominaTransferencia, Long idBanco, String idMoneda,
                       String idMedioDePago) {
        this.jdbc = jdbc;
        try {
            String query = String.format("SELECT \"IdFormatoArchivo\",\"CodigoFormatoArchivo\", \"SeparadorCampos\", \"SeparadorDecimales\",\"RellenarCampo\",\"CodigoFormatoArchivo\",\"NombreBanco\",\"FormatoSalida\",\"Servicio\",\"Descripcion\",\"RutaTesoreria\",\"RutaCompleta\",\"NombreArchivoSalida\",\"CantidadDeTiposDeRegistro\",\"TipoSaltoLinea\" FROM BSCL.\"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo\" WHERE \"IdEmpresa\" = %s AND \"IdBanco\" = %s AND \"IdMoneda\" = '%s' AND \"IdMedioDePago\" = '%s' AND \"IdEstado\" = 'ACT' ", idEmpresa, idBanco, idMoneda, idMedioDePago);
            int cantidadRegistros = jdbc.query(query, rs -> {
                int n = 0;
                while (rs.next()) {
                    n++;
                    this.idFormatoArchivo = rs.getLong("IdFormatoArchivo");
                    this.nombreBanco = rs.getString("NombreBanco");
                    this.descripcion = rs.getString("Descripcion");
                    this.formatoSalida = rs.getString("FormatoSalida");
                    this.servicio = rs.getString("Servicio");
                    this.rutaTesoreria = rs.getString("RutaTesoreria");
                    this.rutaCompleta = rs.getString("RutaCompleta");
                    this.nombreArchivoSalida = rs.getString("NombreArchivoSalida");
                    this.cantidadDeTiposDeRegistro = rs.getInt("CantidadDeTiposDeRegistro");
                    this.codigoFormatoArchivo = rs.getString("CodigoFormatoArchivo");
                    this.separadorCampos = rs.getString("SeparadorCampos");
                    this.separadorDecimales = rs.getString("SeparadorDecimales");
                    this.rellenarCampo = rs.getString("RellenarCampo");
                    this.tipoSaltoLinea = rs.getString("TipoSaltoLinea");
                }
                return n;
            });
            if (cantidadRegistros == 0) {
                throw new Exception("NO EXISTE FORMATO PARA EL ARCHIVO A CREAR");
            }
            cargarFormatoArchivoDetalle();
        } catch (Exception e) {
            log.error("FormatoArchivo: {}", e.toString()); // legacy: printStackTrace, the object stays half loaded
        }
    }

    /** :208-250 */
    private void cargarFormatoArchivoDetalle() {
        try {
            String query = String.format("SELECT \"IdFormatoArchivo\",\"CodigoFormatoArchivo\", \"IdFormatoArchivoDetalle\",\"TipoRegistro\", \"Campo\", \"TipoDato\", \"Largo\", \"Decimales\", \"ValorDefecto\", \"Orden\", \"DescripcionFormatoDetalle\",\"Formato\" FROM BSCL.\"BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle\" WHERE \"IdFormatoArchivo\" = %s ", idFormatoArchivo);
            int cantidadRegistros = jdbc.query(query, rs -> {
                int n = 0;
                while (rs.next()) {
                    n++;
                    Detalle item = new Detalle();
                    item.idFormatoArchivoDetalle = rs.getLong("IdFormatoArchivoDetalle");
                    item.tipoRegistro = rs.getInt("TipoRegistro");
                    item.campo = rs.getString("Campo");
                    item.tipoDato = rs.getString("TipoDato");
                    item.largo = rs.getInt("Largo");
                    item.decimales = rs.getInt("Decimales");
                    item.valorDefecto = rs.getString("ValorDefecto");
                    item.orden = rs.getInt("Orden");
                    item.descripcion = rs.getString("DescripcionFormatoDetalle");
                    item.formato = rs.getString("Formato");
                    item.codigoFormatoArchivo = rs.getString("CodigoFormatoArchivo");
                    formatoArchivoDetalle.add(item);
                }
                return n;
            });
            if (cantidadRegistros == 0) {
                throw new Exception("NO EXISTE FORMATO DETALLE PARA EL ARCHIVO A CREAR");
            }
        } catch (Exception e) {
            log.error("FormatoArchivoDetalle: {}", e.toString());
        }
    }

    /** :296-414, see {@link FormateadorPago}. */
    public String formatear(int tipoRegistro, String nombreCampo, String valor) {
        return FormateadorPago.formatear(this, formatoArchivoDetalle, tipoRegistro, nombreCampo, valor);
    }

    /** :422-444: the nomina row gets the file name; any failure is swallowed (the legacy printed it). */
    public void actualizarTransferencia(String nombreArchivo) {
        try {
            jdbc.update("UPDATE \"BSCL.Tesoreria.BD::TES.NominaTransferencia\" SET \"NombreArchivo\" = ? WHERE \"IdNominaTransferencia\" = ? AND \"IdEmpresa\" = ?",
                    nombreArchivo, (long) this.getIdNominaTransferencia(), (long) this.getIdEmpresa());
        } catch (Exception e) {
            log.error("ERROR AL ACTUALIZAR TRANSFERENCIA: {}", e.toString());
        }
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public Long getIdNominaTransferencia() {
        return idNominaTransferencia;
    }

    public Long getIdFormatoArchivo() {
        return idFormatoArchivo;
    }

    public String getCodigoFormatoArchivo() {
        return codigoFormatoArchivo;
    }

    public String getSeparadorCampos() {
        return separadorCampos;
    }

    public String getSeparadorDecimales() {
        return separadorDecimales;
    }

    public String getRellenarCampo() {
        return rellenarCampo;
    }

    public Long getIdBanco() {
        return idBanco;
    }

    public String getNombreBanco() {
        return nombreBanco;
    }

    public String getIdMoneda() {
        return idMoneda;
    }

    public String getIdMedioDePago() {
        return idMedioDePago;
    }

    public String getFormatoSalida() {
        return formatoSalida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getServicio() {
        return servicio;
    }

    public String getRutaTesoreria() {
        return rutaTesoreria;
    }

    public String getRutaCompleta() {
        return rutaCompleta;
    }

    public String getNombreArchivoSalida() {
        return nombreArchivoSalida;
    }

    public Integer getCantidadDeTiposDeRegistro() {
        return cantidadDeTiposDeRegistro;
    }

    public String getTipoSaltoLinea() {
        return tipoSaltoLinea;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setIdNominaTransferencia(Long idNominaTransferencia) {
        this.idNominaTransferencia = idNominaTransferencia;
    }

    public void setIdBanco(Long idBanco) {
        this.idBanco = idBanco;
    }

    public void setIdMoneda(String idMoneda) {
        this.idMoneda = idMoneda;
    }

    public void setIdMedioDePago(String idMedioDePago) {
        this.idMedioDePago = idMedioDePago;
    }
}
