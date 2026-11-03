package cl.bcs.plataforma.login.dialecto;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * A recording fake of the JDBC seam (Connection, CallableStatement, PreparedStatement, ResultSet, Array) for the Postgres procedure tests: H2 has no such
 * procedures, no refcursor OUT and no composite arrays, so the contract is pinned at the calls the production code makes. Nothing here connects to
 * anything. Reading an OUT parameter by NAME is refused on purpose (pgjdbc does not support it): the production code must read by index.
 */
final class JdbcGrabador {

    /** one value bound to a parameter; {@code tipoSql} is the JDBC type given with it (setNull / setObject with type), or null */
    record Enlace(int indice, Object valor, Integer tipoSql, String metodo) {
    }

    /** what {@code createArrayOf} was asked to build */
    record ArregloGrabado(String tipo, List<Object> elementos) {
    }

    /** what the fake answers when a statement executes */
    record Respuesta(Map<Integer, Object> salidas, List<Map<String, Object>> filas, SQLException fallo) {
        static Respuesta salidas(Map<Integer, Object> s) {
            return new Respuesta(s, List.of(), null);
        }

        static Respuesta filas(List<Map<String, Object>> f) {
            return new Respuesta(Map.of(), f, null);
        }

        static Respuesta falla(SQLException e) {
            return new Respuesta(Map.of(), List.of(), e);
        }
    }

    final class Sentencia {
        final String via;
        final String sql;
        final Map<Integer, Enlace> enlaces = new TreeMap<>();
        final Map<Integer, Integer> salidas = new TreeMap<>();
        final List<String> lecturas = new ArrayList<>();
        final List<ConjuntoGrabado> conjuntos = new ArrayList<>();
        boolean ejecutada;
        boolean cerrada;
        Respuesta respuesta;

        Sentencia(String via, String sql) {
            this.via = via;
            this.sql = sql;
        }

        /** the value bound at the index, with arrays resolved to what they were built from */
        Object valor(int indice) {
            Enlace e = enlaces.get(indice);
            if (e == null) throw new AssertionError("nothing bound at parameter " + indice + " of " + sql + "; bound: " + enlaces.keySet());
            return resolver(e.valor());
        }

        Enlace enlace(int indice) {
            Enlace e = enlaces.get(indice);
            if (e == null) throw new AssertionError("nothing bound at parameter " + indice + " of " + sql + "; bound: " + enlaces.keySet());
            return e;
        }
    }

    final class ConjuntoGrabado {
        boolean cerrado;
    }

    final List<Sentencia> sentencias = new ArrayList<>();
    final List<ArregloGrabado> arreglos = new ArrayList<>();
    /** calls on the connection other than preparing a statement or building an array (commit, rollback, setAutoCommit, close...) */
    final List<String> otrasLlamadas = new ArrayList<>();
    private final IdentityHashMap<Object, ArregloGrabado> proxiesDeArreglo = new IdentityHashMap<>();
    private Function<Sentencia, Respuesta> guion = s -> Respuesta.salidas(Map.of());

    JdbcGrabador guion(Function<Sentencia, Respuesta> g) {
        this.guion = g;
        return this;
    }

    JdbcGrabador responder(Respuesta r) {
        return guion(s -> r);
    }

    /** true when the connection was never asked for anything at all */
    boolean intacta() {
        return sentencias.isEmpty() && arreglos.isEmpty() && otrasLlamadas.isEmpty();
    }

    Sentencia unica() {
        if (sentencias.size() != 1) throw new AssertionError("expected exactly one statement, got " + sentencias.stream().map(s -> s.sql).toList());
        return sentencias.get(0);
    }

    Object resolver(Object v) {
        ArregloGrabado a = v == null ? null : proxiesDeArreglo.get(v);
        return a != null ? a : v;
    }

    // ------------------------------------------------------------------------------------------------ connection

