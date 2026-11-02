package cl.bcs.fondomutuo.liquidacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.certificacion.CrearCargoAbonoPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto;

/**
 * The bridge between the T6-B tests and the production code of the step (the in-process port of {@code LiquidacionSolicitudAporteRescate.logica}, its JDBC adapter, its JavaCall handler and its Spring wiring),
 * which DOES NOT EXIST yet ("red before code"): the tests COMPILE without it and fail with a clear {@code AssertionError} ("T6-B not implemented: the production class ... does not exist yet") until it is
 * written. This is the ONLY file that knows the names of the new production classes and constants (reached by reflection); everything the tests assume about them is here and in {@code API_ASSUMED_T6B.md}:
 * change the names HERE (never the goldens) if production needs another shape.
 *
 * <p>Assumed production API (package {@code cl.bcs.fondomutuo.liquidacion}), see API_ASSUMED_T6B.md:
 * <pre>
 * LiquidacionContrato.Entrada / InMovimientos / InFiltros / Salida / ResultadoLiquidacion / SolicitudProceso / ModalidadPago / Retencion     public static classes, legacy field names
 * interface LiquidacionPort    { Salida logica(Entrada) throws Exception; }                                  // legacy logica(con, entrada) without the connection, NO recount of the errors
 * interface LiquidacionDatos   { porMovimientos, porFiltro, modalidadesPago, configuracionRetencion, actualizar }   // the SQL of the class (reads and the batch UPDATE)
 * class LiquidacionDatosJdbc   implements LiquidacionDatos; public constructor (ConexionUnidadDeTrabajo)
 * class LiquidacionFachada     implements LiquidacionPort; ONE public constructor (LiquidacionDatos, ConfiguracionSistemaPort, DiccionarioMensajesPort, SecuenciaPort, CrearIngresoEgresoPuerto, CrearCargoAbonoPort, LogPort)
 * class LiquidacionJavaCallHandler   @Component, implements JavaCallHandler; public constructor (LiquidacionPort, Compuertas); G0 first; legacy call(): recount by REFERENCE
 * class LiquidacionConfiguracion     @Configuration; public static final String LISTA = "LIQUIDACION_SOLICITUD_APORTE_RESCATE"; @Bean LiquidacionDatos, @Bean LiquidacionPort
 * JavaCallDispatcher.LIQUIDACION_SOLICITUD_APORTE_RESCATE   the legacy FQN, in LISTA_BLANCA (14), not in CLASES_LECTURA
 * </pre>
 */
public final class ApiT6b {

    public static final String PAQUETE = "cl.bcs.fondomutuo.liquidacion.";
    /** The legacy class (what the router sends in "class"; registry id 1689). */
    public static final String FQN_LEGACY = "cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate";
    public static final String LISTA_ESPERADA = "LIQUIDACION_SOLICITUD_APORTE_RESCATE";

    public static final String CONTRATO = PAQUETE + "LiquidacionContrato";
    public static final String ENTRADA = CONTRATO + "$Entrada";
    public static final String MOVIMIENTO = CONTRATO + "$InMovimientos";
    public static final String FILTRO = CONTRATO + "$InFiltros";
    public static final String SALIDA = CONTRATO + "$Salida";
    public static final String RESULTADO = CONTRATO + "$ResultadoLiquidacion";
    public static final String SOLICITUD = CONTRATO + "$SolicitudProceso";
    public static final String MODALIDAD = CONTRATO + "$ModalidadPago";
    public static final String RETENCION = CONTRATO + "$Retencion";
    public static final String PUERTO = PAQUETE + "LiquidacionPort";
    public static final String DATOS = PAQUETE + "LiquidacionDatos";
    public static final String DATOS_JDBC = PAQUETE + "LiquidacionDatosJdbc";
    public static final String FACHADA = PAQUETE + "LiquidacionFachada";
    public static final String HANDLER = PAQUETE + "LiquidacionJavaCallHandler";
    public static final String CONFIGURACION = PAQUETE + "LiquidacionConfiguracion";

