package cl.bcs.replica.caracterizacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.replica.oraculo.GuionColaboradores;

/**
 * The ONLY file of the characterization tests that reaches the production classes of BcsReplica, and it does it by reflection: the production units do not exist yet ("red before
 * code"), so the tests compile and run without them and fail with a clear {@code AssertionError} ("not implemented: ...") until they are written. If production needs another name or
 * shape, change it HERE (never the goldens). Everything the tests assume is listed in {@code API_ASSUMED.md} (same directory); every class is public and every method public.
 *
 * <p>Assumed production API:
 * <pre>
 * package cl.bcs.replica.kit
 *   public final class Compuertas { Compuertas(boolean triggers, boolean respaldoSebra, boolean respaldoOptimus) }       // closed gates by default in production
 *   public class ServicioNoServidoException extends RuntimeException                                                      // what a closed gate raises
 * package cl.bcs.replica.port.out        (every method {@code throws Exception}; the Connection is always the first argument)
 *   ConsultarEmpresaPort      { Long idPais(Connection, Long idEmpresa) }                                                // legacy ConsultarEmpresa.logica(...).IdPais
 *   ObtenerDiaSiguientePort   { JsonObject logica(Connection, JsonObject entrada) }                                      // legacy ObtenerDiaSiguiente.logica, JSON of In / Out
 *   FeriadosPort              { boolean esDiaHabil(Connection, String fecha, Long idPais) }                              // legacy Feriados.esDiaHabil
 *   ConfiguracionSistemaPort  { long parametroLong(Connection, Long idEmpresa, String modulo, String parametro); String parametroString(same args) }
 *   FechaSistemaPort          { String fechaSistema(Connection, String esquema, long idEmpresa) }                        // legacy Utils.getFechaSistema(conn, "BSCL", idEmpresa)
 *   SecuenciaPort             { List<Long> siguientes(Connection, String secuencia, Long cantidad) }                     // legacy Utils.getSecuencia
 *   TriggersReplicaPort       { void desactivar(Connection); void reactivar(Connection) }                                // DROP / CREATE TRIGGER (gate G-TRG)
 *   RespaldoSebraPort         { void respaldarPeriodo(Connection, String codPer); void respaldarDiario(Connection, String codPer, String codDia) }   // gate G-SEB
 *   RespaldoOptimusPort       { void respaldarDiaActual(Connection, String fechaSistema); void copiarDiaInhabil(Connection, String fechaOrigen, String fechaDestino) }   // gate G-OPT
 *   ColaboradoresNoPortados   implements the six collaborator ports above; every method throws ServicioNoServidoException
 * package cl.bcs.replica.adapter.out.jdbc    (public no-argument constructors; they send the SQL of the legacy through the connection they are given)
 *   TriggersReplicaJdbc, RespaldoSebraJdbc, RespaldoOptimusJdbc
 * package cl.bcs.replica.envio
 *   EnviarTablaReplicaServicio(Compuertas, ConsultarEmpresaPort, ObtenerDiaSiguientePort, FeriadosPort, ConfiguracionSistemaPort, TriggersReplicaPort, java.time.Clock)
 *     void logica(Connection, Long idEmpresa, String proceso, Map&lt;String,Object&gt; parametros) throws Exception
 *     List&lt;Workstack&gt; respaldarDataDiaInhabil(Connection, Long idEmpresa, String fechaOrigen, String tablaWorkstack, String parametrosWorkstack) throws Exception
 *     JsonObject call(Connection, JsonObject) throws Exception                                                            // returns null, like the legacy stub
 * package cl.bcs.replica.respaldo
 *   ReplicaUtilsServicio(Compuertas, ConfiguracionSistemaPort, ConsultarEmpresaPort, ObtenerDiaSiguientePort, FeriadosPort, RespaldoSebraPort, RespaldoOptimusPort)
 *     void procesarRespaldoSebra(Connection, String fechaSistemaEjecucion, Long idEmpresa) throws Exception
 *     void procesarRespaldoOptimus(Connection, String fechaSistemaEjecucion, Long idEmpresa) throws Exception
 *     void procesarRespaldoDiarioCarterasSebra(Connection, String fechaSistemaEjecucion, Long idEmpresa) throws Exception
 *     void guardarEnvioPostPauta(Connection, String tabla, String fechaSistema, String fechaEnvio) throws Exception
 *     JsonObject call(Connection, JsonObject) throws Exception                                                            // returns null
 * package cl.bcs.replica.inicio
 *   IniciarReplicaFull(SecuenciaPort, FechaSistemaPort, Clock) and IniciarReplicaDelta(same)
 *     JsonObject logica(Connection, Long idEmpresa, Long idUsuario) throws Exception     // {"P_OUT_MENSAJES":[{id,tipo,codigo,descripcion}],"P_OUT_ERRORESCONTADOR":0}
 *     JsonObject call(Connection, JsonObject entrada) throws Exception                   // the legacy JavaCall entry: the same JSON, the counter recomputed from the messages
 * package cl.bcs.replica.domain
 *   Workstack: the fields Key, Table, Action, Status, Stamp, Parameters (public fields with those names, or accessors key(), table()... of a record)
 * </pre>
 */
