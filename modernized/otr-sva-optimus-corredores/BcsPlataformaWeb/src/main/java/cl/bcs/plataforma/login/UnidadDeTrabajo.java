package cl.bcs.plataforma.login;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

/**
 * One database transaction per request (T-13). The legacy servlets took one {@link Connection} from the pool, ran every statement on it and called
 * {@code commit()} once at the end; an exception left the connection to be closed without commit (rollback). This class keeps exactly that: the work
 * runs on ONE connection with autocommit off, a normal return commits (also when the work chose to answer with an error: a wrong password still
 * confirms the attempt counter, as {@code CrearSesion.java:138}), an exception rolls back. The work may call {@code rollback()} itself
 * ({@code ConfirmarCambioClave} does on a procedure error); the commit that follows then has nothing to confirm.
 */
@Component
public class UnidadDeTrabajo {

    /** The work of one request on the connection of its transaction. */
    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(Connection con) throws Exception;
    }

    private final DataSource dataSource;

    public UnidadDeTrabajo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T ejecutar(Trabajo<T> trabajo) throws Exception {
        try (Connection con = dataSource.getConnection()) {
            boolean autoCommitPrevio = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(con);
                con.commit();
                return resultado;
            } catch (Exception | Error e) {
                try {
                    con.rollback();
                } catch (SQLException r) {
                    e.addSuppressed(r);
                }
                throw e;
            } finally {
                try {
                    con.setAutoCommit(autoCommitPrevio);
                } catch (SQLException ignorada) {
                    // the connection goes back to the pool in any case
                }
            }
        }
    }
}
