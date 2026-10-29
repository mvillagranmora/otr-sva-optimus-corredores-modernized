package cl.bcs.normativa.cuadratura;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.dal.CampoClasificadorDAO;
import cl.bcs.normativa.dal.ClasificadorDAO;
import cl.bcs.normativa.dal.CuadraturaDAO;
import cl.bcs.normativa.data.DataParser;
import cl.bcs.normativa.data.DataParserException;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.CampoClasificador;
import cl.bcs.normativa.esquema.Clasificador;
import cl.bcs.normativa.esquema.Cuadratura;
import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.PropertiesUtil;

/**
 * Contexto de la cuadratura de documentos.
 *
 * @author fpino@email.com
 *
 */
public class ContextoCuadratura extends ContextoProceso {

    /**
     * Clasificador de la cuadratura.
     */
    private final Clasificador clasificadorCuadratura;

    /**
     * Descripci&oacute;n de la cuadratura.
     */
    private final String descripcionCuadratura;

    /**
     * Fecha de la cuadratura.
     */
    private final Date fechaCuadratura;

    /**
     * Tipo de cuadratura.
     */
    private final String tipoCuadratura;

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
     * @throws DataParserException
     *             En caso de error al interpretar dato del clasificador.
     */
    public ContextoCuadratura(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException, DataParserException {
        super(conn, props, idProceso, ds);

        this.tipoCuadratura = PropertiesUtil.getRequiredString(props, "tipoCuadratura");
        this.descripcionCuadratura = buscarDescripcionCuadratura(conn);
        this.clasificadorCuadratura = buscarClasificadorCuadratura(props);
        this.fechaCuadratura = new Date();
    }

    /**
     * Busca el clasificador para cuadratura relacionado al nombre y
     * correspondiente al documento en proceso.
     *
     * @param props
     *            Las {@link Properties}.
     * @return El clasificador para cuadratura relacionado al nombre y
     *         correspondiente al documento en proceso.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error al interpretar dato del clasificador.
     * @throws ProcesoException
     *             En caso de error de proceso.
     */
    private Clasificador buscarClasificadorCuadratura(final Properties props)
            throws SQLException, DataParserException, ProcesoException {
        final String tipoClasificador = PropertiesUtil.getRequiredString(props, "tipoClasificador");
        if (tipoClasificador == null || tipoClasificador.isEmpty()) {
            return null;
        }

        final Clasificador clasificador = ClasificadorDAO.buscarPorDocumentoMasTipo(getConnection(),
                getSchema(), getDocumento(), tipoClasificador);
        final List<CampoClasificador> camposClasificador = CampoClasificadorDAO
                .buscarPorDocumentoMasTipo(getConnection(), getSchema(), getDocumento(),
                        tipoClasificador);

        final DataParser parser = new DataParser();
        for (final CampoClasificador campoClasificador : camposClasificador) {
            final String nombreCampo = campoClasificador.getNombreCampo();
            final Campo campo = getEsquema().buscarCampoPorNombre(nombreCampo);
            final String valor = campoClasificador.getValorClasificador();
            final Comparable<?> cmp = parser.parse(campo, valor);
            campoClasificador.setValorAsComparable(cmp);
        }

        clasificador.setCamposClasificador(camposClasificador);

        return clasificador;
    }

    /**
     * Busca la descripci&oacute;n de la cuadratura.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @return La descripci&oacute;n de la cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private String buscarDescripcionCuadratura(final Connection conn) throws SQLException {
        if ("INDIVIDUAL".equals(tipoCuadratura)) {
            return "Registro a Registro";
        }

        final Cuadratura cuadratura = CuadraturaDAO.buscarPorDocumentoMasTipo(conn, getSchema(),
                getDocumento(), tipoCuadratura);
        return cuadratura.getDescripcion();
    }

    /**
     * Obtiene el clasificador de la cuadratura.
     *
     * @return El clasificador de la cuadratura.
     */
    public Clasificador getClasificadorCuadratura() {
        return clasificadorCuadratura;
    }

    /**
     * Obtiene la descripci&oacute;n cuadratura.
     *
     * @return La cuadratura.
     */
    public String getDescripcionCuadratura() {
        return descripcionCuadratura;
    }

    /**
     * Obtiene la fecha de la cuadratura.
     *
     * @return La fecha de la cuadratura.
     */
    public Date getFechaCuadratura() {
        return fechaCuadratura;
    }

    /**
     * Obtiene el tipo de clasificador.
     *
     * @return El tipo de clasificador o, <code>null</code> si el clasificador
     *         no existe.
     */
    public String getTipoClasificador() {
        if (clasificadorCuadratura == null) {
            return null;
        }

        return clasificadorCuadratura.getTipo();
    }

    /**
     * Obtiene el tipo de cuadratura.
     *
     * @return El tipo de cuadratura.
     */
    public String getTipoCuadratura() {
        return tipoCuadratura;
    }

}
