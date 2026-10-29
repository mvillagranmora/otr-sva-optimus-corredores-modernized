package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.kit.AccesoDatosException;

/**
 * The connection of the CURRENT unit of work (C1): {@code DataSourceUtils} returns the connection bound to the Spring transaction opened by
 * {@code JavaCallUnitOfWorkFilter} (or an in-process caller such as Tesoreria's library joining the same transaction manager), so every port
 * adapter, the legacy-style SQL and any {@code @Transactional} collaborator see the same session. Outside a transaction it is a plain pooled
 * connection, released after the use. No connection is ever stored in a field.
 */
@Component
public class ConexionUnidadDeTrabajo {

    @FunctionalInterface
    public interface Trabajo<T> {
        T hacer(Connection con) throws SQLException;
    }

    private final DataSource dataSource;

    public ConexionUnidadDeTrabajo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Runs the work on the connection of the unit of work. M2: when the Spring transaction has a timeout (see {@code JavaCallUnitOfWorkFilter}) and its
     * deadline has passed, fails BEFORE touching the database with {@code TransactionTimedOutException}; and every statement that {@link SqlNombrado} runs inside
     * the work carries the REMAINING time of the transaction as its query timeout (architecture review F3 W1, H2: a slow query can no longer outlive the deadline).
     */
    public <T> T usar(Trabajo<T> trabajo) {
        return ejecutar(trabajo, true);
    }

    /** Same as {@link #usar} but ignores an expired deadline: ONLY for the release of the anti-duplicate lock (it must run even in a request that timed out). */
    public <T> T usarAunVencido(Trabajo<T> trabajo) {
        return ejecutar(trabajo, false);
    }

    private <T> T ejecutar(Trabajo<T> trabajo, boolean verificarPlazo) {
        Connection con = DataSourceUtils.getConnection(dataSource);
        DataSource anterior = SqlNombrado.plazoDe(verificarPlazo ? dataSource : null);
        try {
            if (verificarPlazo && TransactionSynchronizationManager.getResource(dataSource) instanceof ConnectionHolder holder && holder.hasTimeout()) {
                holder.getTimeToLiveInMillis();   // throws TransactionTimedOutException once the deadline has passed
            }
            return trabajo.hacer(con);
        } catch (SQLException e) {
            // M3 (architecture review F3 W1): the text of a database exception can quote values (HANA does), so only SQLState and ErrorCode are composed
            throw new AccesoDatosException("SQL failure: SQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode(), e);
        } finally {
            SqlNombrado.plazoDe(anterior);
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }
}
