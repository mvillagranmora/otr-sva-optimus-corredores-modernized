package cl.bcs.apv.adapter.javacall;

import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogica;

/**
 * In-process pipeline (no HTTP): registry lookup, legacy JSON to typed record, use case, typed outcome to legacy JSON.
 * {@code ejecutores} maps each use case interface (for example {@code GrabarContratos.class}) to its implementation.
 * Legacy classes whose {@code call()} returns null (not invocable) answer {@code null}, as the legacy did.
 */
public class ApvJavaCallPipelineEnProceso implements ApvJavaCallPipeline {

    private final ApvJavaCallRegistro registro;
    private final Map<Class<?>, Object> ejecutores;

    public ApvJavaCallPipelineEnProceso(ApvJavaCallRegistro registro, Map<Class<?>, Object> ejecutores) {
        this.registro = registro;
        this.ejecutores = ejecutores;
    }

    @Override
    public JsonObject invocar(String claseLegacy, JsonObject entrada) throws Exception {
        if (esOperacionTipada(claseLegacy)) {
            return invocarValidarMandatosPACLogica(entrada);
        }
        OperacionRegistrada op = registro.buscar(claseLegacy)
                .orElseThrow(() -> new IllegalArgumentException("Clase legacy sin registro APV: " + claseLegacy));
        if (!op.invocable()) {
            return null;
        }
        Object caso = ejecutores.get(op.interfaz());
        if (caso == null) {
            throw new IllegalStateException("Sin implementacion para el caso de uso " + op.casoUso() + " (" + op.claseLegacy() + ")");
        }
        try {
            return op.mapeo().invocar(caso, entrada);
        } catch (Throwable t) {
            Throwable causa = causa(t);
            if (causa instanceof Exception e) {
                throw e;
            }
            throw (Error) causa;
        }
    }

    /**
     * {@code true} for the typed in-process operations that are NOT JavaCall operations and therefore not in the registry
     * ({@link ValidarMandatosPACLogicaMapeo#OPERACION}): a dispatcher that routes by {@code ApvJavaCallRegistro#buscar} must also ask this.
     */
    public static boolean esOperacionTipada(String claseLegacy) {
        return ValidarMandatosPACLogicaMapeo.OPERACION.equals(claseLegacy);
    }

    /**
     * Legacy {@code ValidarMandatosPAC.logica(con, SalidaRecaudacionTraspaso, IdEmpresa, IdUsuario)}: runs on the connection/transaction of the caller and
     * answers {@code null} (void). Input shape: {@link ValidarMandatosPACLogicaMapeo}.
     */
    private JsonObject invocarValidarMandatosPACLogica(JsonObject entrada) throws Exception {
        Object caso = ejecutores.get(ValidarMandatosPACLogica.class);
        if (caso == null) {
            throw new IllegalStateException("Sin implementacion para " + ValidarMandatosPACLogicaMapeo.OPERACION);
        }
        try {
            ((ValidarMandatosPACLogica) caso).ejecutar(ValidarMandatosPACLogicaMapeo.entradaDesdeJson(entrada));
            return null;
        } catch (Throwable t) {
            Throwable causa = causa(t);
            if (causa instanceof Exception e) {
                throw e;
            }
            throw (Error) causa;
        }
    }

    /**
     * A checked exception thrown by a use case through a Spring proxy (or by reflection) arrives wrapped: the legacy callers saw the original one.
     */
    private static Throwable causa(Throwable t) {
        Throwable actual = t;
        while ((actual instanceof java.lang.reflect.UndeclaredThrowableException || actual instanceof java.lang.reflect.InvocationTargetException)
                && actual.getCause() != null) {
            actual = actual.getCause();
        }
        return actual;
    }
}