final class ApiReplica {
    static final String K = "cl.bcs.replica.kit.";
    static final String P = "cl.bcs.replica.port.out.";
    static final String J = "cl.bcs.replica.adapter.out.jdbc.";

    private ApiReplica() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------ reflection helpers

    static Class<?> clase(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("not implemented: the production class " + fqcn + " does not exist yet (see API_ASSUMED.md)");
        }
    }

    private static Class<?> envolver(Class<?> t) {
        if (t == boolean.class) return Boolean.class;
        if (t == long.class) return Long.class;
        if (t == int.class) return Integer.class;
        return t;
    }

    static Object construir(String fqcn, Object... args) {
        Class<?> c = clase(fqcn);
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() != args.length) continue;
            boolean ok = true;
            Class<?>[] t = k.getParameterTypes();
            for (int i = 0; i < t.length; i++) {
                if (args[i] != null && !envolver(t[i]).isInstance(args[i])) ok = false;
            }
            if (!ok) continue;
            try {
                return k.newInstance(args);
            } catch (InvocationTargetException e) {
                throw sneaky(e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("not implemented: constructor of " + fqcn + " cannot be called: " + e);
            }
        }
        throw new AssertionError("not implemented: " + fqcn + " has no public constructor with " + args.length + " arguments compatible with the assumed API (see API_ASSUMED.md)");
    }

    static Object invocar(Object o, String metodo, Object... args) throws Throwable {
        for (Method m : o.getClass().getMethods()) {
            if (!m.getName().equals(metodo) || m.getParameterCount() != args.length || Modifier.isStatic(m.getModifiers())) continue;
            boolean ok = true;
            Class<?>[] t = m.getParameterTypes();
            for (int i = 0; i < t.length; i++) {
                if (args[i] != null && !envolver(t[i]).isInstance(args[i])) ok = false;
                if (args[i] == null && t[i].isPrimitive()) ok = false;
            }
            if (!ok) continue;
            try {
                return m.invoke(o, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        throw new AssertionError("not implemented: " + o.getClass().getName() + " has no public method " + metodo + " with " + args.length + " arguments compatible with the assumed API (see API_ASSUMED.md)");
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
        throw (E) t;
    }

    // ------------------------------------------------------------------------------------------------------------------------------ collaborator ports

    /** A port of the module answered by the case script ({@link GuionColaboradores}): the same record and the same answers as the oracle doubles. */
    static Object puerto(String interfaz, GuionColaboradores guion) {
        Class<?> i = clase(P + interfaz);
        return Proxy.newProxyInstance(ApiReplica.class.getClassLoader(), new Class<?>[] { i }, (p, m, a) -> {
            JsonArray args = new JsonArray();
            for (int k = 1; a != null && k < a.length; k++) args.add(aJson(a[k]));
            String clave = interfaz + "." + m.getName();
            switch (clave) {
            case "ConsultarEmpresaPort.idPais": {
                JsonElement r = guion.responder("ConsultarEmpresa", "logica", args);
                if (!r.isJsonObject() || !r.getAsJsonObject().has("IdPais") || r.getAsJsonObject().get("IdPais").isJsonNull()) return null;
                return r.getAsJsonObject().get("IdPais").getAsLong();
            }
            case "ObtenerDiaSiguientePort.logica":
                return guion.responder("ObtenerDiaSiguiente", "logica", args).getAsJsonObject();
            case "FeriadosPort.esDiaHabil":
                return guion.responder("Feriados", "esDiaHabil", args).getAsBoolean();
            case "ConfiguracionSistemaPort.parametroLong":
                return Long.parseLong(texto(guion.responder("ConfiguracionSistema", "getParametroConfiguracionLong", args)));
            case "ConfiguracionSistemaPort.parametroString":
                return texto(guion.responder("ConfiguracionSistema", "getParametroConfiguracionString", args));
            case "FechaSistemaPort.fechaSistema":
                return texto(guion.responder("Utils", "getFechaSistema", args));
            case "SecuenciaPort.siguientes": {
                List<Long> out = new ArrayList<>();
                if (a[2] == null || ((Long) a[2]) <= 0) return out;
                JsonElement r = guion.responder("Utils", "getSecuencia", args);
                if (r != null && r.isJsonArray()) for (JsonElement e : r.getAsJsonArray()) out.add(e.getAsLong());
                return out;
            }
            default:
                throw new AssertionError("API_ASSUMED: the port " + interfaz + " has the unexpected method " + m.getName());
            }
        });
    }

    private static String texto(JsonElement r) {
        return r == null || r.isJsonNull() ? null : r.getAsString();
    }

    private static JsonElement aJson(Object o) {
        if (o == null) return JsonNull.INSTANCE;
        if (o instanceof JsonElement) return (JsonElement) o;
        if (o instanceof Number) return new JsonPrimitive((Number) o);
        if (o instanceof Boolean) return new JsonPrimitive((Boolean) o);
        return new JsonPrimitive(o.toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------ context

    /** What a run needs: the gates, the collaborators (scripted, or the closed "not ported" default) and the clock. */
    static final class Contexto {
        final boolean triggers;
        final boolean respaldoSebra;
        final boolean respaldoOptimus;
        final GuionColaboradores guion;
        final boolean colaboradoresNoPortados;
        final Clock reloj;
        final Object compuertasDadas;

        Contexto(boolean triggers, boolean respaldoSebra, boolean respaldoOptimus, GuionColaboradores guion, boolean colaboradoresNoPortados, Clock reloj) {
            this(triggers, respaldoSebra, respaldoOptimus, guion, colaboradoresNoPortados, reloj, null);
        }

        Contexto(boolean triggers, boolean respaldoSebra, boolean respaldoOptimus, GuionColaboradores guion, boolean colaboradoresNoPortados, Clock reloj, Object compuertasDadas) {
            this.triggers = triggers;
            this.respaldoSebra = respaldoSebra;
            this.respaldoOptimus = respaldoOptimus;
            this.guion = guion;
            this.colaboradoresNoPortados = colaboradoresNoPortados;
            this.reloj = reloj;
            this.compuertasDadas = compuertasDadas;
        }

        /** every gate open and the collaborators scripted: what the legacy does (the legacy has no gates) */
        static Contexto abierto(GuionColaboradores guion, String reloj) {
            return new Contexto(true, true, true, guion, false, relojFijo(reloj));
        }

        Contexto conCompuertas(boolean triggers, boolean sebra, boolean optimus) {
            return new Contexto(triggers, sebra, optimus, guion, colaboradoresNoPortados, reloj);
        }

        Contexto conColaboradoresNoPortados() {
            return new Contexto(triggers, respaldoSebra, respaldoOptimus, guion, true, reloj);
        }

        /** the Compuertas object as built by Spring from the properties (the production default) instead of the three flags */
        Contexto conCompuertasObjeto(Object compuertas) {
            return new Contexto(triggers, respaldoSebra, respaldoOptimus, guion, colaboradoresNoPortados, reloj, compuertas);
        }

        Object compuertas() {
            if (compuertasDadas != null) return compuertasDadas;
            return construir(K + "Compuertas", triggers, respaldoSebra, respaldoOptimus);
        }

        Object port(String nombre) {
            return colaboradoresNoPortados ? construir(P + "ColaboradoresNoPortados") : puerto(nombre, guion);
        }
    }

    static Clock relojFijo(String texto) {
        return Clock.fixed(LocalDateTime.parse(texto.replace(' ', 'T')).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    /**
     * The Compuertas bean as Spring builds it from the properties {@code replica.compuertas.triggers}, {@code replica.compuertas.respaldo-sebra} and {@code replica.compuertas.respaldo-optimus}
     * (all three false when absent: the production default is every gate closed).
     */
    static Object compuertasSpring(Map<String, Object> propiedades) {
        Class<?> c = clase(K + "Compuertas");
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("prueba", propiedades));
            ctx.register(c);
            ctx.refresh();
            return ctx.getBean(c);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------ services

    static Object enviar(Contexto c) {
        return construir("cl.bcs.replica.envio.EnviarTablaReplicaServicio", c.compuertas(), c.port("ConsultarEmpresaPort"), c.port("ObtenerDiaSiguientePort"), c.port("FeriadosPort"),
                c.port("ConfiguracionSistemaPort"), construir(J + "TriggersReplicaJdbc"), c.reloj);
    }

    static Object utils(Contexto c) {
        return construir("cl.bcs.replica.respaldo.ReplicaUtilsServicio", c.compuertas(), c.port("ConfiguracionSistemaPort"), c.port("ConsultarEmpresaPort"), c.port("ObtenerDiaSiguientePort"),
                c.port("FeriadosPort"), construir(J + "RespaldoSebraJdbc"), construir(J + "RespaldoOptimusJdbc"));
    }

    static Object iniciar(String clase, Contexto c) {
        return construir("cl.bcs.replica.inicio." + clase, c.port("SecuenciaPort"), c.port("FechaSistemaPort"), c.reloj);
    }

    // ------------------------------------------------------------------------------------------------------------------------------ one call of the legacy entry points

    private static String texto(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private static Long largo(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? Long.valueOf(o.get(k).getAsLong()) : null;
    }

    static Object valorJson(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) {
            BigDecimal b = p.getAsBigDecimal();
            return b.scale() <= 0 ? (Object) Long.valueOf(b.longValueExact()) : (Object) Double.valueOf(b.doubleValue());
        }
        return p.getAsString();
    }

    /**
     * Calls the production unit that stands for the legacy entry point named by {@code servicio} with the input of the case; returns what the legacy returns as JSON (null: the entry
     * point returns nothing; {@link JsonNull}: it returns null) and lets every exception through.
     */
    static JsonElement ejecutar(String servicio, Connection con, JsonObject e, Contexto c) throws Throwable {
        switch (servicio) {
        case "EnviarTablaReplica.logica": {
            Map<String, Object> parametros = null;
            if (!e.has("p_Parametros")) {
                parametros = new HashMap<>();
            } else if (!e.get("p_Parametros").isJsonNull()) {
                parametros = new HashMap<>();
                for (Map.Entry<String, JsonElement> m : e.getAsJsonObject("p_Parametros").entrySet()) parametros.put(m.getKey(), valorJson(m.getValue()));
            }
            invocar(enviar(c), "logica", con, largo(e, "p_IdEmpresa"), texto(e, "p_Proceso"), parametros);
            return new JsonObject();
        }
        case "EnviarTablaReplica.call":
            return resultadoCall(invocar(enviar(c), "call", con, e));
        case "EnviarTablaReplica.respaldarDataDiaInhabil": {
            Object l = invocar(enviar(c), "respaldarDataDiaInhabil", con, largo(e, "idEmpresa"), texto(e, "fechaOrigen"), texto(e, "tablaWorkstack"), texto(e, "parametrosWorkstack"));
            JsonArray out = new JsonArray();
            for (Object w : (List<?>) l) out.add(workstack(w));
            return out;
        }
        case "ReplicaUtils.procesarRespaldoSebra":
            invocar(utils(c), "procesarRespaldoSebra", con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.procesarRespaldoOptimus":
            invocar(utils(c), "procesarRespaldoOptimus", con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.procesarRespaldoDiarioCarterasSebra":
            invocar(utils(c), "procesarRespaldoDiarioCarterasSebra", con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.guardarEnvioPostPauta":
            invocar(utils(c), "guardarEnvioPostPauta", con, texto(e, "tabla"), texto(e, "fechasistema"), texto(e, "fechaenvio"));
            return null;
        case "ReplicaUtils.call":
            return resultadoCall(invocar(utils(c), "call", con, e));
        case "IniciarReplicaFull.logica":
        case "IniciarReplicaDelta.logica":
            return (JsonElement) invocar(iniciar(servicio.split("\\.")[0], c), "logica", con, largo(e, "p_IdEmpresa"), largo(e, "p_IdUsuario"));
        case "IniciarReplicaFull.call":
        case "IniciarReplicaDelta.call":
            return (JsonElement) invocar(iniciar(servicio.split("\\.")[0], c), "call", con, e);
        default:
            throw new IllegalArgumentException("unknown servicio " + servicio);
        }
    }

    private static JsonElement resultadoCall(Object r) {
        return r == null ? null : (JsonElement) r;
    }

    /** the legacy Workstack as JSON: the fields Key, Table, Action, Status, Stamp, Parameters (null ones omitted) */
    static JsonObject workstack(Object w) throws Exception {
        JsonObject o = new JsonObject();
        for (String n : new String[] { "Key", "Table", "Action", "Status", "Stamp", "Parameters" }) {
            Object v = leer(w, n);
            if (v == null) continue;
            if (v instanceof Number) o.add(n, new JsonPrimitive((Number) v));
            else o.addProperty(n, v.toString());
        }
        return o;
    }

    private static Object leer(Object w, String nombre) throws Exception {
        try {
            return w.getClass().getField(nombre).get(w);
        } catch (NoSuchFieldException e) {
            String m = Character.toLowerCase(nombre.charAt(0)) + nombre.substring(1);
            try {
                return w.getClass().getMethod(m).invoke(w);
            } catch (NoSuchMethodException e2) {
                throw new AssertionError("API_ASSUMED: Workstack has neither the field " + nombre + " nor the accessor " + m + "()");
            }
        }
    }
}
