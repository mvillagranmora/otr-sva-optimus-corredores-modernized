package cl.bcs.normativa.validacion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.dal.RecordNotFoundException;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.normativa.interfaces.CambiarEstadoDJValidacion;
import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.utils.PropertiesUtil;

/**
 * Implementaci&oacute;n de la validaci&oacute;n de datos de un documento.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoValidacion implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoValidacion.class.getName());

    /**
     * Obtiene instancia de {@link ProcesoValidacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param map
     *            {@link Map} de par&aacute;metros proporcionado por el Job
     *            Manager.
     * @param idProceso
     *            El id del proceso.
     * @return Instancia de {@link ProcesoValidacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoValidacion newInstance(final Connection conn,
            final Map<String, String> map, final long idProceso, final DataSource ds) throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        return newInstance(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ProcesoValidacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoValidacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static ProcesoValidacion newInstance(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        return new ProcesoValidacion(conn, props, idProceso, ds);
    }

    /**
     * Contexto de validaci&oacute;n.
     */
    private final ContextoValidacion contexto;

    /**
     * Resultado de la validaci&oacute;n.
     */
    private final ResultadoValidacion resultado;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoValidacion(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        this.contexto = new ContextoValidacion(conn, props, idProceso, ds);
        this.resultado = new ResultadoValidacion(contexto);
    }

    /**
     * Obtiene y asigna los m&eacute;todos de validaci&oacute;n de cada regla,
     * para efectos de que est&eacute;n disponibles durante el proceso.
     */
    private void obtenerMetodosValidacion() {
        for (final Validacion validacion : contexto.getValidaciones()) {
            validacion.setMetodo(ValidadorBuilder.getMetodoValidacion(validacion));
        }
    }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de validacion."));

        try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
            try {
                resultado.limpiar();
                validar();
                resultado.registrar();
            } catch (IOException | SQLException ex) {
                final String msg = "Error durante proceso de validacion";
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProcesoException(msg, ex);
            }
        }
    }

    /**
     * Implementa el proceso de validaci&oacute;n.
     *
     * @throws SQLException
     *             En caso de error SQL.
     * @throws ProcesoException
     *             En caso de error de proceso.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void validar() throws SQLException, ProcesoException, IOException {

        final Esquema esquema = contexto.getEsquema();
        final Validador validador = ValidadorBuilder.newInstance(contexto);
        if (validador == null) {
            throw new ProcesoException(
                    "No esta implementado el validador para " + esquema.getNombre());
        }

        obtenerMetodosValidacion();

        final String schema = contexto.getSchema();
        //obtener nombre de la vista desde la tabla de configuracion, si no existe usar valor por defecto.
        final String nombreCodigoParametro = "Validacion"+esquema.getNombre();
        String nombreTablaDestino = esquema.getNombreVistaDestino(); 
        try {
        	nombreTablaDestino = ConfiguracionDAO.buscarPorEmpresaMasCodigo(contexto.getConnection(), schema, contexto.getEmpresa().getId(), nombreCodigoParametro);
        }
        catch(RecordNotFoundException e) {    
        	LOGGER.info("Origen de datos para validaciones no esta configurado para esquema "+esquema.getNombre());
        }
        
        final SqlMetadata metadata = contexto.getMetadata();
        final SqlTable tablaDestino = metadata.getTable(schema, nombreTablaDestino);
        try (final Lector lector = LectorBuilder.newLectorDestino(contexto, tablaDestino, null,
                true, null, true)) {
            Registro registro = lector.siguiente();
            while (registro != null) {
                validar(lector.getCantidadRegistrosLeidos(), validador, registro);
                registro = lector.siguiente();
            }

            LOGGER.info(
                    String.format("%d registros validados", lector.getCantidadRegistrosLeidos()));
        }
    }

    /**
     * Valida un registro espec&iacute;fico, registrando el resultado de la
     * validaci&oacute;n en el caso de que el registro no sea v&aacute;lido.
     *
     * @param secuencia
     *            El n&uacute;mero del registro.
     * @param validador
     *            El validador.
     * @param registro
     *            El registro de datos a validar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private void validar(final long secuencia, final Validador validador, final Registro registro)
            throws SQLException {
        LOGGER.info(String.format("Validando registro %d: %s", secuencia, registro));

        for (final Validacion validacion : contexto.getValidaciones()) {
            boolean valido;
            try {
                valido = validador.validar(validacion, registro);
                LOGGER.info(String.format("%s=%s: %s", validacion.getCodigo(), valido,
                        validacion.getFormula()));
            } catch (Exception ex) {
                valido = false;
                validacion.setDescripcion(ex.getMessage());
                LOGGER.log(Level.SEVERE, String.format(String.format("%s", ex.getMessage())));
            }

            if (!valido) {
                resultado.contar(validacion.getCodigo(), validacion.getDescripcion(), registro);
            }
        }
    }

}
