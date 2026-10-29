package cl.bcs.normativa.validacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.dal.ValidacionDAO;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.Validacion;
import cl.bcs.normativa.procesos.ContextoProceso;
import cl.bcs.normativa.procesos.ProcesoException;

/**
 * Contexto del proceso de validaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public class ContextoValidacion extends ContextoProceso {

    /**
     * Fecha de la validaci&oacute;n.
     */
    private final Date fechaValidacion;

    /**
     * Lista de las reglas de validaci&oacute;n a aplicar a los detalles del
     * documento.
     */
    private final List<Validacion> validaciones;

    /**
     * Constructor.
     *
     * @param connection
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ContextoValidacion(final Connection connection, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        super(connection, props, idProceso, ds);

        this.fechaValidacion = new Date();
        final Documento documento = getDocumento();
        this.validaciones = ValidacionDAO.buscarActivasPorDocumento(connection, getSchema(),
                documento);
        if (this.validaciones.isEmpty()) {
            final String msg = String.format(
                    "No hay reglas de validacion especificadas para: %s-%s",
                    documento.getNombreEsquema(), documento.getVersionEsquema());
            throw new ProcesoException(msg);
        }
    }

    /**
     * Obtiene la fecha de validaci&oacute;n.
     *
     * @return la fecha de validaci&oacute;n.
     */
    public Date getFechaValidacion() {
        return fechaValidacion;
    }

    /**
     * Obtiene las reglas de validaci&oacute;n.
     *
     * @return Las reglas de validaci&oacute;n.
     */
    public List<Validacion> getValidaciones() {
        return validaciones;
    }

}
