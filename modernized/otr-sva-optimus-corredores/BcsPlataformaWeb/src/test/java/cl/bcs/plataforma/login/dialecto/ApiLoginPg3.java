package cl.bcs.plataforma.login.dialecto;

import java.lang.reflect.InvocationTargetException;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;

/**
 * The ONLY place where the LOGIN-PG3 tests (dialect layer WITHOUT the equivalence catalog at run time) reach production code that does not exist yet. It
 * replaces ApiLoginPg and ApiLoginPg2: every production object is built here through its public no-arg constructor (A3-2, A3-5) or through
 * {@code SentenciasSql(SqlDialecto)} (A3-3), by reflection, so the tests COMPILE before the production code is redesigned and fail with
 * <ul>
 *   <li>{@code LOGIN-PG3 not implemented: ...} when something the new contract demands is missing, and</li>
 *   <li>{@code LOGIN-PG3 catalog still in production: ...} when something that must be GONE is still there.</li>
 * </ul>
 * The types {@code SqlDialecto}, {@code SentenciasSql} and {@code ProcedimientoAlmacenado} exist in the old and in the new production, so the tests use
 * them directly (a call to {@code nombre()}, {@code siguienteValor}, {@code seleccionarSinTabla}, {@code texto}, {@code de}, {@code ids},
 * {@code exigirDisponibles} compiles against both). Assumptions: API_ASSUMED_LOGINPG3.md.
 */
public final class ApiLoginPg3 {

    public static final String PAQUETE = "cl.bcs.plataforma.login.dialecto.";
    public static final String PROPIEDAD = "plataforma.db.dialect";

    private static SentenciasSql hanaCache;
    private static SentenciasSql postgresCache;

    private ApiLoginPg3() {
    }

    // ------------------------------------------------------------------------------------------------ standard failures

    public static AssertionError pendiente(String que) {
        return new AssertionError("LOGIN-PG3 not implemented: " + que);
    }

    public static AssertionError catalogoEnProduccion(String que) {
        return new AssertionError("LOGIN-PG3 catalog still in production: " + que);
    }

    // ------------------------------------------------------------------------------------------------ reflection helpers

    public static Class<?> clase(String simple) {
        try {
            return Class.forName(PAQUETE + simple);
        } catch (ClassNotFoundException e) {
            throw pendiente("the production class " + PAQUETE + simple + " does not exist yet");
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

    /** a public constructor with exactly the given arguments' count (the production classes have one per arity) */
    static Object nuevo(String simple, Object... args) {
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
        throw pendiente("the production class " + c.getName() + " has no public constructor with " + args.length + " parameter(s)");
    }

    // ------------------------------------------------------------------------------------------------ SqlDialecto (A3-2)

    public static SqlDialecto hana() {
        return (SqlDialecto) nuevo("HanaDialecto");
    }

    public static SqlDialecto postgres() {
        return (SqlDialecto) nuevo("PostgresDialecto");
    }

    // ------------------------------------------------------------------------------------------------ SentenciasSql (A3-3)

    public static SentenciasSql sentencias(SqlDialecto dialecto) {
        return (SentenciasSql) nuevo("SentenciasSql", dialecto);
    }

    public static synchronized SentenciasSql sentenciasHana() {
        if (hanaCache == null) hanaCache = sentencias(hana());
        return hanaCache;
    }

    public static synchronized SentenciasSql sentenciasPostgres() {
        if (postgresCache == null) postgresCache = sentencias(postgres());
        return postgresCache;
    }

    /** the Postgres text of a statement (or the Postgres name of an object-name statement) in the production resources */
    public static String textoPg(String id) {
        return sentenciasPostgres().texto(id);
    }

    // ------------------------------------------------------------------------------------------------ procedures (A3-5)

    public static ProcedimientoAlmacenado procedimientosPostgres() {
        Object o = nuevo("ProcedimientosPostgres");
        if (!(o instanceof ProcedimientoAlmacenado p)) {
            throw new AssertionError("ProcedimientosPostgres must implement cl.bcs.plataforma.login.ProcedimientoAlmacenado (A3-5)");
        }
        return p;
    }

    /** the selector: {@code ProcedimientosPorDialecto(SqlDialecto, ProcedimientoAlmacenado hana, ProcedimientoAlmacenado postgres)} (A2-2, unchanged) */
    public static ProcedimientoAlmacenado porDialecto(SqlDialecto dialecto, ProcedimientoAlmacenado hana, ProcedimientoAlmacenado postgres) {
        Object o = nuevo("ProcedimientosPorDialecto", dialecto, hana, postgres);
        if (!(o instanceof ProcedimientoAlmacenado p)) {
            throw new AssertionError("ProcedimientosPorDialecto must implement cl.bcs.plataforma.login.ProcedimientoAlmacenado (A2-2)");
        }
        return p;
    }
}
