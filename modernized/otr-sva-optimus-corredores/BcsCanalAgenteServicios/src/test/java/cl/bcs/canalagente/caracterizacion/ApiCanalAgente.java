package cl.bcs.canalagente.caracterizacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.canalagente.oraculo.GuionColaboradores;
import cl.bcs.canalagente.oraculo.ProcedimientosBd;

/**
 * The ONLY file of the characterization tests that reaches the production classes of BcsCanalAgenteServicios, and it does it by reflection: the production units do not exist yet ("red before
 * code"), so the tests compile and run without them and fail with a clear {@code AssertionError} ("not implemented: ...") until they are written. If production needs another name or shape,
 * change it HERE (never the goldens). Everything the tests assume is listed in {@code API_ASSUMED.md} (same directory); every class is public and every method public.
 *
 * <p>Assumed production API:
 * <pre>
 * package cl.bcs.canalagente.kit
 *   public final class Compuertas { Compuertas(boolean cartola) }              // Spring bean: reads canalagente.compuertas.cartola, false (closed) when absent
 *   public class ServicioNoServidoException extends RuntimeException           // what a closed gate and a not ported collaborator raise
 * package cl.bcs.canalagente.port.out    (every method {@code throws Exception}; the Connection is always the first argument)
 *   ConsultarCartolaDeMovimientoPort   { JsonObject consultar(Connection, JsonObject entrada) }    // legacy ConsultarCartolaDeMovimiento.logica, JSON form of its Entrada / Salida POJOs
 *   ColaboradoresNoPortados            implements the port; consultar throws ServicioNoServidoException (the production default)
 *   ProcedimientosBdPort               { String texto(String nombre) }                              // SQLScript text of a BD/Servicios procedure
 * package cl.bcs.canalagente.adapter.out.bd
 *   ProcedimientosBdRecursos           implements ProcedimientosBdPort over cl/bcs/canalagente/bd/servicios/&lt;nombre&gt;.sql   (public no-argument constructor)
 * package cl.bcs.canalagente.service
 *   ConsultarHistorialMovimientosCuentaInversionServicio(Compuertas, ConsultarCartolaDeMovimientoPort)
 *     Salida logica(Connection, Entrada) throws Exception
 *     JsonObject call(Connection, JsonObject) throws Exception
 *     public static class Entrada   public fields p_IdEmpresa Long, p_IdUsuario Long, p_IdModulo String, p_IdPortafolio Long, p_FechaDesde String, p_FechaHasta String, p_IdMoneda String,
 *                                   p_IdMatrizAgrupador Long, p_EsADC String                    (no-argument constructor)
 *     public static class Salida    public fields P_SALDOEFECTIVOINICIAL BigDecimal, P_SALDOEFECTIVOFINAL BigDecimal, P_OUT_RESULTADO List&lt;Movimiento&gt;, P_OUT_ADVERTENCIASCONTADOR Long,
 *                                   P_OUT_ADVERTENCIASDETALLE List&lt;AdvertenciasDetalle&gt;, P_OUT_ERRORESCONTADOR Long, P_OUT_ERRORESDETALLE List&lt;ErroresDetalle&gt;  (the legacy names, in this order)
 *     Movimiento                    the 26 fields of the legacy MovimientoCuentaInversion (same names and order; dates java.sql.Date: they print as yyyy-MM-dd HH:mm:ss.SSS)
 * </pre>
 * The typed output is turned into JSON with the Gson of the platform ({@code serializeNulls}, dates {@code yyyy-MM-dd HH:mm:ss.SSS}), the same one the legacy {@code call} uses.
 */
final class ApiCanalAgente {
    static final String K = "cl.bcs.canalagente.kit.";
    static final String P = "cl.bcs.canalagente.port.out.";
    static final String SERVICIO = "cl.bcs.canalagente.service.ConsultarHistorialMovimientosCuentaInversionServicio";
    static final String RECURSOS_BD = "cl.bcs.canalagente.adapter.out.bd.ProcedimientosBdRecursos";

    /** the Gson of the platform (legacy Utils.getGsonBuilder): nulls kept, dates as yyyy-MM-dd HH:mm:ss.SSS */
    static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeSpecialFloatingPointValues().serializeNulls().create();

