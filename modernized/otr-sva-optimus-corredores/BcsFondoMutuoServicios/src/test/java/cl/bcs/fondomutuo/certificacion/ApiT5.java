package cl.bcs.fondomutuo.certificacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * The bridge between the T5 tests and the production code of FRONT J (the closing) and of the FACADE of the recursion, which DO NOT EXIST yet ("red before code"): the tests COMPILE without
 * them and fail with a clear {@code AssertionError} ("T5 not implemented: the production class ... does not exist yet") until they are written. This is the ONLY file that knows the names of the
 * new production classes and fields (reached by reflection); everything the tests assume about them is here and in {@code API_ASSUMED_T5.md}: change the names HERE (never the goldens) if
 * production needs another shape.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.certificacion}), see API_ASSUMED_T5.md:
 * <pre>
 * CertificarAporteRescateFabrica.Dependencias   gains public fields (the existing ones stay): flujoCaja, procesarCustodia, calculaCustodia, comisiones, formato, crearCargoAbono, tesoreria, spot,
 *                                               facturacion, crearAporteReinversion, escrituraMonto, escrituraCierre, notificacionCertificacion, notificacionOwp, agregarLog, compuertas,
 *                                               envolverHija. With posteriores == null, CertificarAporteRescateFabrica.crear(d) returns THE FACADE (all the fronts A..J behind one port).
 * public final class CertificarAporteRescateFachada implements CertificarAporteRescatePort, CertificacionHijaPort    // the facade; certificar(Entrada) is the recursion (depth + 1, guarded)
 * public interface NotificacionCertificacionPort { void notificar(Contrato.NotificarEstadoOrdenEntrada e) throws Exception; }   // FM-CER-62, gate G8
 * public interface NotificacionOrdenesOwpPort    { void notificar(List&lt;Contrato.OrdenOwp&gt; ordenes) throws Exception; }          // FM-CER-65, gate G8
 * public interface CertificarAporteRescateEscrituraCierre { void actualizarSolicitudes(List&lt;SolicitudProceso&gt; s); }               // FM-CER-61; JDBC adapter ...Jdbc(ConexionUnidadDeTrabajo)
 * public final class CertificarAporteRescateCierre                                                                         // front J (unit): the UPDATE, notifications, log, output, simulation
 * Compuertas                                    gains a 10-argument constructor (the 9th and last booleans: facturacion, notificaciones) and g8Notificaciones() (closed by default)
 * </pre>
 */
final class ApiT5 {

    static final String PAQUETE = ApiT2.PAQUETE;
    static final String FACHADA = PAQUETE + "CertificarAporteRescateFachada";
    static final String CIERRE = PAQUETE + "CertificarAporteRescateCierre";
    static final String PUERTO_NOTIFICACION = PAQUETE + "NotificacionCertificacionPort";
    static final String PUERTO_OWP = PAQUETE + "NotificacionOrdenesOwpPort";
    static final String ESCRITURA_CIERRE = PAQUETE + "CertificarAporteRescateEscrituraCierre";
    static final String ESCRITURA_CIERRE_JDBC = PAQUETE + "CertificarAporteRescateEscrituraCierreJdbc";
    static final String ENTRADA_NOTIFICACION = PAQUETE + "CertificarAporteRescateContrato$NotificarEstadoOrdenEntrada";
    static final String ORDEN_OWP = PAQUETE + "CertificarAporteRescateContrato$OrdenOwp";

    /** The fields of {@code CertificarAporteRescateFabrica.Dependencias} that T5 ADDS (the existing ones are set directly by {@link CasoCerT5}). */
    static final String[] CAMPOS_NUEVOS = { "flujoCaja", "procesarCustodia", "calculaCustodia", "comisiones", "formato", "crearCargoAbono", "tesoreria", "spot", "facturacion", "crearAporteReinversion",
            "escrituraMonto", "escrituraCierre", "notificacionCertificacion", "notificacionOwp", "agregarLog", "compuertas", "envolverHija" };

    private ApiT5() {
    }

    /** What a port answers: the name of the scripted collaborator (single-method port) or one name per method (Facturacion). */
    static final class Puerto {
        final Map<String, String> metodos = new LinkedHashMap<>();
        final String unico;

        Puerto(String colaborador) {
            this.unico = colaborador;
        }

        Puerto(String... pares) {
            this.unico = null;
            for (int i = 0; i < pares.length; i += 2) metodos.put(pares[i], pares[i + 1]);
        }
    }