    /** The SQL of the class: file names under cl/bcs/fondomutuo/sql/ and their SHA-256 (the verbatim legacy text). */
    public static final String SQL_POR_MOVIMIENTOS = "LiquidacionSolicitudAporteRescate_SelectSolicitudes.sql";
    public static final String SQL_POR_FILTRO = "LiquidacionSolicitudAporteRescate_SelectSolicitudesFiltros.sql";
    public static final String SQL_ACTUALIZAR = "LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql";
    public static final String SQL_MODALIDADES = "ComunesSolicitudes_SelectModalidadPago.sql";
    public static final String SQL_RETENCION_RUTA = "cl/bcs/APV/sql/ConsultarRetencion.sql";
    public static final Map<String, String> SQL_SHA256 = Map.of(
            SQL_POR_MOVIMIENTOS, "9069fa2a0bb4a52a21eaeb506a8769081b135f86dd4cd63366067966ce31e420",
            SQL_POR_FILTRO, "5889aed6e7be9ceb1ba3870a6c1d9ce8aacb660d1546970424b6db91b7ac02ca",
            SQL_ACTUALIZAR, "704c706b58b3c7fae46a61e8dbefe4202e745dd68d3d7657585ad32d265d1cea",
            SQL_MODALIDADES, "69512e6614364d5db819e82f53e270f22e2213cdc4d8f2d885353858c089812d",
            SQL_RETENCION_RUTA, "26785d286424e99eabb3897b5dfc569deadcd1f2cbb2d0e6599ef32b0db76f63");
    public static final String SECUENCIA_CARTA = "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate";
    public static final String SECUENCIA_COMPROBANTE = "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion";

    private ApiT6b() {
    }

