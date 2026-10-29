package cl.bcs.normativa.consolidacli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Contexto de la llamada.
 *
 * @author
 *
 */
// public class ContextoConsolidaCli extends ContextoProceso {
public class ContextoConsolidaCli {

    private transient final Connection connection;

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
    public ContextoConsolidaCli(final Connection conn, final long idProceso) throws SQLException {
        this.connection = conn;

    }

    public Connection getConnection() {
        return connection;
    }

}
