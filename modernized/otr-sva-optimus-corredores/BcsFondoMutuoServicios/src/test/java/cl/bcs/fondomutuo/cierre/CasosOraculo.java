package cl.bcs.fondomutuo.cierre;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ComisionesJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.FechasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.PerfilFondoMutuoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior;
import cl.bcs.fondomutuo.fechas.ObtenerDiaHabilMultiplesPaises;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Loader and runner of the step-3A characterization cases. A case is a directory of {@code fondomutuo-3a/cases/} with {@code case.json}, {@code seed.sql} and
 * the golden {@code expected.json} RECORDED by {@code legacy-oracle/record3a.sh} from the unmodified legacy classes (never by hand). The new implementation
 * runs the same input on the same H2 schema/seed with the same scripted collaborators and its dump must equal the golden: output (dates as text, BigDecimal
 * text so the scale counts), the input after the run, the exception, and every call to a collaborator. A missing fixture FAILS the test (never skipped).
 */
final class CasosOraculo {

    private static final Gson OUT = new GsonBuilder().registerTypeAdapter(LocalDate.class, new FechaSerializer()).serializeNulls().disableHtmlEscaping().create();
    private static final Gson PLANO = new Gson();

    private CasosOraculo() {
    }

    record Caso(String id, JsonObject caso, String seed, JsonObject esperado) {
    }

