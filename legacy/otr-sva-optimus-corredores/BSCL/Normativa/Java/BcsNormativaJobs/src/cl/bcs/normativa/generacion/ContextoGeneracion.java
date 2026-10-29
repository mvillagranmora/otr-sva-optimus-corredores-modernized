package cl.bcs.normativa.generacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.dal.CodigoCorredoraDAO;
import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.utils.PropertiesUtil;

/**
 * Contexto del generador.
 *
 * @author fpino@email.com
 *
 */
public class ContextoGeneracion extends ContextoProceso {
    /**
     * Ruta del archivo generado.
     */
    private String rutaArchivoGenerado;

    /**
     * C&oacute;digo de la corredora (seg&uacute;n BCS).
     */
    private final int codigoCorredora;

    /**
     * Modo de presentaci&oacute;n.
     */
    private final ModoPresentacion modoPresentacion;

    /**
     * N&uacute;mero de la presentaci&oacute;n.
     */
    private final int numeroPresentacion;

    /**
     * C&oacute;digo de empresa al declarar por Internet.
     */
    private final int codigoEmpresaInternet;

    /**
     * C&oacute;digo de empresa al declarar por medio magn&eacute;tico.
     */
    private final int codigoEmpresaMagnetico;

    /**
     * Documento original o rectificado para DJ's Tributarios
     */
    private final String rectificador;


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
    public ContextoGeneracion(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        super(conn, props, idProceso, ds);
        this.codigoCorredora = buscarCodigoCorredora();
        this.modoPresentacion = buscarModoPresentacion(props);
        this.numeroPresentacion = PropertiesUtil.getRequiredInt(props, "numeroPresentacion");
        this.rectificador = props.getProperty("rectificador");
        this.codigoEmpresaInternet = Integer.valueOf(ConfiguracionDAO.buscarPorEmpresaMasCodigo(
                conn, getSchema(), getEmpresa().getId(), "CodigoEmpresaDJInternet"));
        this.codigoEmpresaMagnetico = Integer.valueOf(ConfiguracionDAO.buscarPorEmpresaMasCodigo(
                conn, getSchema(), getEmpresa().getId(), "CodigoEmpresaDJMagnetico"));
        this.rutaArchivoGenerado = buscarRutaArchivo("generacion");
    }

    /**
     * Busca el c&oacute;digo de la corredora.
     *
     * @return El c&oacute;digo de la corredora.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private int buscarCodigoCorredora() throws SQLException {
        final String codigo = CodigoCorredoraDAO.buscarCodigoCorredora(getConnection(), getSchema(),
                getEmpresa().getId(), "BCS");
        return Integer.valueOf(codigo);
    }

    /**
     * Busca el modo de presentaci&oacute;n desde las {@link Properties}.
     *
     * @param props
     *            Las {@link Properties}
     * @return El modo de presentaci&oacute;n.
     */
    private ModoPresentacion buscarModoPresentacion(final Properties props) {
        final String mp = PropertiesUtil.getRequiredString(props, "modoPresentacion");
        return ModoPresentacion.valueOf(mp);
    }

    /**
     * Obtiene el a&ntilde;o tributario.
     *
     * @return El a&ntilde;o tributario.
     */
    public long getAnoTributario() {
        return Long.valueOf((getEsquema().getVersion()).substring(0,4));
    }

    /**
     * Obtiene el mes tributario.
     *
     * @return El mes tributario.
     */
    public long getMesTributario() {
        return Long.valueOf((getEsquema().getVersion()).substring(5,7));
    }
    
    /**
     * Obtiene el c&oacute;digo de la empresa.
     *
     * @return El c&oacute;digo de la empresa.
     */
    public int getCodigoEmpresa() {
        if (getModoPresentacion() == ModoPresentacion.INTERNET) {
            return codigoEmpresaInternet;
        } else {
            return codigoEmpresaMagnetico;
        }
    }

    /**
     * Obtiene el c&oacute;digo de presentaci&oacute;n anterior.
     *
     * @return El c&oacute;digo de presentaci&oacute;n anterior.
     */
    public String getCodigoModoPresentacionAnterior() {
        return getTipoDeclaracion() == TipoDeclaracion.ORIGINAL ? " "
                : getModoPresentacion().getCodigo();
    }

    /**
     * Obtiene la comuna a informar en la declaraci&oacute;n.
     *
     * @return La comuna a informar en la declaraci&oacute;n.
     */
    public String getComunaDeclaracion() {
        return getDocumento().getComunaDeclaracion();
    }

    /**
     * Obtiene la direcci&oacute;n a presentar en la declaraci&oacute;n.
     *
     * @return La direcci&oacute;n a presentar en la declaraci&oacute;n.
     */
    public String getDireccionDeclaracion() {
        return getDocumento().getDireccionDeclaracion();
    }

