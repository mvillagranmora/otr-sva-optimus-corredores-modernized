package cl.bcs.cuentainversion.transaccional.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stands in for the HANA stored procedure {@code BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono}, which H2 cannot run: the
 * connection hands out a no-op {@link CallableStatement} for that call and records the scalar inputs bound by name
 * ({@code p_IdEmpresa=>?}). As in the oracle (SQL.call shim) the procedure itself does nothing: the case seed pre-fills its output tables.
 */
public final class ProcedimientoDeCaso {

    private static final Pattern PARAMETRO = Pattern.compile("([A-Za-z_0-9]+)=>[?]");

    /** Scalar inputs of the calls made so far (parameter name -> value). */
    private final List<Map<String, Object>> llamadas = new ArrayList<>();

    public List<Map<String, Object>> llamadas() {
        return llamadas;
    }

    public Connection envolver(Connection real) {
        InvocationHandler h = (proxy, m, a) -> {
            try {
                if (m.getName().equals("prepareCall") && a != null && a.length > 0 && a[0] instanceof String sql && sql.contains("CrearCargoAbono")) {
                    return llamada(sql);
                }
                return m.invoke(real, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(ProcedimientoDeCaso.class.getClassLoader(), new Class<?>[]{Connection.class}, h);
    }

    private CallableStatement llamada(String sql) {
        List<String> nombres = new ArrayList<>();
        Matcher m = PARAMETRO.matcher(sql);
        while (m.find()) {
            nombres.add(m.group(1));
        }
        Map<String, Object> entrada = new TreeMap<>();
        llamadas.add(entrada);
        InvocationHandler h = (proxy, metodo, a) -> {
            String n = metodo.getName();
            if (n.startsWith("set") && a != null && a.length >= 2 && a[0] instanceof Integer i && i >= 1 && i <= nombres.size()
                    && !nombres.get(i - 1).startsWith("p_out_")) {
                entrada.put(nombres.get(i - 1), n.equals("setNull") ? null : a[1]);
            }
            Class<?> r = metodo.getReturnType();
            if (r == boolean.class) {
                return false;
            }
            if (r == int.class || r == long.class || r == short.class || r == byte.class) {
                return 0;
            }
            return null;
        };
        return (CallableStatement) Proxy.newProxyInstance(ProcedimientoDeCaso.class.getClassLoader(), new Class<?>[]{CallableStatement.class}, h);
    }
}