    public static Class<?> cargar(String nombre) {
        try {
            return Class.forName(nombre);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("T6-B not implemented: the production class " + nombre + " does not exist yet (red before code; see API_ASSUMED_T6B.md)", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    /** The value of a public static final String of a production class ({@code LISTA}, the constant of the dispatcher). */
    public static String constante(Class<?> c, String campo) {
        try {
            Field f = c.getField(campo);
            if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers()) || f.getType() != String.class) {
                throw new AssertionError("T6-B: " + c.getSimpleName() + "." + campo + " must be a public static final String");
            }
            return (String) f.get(null);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("T6-B not implemented: " + c.getName() + "." + campo + " does not exist yet (red before code; see API_ASSUMED_T6B.md)", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("T6-B: " + c.getName() + "." + campo + " must be public", e);
        }
    }

    /** {@code LiquidacionConfiguracion.LISTA}. */
    public static String lista() {
        return constante(cargar(CONFIGURACION), "LISTA");
    }

    /** {@code JavaCallDispatcher.LIQUIDACION_SOLICITUD_APORTE_RESCATE}. */
    public static String fqnDelDispatcher() {
        return constante(JavaCallDispatcher.class, "LIQUIDACION_SOLICITUD_APORTE_RESCATE");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // building the production objects
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** {@code new LiquidacionDatosJdbc(uow)}. */
    public static Object datosJdbc(ConexionUnidadDeTrabajo uow) {
        Class<?> c = cargar(DATOS_JDBC);
        try {
            return c.getConstructor(ConexionUnidadDeTrabajo.class).newInstance(uow);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-B: " + DATOS_JDBC + " must have a public constructor (ConexionUnidadDeTrabajo)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T6-B: the constructor of " + DATOS_JDBC + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-B: cannot build " + DATOS_JDBC + ": " + e, e);
        }
    }

    /** {@code new LiquidacionFachada(datos, configuracion, diccionario, secuencias, tesoreria, crearCargoAbono, log)}: {@code datos} must implement {@code LiquidacionDatos}. */
    public static Object fachada(Object datos, ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, SecuenciaPort secuencias, CrearIngresoEgresoPuerto tesoreria,
            CrearCargoAbonoPort crearCargoAbono, LogPort log) {
        Class<?> c = cargar(FACHADA);
        Class<?> puertoDatos = cargar(DATOS);
        Class<?> puerto = cargar(PUERTO);
        Object fachada;
        try {
            Constructor<?> k = c.getConstructor(puertoDatos, ConfiguracionSistemaPort.class, DiccionarioMensajesPort.class, SecuenciaPort.class, CrearIngresoEgresoPuerto.class, CrearCargoAbonoPort.class,
                    LogPort.class);
            fachada = k.newInstance(datos, configuracion, diccionario, secuencias, tesoreria, crearCargoAbono, log);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-B: " + FACHADA + " must have a public constructor (LiquidacionDatos, ConfiguracionSistemaPort, DiccionarioMensajesPort, SecuenciaPort, CrearIngresoEgresoPuerto, "
                    + "CrearCargoAbonoPort, LogPort)", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T6-B: the constructor of " + FACHADA + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-B: cannot build " + FACHADA + ": " + e, e);
        }
        if (!puerto.isInstance(fachada)) throw new AssertionError("T6-B: " + FACHADA + " must implement " + PUERTO);
        return fachada;
    }

    /** {@code new LiquidacionJavaCallHandler(puerto, compuertas)}. */
    public static JavaCallHandler handler(Object puerto, Compuertas compuertas) {
        Class<?> c = cargar(HANDLER);
        try {
            Constructor<?> k = c.getConstructor(cargar(PUERTO), Compuertas.class);
            return (JavaCallHandler) k.newInstance(puerto, compuertas);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-B: " + HANDLER + " must have a public constructor (LiquidacionPort, Compuertas) and implement JavaCallHandler", e);
        } catch (ClassCastException e) {
            throw new AssertionError("T6-B: " + HANDLER + " must implement JavaCallHandler", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("T6-B: the constructor of " + HANDLER + " raised " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-B: cannot build " + HANDLER + ": " + e, e);
        }
    }

    /** The handler with its list OPEN (the name is the production constant): the equivalence run. */
    public static JavaCallHandler handlerAbierto(Object puerto) {
        return handler(puerto, new Compuertas(lista(), false, false, false, false, false));
    }

    /** The legacy {@code call(Connection, JsonObject)}: the connection is the unit of work of the host and the handler never touches it (null here). */
    public static JsonObject llamarPorHandler(Object puerto, JsonObject entrada) throws Exception {
        return handlerAbierto(puerto).call(null, entrada);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the typed port
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** What the unit tests need from the port: the typed entry in, the typed output out (whatever the port throws comes out UNCHANGED, same instance). */
    public interface PorLiquidacion {
        Object logica(Object entrada) throws Exception;

        /** JSON in (the legacy decode with the plain codec of the module), the output as JSON with nulls written (what the legacy {@code call} writes before the recount) */
        JsonObject llamar(JsonObject entrada) throws Exception;
    }

    public static PorLiquidacion comoPuerto(Object bean) {
        Class<?> puerto = cargar(PUERTO);
        if (!puerto.isInstance(bean)) throw new AssertionError("T6-B: " + bean.getClass().getName() + " must implement " + PUERTO);
        Method logica;
        try {
            logica = puerto.getMethod("logica", cargar(ENTRADA));
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T6-B: " + PUERTO + " must declare logica(LiquidacionContrato.Entrada)", e);
        }
        return new PorLiquidacion() {
            @Override
            public Object logica(Object entrada) throws Exception {
                try {
                    return logica.invoke(bean, entrada);
                } catch (InvocationTargetException e) {
                    ApiT6b.<RuntimeException>sneaky(e.getCause());
                    return null;
                }
            }

            @Override
            public JsonObject llamar(JsonObject in) throws Exception {
                Object salida = logica(entradaDe(in));
                return JsonParser.parseString(JsonLegacy.conNulos().toJson(salida)).getAsJsonObject();
            }
        };
    }

    /** The legacy {@code gson.fromJson(in.toString(), ..._IN.class)} (Utils.getGsonBuilder(): the codec of the module with nulls). */
    public static Object entradaDe(JsonObject in) {
        return JsonLegacy.conNulos().fromJson(in.toString(), cargar(ENTRADA));
    }

    /** What a fake of the port does with the typed entry: answers the typed output or throws whatever it likes. */
    @FunctionalInterface
    public interface LogicaFalsa {
        Object logica(Object entrada) throws Throwable;
    }

    /** A proxy of {@code LiquidacionPort}: the handler tests need a port that returns what they say and records nothing else. */
    public static Object puertoFalso(LogicaFalsa f) {
        Class<?> puerto = cargar(PUERTO);
        if (!puerto.isInterface()) throw new AssertionError("T6-B: " + PUERTO + " must be an interface (a port)");
        return Proxy.newProxyInstance(puerto.getClassLoader(), new Class<?>[] { puerto }, (p, m, a) -> {
            if (m.getDeclaringClass() == Object.class) {
                switch (m.getName()) {
                    case "equals":
                        return p == a[0];
                    case "hashCode":
                        return System.identityHashCode(p);
                    default:
                        return "puertoFalso";
                }
            }
            return f.logica(a[0]);
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // a fake of the data port: what each call got, what it answers (the unit tests of the facade)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** What the fake of the data port answers (null = an empty list / no-op). */
    public static class RespuestasDatos {
        public List<Object> porMovimientos = new ArrayList<>();
        public List<Object> porFiltro = new ArrayList<>();
        public List<Object> modalidades = new ArrayList<>();
        public List<Object> retencion = new ArrayList<>();
        public Throwable falla;
        /** called with the argument of {@code actualizar} AT THE MOMENT of the call (the list is live: the facade keeps mutating the same solicitudes) */
        public java.util.function.Consumer<List<Object>> alActualizar;
        /** called with the name of EVERY call to the data port, before it is answered (the tests keep ONE ordered list of events) */
        public java.util.function.Consumer<String> alLlamar;
    }

    /** One call to the fake: method name and arguments as the facade passed them. */
    public static final class LlamadaDatos {
        public final String metodo;
        public final List<Object> argumentos;

        LlamadaDatos(String metodo, List<Object> argumentos) {
            this.metodo = metodo;
            this.argumentos = argumentos;
        }
    }

    /** A proxy of {@code LiquidacionDatos} that records every call in order and answers from {@code respuestas}. The ONE data port has exactly the five methods named below (see API_ASSUMED_T6B.md). */
    public static Object datosFalso(List<LlamadaDatos> llamadas, RespuestasDatos respuestas) {
        Class<?> datos = cargar(DATOS);
        if (!datos.isInterface()) throw new AssertionError("T6-B: " + DATOS + " must be an interface (a port)");
        return Proxy.newProxyInstance(datos.getClassLoader(), new Class<?>[] { datos }, (p, m, args) -> {
            if (m.getDeclaringClass() == Object.class) return m.invoke(llamadas, args);
            llamadas.add(new LlamadaDatos(m.getName(), args == null ? List.of() : new ArrayList<>(java.util.Arrays.asList(args))));
            if (respuestas.alLlamar != null) respuestas.alLlamar.accept(m.getName());
            if (respuestas.falla != null) throw respuestas.falla;
            switch (m.getName()) {
                case "porMovimientos":
                    return new ArrayList<>(respuestas.porMovimientos);
                case "porFiltro":
                    return new ArrayList<>(respuestas.porFiltro);
                case "modalidadesPago":
                    return new ArrayList<>(respuestas.modalidades);
                case "configuracionRetencion":
                    return new ArrayList<>(respuestas.retencion);
                case "actualizar":
                    if (respuestas.alActualizar != null) respuestas.alActualizar.accept(new ArrayList<>((List<?>) args[0]));
                    return null;
                default:
                    throw new AssertionError("T6-B: " + DATOS + "." + m.getName() + " is not one of the five methods of the data port (see API_ASSUMED_T6B.md)");
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // reflection helpers over the typed contract (public fields by legacy name)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static Object nuevo(String clase) {
        try {
            return cargar(clase).getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-B: " + clase + " must be a public static class with a public no-argument constructor", e);
        }
    }

    /** Sets public fields by name; the value is converted to the declared type (Long, String, BigDecimal, java.sql.Date from "yyyy-MM-dd"). */
    public static Object con(Object o, Object... paresNombreValor) {
        for (int i = 0; i < paresNombreValor.length; i += 2) {
            String nombre = (String) paresNombreValor[i];
            Object valor = paresNombreValor[i + 1];
            try {
                Field f = o.getClass().getField(nombre);
                f.set(o, convertir(valor, f.getType()));
            } catch (NoSuchFieldException e) {
                throw new AssertionError("T6-B: " + o.getClass().getName() + " must have the public field " + nombre + " (legacy name)", e);
            } catch (IllegalAccessException e) {
                throw new AssertionError("T6-B: the field " + nombre + " must be public", e);
            }
        }
        return o;
    }

    private static Object convertir(Object v, Class<?> tipo) {
        if (v == null) return null;
        if (tipo == Long.class && v instanceof Number n) return n.longValue();
        if (tipo == BigDecimal.class && !(v instanceof BigDecimal)) return new BigDecimal(v.toString());
        if (tipo == Date.class && v instanceof String s) return Date.valueOf(s);
        return v;
    }

    /** A public field by name. */
    public static Object campo(Object o, String nombre) {
        try {
            return o.getClass().getField(nombre).get(o);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("T6-B: " + o.getClass().getName() + " must have the public field " + nombre, e);
        }
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
            throw new AssertionError("T6-B: the fields of " + o.getClass().getName() + " must be public", e);
        }
        return m;
    }
}
