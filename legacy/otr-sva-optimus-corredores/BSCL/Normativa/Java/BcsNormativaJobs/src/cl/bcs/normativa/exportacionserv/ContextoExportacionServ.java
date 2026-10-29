package cl.bcs.normativa.exportacionserv;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import cl.bcs.normativa.procesos.ContextoProceso;

/**
 * Contexto del exportador.
 *
 * @author 
 *
 */
public class ContextoExportacionServ extends ContextoProceso {


	/**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            Las {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @param ds
     *            Objeto DataSource.
     * @throws SQLException
     *             En caso de error SQL.
     */

    public ContextoExportacionServ(final Connection conn, final Properties props, final long idProceso,
    		final DataSource ds)
            throws SQLException {
        super(conn, props, idProceso, ds);
    }
    
}