    /**
     * Obtiene el email a informar en la declaraci&oacute;n.
     *
     * @return El email a informar en la declaraci&oacute;n.
     */
    public String getEmailDeclaracion() {
        return getDocumento().getEmailDeclaracion();
    }

    /**
     * Obtiene la fecha de presentaci&oacute;n.
     *
     * @return La fecha de presentaci&oacute;n.
     */
    public String getFechaPresentacion() {
        if (getTipoDeclaracion() == TipoDeclaracion.ORIGINAL) {
            return "00   0000";
        } else {
            final DateFormat df = new SimpleDateFormat("ddMMMyyyy", new Locale("es"));
            final String ret = df.format(new Date());
            return ret.toUpperCase(new Locale("es"));
        }
    }

    /**
     * Obtiene el folio de la declaraci&oacute;n.
     *
     * @return El folio de la declaraci&oacute;n.
     */
    public long getFolioDeclaracion() {
        final String s = String.format("%02d%s%02d", getCodigoEmpresa(),
                getRutDeclarante().substring(1, 4), getNumeroPresentacion());
        return Long.valueOf(s);
    }

    /**
     * Obtiene el folio de la declaraci&oacute;n anterior.
     *
     * @return El folio de la declaraci&oacute;n anterior.
     */
    public long getFolioDeclaracionAnterior() {
        return getTipoDeclaracion() == TipoDeclaracion.ORIGINAL ? 0 : getFolioDeclaracion() - 1;
    }

    /**
     * Obtiene el modo de presentaci&oacute;n.
     *
     * @return El modo de presentaci&oacute;n.
     */
    public ModoPresentacion getModoPresentacion() {
        return modoPresentacion;
    }

    /**
     * Obtiene el n&uacute;mero de cliente.
     *
     * @return El n&uacute;mero de cliente.
     */
    public int getNumeroCliente() {
        return codigoCorredora;
    }

    /**
     * Obtiene el n&uacute;mero de presentaci&oacute;n.
     *
     * @return El n&uacute;mero de presentaci&oacute;n.
     */
    public int getNumeroPresentacion() {
        return numeroPresentacion;
    }

    /**
     * Obtiene el n&uacute;mero de versi&oacute;n asignado por el SII.
     *
     * @return El n&uacute;mero de versi&oacute;n asignado por el SII.
     */
    public int getNumeroVersion() {
        return getEsquema().getCodigoVersionSII();
    }

    /**
     * Obtiene la raz&oacute;n social de la empresa.
     *
     * @return La raz&oacute;n social de la empresa.
     */
    public String getRazonSocial() {
        return getEmpresa().getRazonSocial();
    }

    /**
     * Obtiene la ruta del archivo generado.
     *
     * @return La ruta del archivo generado.
     */
    public String getRutaArchivoGenerado() {
        return rutaArchivoGenerado;
    }
    
    /**
     * Modifica la ruta del archivo generado.
     *
     * @return La ruta del archivo generado modificada.
     */
    public String setRutaArchivoGenerado(String nombre) {
        return this.rutaArchivoGenerado = nombre;
    }

    /**
     * Obtiene el RUT del declarante.
     *
     * @return El RUT del declarante.
     */
    public String getRutDeclarante() {
        return getDocumento().getRutDeclarante();
    }

    /**
     * Obtiene el RUT del declarante anterior.
     *
     * @return El RUT del declarante anterior.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public String getRutDeclaranteAnterior() throws SerializadorException {
        return getTipoDeclaracion() == TipoDeclaracion.ORIGINAL ? "000000000"
                : Serializador.serializarRut(getRutDeclarante());
    }

    /**
     * Obtiene el RUT del representante legal.
     *
     * @return El RUT del representante legal.
     */
    public String getRutRepresentante() {
        return getDocumento().getRutRepresentante();
    }

    /**
     * Obtiene el fono de la declaraci&oacute;n.
     *
     * @return El fono de la declaraci&oacute;n.
     */
    public long getTelefonoDeclaracion() {
        return Long.valueOf(getDocumento().getTelefonoDeclaracion());
    }

    /** Obtiene si es un documento Rectificado S u original N 
     *
     * @return S o N
     */
    public String getRectificador() {
        return rectificador;
    }
    
    /**
     * Obtiene el tipo de declaracion.
     *
     * @return El tipo de declaracion.
     */
    public TipoDeclaracion getTipoDeclaracion() {
    
        if (rectificador == null || rectificador.trim().isEmpty()) {
            return (getNumeroPresentacion() == 1)
                ? TipoDeclaracion.ORIGINAL
                : TipoDeclaracion.RECTIFICATORIA;
        }
        return "N".equalsIgnoreCase(getRectificador())
            ? TipoDeclaracion.ORIGINAL
            : TipoDeclaracion.RECTIFICATORIA;
    }
}
