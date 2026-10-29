package cl.bcs.fondomutuo.consultas;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * HANA-vs-H2 dialect bridge of the read-only queries of F3 W1 (fronts FMR / IFM / VDO), the SAME rule the oracle applies to the legacy (its
 * {@code legacy-oracle/runner-lect/DialectoLect.java}); H2 ONLY, never HANA. ONE rule:
 * <pre>"IdEmpresa" = (CASE WHEN ? &lt;&gt; '' THEN ? ELSE ...</pre>
 * ({@code ConsultarFondoMutuo_SelectReinversion.sql}): the company id is a BIGINT parameter compared with the empty text. H2 refuses the comparison ("Data conversion error converting CHARACTER VARYING to DECFLOAT") for every call; HANA runs it. Q-F3A-9 CLOSED against HANA QA (read-only SELECT, parameter bound with setLong as the legacy does): a bound BIGINT parameter compared with '' is ALWAYS true (1 and 0 both give 'distinto'; a CAST literal would raise error 339 but a bound parameter is compared as text). So in HANA the CASE always takes :IdEmpresa and a company 0 (absent, null or {}) filters IdEmpresa = 0 (no rows), it does NOT return every company. The bridge writes the comparison as CAST(? AS VARCHAR) <> '' (always true in H2 too).
 */
public final class DialectoLectura {

    private DialectoLectura() {
    }

    public static String traducir(String sql) {
        return sql.replaceAll("(\"IdEmpresa\"\\s*=\\s*\\(CASE\\s+WHEN\\s+)\\?(\\s*<>\\s*'')", "$1CAST(? AS VARCHAR)$2");
    }

    public static Connection envolver(Connection raw) {
        return (Connection) Proxy.newProxyInstance(DialectoLectura.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if ((m.getName().equals("prepareStatement") || m.getName().equals("prepareCall")) && a != null && a.length > 0 && a[0] instanceof String) {
                a = a.clone();
                a[0] = traducir((String) a[0]);
            }
            try {
                return m.invoke(raw, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
