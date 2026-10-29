package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The {@link Connection} handed to a LIBRARY that runs inside the unit of work of the request (the custody flow of {@code BcsCustodiaServicios}): a
 * {@link Proxy} that keeps the library inside the two limits the host owns (architecture review of FFMM and custody in process, 2026-10-28, H2).
 * <ul>
 *   <li><b>Deadline</b>: every {@code Statement}, {@code PreparedStatement} or {@code CallableStatement} it creates carries the REMAINING time of the Spring
 *       transaction as its query timeout, through the same mechanism as {@link SqlNombrado} (the {@code PLAZO} of {@link ConexionUnidadDeTrabajo#usar}, applied with
 *       {@code DataSourceUtils.applyTimeout}); outside a unit of work, or where {@code usarAunVencido} runs, no timeout is applied (as before).
 *       DECISION: a statement whose SQL is a {@code DELETE} (the cleanup of the library's work rows in its {@code finally}, CUS-FLU-14) must NOT fail because the
 *       deadline has already passed (the library would then attach the failure as suppressed and leave the rows to the rollback, hiding nothing but noisy): for it an
 *       expired deadline becomes a minimum timeout of 1 second; with time left it carries the remaining time like any other statement.</li>
 *   <li><b>Transaction ownership</b>: the host decides commit and rollback. {@code commit}, {@code rollback} (both forms), {@code setAutoCommit}, {@code close},
 *       {@code setReadOnly}, {@code setTransactionIsolation}, {@code abort}, {@code releaseSavepoint}, {@code setSavepoint} (both forms), {@code setHoldability},
 *       {@code setCatalog}, {@code setSchema} and {@code unwrap} (it would hand out the raw connection and void this guard) raise {@link IllegalStateException}. The library
 *       never calls them today (pinned by tests); this fails loudly if a future version does. Everything else is delegated.</li>
 * </ul>
 */
public final class ConexionAcotada {

    private static final Set<String> PROHIBIDOS = Set.of("commit", "rollback", "setAutoCommit", "close", "setReadOnly", "setTransactionIsolation", "abort",
            "releaseSavepoint", "setSavepoint", "setHoldability", "setCatalog", "setSchema", "unwrap");

    private static final Pattern BORRADO = Pattern.compile("^\\s*delete\\b",Pattern.CASE_INSENSITIVE);

    private ConexionAcotada() {
    }

    /** @return a proxy of {@code real}; call it inside {@link ConexionUnidadDeTrabajo#usar} so that the deadline of the transaction is known */
    public static Connection de(Connection real) {
        return (Connection) Proxy.newProxyInstance(ConexionAcotada.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, metodo, args) -> {
            String nombre = metodo.getName();
            if (PROHIBIDOS.contains(nombre)) {
                throw new IllegalStateException("Connection." + nombre + " is not allowed to a library running in the unit of work of the request: "
                        + "the host owns the transaction (commit, rollback, close and session settings)");
            }
            Object r;
            try {
                r = metodo.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (r instanceof Statement st && (nombre.equals("createStatement") || nombre.equals("prepareStatement") || nombre.equals("prepareCall"))) {
                boolean borrado = args != null && args.length > 0 && args[0] instanceof String sql && BORRADO.matcher(sql).find();
                try {
                    SqlNombrado.aplicarPlazo(st, borrado);
                } catch (SQLException | RuntimeException e) {
                    try {
                        st.close();
                    } catch (SQLException cierre) {
                        e.addSuppressed(cierre);
                    }
                    throw e;
                }
            }
            return r;
        });
    }

}