    private static final class FechaSerializer implements JsonSerializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate fecha, Type tipo, JsonSerializationContext contexto) {
            return new JsonPrimitive(FechaLegacy.aTexto(fecha));
        }
    }

    /** The cases whose id starts with {@code prefijo}; FAILS when there is none (a fixture that is not there is not a pass). */
    static List<Caso> cargar(String prefijo) {
        List<Caso> casos = new ArrayList<>();
        try {
            Resource[] recursos = new PathMatchingResourcePatternResolver().getResources("classpath*:fondomutuo-3a/cases/" + prefijo + "*/case.json");
            for (Resource r : recursos) {
                String ruta = r.getURL().toString();
                String dir = ruta.substring(0, ruta.lastIndexOf('/'));
                String id = dir.substring(dir.lastIndexOf('/') + 1);
                JsonObject caso = JsonParser.parseString(leer(r)).getAsJsonObject();
                String seed = existe(dir + "/seed.sql") ? leerUrl(dir + "/seed.sql") : null;
                if (!existe(dir + "/expected.json")) {
                    throw new AssertionError("oracle golden missing for case " + id + " (record it with legacy-oracle/record3a.sh and sync3a.py)");
                }
                casos.add(new Caso(id, caso, seed, JsonParser.parseString(leerUrl(dir + "/expected.json")).getAsJsonObject()));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        casos.sort((a, b) -> a.id().compareTo(b.id()));
        assertThat(casos).as("oracle fixtures for " + prefijo + " must be on the classpath (legacy-oracle/sync3a.py)").isNotEmpty();
        return casos;
    }

    private static String leer(Resource r) throws IOException {
        return new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static boolean existe(String url) {
        try (java.io.InputStream in = java.net.URI.create(url).toURL().openStream()) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    private static String leerUrl(String url) throws IOException {
        try (java.io.InputStream in = java.net.URI.create(url).toURL().openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Runs the new implementation on the case and returns the dump in the oracle's format. */
    static JsonObject ejecutar(Caso c) {
        DataSource ds = BdOraculo.nueva(c.seed());
        JsonObject config = JsonParser.parseString(BdOraculo.leer("fondomutuo-3a/seed/doubles-base.json")).getAsJsonObject();
        if (c.caso().has("colaboradores")) {
            for (String k : c.caso().getAsJsonObject("colaboradores").keySet()) {
                config.add(k, c.caso().getAsJsonObject("colaboradores").get(k));
            }
        }
        DoblesGuionados dobles = new DoblesGuionados(config);
        ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(ds);
        ConfiguracionSistemaPort configuracion = new ConfiguracionSistemaJdbc(conexion);
        CalendarioPort calendario = new CalendarioJdbc(conexion, configuracion);
        SeriesFondoJdbc series = new SeriesFondoJdbc(conexion);

        JsonObject res = new JsonObject();
        String clase = c.caso().get("clase").getAsString();
        String entradaJson = c.caso().getAsJsonObject("entrada").toString();
        Object entrada = null;
        try {
            Object salida;
            switch (clase) {
                case "CalcularFechaSolicitudAporteRescate" -> {
                    var e = PLANO.fromJson(entradaJson, CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate.class);
                    entrada = e;
                    salida = new CalcularFechaSolicitudAporteRescate(configuracion, dobles, series, new FechasJdbc(conexion), calendario).logica(e);
                }
                case "CalcularMontoNumeroCuota" -> {
                    var e = PLANO.fromJson(entradaJson, CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota.class);
                    entrada = e;
                    salida = new CalcularMontoNumeroCuota(configuracion, series, new CuotasJdbc(conexion), calendario, dobles, dobles).logica(e);
                }
                case "CalcularComisiones" -> {
                    var e = PLANO.fromJson(entradaJson, CalcularComisiones.EntradaCalcularComisiones.class);
                    entrada = e;
                    salida = new CalcularComisiones(configuracion, dobles, series, new ComisionesJdbc(conexion), dobles, dobles, dobles).logica(e);
                }
                case "ObtenerPerfilFondoMutuo" -> {
                    var e = PLANO.fromJson(entradaJson, ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo.class);
                    entrada = e;
                    salida = new ObtenerPerfilFondoMutuo(new PerfilFondoMutuoJdbc(conexion)).logica(e);
                }
                case "MonedaFormato" -> {
                    JsonObject p = c.caso().getAsJsonObject("entrada");
                    BigDecimal valor = p.get("valor").isJsonNull() ? null : p.get("valor").getAsBigDecimal();
                    String moneda = p.get("moneda").isJsonNull() ? null : p.get("moneda").getAsString();
                    JsonObject o = new JsonObject();
                    o.addProperty("valor", new FormatoMoneda(calendario).formatear(valor, p.get("tipo").getAsString(), moneda,
                            p.has("idTipoInstrumento") ? p.get("idTipoInstrumento").getAsString() : "",
                            p.has("idTipoOperacion") ? p.get("idTipoOperacion").getAsString() : ""));
                    salida = o;
                }
                case "ObtenerDiaSiguiente" -> {
                    var e = PLANO.fromJson(entradaJson, ObtenerDiaSiguiente.InObtenerDiaSiguiente.class);
                    entrada = e;
                    salida = new ObtenerDiaSiguiente(calendario).logica(e);
                }
                case "ObtenerDiaAnterior" -> {
                    var e = PLANO.fromJson(entradaJson, ObtenerDiaAnterior.InObtenerDiaAnterior.class);
                    entrada = e;
                    salida = new ObtenerDiaAnterior(calendario).logica(e);
                }
                case "ObtenerDiaHabilMultiplesPaises" -> {
                    var e = PLANO.fromJson(entradaJson, ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.class);
                    entrada = e;
                    salida = new ObtenerDiaHabilMultiplesPaises(calendario).logica(e);
                }
                default -> throw new IllegalArgumentException("unknown clase " + clase);
            }
            res.add("salida", OUT.toJsonTree(salida));
        } catch (Throwable t) {
            res.addProperty("excepcionClase", t.getClass().getSimpleName());
            res.addProperty("excepcionMensaje", String.valueOf(t.getMessage()));
        }
        if (entrada != null) {
            res.add("entradaTrasEjecucion", OUT.toJsonTree(entrada));
        }
        res.add("colaboradores", dobles.llamadas());
        return res;
    }

    /** The new dump must equal the golden (without provenance); the exception message of a NullPointerException (helpful text of the JVM) is not compared. */
    static void comparar(Caso c, JsonObject nuevo) {
        JsonObject esperado = c.esperado().deepCopy();
        esperado.remove("_provenance");
        JsonObject real = JsonParser.parseString(OUT.toJson(nuevo)).getAsJsonObject();
        if (esperado.has("excepcionClase")) {
            String claseEsperada = esperado.get("excepcionClase").getAsString();
            assertThat(real.has("excepcionClase")).as(c.id() + ": the legacy raised " + claseEsperada + ", the new code did not").isTrue();
            String claseReal = real.get("excepcionClase").getAsString();
            if (!claseEsperada.equals("Exception")) {
                assertThat(claseReal).as(c.id() + ": exception class").isEqualTo(claseEsperada);
            }
            if (!claseEsperada.equals("NullPointerException")) {
                String m1 = esperado.get("excepcionMensaje").getAsString();
                String m2 = real.get("excepcionMensaje").getAsString();
                assertThat(sinValores(m2)).as(c.id() + ": exception message").isEqualTo(sinValores(m1));
            }
            esperado.remove("excepcionClase");
            esperado.remove("excepcionMensaje");
            real.remove("excepcionClase");
            real.remove("excepcionMensaje");
        }
        assertThat(canonico(real)).as(c.id() + " (" + c.caso().get("ref").getAsString() + ")").isEqualTo(canonico(esperado));
    }

    /**
     * {@code Collectors.toMap} duplicate-key messages quote the toString of the two values: only the key part is comparable. The legacy runs these
     * collectors on a {@code parallelStream}, whose fork-join wrapper puts the class name in front of the message ("java.lang.IllegalStateException: Duplicate key ...").
     */
    private static String sinValores(String mensaje) {
        String m = mensaje.startsWith("java.lang.IllegalStateException: ") ? mensaje.substring("java.lang.IllegalStateException: ".length()) : mensaje;
        int i = m.indexOf(" (attempted merging values");
        return i < 0 ? m : m.substring(0, i);
    }

    /** Text of the tree with the keys sorted and numbers as written (a scale difference is a difference). */
    static String canonico(JsonElement e) {
        StringBuilder sb = new StringBuilder();
        escribir(e, sb);
        return sb.toString();
    }

    private static void escribir(JsonElement e, StringBuilder sb) {
        if (e == null || e.isJsonNull()) {
            sb.append("null");
        } else if (e.isJsonObject()) {
            Map<String, JsonElement> ordenado = new TreeMap<>();
            e.getAsJsonObject().entrySet().forEach(x -> ordenado.put(x.getKey(), x.getValue()));
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<String, JsonElement> x : ordenado.entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                sb.append('"').append(x.getKey()).append("\":");
                escribir(x.getValue(), sb);
            }
            sb.append('}');
        } else if (e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            sb.append('[');
            for (int i = 0; i < a.size(); i++) {
                if (i > 0) sb.append(',');
                escribir(a.get(i), sb);
            }
            sb.append(']');
        } else {
            sb.append(e.toString());
        }
    }
}
