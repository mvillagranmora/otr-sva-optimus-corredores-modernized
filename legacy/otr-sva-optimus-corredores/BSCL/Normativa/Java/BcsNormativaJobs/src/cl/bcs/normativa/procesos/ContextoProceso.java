package cl.bcs.normativa.procesos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.dal.DocumentoDAO;
import cl.bcs.normativa.dal.EmpresaDAO;
import cl.bcs.normativa.dal.EsquemaDAO;
import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.normativa.dal.ParametroDAO;
import cl.bcs.normativa.dal.UsuarioDAO;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Empresa;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.esquema.RangoFechas;
import cl.bcs.normativa.esquema.Usuario;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.utils.PropertiesUtil;

/**
 * Contexto de los procesos.
 *
 * @author fpino@email.com
 *
 */
public class ContextoProceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ContextoProceso.class.getName());

    /**
     * Conexi&oacute;n a la base de datos.
     */
    private transient final Connection connection;

    /**
     * Documento a procesar.
     */
    private final Documento documento;

    /**
     * Empresa del documento.
     */
    private final Empresa empresa;

    /**
     * Esquema del documento.
     */
    private final Esquema esquema;

    /**
     * Rango de fechas para el filtro.
     */
    private final RangoFechas filtroFechas;

    /**
     * Id del proceso.
     */
    private final long idProceso;
    /**
     * Periodicidad.
     */
    private final String periodicidad;
    /**
     * Fecha del periodo comercial.
     */
    private final String periodoComercial;
    /**
     * Metadata de la base de datos.
     */
    private final SqlMetadata metadata;
    /**
     *  Rectificacion para DJ1
     */
    private final String rectifica;

    /**
     *  Documento original o rectificado
     */
    private final String rectificador;

    /**
     * Schema de la base de datos.
     */
    private final String schema;

    /**
     * Usuario que ejecut&oacute; el proceso.
     */
    private final Usuario usuario;

    private final Properties parametros;

    private transient final DataSource ds;
    
    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ContextoProceso(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {

        this.parametros = props;
        this.connection = conn;
        this.idProceso = idProceso;
        this.periodicidad = props.getProperty("periodicidad");
        this.periodoComercial = props.getProperty("periodoComercial");
        this.rectifica = props.getProperty("rectifica");
        this.rectificador = props.getProperty("rectificador");
        this.schema = PropertiesUtil.getRequiredString(props, "schema");
        this.metadata = new SqlMetadata(conn);
        this.ds = ds;
        final long idUsuario = PropertiesUtil.getRequiredLong(props, "idUsuario");
        this.usuario = UsuarioDAO.buscarPorId(conn, schema, idUsuario);

        final long idDocumento = PropertiesUtil.getRequiredLong(props, "idDocumento");
        this.documento = DocumentoDAO.buscarPorId(conn, schema, idDocumento);
        this.esquema = EsquemaDAO.buscarPorDocumento(conn, schema, documento);
        this.empresa = EmpresaDAO.buscarPorDocumento(conn, schema, documento);
        this.filtroFechas = newFiltroFechas(new RangoFechas(props));
    }

    /**
     * Obtiene la ruta del archivo del tipo solicitado.
     * 
     * @param tipo
     *            El tipo del archivo.
     * @return La ruta del archivo del tipo solicitado.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public String buscarRutaArchivo(final String tipo) throws SQLException {
        return LocalizadorArchivo.buscarRutaArchivo(this.getConnection(), this.getSchema(), tipo,
                this.getDocumento(), this.getIdProceso(), this.getEsquema(), this.getParametros());
    }

    public String buscarRutaReporte() {
        try {
            return Reporte.buscarRutaReporte(this);
        } catch (SQLException ex) {
            final String msg = "Error al obtener ruta del reporte";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

    /**
     * Obtiene la conexi&oacute;n a la base de datos.
     *
     * @return La conexi&oacute;n a la base de datos.
     */
    public Connection getConnection() {
        return connection;
    }
    
    public DataSource getDS() {
        return ds;
    }

    /**
     * Obtiene el documento.
     *
     * @return El documento.
     */
    public Documento getDocumento() {
        return documento;
    }

    /**
     * Obtiene la empresa.
     *
     * @return La empresa.
     */
    public Empresa getEmpresa() {
        return empresa;
    }

    /**
     * Obtiene el esquema.
     *
     * @return El esquema.
     */
    public Esquema getEsquema() {
        return esquema;
    }

    /**
     * Obtiene rango de fechas para el filtro.
     *
     * @return Rango de fechas para el filtro.
     */
    public RangoFechas getFiltroFechas() {
        return filtroFechas;
    }

    /**
     * Obtiene el id del proceso.
     *
     * @return El id del proceso.
     */
    public long getIdProceso() {
        return idProceso;
    }
    
    /**
     * Obtiene la periodicidad.
     *
     * @return El id del proceso.
     */
    public String getPeriodicidad() {
        return periodicidad;
    }
    
    /**
     * Obtiene la fecha del periodo comercial cuando se genera reporte Diario SIID.
     *
     * @return El id del proceso.
     */
    public String getPeriodoComercial() {
        return periodoComercial;
    }
    
    /**
     * Obtiene parametro si Rectifica para generacion de archivo
     *
     * @return S o N.
     */
    public String getRectifica() {
        return rectifica;
    }

    /**
     * Obtiene parametro si Rectifica para generacion de archivo
     *
     * @return S o N.
     */
    public String getRectificador() {
        return rectificador;
    }

    /**
     * Obtiene la metadata.
     *
     * @return La metadata.
     */
    public SqlMetadata getMetadata() {
        return metadata;
    }

    /**
     * Obtiene par&aacute;metro del esquema como valor double, seg&uacute;n el
     * c&oacute;digo dado.
     *
     * @param codigo
     *            El c&oacute;digo.
     * @return Valor double asociado a c&oacute;digo.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public double getParametroEsquemaAsDouble(final String codigo) throws SQLException {
        final String valor = ParametroDAO.buscarValor(getConnection(), getSchema(), getEsquema(),
                codigo);
        final BigDecimal bd = new BigDecimal(valor);
        return bd.doubleValue();
    }

    /**
     * Obtiene los par&aacute;metros de proceso.
     *
     * @return Los par&aacute;metros de proceso.
     */
    public Properties getParametros() {
        return parametros;
    }

    /**
     * Obtiene el schema.
     *
     * @return El schema.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Obtiene el usuario.
     *
     * @return El usuario.
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Obtiene nueva instancia de {@link RangoFechas} para el filtro de datos.
     *
     * @param rangoFechas
     *            El rango de fechas solicitado por el usuario.
     *
     * @return Nueva instancia de {@link RangoFechas} para el filtro de datos.
     */
    public final RangoFechas newFiltroFechas(final RangoFechas rangoFechas) {
        Date fechaDesde = esquema.getRangoFechas().getFechaDesde();
        Date fechaHasta = esquema.getRangoFechas().getFechaHasta();

        if (rangoFechas.getFechaDesde() != null) {
            if (rangoFechas.getFechaDesde().compareTo(fechaDesde) > 0
                    && rangoFechas.getFechaDesde().compareTo(fechaHasta) < 0) {
                fechaDesde = rangoFechas.getFechaDesde();
            }
        }

        if (rangoFechas.getFechaHasta() != null) {
            if (rangoFechas.getFechaHasta().compareTo(fechaDesde) > 0
                    && rangoFechas.getFechaHasta().compareTo(fechaHasta) < 0) {
                fechaHasta = rangoFechas.getFechaHasta();
            }
        }

        return new RangoFechas(fechaDesde, fechaHasta);
    }

}
