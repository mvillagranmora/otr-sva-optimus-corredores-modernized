package cl.bcs.sura.support;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.port.out.SuraDataPort;
import cl.bcs.sura.service.PerfilesPortafolioUtilsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Shared test doubles for the SURA characterization suite.
 *
 * <p>The legacy code talks to HANA through {@code SQL.query/batch(conn, ...)} and to the message
 * catalogue through {@code PlataformaUtils.MensajeSalida(conn, ...)}. Both are modelled here as a
 * recording {@link SuraDataPort} double. Two port operations that the current port does NOT yet
 * declare are part of the intended API and are looked up by name (the double is a dynamic proxy, so
 * it works with whatever signature main eventually declares):
 * <ul>
 *   <li>{@value #FILTRADOS}: legacy generarTablaTemporal + ConsultarPerfilesComercialesFiltradosPorTablaTemporal
 *       + Borrar (input = seeded perfiles, output = enriched rows);</li>
 *   <li>{@value #MENSAJE}: legacy PlataformaUtils.MensajeSalida(idCliente, ERROR, codigo, params, conn, idEmpresa)
 *       (args = Long idCliente, Long idEmpresa, String codigo, Map params; returns Map with keys
 *       codigo, descripcion, tipo).</li>
 * </ul>
 */
public final class SuraTestSupport {

    public static final String IDS = "consultarIdsPortafolioIdCliente";
    public static final String PERFILES = "consultarPerfilesComerciales";
    public static final String FILTRADOS = "consultarPerfilesFiltrados";
    public static final String MENSAJE = "resolverMensajeError";
    public static final String INGRESAR = "ingresarIndicadoresCobroCustodia";
    public static final String MODIF_IND = "modificarIndicadoresCobroCustodia";
    public static final String MODIF_PERF = "modificarPerfilesPortafolio";

    public static final String CODIGO_ERROR = "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS";
    public static final String DESC_FAKE = "DESCRIPCION-RESUELTA-POR-CATALOGO-FAKE";

    private SuraTestSupport() {}

    // ------------------------------------------------------------------ snapshots

    /** Immutable copy of IdsEntidadYPerfiles taken at call time (services mutate the originals). */
    public record Snap(long idPortafolio, long idCliente, String doc, long idIndicador, String formaCobro,
                       String categoria, String perfilComercial, String perfilCobros, String perfilProductos,
                       String proposito, String actualizar) {
        public static Snap of(IdsEntidadYPerfiles p) {
            return new Snap(p.idPortafolio, p.idCliente, p.documentoIdentidad, p.idIndicador, p.idFormaCobro,
                    p.idCategoriaInversionista, p.idPerfilComercial, p.idPerfilCobros, p.idPerfilProductos,
                    p.proposito, p.actualizarFormaCobro);
        }
    }

    public record Call(String name, List<Object> args) {
        @SuppressWarnings("unchecked")
        public List<Snap> snaps(int i) { return (List<Snap>) args.get(i); }
        public String str(int i) { return (String) args.get(i); }
        @SuppressWarnings("unchecked")
        public List<String> strs(int i) { return (List<String>) args.get(i); }
    }

    // ------------------------------------------------------------------ the port double

    public static final class FakePort {
        public final List<Call> calls = new ArrayList<>();
        private final Map<String, Function<Object[], Object>> handlers = new HashMap<>();
        public final SuraDataPort port;

        public FakePort() {
            this.port = (SuraDataPort) Proxy.newProxyInstance(
                    SuraDataPort.class.getClassLoader(), new Class<?>[]{SuraDataPort.class},
                    (proxy, m, a) -> {
                        if (m.getDeclaringClass() == Object.class) {
                            return switch (m.getName()) {
                                case "toString" -> "FakeSuraDataPort";
                                case "hashCode" -> System.identityHashCode(proxy);
                                default -> proxy == a[0];
                            };
                        }
                        Object[] args = a == null ? new Object[0] : a;
                        calls.add(new Call(m.getName(), snapshot(args)));
                        Function<Object[], Object> h = handlers.get(m.getName());
                        if (h != null) return h.apply(args);
                        return defaultFor(m);
                    });
            on(MENSAJE, a -> Map.of("codigo", a[2], "descripcion", DESC_FAKE, "tipo", "ERROR"));
        }

        public FakePort on(String method, Function<Object[], Object> handler) {
            handlers.put(method, handler);
            return this;
        }

        /** consultarIdsPortafolioIdCliente returns fresh copies each time (services mutate them). */
        public FakePort ids(IdsEntidadYPerfiles... rows) {
            return on(IDS, a -> copies(rows));
        }

        public FakePort perfiles(IdsEntidadYPerfiles... rows) {
            return on(PERFILES, a -> copies(rows));
        }

        public FakePort filtrados(IdsEntidadYPerfiles... rows) {
            return on(FILTRADOS, a -> copies(rows));
        }

        public List<Call> calls(String name) {
            return calls.stream().filter(c -> c.name().equals(name)).toList();
        }

        public List<String> names() {
            return calls.stream().map(Call::name).toList();
        }

        /** Names of the write operations only, in order. */
        public List<String> writes() {
            return names().stream().filter(n -> n.equals(INGRESAR) || n.equals(MODIF_IND) || n.equals(MODIF_PERF)).toList();
        }

        private static Object defaultFor(Method m) {
            Class<?> r = m.getReturnType();
            if (r == void.class) return null;
            if (List.class.isAssignableFrom(r)) return new ArrayList<>();
            if (Map.class.isAssignableFrom(r)) return new HashMap<>();
            return null;
        }

        private static List<Object> snapshot(Object[] args) {
            List<Object> out = new ArrayList<>();
            for (Object o : args) {
                if (o instanceof List<?> l) {
                    if (!l.isEmpty() && l.get(0) instanceof IdsEntidadYPerfiles) {
                        List<Snap> s = new ArrayList<>();
                        for (Object e : l) s.add(Snap.of((IdsEntidadYPerfiles) e));
                        out.add(s);
                    } else {
                        out.add(new ArrayList<>(l));
                    }
                } else if (o instanceof Map<?, ?> m) {
                    out.add(new HashMap<>(m));
                } else {
                    out.add(o);
                }
            }
            return out;
        }

        private static List<IdsEntidadYPerfiles> copies(IdsEntidadYPerfiles[] rows) {
            List<IdsEntidadYPerfiles> r = new ArrayList<>();
            for (IdsEntidadYPerfiles p : rows) r.add(copy(p));
            return r;
        }
    }

    // ------------------------------------------------------------------ row builders

    /** A row as returned by legacy ConsultarIdsPortafolioIdClientePorDocumentoIdentidad (profile fields stay default ""). */
    public static IdsEntidadYPerfiles idRow(long portafolio, long cliente, String doc, long idIndicador, String proposito) {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = portafolio;
        p.idCliente = cliente;
        p.documentoIdentidad = doc;
        p.idIndicador = idIndicador;
        p.proposito = proposito;
        return p;
    }

    /** A fully resolved profile row (what the profile queries return). */
    public static IdsEntidadYPerfiles perfilRow(long portafolio, long cliente, String doc, long idIndicador,
                                                String formaCobro, String pComercial, String pCobros, String pProductos,
                                                String proposito) {
        IdsEntidadYPerfiles p = idRow(portafolio, cliente, doc, idIndicador, proposito);
        p.idFormaCobro = formaCobro;
        p.idCategoriaInversionista = "CAT-A";
        p.idPerfilComercial = pComercial;
        p.idPerfilCobros = pCobros;
        p.idPerfilProductos = pProductos;
        return p;
    }

    public static IdsEntidadYPerfiles copy(IdsEntidadYPerfiles s) {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = s.idPortafolio;
        p.idCliente = s.idCliente;
        p.documentoIdentidad = s.documentoIdentidad;
        p.idIndicador = s.idIndicador;
        p.idFormaCobro = s.idFormaCobro;
        p.idCategoriaInversionista = s.idCategoriaInversionista;
        p.idPerfilComercial = s.idPerfilComercial;
        p.idPerfilCobros = s.idPerfilCobros;
        p.idPerfilProductos = s.idPerfilProductos;
        p.proposito = s.proposito;
        p.actualizarFormaCobro = s.actualizarFormaCobro;
        return p;
    }

    // ------------------------------------------------------------------ effective-write helpers

    /** Portfolio ids that would actually be INSERTed (legacy filter IdIndicador == 0), whoever applies the filter. */
    public static List<Long> efectivosInsert(Call c) {
        return c.snaps(0).stream().filter(s -> s.idIndicador() == 0).map(Snap::idPortafolio).toList();
    }

    /** Indicator ids that would actually be UPDATEd (IdIndicador != 0 and ActualizarFormaCobro == "S"). */
    public static List<Long> efectivosUpdate(Call c) {
        return c.snaps(0).stream().filter(s -> s.idIndicador() != 0 && "S".equals(s.actualizar()))
                .map(Snap::idIndicador).toList();
    }

    // ------------------------------------------------------------------ seam factory (red until main exposes the port seam)

    /**
     * Builds a main-code class wired to the double. Each constructor parameter must be a SuraDataPort or a
     * PerfilesPortafolioUtilsService (itself built on the port). Today every main service is built on
     * NamedParameterJdbcTemplate, so this fails with a GAP-SEAM message: that is the intended red.
     */
    public static <T> T create(Class<T> type, SuraDataPort port) {
        for (Constructor<?> c : type.getConstructors()) {
            Object[] args = new Object[c.getParameterCount()];
            boolean ok = true;
            Class<?>[] pt = c.getParameterTypes();
            for (int i = 0; i < pt.length; i++) {
                if (pt[i].isAssignableFrom(SuraDataPort.class) && pt[i] == SuraDataPort.class) args[i] = port;
                else if (pt[i] == PerfilesPortafolioUtilsService.class) args[i] = create(PerfilesPortafolioUtilsService.class, port);
                else { ok = false; break; }
            }
            if (ok) {
                try {
                    return type.cast(c.newInstance(args));
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("GAP-SEAM: cannot instantiate " + type.getSimpleName(), e);
                }
            }
        }
        throw new AssertionError("GAP-SEAM: " + type.getSimpleName()
                + " has no constructor built only on SuraDataPort / PerfilesPortafolioUtilsService; "
                + "tests cannot inject the port double (main constructors take NamedParameterJdbcTemplate).");
    }

    /** Loads a main service by simple name in cl.bcs.sura.service; AssertionError (not skip) when absent. */
    public static Class<?> serviceClass(String simpleName) {
        try {
            return Class.forName("cl.bcs.sura.service." + simpleName);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("GAP-MISSING-SERVICE: cl.bcs.sura.service." + simpleName + " does not exist in src/main");
        }
    }

    /** Runs service.execute(JsonObject) reflectively; unwraps the target exception. */
    public static JsonObject execute(String serviceSimpleName, SuraDataPort port, JsonObject params) throws Exception {
        Class<?> cls = serviceClass(serviceSimpleName);
        Object svc = create(cls, port);
        try {
            Method m = cls.getMethod("execute", JsonObject.class);
            return (JsonObject) m.invoke(svc, params);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    // ------------------------------------------------------------------ json helpers

    public static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    public static JsonArray arr(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e == null ? new JsonArray() : e.getAsJsonArray();
    }

    /** Builds p_SocioNegocio entries with IndicadorPortafolio (Crear/Modificar SocioNegocio shape). */
    public static String socio(String tipoEntidad, String doc, String... cobrocust) {
        StringBuilder ind = new StringBuilder();
        for (int i = 0; i < cobrocust.length; i += 2) {
            if (i > 0) ind.append(',');
            ind.append("{\"CodigoTipoIndicador\":\"").append(cobrocust[i]).append("\",\"ValorIndicador\":\"")
                    .append(cobrocust[i + 1]).append("\"}");
        }
        return "{\"TipoEntidad\":\"" + tipoEntidad + "\",\"DocumentoIdentidad\":\"" + doc
                + "\",\"IndicadorPortafolio\":[" + ind + "]}";
    }
}
