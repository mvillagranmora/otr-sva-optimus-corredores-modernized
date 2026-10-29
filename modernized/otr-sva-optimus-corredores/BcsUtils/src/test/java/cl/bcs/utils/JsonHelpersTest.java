package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import cl.bcs.utils.equivalencia.Pojos;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.d;
import static cl.bcs.utils.equivalencia.Golden.ts;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON helpers, per JVM default zone (America/Santiago and UTC goldens):
 * UTIL-20 Utils.printJson (Utils.java:750-758), UTIL-21 BcsPojo.toString (BcsPojo.java:5-7),
 * UTIL-22 Utils.getGsonBuilder / JavaCall.getGsonBuilder (Utils.java:1018-1025, JavaCall.java:45-52),
 * UTIL-23 GsonDateDeserializer / GsonTimestampDeserializer (GsonDateDeserializer.java:21-33,
 * GsonTimestampDeserializer.java:20-32): LENIENT SimpleDateFormat chosen by input length, null on ParseException.
 */
class JsonHelpersTest {

    static final String AREA = "json";

    static Pojos.Muestra muestra() {
        Pojos.Muestra m = new Pojos.Muestra();
        m.IdCliente = 1313L;
        m.Nombre = "José <b>&'=\"\\ ñ";
        m.Monto = new BigDecimal("1250.50");
        m.Tasa = Double.NaN;
        m.Activo = Boolean.TRUE;
        m.Cantidad = 7;
        m.Fecha = new java.util.Date(1725764400000L);
        m.FechaHora = ts(1710512430123L, 123456789);
        m.FechaSql = new java.sql.Date(1725764400000L);
        m.Etiquetas = Arrays.asList("a", null, "c");
        m.Nulo = null;
        m.Detalle = new Pojos.Detalle("X1", 9L);
        return m;
    }

    static final JavaCall JC = (Connection c, JsonObject i) -> i;

    Callable<Object> moderno(Caso c) {
        Gson g = Utils.getGsonBuilder();
        String fn = c.s("fn");
        // golden "json" input: a JSON string is the literal text; "null" / "{}" stand for a JSON null / an empty object
        JsonElement json = c.input().get("json");
        String texto = json == null ? null
                : json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()
                ? (json.getAsString().equals("null") || json.getAsString().equals("{}") ? json.getAsString() : new JsonPrimitive(json.getAsString()).toString())
                : json.toString();
        return switch (fn) {
            case "printJson" -> () -> {
                boolean p = c.input().get("pretty").getAsBoolean();
                String obj = c.s("obj");
                Object o = obj == null ? null : switch (obj) {
                    case "Muestra" -> muestra();
                    case "\"abc\"" -> "abc";
                    case "[1, 2.5, +Inf]" -> Arrays.asList(1, 2.5, Double.POSITIVE_INFINITY);
                    case "Muestra(empty)" -> new Pojos.Muestra();
                    default -> throw new AssertionError(obj);
                };
                return Utils.printJson(o, p);
            };
            case "BcsPojo.toString" -> () -> {
                Pojos.MuestraPojo p = new Pojos.MuestraPojo();
                p.Id = 5L;
                p.Texto = "t";
                p.FechaHora = ts(0L, 0);
                return p.toString();
            };
            case "getGsonBuilder.toJson" -> () -> g.toJson(muestra());
            case "JavaCall.getGsonBuilder.toJson" -> () -> JC.getGsonBuilder().toJson(muestra());
            case "GsonDateDeserializer" -> () -> d(g.fromJson("{\"FechaSql\":" + texto + "}", Pojos.Muestra.class).FechaSql);
            case "GsonTimestampDeserializer" -> () -> d(g.fromJson("{\"FechaHora\":" + texto + "}", Pojos.Muestra.class).FechaHora);
            case "GsonDateDeserializer.direct" -> () -> d(new GsonDateDeserializer().deserialize(JsonNull.INSTANCE, java.sql.Date.class, null));
            case "GsonTimestampDeserializer.direct" -> () -> d(new GsonTimestampDeserializer().deserialize(JsonNull.INSTANCE, java.sql.Timestamp.class, null));
            case "getGsonBuilder.fromJson.utilDate" -> () -> d(g.fromJson("{\"Fecha\":" + texto + "}", Pojos.Muestra.class).Fecha);
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-20..23 equivalence per zone: printJson / BcsPojo / getGsonBuilder / Gson deserializers == legacy")
    Stream<DynamicContainer> util20a23_equivalencia() {
        return Golden.ZONAS.entrySet().stream().map(z -> DynamicContainer.dynamicContainer(z.getValue(),
                Golden.casos(z.getKey(), AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(),
                        () -> Golden.enZona(z.getValue(), () -> Golden.igual(AREA, c, moderno(c)))))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-20: printJson keeps Gson HTML escaping, NaN/Infinity literals, nulls, and \"[]\" for null")
    void util20_formato() throws Exception {
        Golden.enZona("UTC", () -> {
            assertThat(Utils.printJson(null, false)).isEqualTo("[]");
            assertThat(Utils.printJson(Map.of("k", "<&='>"), false)).isEqualTo("{\"k\":\"\\u003c\\u0026\\u003d\\u0027\\u003e\"}");
            assertThat(Utils.printJson(Arrays.asList(Double.NaN, null), false)).isEqualTo("[NaN,null]");
            assertThat(Utils.printJson(new java.sql.Timestamp(0), false)).isEqualTo("\"1970-01-01 00:00:00.000\"");
        });
    }

    @Test
    @DisplayName("UTIL-23 legacy behavior pinned: deserializers are LENIENT and return null (not throw) on unparseable text")
    void util23_lenienteYNull() throws Exception {
        Golden.enZona("UTC", () -> {
            Gson g = Utils.getGsonBuilder();
            assertThat(g.fromJson("{\"FechaSql\":\"2024-02-30\"}", Pojos.Muestra.class).FechaSql).hasToString("2024-03-01");
            assertThat(g.fromJson("{\"FechaHora\":\"abc\"}", Pojos.Muestra.class).FechaHora).isNull();
            // 26 chars -> pattern ...ss.SSS parses "123456" as 123456 ms: +2 min 3.456 s
            assertThat(g.fromJson("{\"FechaHora\":\"2024-03-15 10:20:30.123456\"}", Pojos.Muestra.class).FechaHora).hasToString("2024-03-15 10:22:33.456");
            // java.sql.Date keeps the time of day it was given
            assertThat(g.fromJson("{\"FechaSql\":\"2024-03-15 10:20:30\"}", Pojos.Muestra.class).FechaSql.getTime()).isEqualTo(1710498030000L);
        });
    }

    @Test
    @DisplayName("UTIL-23: through Gson a JSON null is null; called directly with JsonNull the deserializer throws UnsupportedOperationException")
    void util23_nulos() {
        assertThat(Utils.getGsonBuilder().fromJson("{\"FechaHora\":null}", Pojos.Muestra.class).FechaHora).isNull();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new GsonTimestampDeserializer().deserialize(JsonNull.INSTANCE, null, null))
                .isInstanceOf(UnsupportedOperationException.class).isNotInstanceOf(JsonParseException.class);
    }
}
