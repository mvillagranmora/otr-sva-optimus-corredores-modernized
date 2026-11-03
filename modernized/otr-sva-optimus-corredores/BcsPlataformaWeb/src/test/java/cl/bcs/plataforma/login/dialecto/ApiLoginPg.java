package cl.bcs.plataforma.login.dialecto;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

/**
 * The ONLY place where the LOGIN-PG tests touch the production dialect layer (package {@code cl.bcs.plataforma.login.dialecto}). It is bridged by
 * reflection so the tests COMPILE before the production code exists and fail with
 * {@code LOGIN-PG not implemented: the production class ... does not exist yet}. When the production API differs from the assumption, only this file
 * changes (see API_ASSUMED_LOGINPG.md for every assumption, A-1..A-n).
 */
public final class ApiLoginPg {

    public static final String PAQUETE = "cl.bcs.plataforma.login.dialecto.";
    public static final String RECURSO_OBJETOS = "/equivalencias/equivalencias-objetos.csv";
    public static final String RECURSO_COLUMNAS = "/equivalencias/equivalencias-columnas.csv";
    public static final String PROPIEDAD = "plataforma.db.dialect";

    private ApiLoginPg() {
    }

    // ------------------------------------------------------------------------------------------------ reflection helpers

    public static Class<?> clase(String simple) {
        try {
            return Class.forName(PAQUETE + simple);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("LOGIN-PG not implemented: the production class " + PAQUETE + simple + " does not exist yet");
        }
    }

    public static boolean existe(String simple) {
        try {
            Class.forName(PAQUETE + simple);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Object invocar(Object objetivo, Class<?> tipo, String nombre, Object... args) {
        Method m = buscar(tipo, nombre, args.length, objetivo == null);
        try {
            m.setAccessible(true);
            return m.invoke(objetivo, args);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof RuntimeException r) throw r;
            if (c instanceof Error er) throw er;
            throw new AssertionError("checked exception from " + tipo.getSimpleName() + "." + nombre, c);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static Method buscar(Class<?> tipo, String nombre, int n, boolean estatico) {
        for (Method m : tipo.getMethods()) {
            if (m.getName().equals(nombre) && m.getParameterCount() == n && Modifier.isStatic(m.getModifiers()) == estatico) return m;
        }
        throw new AssertionError("LOGIN-PG not implemented: the production class " + tipo.getName() + " has no public " + (estatico ? "static " : "")
                + "method " + nombre + " with " + n + " parameter(s)");
    }

    private static Object nuevo(String simple, Object... args) {
        Class<?> c = clase(simple);
        for (var k : c.getConstructors()) {
            if (k.getParameterCount() == args.length) {
                try {
                    return k.newInstance(args);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getCause();
                    if (t instanceof RuntimeException r) throw r;
                    if (t instanceof Error er) throw er;
                    throw new AssertionError(t);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            }
        }
        throw new AssertionError("LOGIN-PG not implemented: the production class " + c.getName() + " has no public constructor with " + args.length
                + " parameter(s)");
    }

    // ------------------------------------------------------------------------------------------------ CatalogoEquivalencias (T-32)

    /** the catalog the application loads from its two CSV resources */
    public static Object catalogoDeRecursos() {
        return invocar(null, clase("CatalogoEquivalencias"), "desdeRecursos");
    }

    /** a catalog from the text of the two CSV files (header row; columns by name: A-5) */
    public static Object catalogoDeCsv(String objetosCsv, String columnasCsv) {
        Reader o = new StringReader(objetosCsv);
        Reader c = new StringReader(columnasCsv);
        return invocar(null, clase("CatalogoEquivalencias"), "desdeCsv", o, c);
    }

    /** Postgres name of a HANA object; unknown or NO_DISPONIBLE: ObjetoNoDisponibleException */
    public static String nombrePostgres(Object catalogo, String hana) {
        return (String) invocar(catalogo, clase("CatalogoEquivalencias"), "nombrePostgres", hana);
    }

    public static String tipo(Object catalogo, String hana) {
        return (String) invocar(catalogo, clase("CatalogoEquivalencias"), "tipo", hana);
    }

    public static boolean disponible(Object catalogo, String hana) {
        return (Boolean) invocar(catalogo, clase("CatalogoEquivalencias"), "disponible", hana);
    }

    public static String columnaPostgres(Object catalogo, String tablaHana, String columnaHana) {
        return (String) invocar(catalogo, clase("CatalogoEquivalencias"), "columnaPostgres", tablaHana, columnaHana);
    }

    /** full HANA name of an object token ({@code ACC.Usuario}): the part after {@code ::} is unique in the table */
    public static String hanaDeFicha(Object catalogo, String ficha) {
        return (String) invocar(catalogo, clase("CatalogoEquivalencias"), "hanaDeFicha", ficha);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> objetosHana(Object catalogo) {
        return (Set<String>) invocar(catalogo, clase("CatalogoEquivalencias"), "objetosHana");
    }

    // ------------------------------------------------------------------------------------------------ SqlDialecto (T-30)

    public static Object hana(Object catalogo) {
        return nuevo("HanaDialecto", catalogo);
    }

    public static Object postgres(Object catalogo) {
        return nuevo("PostgresDialecto", catalogo);
    }

    public static String f(Object dialecto, String funcion, Object... args) {
        return (String) invocar(dialecto, clase("SqlDialecto"), funcion, args);
    }

    // ------------------------------------------------------------------------------------------------ SentenciasSql (T-31)

    public static Object sentencias(Object dialecto) {
        return nuevo("SentenciasSql", dialecto);
    }

    public static String texto(Object sentencias, String id) {
        return (String) invocar(sentencias, clase("SentenciasSql"), "texto", id);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> ids(Object sentencias) {
        return (Set<String>) invocar(sentencias, clase("SentenciasSql"), "ids");
    }

    /** the objects (full HANA names) a statement touches, as the production code resolves them before running anything */
    @SuppressWarnings("unchecked")
    public static Collection<String> objetosDe(Object sentencias, String id) {
        return (Collection<String>) invocar(sentencias, clase("SentenciasSql"), "objetosDe", id);
    }

    // ------------------------------------------------------------------------------------------------ exceptions

    /** true for the exception of an object that is NO_DISPONIBLE or absent from the catalog (the 503 of Postgres mode) */
    public static boolean esNoDisponible(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getClass().getName().equals(PAQUETE + "ObjetoNoDisponibleException")) return true;
        }
        return false;
    }
}
