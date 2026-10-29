package cl.bcs.apv.application.comun;

/**
 * Throws a checked exception through the typed use-case contract ({@code R ejecutar(E)} declares no checked exceptions).
 * The legacy classes declared {@code throws Exception} and the goldens pin the SIMPLE CLASS NAME of what they threw
 * ({@code SQLException}, {@code Exception}...), so a use case re-throws the very same exception object instead of wrapping it.
 * Use as {@code throw Sneaky.lanzar(e);}. Spring transactions roll back on it because every write entry point is
 * {@code @Transactional(rollbackFor = Exception.class)}; the REST adapter maps it in ONE place (ApvExceptionHandler).
 */
public final class Sneaky {

    private Sneaky() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException lanzar(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * The exception the legacy code threw: a checked exception thrown through {@link #lanzar} that crosses a Spring transaction (CGLIB) proxy arrives
     * wrapped in {@link java.lang.reflect.UndeclaredThrowableException}; the borders of the module unwrap it here.
     */
    public static Throwable causa(Throwable t) {
        Throwable actual = t;
        while (actual instanceof java.lang.reflect.UndeclaredThrowableException u && u.getUndeclaredThrowable() != null) {
            actual = u.getUndeclaredThrowable();
        }
        return actual;
    }

    /** A legacy code block that declares {@code throws Exception}. */
    @FunctionalInterface
    public interface Bloque<T> {
        T ejecutar() throws Exception;
    }

    @FunctionalInterface
    public interface BloqueVoid {
        void ejecutar() throws Exception;
    }

    /** Runs the block and re-throws whatever it throws, unchanged (checked or not). */
    public static <T> T sinChequear(Bloque<T> bloque) {
        try {
            return bloque.ejecutar();
        } catch (Exception e) {
            throw lanzar(e);
        }
    }

    public static void sinChequear(BloqueVoid bloque) {
        try {
            bloque.ejecutar();
        } catch (Exception e) {
            throw lanzar(e);
        }
    }
}
