package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionPort;
import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * The bridge between the T4-C tests and the production code of the step (the JavaCall handler of Certificar, its Spring wiring and the in-process adapter of AR), which DOES NOT EXIST yet
 * ("red before code"): the tests COMPILE without it and fail with a clear {@code AssertionError} ("T4-C not implemented: the production class ... does not exist yet") until it is written. This is the
 * ONLY file that knows the names of the new production classes and constants (reached by reflection); everything the tests assume about them is here and in {@code API_ASSUMED_T4C.md}: change the
 * names HERE (never the goldens) if production needs another shape. It is public because the adapter test lives in the package {@code aportereinversion}.
 *
 * <p>Assumed production API, see API_ASSUMED_T4C.md:
 * <pre>
 * cl.bcs.fondomutuo.certificacion.CertificarAporteRescateJavaCallHandler(CertificarAporteRescatePort, Compuertas)   @Component, implements JavaCallHandler
 * cl.bcs.fondomutuo.certificacion.CertificarAporteRescateConfiguracion                                           @Configuration; public static final String LISTA = "CERTIFICAR_SOLICITUD_APORTE_RESCATE"
 * cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher.CERTIFICAR_SOLICITUD_APORTE_RESCATE                    the legacy FQN, in LISTA_BLANCA (13), not in CLASES_LECTURA
 * cl.bcs.fondomutuo.aportereinversion.AporteReinversionJsonAdapter(AporteReinversionPort)                         implements certificacion.CrearAporteReinversionPort, final fields only
 * cl.bcs.fondomutuo.aportereinversion.AporteReinversionConfiguracion                                             @Configuration; @Bean AporteReinversionPort
 * </pre>
 */
public final class ApiT4c {

    public static final String PAQUETE_CER = "cl.bcs.fondomutuo.certificacion.";
    public static final String PAQUETE_AR = "cl.bcs.fondomutuo.aportereinversion.";

    /** The legacy class the handler replaces (what the router sends in "class"). */
    public static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";
    /** The legacy AR class: it has NO handler (decision of the user: 0 direct uses). */
    public static final String FQN_LEGACY_AR = "cl.bcs.fondomutuo.CrearSolicitudAporteReinversion";
    public static final String LISTA_ESPERADA = "CERTIFICAR_SOLICITUD_APORTE_RESCATE";

    public static final String HANDLER = PAQUETE_CER + "CertificarAporteRescateJavaCallHandler";
    public static final String CONFIGURACION = PAQUETE_CER + "CertificarAporteRescateConfiguracion";
    public static final String FACHADA = PAQUETE_CER + "CertificarAporteRescateFachada";
    public static final String ADAPTADOR = PAQUETE_AR + "AporteReinversionJsonAdapter";
    public static final String CONFIGURACION_AR = PAQUETE_AR + "AporteReinversionConfiguracion";

    private ApiT4c() {
    }

    public static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T4-C not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T4C.md)", e);
        }
    }

    /** The value of a public static final String of a production class ({@code LISTA}, the constant of the dispatcher). */
    public static String constante(Class<?> c, String campo) {
        try {
            Field f = c.getField(campo);
            if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers()) || f.getType() != String.class) {
                throw new AssertionError("T4-C: " + c.getSimpleName() + "." + campo + " must be a public static final String");
            }
            return (String) f.get(null);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("T4-C not implemented: " + c.getName() + "." + campo + " does not exist yet (red before code; see API_ASSUMED_T4C.md)", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("T4-C: " + c.getName() + "." + campo + " must be public", e);
        }
    }

    /** {@code CertificarAporteRescateConfiguracion.LISTA}. */
    public static String lista() {
        return constante(cargar(CONFIGURACION), "LISTA");
    }

    /** {@code JavaCallDispatcher.CERTIFICAR_SOLICITUD_APORTE_RESCATE}. */
    public static String fqnDelDispatcher() {
        return constante(JavaCallDispatcher.class, "CERTIFICAR_SOLICITUD_APORTE_RESCATE");
    }

    /** {@code new CertificarAporteRescateJavaCallHandler(servicio, compuertas)}. */
    public static JavaCallHandler handler(CertificarAporteRescatePort servicio, Compuertas compuertas) {
        Class<?> c = cargar(HANDLER);
        try {
            Constructor<?> k = c.getConstructor(CertificarAporteRescatePort.class, Compuertas.class);
            return (JavaCallHandler) k.newInstance(servicio, compuertas);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4-C: " + HANDLER + " must have a public constructor (CertificarAporteRescatePort, Compuertas) and implement JavaCallHandler", e);
        } catch (ClassCastException e) {
            throw new AssertionError("T4-C: " + HANDLER + " must implement JavaCallHandler", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T4-C: the constructor of " + HANDLER + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T4-C: cannot build " + HANDLER + ": " + e, e);
        }
    }

    /** The handler with its list OPEN (the name is the production constant): the equivalence run. */
    public static JavaCallHandler handlerAbierto(CertificarAporteRescatePort servicio) {
        return handler(servicio, new Compuertas(lista(), false, false, false, false, false));
    }

    /** The legacy {@code call(Connection, JsonObject)}: the connection is the unit of work of the host and the handler never touches it (null here). */
    public static JsonObject llamarPorHandler(CertificarAporteRescatePort servicio, JsonObject entrada) throws Exception {
        return handlerAbierto(servicio).call(null, entrada);
    }

    /** {@code new AporteReinversionJsonAdapter(puerto)}, typed as the port Certificar consumes. */
    public static CrearAporteReinversionPort adaptador(AporteReinversionPort puerto) {
        Class<?> c = cargar(ADAPTADOR);
        try {
            Constructor<?> k = c.getConstructor(AporteReinversionPort.class);
            return (CrearAporteReinversionPort) k.newInstance(puerto);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4-C: " + ADAPTADOR + " must have a public constructor (AporteReinversionPort) and implement CrearAporteReinversionPort", e);
        } catch (ClassCastException e) {
            throw new AssertionError("T4-C: " + ADAPTADOR + " must implement CrearAporteReinversionPort", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T4-C: the constructor of " + ADAPTADOR + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T4-C: cannot build " + ADAPTADOR + ": " + e, e);
        }
    }

    /** What the CSR harness hands over instead of {@code ApiCsr.llamar}: the JSON goes through the adapter (JSON in, JSON out), whatever the real port throws comes out UNCHANGED. */
    public static JsonObject llamarPorAdaptador(Object puertoReal, JsonObject entrada) throws Exception {
        if (!(puertoReal instanceof AporteReinversionPort p)) {
            throw new AssertionError("T4-C: the harness built " + (puertoReal == null ? "null" : puertoReal.getClass().getName()) + ", which is not an AporteReinversionPort");
        }
        return adaptador(p).crear(entrada);
    }
}