    private ApiCanalAgente() {
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

    // ------------------------------------------------------------------------------------------------------------------------------ the collaborator port

    /** The port of the cartola answered by the case script ({@link GuionColaboradores}): the same record and the same answers as the oracle double. A JSON null answer is a Java null. */
    static Object puertoCartola(GuionColaboradores guion) {
        Class<?> i = clase(P + "ConsultarCartolaDeMovimientoPort");
        return Proxy.newProxyInstance(ApiCanalAgente.class.getClassLoader(), new Class<?>[] { i }, (p, m, a) -> {
            if (!m.getName().equals("consultar")) throw new AssertionError("API_ASSUMED: the port ConsultarCartolaDeMovimientoPort has the unexpected method " + m.getName());
            com.google.gson.JsonArray args = new com.google.gson.JsonArray();
            args.add(a[1] == null ? JsonNull.INSTANCE : (JsonElement) a[1]);
            JsonElement r = guion.responder("ConsultarCartolaDeMovimiento", "logica", args);
            return r == null || r.isJsonNull() ? null : r.getAsJsonObject();
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------ context

    /** What a run needs: the gate, the collaborator (scripted, or the closed "not ported" default). */
    static final class Contexto {
        final boolean cartola;
        final GuionColaboradores guion;
        final boolean colaboradoresNoPortados;
        final Object compuertasDadas;

        Contexto(boolean cartola, GuionColaboradores guion, boolean colaboradoresNoPortados, Object compuertasDadas) {
            this.cartola = cartola;
            this.guion = guion;
            this.colaboradoresNoPortados = colaboradoresNoPortados;
            this.compuertasDadas = compuertasDadas;
        }

        /** the gate open and the collaborator scripted: what the legacy does (the legacy has no gates) */
        static Contexto abierto(GuionColaboradores guion) {
            return new Contexto(true, guion, false, null);
        }

        Contexto conCompuerta(boolean cartola) {
            return new Contexto(cartola, guion, colaboradoresNoPortados, null);
        }

        Contexto conColaboradoresNoPortados() {
            return new Contexto(cartola, guion, true, compuertasDadas);
        }

        /** the Compuertas object as built by Spring from the properties (the production default) instead of the flag */
        Contexto conCompuertasObjeto(Object compuertas) {
            return new Contexto(cartola, guion, colaboradoresNoPortados, compuertas);
        }

        Object compuertas() {
            return compuertasDadas != null ? compuertasDadas : construir(K + "Compuertas", cartola);
        }

        Object puerto() {
            return colaboradoresNoPortados ? construir(P + "ColaboradoresNoPortados") : puertoCartola(guion);
        }
    }

    /**
     * The Compuertas bean as Spring builds it from the property {@code canalagente.compuertas.cartola} (false when absent: the production default is the gate closed).
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

    static Object servicio(Contexto c) {
        return construir(SERVICIO, c.compuertas(), c.puerto());
    }

    /** the SQLScript text of a BD/Servicios procedure as the production port gives it (the same text the production resource holds) */
    static String textoProcedimiento(String nombre) throws Throwable {
        Object adaptador = construir(RECURSOS_BD);
        return (String) invocar(adaptador, "texto", nombre);
    }

    // ------------------------------------------------------------------------------------------------------------------------------ one call of the legacy entry points

    private static Object valor(Class<?> tipo, JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        if (tipo == Long.class) return e.getAsLong();
        if (tipo == String.class) return e.getAsString();
        throw new IllegalStateException("type not handled by the test API: " + tipo);
    }

    private static Object entradaTipada(JsonObject e) throws Exception {
        Class<?> c = clase(SERVICIO + "$Entrada");
        Object entrada = c.getConstructor().newInstance();
        for (Map.Entry<String, JsonElement> m : e.entrySet()) {
            try {
                Field f = c.getField(m.getKey());
                f.set(entrada, valor(f.getType(), m.getValue()));
            } catch (NoSuchFieldException ex) {
                throw new AssertionError("API_ASSUMED: Entrada has no public field " + m.getKey());
            }
        }
        return entrada;
    }

    /**
     * Calls the production unit that stands for the legacy entry point named by {@code servicio} with the input of the case; returns what the legacy returns as JSON and lets every exception
     * through. {@code raw} is the unwrapped H2 connection (only the procedure family reads it directly).
     */
    static JsonElement ejecutar(String servicio, Connection con, Connection raw, JsonObject e, Contexto c) throws Throwable {
        switch (servicio) {
        case "ConsultarHistorial.logica": {
            Object salida = invocar(servicio(c), "logica", con, entradaTipada(e));
            return GSON.toJsonTree(salida);
        }
        case "ConsultarHistorial.call":
            return (JsonElement) invocar(servicio(c), "call", con, e);
        case "Procedimiento.seleccionFinal":
            return ProcedimientosBd.ejecutar(raw, textoProcedimiento(e.get("procedimiento").getAsString().replace(".sql", "")), e.get("salida").getAsString(), e.get("variableTabla").getAsString(),
                    e.get("tablaH2").getAsString(), e.get("orden").getAsString());
        default:
            throw new IllegalArgumentException("unknown servicio " + servicio);
        }
    }
}
