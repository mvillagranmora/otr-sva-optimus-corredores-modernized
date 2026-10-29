import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;

import cl.bcs.oracle.Doubles;

/**
 * F3 W3 T1 (CertificarSolicitudAporteRescate): OWN wrapper of the connection the legacy class receives (the shared SessionConnection / Doubles are not touched). ONE job: the legacy
 * INSERT of FMM.LogCertificacionAporteRescate (AgregarLogCertificacionAporteRescate) takes its id INLINE from the HANA sequence ({@code "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate".NEXTVAL}, one value per row of
 * the batch), while the new code is asked to get the ids from the sequence port first. To compare the same thing, the consumption of that sequence is recorded exactly like
 * {@code Utils.getSecuencia} does ({secuencia, cantidad}, in {@code secuenciasSolicitadas}): ONE entry per executed batch = the number of rows of the batch, registered when the
 * batch is executed (before it runs, so a failing batch has still asked for its ids, as the port does). Nothing else is altered; the statement is delegated untouched.
 */
final class ConexionCer {

    static final String SECUENCIA = "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate";

    private ConexionCer() {
    }

    static Connection envolver(Connection interna) {
        return (Connection) Proxy.newProxyInstance(ConexionCer.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            Object r = invocar(interna, m, a);
            if (m.getName().equals("prepareStatement") && a != null && a.length > 0 && a[0] instanceof String && ((String) a[0]).contains("\"" + SECUENCIA + "\".NEXTVAL")) {
                return contar((PreparedStatement) r);
            }
            return r;
        });
    }

    private static PreparedStatement contar(PreparedStatement st) {
        final int[] pendientes = { 0 };
        return (PreparedStatement) Proxy.newProxyInstance(ConexionCer.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
            switch (m.getName()) {
                case "addBatch":
                    if (a == null || a.length == 0) pendientes[0]++;
                    break;
                case "executeBatch":
                case "executeLargeBatch":
                    if (pendientes[0] > 0) Doubles.registrarSecuencia(SECUENCIA, pendientes[0]);
                    pendientes[0] = 0;
                    break;
                case "execute":
                case "executeUpdate":
                case "executeQuery":
                    Doubles.registrarSecuencia(SECUENCIA, 1);
                    break;
                default:
                    break;
            }
            return invocar(st, m, a);
        });
    }

    private static Object invocar(Object destino, java.lang.reflect.Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(destino, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
