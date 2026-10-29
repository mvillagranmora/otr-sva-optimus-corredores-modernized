package cl.bcs.tesoreria.javacall;

import java.sql.Connection;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * The legacy services roll back the connection of the platform transaction explicitly (a simulation, a failed batch). The platform commits when the
 * service returns, so a rollback in the middle discards everything written so far in the same transaction.
 */
final class LiquidarComprobanteTransaccion {

    private LiquidarComprobanteTransaccion() {
    }

    static void rollback(NamedParameterJdbcTemplate jdbc) {
        jdbc.getJdbcOperations().execute((ConnectionCallback<Void>) (Connection con) -> {
            con.rollback();
            return null;
        });
    }
}