    Connection conexion() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            switch (m.getName()) {
                case "prepareCall":
                    return nueva("prepareCall", (String) a[0], CallableStatement.class);
                case "prepareStatement":
                    return nueva("prepareStatement", (String) a[0], PreparedStatement.class);
                case "createStatement":
                    otrasLlamadas.add("createStatement");
                    return nueva("createStatement", "<createStatement>", Statement.class);
                case "createArrayOf": {
                    ArregloGrabado ag = new ArregloGrabado((String) a[0], new ArrayList<>(Arrays.asList((Object[]) a[1])));
                    arreglos.add(ag);
                    java.sql.Array proxy = (java.sql.Array) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { java.sql.Array.class }, (pp, mm, aa) -> {
                        switch (mm.getName()) {
                            case "getBaseTypeName":
                                return ag.tipo();
                            case "getArray":
                                return ag.elementos().toArray();
                            case "toString":
                                return "Array" + ag;
                            case "hashCode":
                                return System.identityHashCode(pp);
                            case "equals":
                                return pp == aa[0];
                            default:
                                return defecto(mm.getReturnType());
                        }
                    });
                    proxiesDeArreglo.put(proxy, ag);
                    return proxy;
                }
                case "getAutoCommit":
                    return Boolean.FALSE;
                case "isClosed":
                    return Boolean.FALSE;
                case "toString":
                    return "JdbcGrabador.Connection";
                case "hashCode":
                    return System.identityHashCode(p);
                case "equals":
                    return p == a[0];
                default:
                    otrasLlamadas.add(m.getName());
                    return defecto(m.getReturnType());
            }
        });
    }

    private Object nueva(String via, String sql, Class<?> tipo) {
        Sentencia s = new Sentencia(via, sql);
        sentencias.add(s);
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { tipo }, manejador(s));
    }

    // ------------------------------------------------------------------------------------------------ statements

    private InvocationHandler manejador(Sentencia s) {
        boolean[] ultimoNulo = new boolean[1];
        return (p, m, a) -> {
            String n = m.getName();
            switch (n) {
                case "setString", "setLong", "setInt", "setShort", "setBoolean", "setBigDecimal", "setBytes", "setTimestamp", "setDate", "setDouble", "setArray",
                        "setObject": {
                    Integer tipo = n.equals("setObject") && a.length >= 3 && a[2] instanceof Integer t ? t : null;
                    s.enlaces.put((Integer) a[0], new Enlace((Integer) a[0], a[1], tipo, n));
                    return null;
                }
                case "setNull":
                    s.enlaces.put((Integer) a[0], new Enlace((Integer) a[0], null, (Integer) a[1], n));
                    return null;
                case "registerOutParameter":
                    s.salidas.put((Integer) a[0], a[1] instanceof Integer t ? t : null);
                    return null;
                case "execute", "executeQuery", "executeUpdate", "executeLargeUpdate": {
                    s.ejecutada = true;
                    s.respuesta = guion.apply(s);
                    if (s.respuesta.fallo() != null) throw s.respuesta.fallo();
                    if (n.equals("execute")) return Boolean.TRUE;
                    if (n.equals("executeQuery")) return conjunto(s, s.respuesta.filas());
                    return 0;
                }
                case "getResultSet":
                    return s.respuesta == null ? null : conjunto(s, s.respuesta.filas());
                case "getMoreResults":
                    return Boolean.FALSE;
                case "getUpdateCount":
                    return -1;
                case "getString", "getInt", "getLong", "getObject", "getBytes", "getBigDecimal", "getBoolean", "getDouble": {
                    if (!(a[0] instanceof Integer idx)) {
                        throw new SQLFeatureNotSupportedException("OUT parameters are read by index (pgjdbc does not support names): " + n + "(" + a[0] + ")");
                    }
                    s.lecturas.add(n + "(" + idx + ")");
                    Object v = s.respuesta == null ? null : s.respuesta.salidas().get(idx);
                    if (v instanceof SQLException fallo) throw fallo;
                    ultimoNulo[0] = v == null;
                    if (v == null) return defecto(m.getReturnType());
                    if (n.equals("getObject")) return v instanceof List<?> l ? conjunto(s, castFilas(l)) : v;
                    if (n.equals("getString")) return String.valueOf(v);
                    if (n.equals("getInt")) return v instanceof Number x ? x.intValue() : Integer.parseInt(v.toString());
                    if (n.equals("getLong")) return v instanceof Number x ? x.longValue() : Long.parseLong(v.toString());
                    return v;
                }
                case "wasNull":
                    return ultimoNulo[0];
                case "close":
                    s.cerrada = true;
                    return null;
                case "isClosed":
                    return s.cerrada;
                case "toString":
                    return "JdbcGrabador.Statement[" + s.sql + "]";
                case "hashCode":
                    return System.identityHashCode(p);
                case "equals":
                    return p == a[0];
                default:
                    return defecto(m.getReturnType());
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castFilas(List<?> l) {
        return (List<Map<String, Object>>) l;
    }

    // ------------------------------------------------------------------------------------------------ result sets

    private ResultSet conjunto(Sentencia s, List<Map<String, Object>> filas) {
        ConjuntoGrabado cg = new ConjuntoGrabado();
        s.conjuntos.add(cg);
        List<String> etiquetas = filas.isEmpty() ? List.of() : new ArrayList<>(filas.get(0).keySet());
        int[] pos = { -1 };
        boolean[] nulo = new boolean[1];
        ResultSetMetaData md = (ResultSetMetaData) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResultSetMetaData.class }, (p, m, a) -> {
            switch (m.getName()) {
                case "getColumnCount":
                    return etiquetas.size();
                case "getColumnLabel", "getColumnName":
                    return etiquetas.get((Integer) a[0] - 1);
                case "toString":
                    return "JdbcGrabador.MetaData";
                case "hashCode":
                    return System.identityHashCode(p);
                case "equals":
                    return p == a[0];
                default:
                    return defecto(m.getReturnType());
            }
        });
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResultSet.class }, (p, m, a) -> {
            String n = m.getName();
            switch (n) {
                case "next":
                    return ++pos[0] < filas.size();
                case "getMetaData":
                    return md;
                case "getString", "getObject", "getInt", "getLong", "getBytes", "getBigDecimal", "getBoolean", "getTimestamp", "getDate": {
                    Object v;
                    if (a[0] instanceof Integer idx) {
                        v = filas.get(pos[0]).get(etiquetas.get(idx - 1));
                    } else {
                        v = filas.get(pos[0]).get((String) a[0]);
                    }
                    nulo[0] = v == null;
                    if (v == null) return defecto(m.getReturnType());
                    if (n.equals("getString")) return String.valueOf(v);
                    if (n.equals("getInt")) return v instanceof Number x ? x.intValue() : Integer.parseInt(v.toString());
                    if (n.equals("getLong")) return v instanceof Number x ? x.longValue() : Long.parseLong(v.toString());
                    return v;
                }
                case "wasNull":
                    return nulo[0];
                case "close":
                    cg.cerrado = true;
                    return null;
                case "isClosed":
                    return cg.cerrado;
                case "toString":
                    return "JdbcGrabador.ResultSet";
                case "hashCode":
                    return System.identityHashCode(p);
                case "equals":
                    return p == a[0];
                default:
                    return defecto(m.getReturnType());
            }
        });
    }

    // ------------------------------------------------------------------------------------------------ helpers

    static Object defecto(Class<?> t) {
        if (t == boolean.class) return Boolean.FALSE;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == short.class) return (short) 0;
        if (t == double.class) return 0d;
        if (t == float.class) return 0f;
        if (t == byte.class) return (byte) 0;
        return null;
    }

    /** a row of a cursor or of a function result, in column order */
    static Map<String, Object> fila(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) m.put((String) pares[i], pares[i + 1]);
        return m;
    }

    /** the two values are the same bound value: numbers by value (setInt vs setLong), the rest by equals */
    static boolean iguales(Object esperado, Object real) {
        if (esperado instanceof Number a && real instanceof Number b) return a.longValue() == b.longValue();
        return esperado == null ? real == null : esperado.equals(real);
    }
}
