package cl.bcs.fondomutuo.certificacionporfiltros;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * The bridge between the T6-A tests and the production code of the step (the in-process port of {@code CertificarPorFiltrosSolicitudAporteRescate}, its read-only adapter and its Spring wiring),
 * which DOES NOT EXIST yet ("red before code"): the tests COMPILE without it and fail with a clear {@code AssertionError} ("T6-A not implemented: the production class ... does not exist yet") until
 * it is written. This is the ONLY file that knows the names of the new production classes (reached by reflection); everything the tests assume about them is here and in
 * {@code API_ASSUMED_T6A.md}: change the names HERE (never the goldens) if production needs another shape.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.certificacionporfiltros}), see API_ASSUMED_T6A.md:
 * <pre>
 * CertificarPorFiltrosContrato.Entrada / .InFiltros / .Salida    public static classes with the legacy field names (plain Gson reads and writes them like the legacy)
 * interface CertificarPorFiltrosPort           { Salida logica(Entrada) throws Exception; }                 // legacy logica(con, entrada) without the connection
 * interface CertificarPorFiltrosDatos          { List&lt;CertificarAporteRescateContrato.InSolicitudes&gt; solicitudes(InFiltros filtro, String fechaCertificacion, Long idEmpresa); }
 * class CertificarPorFiltrosDatosJdbc          implements ...Datos; public constructor (ConexionUnidadDeTrabajo); reads SQL CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql
 * class CertificarPorFiltrosFachada            implements ...Port;  public constructor (CertificarAporteRescatePort, CertificarPorFiltrosDatos); final fields only
 * class CertificarPorFiltrosConfiguracion      @Configuration; @Bean CertificarPorFiltrosDatos, @Bean CertificarPorFiltrosPort; NO list constant, NO handler, NOT served
 * </pre>
 */
public final class ApiT6a {

    public static final String PAQUETE = "cl.bcs.fondomutuo.certificacionporfiltros.";
    /** The legacy class that is NOT served over HTTP (in-process only, decision of the user). */
    public static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate";
    public static final String SQL_SELECT = "CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql";
    /** SHA-256 of the legacy SQL file (the SQL of the module must be the verbatim legacy text). */
    public static final String SQL_SELECT_SHA256 = "1187dfe3e85966b8ebb9ccb84c1c68fe84263d024f5b6b680652bbd0eb0e5b0a";

    public static final String CONTRATO = PAQUETE + "CertificarPorFiltrosContrato";
    public static final String ENTRADA = CONTRATO + "$Entrada";
    public static final String FILTRO = CONTRATO + "$InFiltros";
    public static final String SALIDA = CONTRATO + "$Salida";
    public static final String PUERTO = PAQUETE + "CertificarPorFiltrosPort";
    public static final String DATOS = PAQUETE + "CertificarPorFiltrosDatos";
    public static final String DATOS_JDBC = PAQUETE + "CertificarPorFiltrosDatosJdbc";
    public static final String FACHADA = PAQUETE + "CertificarPorFiltrosFachada";
    public static final String CONFIGURACION = PAQUETE + "CertificarPorFiltrosConfiguracion";

    private ApiT6a() {
    }

