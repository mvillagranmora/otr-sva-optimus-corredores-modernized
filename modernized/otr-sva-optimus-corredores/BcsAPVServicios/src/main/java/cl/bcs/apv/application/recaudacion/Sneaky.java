package cl.bcs.apv.application.recaudacion;

/**
 * The typed use case interfaces declare no checked exception, but the legacy {@code logica} threw {@code Exception}/{@code SQLException} and the goldens
 * pin the exception CLASS: the original throwable is rethrown unchanged (never wrapped).
 */
final class Sneaky {

    private Sneaky() {
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> RuntimeException lanzar(Throwable t) throws T {
        throw (T) t;
    }

    /** The exception the peer use case really threw: unwraps the {@code UndeclaredThrowableException} added by a Spring proxy. */
    static Throwable causa(Throwable t) {
        return cl.bcs.apv.application.comun.Sneaky.causa(t);
    }
}
