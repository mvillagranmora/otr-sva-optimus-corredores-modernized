package cl.bcs.tesoreria.familia_b.support;

import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * One equivalence comparison: loads the recorded case, runs the MODERN service on the case's H2 database inside the production
 * transaction model (commit on return, rollback on throw) and compares output / exception type / table dumps (and optionally the
 * collaborator trace) with the golden recorded from the unmodified legacy. Counts the case as executed.
 */
public final class Equivalencia {

    /** The modern entry point of one case; may throw any exception (the legacy throws plain {@code Exception} in several paths). */
    @FunctionalInterface
    public interface Moderno {
        JsonElement ejecutar(Caso caso) throws Exception;
    }

    private Equivalencia() {
    }

    public static void verificar(Caso.Contador contador, String nombre, Moderno moderno, String... sinOrden) {
        verificar(contador, nombre, moderno, null, sinOrden);
    }

    /** As {@link #verificar} and also compares the files the code wrote below the case's ${RUTA_BASE} (golden "archivos"). */
    public static void verificarConArchivos(Caso.Contador contador, String nombre, Moderno moderno, String... sinOrden) {
        verificar(contador, nombre, moderno, null, true, sinOrden);
    }

    public static void verificar(Caso.Contador contador, String nombre, Moderno moderno, Supplier<List<JsonObject>> traza, String... sinOrden) {
        verificar(contador, nombre, moderno, traza, false, sinOrden);
    }

    private static void verificar(Caso.Contador contador, String nombre, Moderno moderno, Supplier<List<JsonObject>> traza, boolean archivos, String... sinOrden) {
        Caso caso = Caso.cargar(nombre);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> {
            try {
                return moderno.ejecutar(caso);
            } catch (Exception e) {
                throw sneaky(e);
            }
        });
        contador.ejecutado(nombre);
        AssertionError fallo = null;
        try {
            if (traza == null) {
                Verifica.comparar(caso, r, sinOrden);
            } else {
                Verifica.comparar(caso, r, traza.get(), sinOrden);
            }
        } catch (AssertionError e) {
            fallo = e;
        }
        if (archivos) {
            try {
                Verifica.archivos(caso);
            } catch (AssertionError e) {
                if (fallo == null) {
                    fallo = e;
                } else {
                    fallo.addSuppressed(e);
                }
            }
        }
        if (fallo != null) {
            throw fallo;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneaky(Throwable t) throws T {
        throw (T) t;
    }
}