    /** The scripted collaborator that answers a call of a port: {@code (collaborator, argument as JSON or null, return type) -> answer}. May throw whatever the script says. */
    @FunctionalInterface
    interface Responder {
        Object responder(String colaborador, Object argumento, Class<?> retorno) throws Throwable;
    }

    static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T5 not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T5.md)", e);
        }
    }

    /** A proxy of a port interface answered through the {@link Responder}; the arguments go to the responder as they are, the answer is converted by the caller to the return type. */
    static Object proxy(Class<?> interfaz, Puerto puerto, Responder responder) {
        if (!interfaz.isInterface()) throw new AssertionError("T5: " + interfaz.getName() + " must be an interface (a port)");
        return Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] { interfaz }, (p, m, args) -> {
            if (m.getDeclaringClass() == Object.class) return m.invoke(puerto, args);
            String colaborador = puerto.unico != null ? puerto.unico : puerto.metodos.get(m.getName());
            if (colaborador == null) throw new AssertionError("T5: the port " + interfaz.getSimpleName() + " has the unexpected method " + m);
            if (puerto.unico != null && interfaz.getMethods().length != 1) {
                throw new AssertionError("T5: the port " + interfaz.getSimpleName() + " must have exactly ONE method, it has " + interfaz.getMethods().length);
            }
            Object arg = args == null || args.length == 0 ? null : args[0];
            if (args != null && args.length > 1) throw new AssertionError("T5: the port method " + m + " must take a single argument");
            return responder.responder(colaborador, arg, m.getReturnType());
        });
    }

    /** Sets the NEW fields of {@code CertificarAporteRescateFabrica.Dependencias} by name; a value that is a {@link Puerto} becomes a proxy of the TYPE of the field. */
    static void asignar(Object dependencias, Map<String, Object> valores, Responder responder) {
        for (Map.Entry<String, Object> e : valores.entrySet()) {
            Field f;
            try {
                f = dependencias.getClass().getField(e.getKey());
            } catch (NoSuchFieldException ex) {
                throw new AssertionError("T5 not implemented: CertificarAporteRescateFabrica.Dependencias." + e.getKey() + " does not exist yet (red before code; see API_ASSUMED_T5.md)", ex);
            }
            Object valor = e.getValue();
            if (valor instanceof Puerto p) valor = proxy(f.getType(), p, responder);
            try {
                f.set(dependencias, valor);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new AssertionError("T5: Dependencias." + e.getKey() + " has the type " + f.getType().getName() + " and cannot take " + (valor == null ? "null" : valor.getClass().getName()), ex);
            }
        }
    }

    /** {@code new Compuertas(listas, tesoreria, spot, flujoCaja, owp, custodia, custodiaNormal, comisionesEnProceso, facturacion, notificaciones)} (the 10-argument constructor T5 adds). */
    static Compuertas compuertas(boolean g1, boolean g2, boolean g3, boolean g4, boolean gCustodia, boolean g6, boolean g8) {
        try {
            Constructor<Compuertas> c = Compuertas.class.getConstructor(String.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class,
                    boolean.class, boolean.class);
            return c.newInstance("", g1, g2, g3, g4, gCustodia, gCustodia, false, g6, g8);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T5 not implemented: Compuertas has no 10-argument constructor (..., boolean facturacion, boolean notificaciones) yet (red before code; see API_ASSUMED_T5.md)", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AssertionError("T5: cannot build Compuertas: " + e, e);
        }
    }

    /** {@code Compuertas.g8Notificaciones()}: raises ServicioNoServidoException when G8 is closed. */
    static Method g8() {
        try {
            return Compuertas.class.getMethod("g8Notificaciones");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T5 not implemented: Compuertas.g8Notificaciones() does not exist yet (red before code; see API_ASSUMED_T5.md)", e);
        }
    }

    /** Builds the JDBC adapter of the UPDATE of FM-CER-61 over the unit of work of the case. */
    static Object escrituraCierre(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo uow) {
        Class<?> c = cargar(ESCRITURA_CIERRE_JDBC);
        try {
            return c.getConstructor(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo.class).newInstance(uow);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T5: " + ESCRITURA_CIERRE_JDBC + " must have a public constructor (ConexionUnidadDeTrabajo)", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AssertionError("T5: cannot build " + ESCRITURA_CIERRE_JDBC + ": " + e, e);
        }
    }

    /** The depth-guard hook: the facade applies {@code envolverHija} to the guarded recursion port it hands to the switch unit; the harness uses it to RECORD the child (entry, answer, inner calls). */
    static UnaryOperator<CertificacionHijaPort> envoltura(UnaryOperator<CertificacionHijaPort> f) {
        return f;
    }
}
