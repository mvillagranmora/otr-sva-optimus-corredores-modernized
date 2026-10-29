package cl.bcs.custodia.flujo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonObject;

/**
 * Finds the NEW implementation of the slice by reflection (src/main has none yet: every comparison is expected to FAIL until it exists, never to
 * be skipped) and adapts it to the test contracts {@link FlujoCustodiaPort} and {@link #reglas()}. A missing class, constructor or method is an
 * AssertionError naming exactly what the contract expects.
 */
public final class FlujoCustodiaContrato {

    /** the JavaCall-equivalent entry point (see FlujoCustodiaPort) */
    public static final String IMPL = System.getProperty("custodia.flujo.impl", "cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo");
    /** the pure, deterministic pieces pinned by FlujoCustodiaReglasTest (static methods) */
    public static final String REGLAS = System.getProperty("custodia.flujo.reglas", "cl.bcs.custodia.flujo.FlujoCustodiaReglas");

    private FlujoCustodiaContrato() {
    }

    /** fails (AssertionError) when the implementation class or its contract constructor / method is missing */
    public static void verificarImplementacion() {
        Class<?> c = clase(IMPL, "the ConsultarFlujoCustodia slice implementation");
        constructor(c);
        metodoCall(c);
    }

    public static FlujoCustodiaPort crear(Function<JsonObject, JsonObject> valorizador, BiFunction<String, Long, List<Long>> secuencias) {
        Class<?> c = clase(IMPL, "the ConsultarFlujoCustodia slice implementation");
        Constructor<?> k = constructor(c);
        Method call = metodoCall(c);
        Object impl;
        try {
            impl = k.newInstance(valorizador, secuencias);
        } catch (InvocationTargetException e) {
            throw new AssertionError("the constructor of " + IMPL + " failed", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot instantiate " + IMPL, e);
        }
        return (con, entrada) -> {
            try {
                return (JsonObject) call.invoke(impl, con, entrada);
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (t instanceof Exception) throw (Exception) t;
                if (t instanceof Error) throw (Error) t;
                throw new IllegalStateException(t);
            }
        };
    }

    public static Class<?> reglas() {
        return clase(REGLAS, "the deterministic rules of the slice (FlujoCustodiaReglasTest)");
    }

    /** invokes a public static method of the rules class; the exception it throws is rethrown unchanged */
    public static Object regla(String nombre, Class<?>[] tipos, Object... args) throws Throwable {
        Method m;
        try {
            m = reglas().getMethod(nombre, tipos);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("contract not implemented: public static method " + nombre + java.util.Arrays.toString(tipos) + " in " + REGLAS);
        }
        if (!Modifier.isStatic(m.getModifiers())) throw new AssertionError(REGLAS + "." + nombre + " must be static");
        try {
            return m.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static Class<?> clase(String fqcn, String que) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("implementation missing: " + fqcn + " (" + que + ") is not on the test classpath; "
                    + "the characterization tests define 'done' for it (see src/test/java/cl/bcs/custodia/flujo/README.md)");
        }
    }

    private static Constructor<?> constructor(Class<?> c) {
        try {
            return c.getConstructor(Function.class, BiFunction.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("contract not implemented: public " + c.getSimpleName()
                    + "(Function<JsonObject,JsonObject> valorizador, BiFunction<String,Long,List<Long>> secuencias)");
        }
    }

    private static Method metodoCall(Class<?> c) {
        try {
            return c.getMethod("call", Connection.class, JsonObject.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("contract not implemented: public JsonObject call(java.sql.Connection, JsonObject) in " + c.getName());
        }
    }

    /** rethrows any Throwable without declaring it (the valuation script throws java.lang.Exception as the legacy collaborator does) */
    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
        throw (E) t;
    }
}