    public static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T6-A not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T6A.md)", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    /** What the tests need from the port: JSON in, JSON out (the codec of the legacy {@code call}), whatever the port throws comes out UNCHANGED (same instance). */
    public interface PorFiltros {
        JsonObject llamar(JsonObject entrada) throws Exception;

        /** the typed entry (built by {@link ApiT6a#entradaDe(JsonObject)}) to the typed port; returns the typed Salida */
        Object logica(Object entrada) throws Exception;
    }

    /** {@code new CertificarPorFiltrosFachada(certificar, datos)}: {@code datos} must implement {@code CertificarPorFiltrosDatos}. */
    public static PorFiltros crear(CertificarAporteRescatePort certificar, Object datos) {
        Class<?> c = cargar(FACHADA);
        Class<?> puertoDatos = cargar(DATOS);
        Class<?> puerto = cargar(PUERTO);
        Object fachada;
        try {
            Constructor<?> k = c.getConstructor(CertificarAporteRescatePort.class, puertoDatos);
            fachada = k.newInstance(certificar, datos);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-A: " + FACHADA + " must have a public constructor (CertificarAporteRescatePort, CertificarPorFiltrosDatos)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T6-A: the constructor of " + FACHADA + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-A: cannot build " + FACHADA + ": " + e, e);
        }
        if (!puerto.isInstance(fachada)) throw new AssertionError("T6-A: " + FACHADA + " must implement " + PUERTO);
        return porFiltros(puerto, fachada);
    }

    /** The same bridge over any object that implements the port (the Spring bean). */
    public static PorFiltros porFiltros(Class<?> puerto, Object bean) {
        Method logica;
        try {
            logica = puerto.getMethod("logica", cargar(ENTRADA));
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-A: " + PUERTO + " must declare logica(CertificarPorFiltrosContrato.Entrada)", e);
        }
        return new PorFiltros() {
            @Override
            public JsonObject llamar(JsonObject in) throws Exception {
                Object salida = logica(entradaDe(in));
                return JsonParser.parseString(JsonLegacy.plano().toJson(salida)).getAsJsonObject();
            }

            @Override
            public Object logica(Object entrada) throws Exception {
                try {
                    return logica.invoke(bean, entrada);
                } catch (InvocationTargetException e) {
                    ApiT6a.<RuntimeException>sneaky(e.getCause());
                    return null;
                }
            }
        };
    }

    /** The legacy {@code gson.fromJson(in.toString(), Entrada.class)} with the plain Gson. */
    public static Object entradaDe(JsonObject in) {
        return JsonLegacy.plano().fromJson(in.toString(), cargar(ENTRADA));
    }

    /** {@code new CertificarPorFiltrosDatosJdbc(uow)}. */
    public static Object datosJdbc(ConexionUnidadDeTrabajo uow) {
        Class<?> c = cargar(DATOS_JDBC);
        try {
            return c.getConstructor(ConexionUnidadDeTrabajo.class).newInstance(uow);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-A: " + DATOS_JDBC + " must have a public constructor (ConexionUnidadDeTrabajo)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T6-A: the constructor of " + DATOS_JDBC + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-A: cannot build " + DATOS_JDBC + ": " + e, e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // a fake of the read port: what each query got, what it answers
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** One call of the select: the filter AS THE PORT RECEIVED IT (public fields by name, nulls included), the date and the company. */
    public static final class ConsultaVista {
        public final Map<String, Object> filtro;
        public final String fechaCertificacion;
        public final Long idEmpresa;

        ConsultaVista(Map<String, Object> filtro, String fechaCertificacion, Long idEmpresa) {
            this.filtro = filtro;
            this.fechaCertificacion = fechaCertificacion;
            this.idEmpresa = idEmpresa;
        }
    }

    /** What the fake answers: the ids of the solicitudes the select returns (in order, repeats allowed), or throws. */
    @FunctionalInterface
    public interface Respuesta {
        List<Long> responder(int numeroDeConsulta, ConsultaVista consulta) throws Throwable;
    }

    /** A proxy of {@code CertificarPorFiltrosDatos} that records every call in {@code consultas} and answers {@code InSolicitudes} built from the ids. */
    public static Object datosFalso(List<ConsultaVista> consultas, Respuesta respuesta) {
        Class<?> datos = cargar(DATOS);
        if (!datos.isInterface()) throw new AssertionError("T6-A: " + DATOS + " must be an interface (a port)");
        Method[] metodos = datos.getMethods();
        if (metodos.length != 1) throw new AssertionError("T6-A: " + DATOS + " must have exactly ONE method, it has " + metodos.length);
        return Proxy.newProxyInstance(datos.getClassLoader(), new Class<?>[] { datos }, (p, m, args) -> {
            if (m.getDeclaringClass() == Object.class) return m.invoke(consultas, args);
            if (args == null || args.length != 3 || !(args[1] == null || args[1] instanceof String) || !(args[2] == null || args[2] instanceof Long)) {
                throw new AssertionError("T6-A: " + DATOS + "." + m.getName() + " must take (InFiltros filtro, String fechaCertificacion, Long idEmpresa)");
            }
            ConsultaVista c = new ConsultaVista(camposPublicos(args[0]), (String) args[1], (Long) args[2]);
            consultas.add(c);
            List<Long> ids = respuesta.responder(consultas.size(), c);
            List<CertificarAporteRescateContrato.InSolicitudes> out = new ArrayList<>();
            for (Long id : ids) {
                CertificarAporteRescateContrato.InSolicitudes s = new CertificarAporteRescateContrato.InSolicitudes();
                s.IdSolicitudAporteRescate = id;
                out.add(s);
            }
            return out;
        });
    }

    /** The public instance fields of an object by name, nulls included (a plain Gson would drop them). */
    public static Map<String, Object> camposPublicos(Object o) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (o == null) return m;
        try {
            for (Field f : o.getClass().getFields()) {
                if (!Modifier.isStatic(f.getModifiers())) m.put(f.getName(), f.get(o));
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError("T6-A: the fields of " + o.getClass().getName() + " must be public", e);
        }
        return m;
    }
}
