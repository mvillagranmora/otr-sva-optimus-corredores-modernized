package cl.bcs.sql.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase base para ejecutar sentencias en lote mediante JDBC.
 *
 * @author fpino@email.com
 *
 */
public class SqlBulk {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SqlBulk.class.getName());

    /**
     * Secuencia del lote (se cuenta desde 1 en adelante).
     */
    private int batch;

    /**
     * Cantidad de sentencias por lote.
     */
    private final int batchSize;

    /**
     * Sentencia a ser ejecutada masivamente.
     */
    private final PreparedStatement bulkStatement;

    /**
     * Cantidad registros afectados.
     */
    private int commited;

    /**
     * La conexi&oacute;n a la base de datos.
     */
    protected final Connection conn;

    /**
     * Cantidad de sentencias procesadas.
     */
    private int count;

    /**
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param sql
     *            La sentencia SQL.
     * @param batchSize
     *            Cantidad de sentencias por lote.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public SqlBulk(
            final Connection conn,
            final String sql,
            final int batchSize) throws SQLException {
        this.bulkStatement = conn.prepareStatement(sql);
        this.batchSize = batchSize;
        this.batch = 0;
        this.commited = 0;
        this.conn = conn;
        this.count = 0;
    }

    /**
     * Agrega nuevo registro de datos a ejecutarse por lote.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void addBatch()
            throws SQLException {
        bulkStatement.addBatch();
        count++;

        if (endOfBlock()) {
            executeBatch();
        }
    }

    /**
     * Finaliza ejecuci&oacute;n por lotes.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void endBatch()
            throws SQLException {
        if (!endOfBlock()) {
            executeBatch();
        }

        bulkStatement.close();
    }

    /**
     * Determina si es el fin de un bloque.
     *
     * @return <code>true</code> si es el fin de un bloque, <code>false</code>
     *         en caso contrario.
     */
    private boolean endOfBlock() {
        return count % batchSize == 0;
    }

    /**
     * Ejecuta el lote actual.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    private void executeBatch()
            throws SQLException {
        batch++;
        LOGGER.info(String.format("Ejecucion del lote %d", batch));

        try {
            int successful = 0;
            final int[] result = bulkStatement.executeBatch();

            for (int i = 0; i < result.length; i++) {
                if (result[i] > 0) {
                    successful += result[i];
                } else if (result[i] == Statement.EXECUTE_FAILED) {
                    final String msg = String.format("Fallo al menos la sentencia %d del lote %d",
                            i, batch);
                    throw new SqlBulkException(msg);
                }
            }

            LOGGER.info(String.format("COMMIT del lote %d", batch));

        	conn.commit();
            
            commited += successful;
        } catch (final SQLException ex) {
            LOGGER.log(Level.SEVERE, String.format("ROLLBACK del lote %d", batch), ex);
            conn.rollback();
            throw ex;
        }
    }

    /**
     * Obtiene la sentencia de ejecuci&oacute;n masiva.
     *
     * @return La sentencia de ejecuci&oacute;n masiva.
     */
    public PreparedStatement getBulkStatement() {
        return bulkStatement;
    }

    /**
     * Obtiene la cantidad de registros afectados.
     *
     * @return La cantidad de registros afectados.
     */
    public int getCommited() {
        return commited;
    }

}
