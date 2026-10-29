package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plugin contract: UTIL-40 JavaCall / JavaCallExtra / JavaUtil default methods (JavaCall.java:10-53,
 * JavaCallExtra.java:10-53, JavaUtil.java:10-44), UTIL-41 JavaCallMetadata (JavaCallMetadata.java:3-17).
 * Resources: src/test/resources/oracle/recurso.sql (UTF-8, "ñandú") and oracle/vacio.sql, identical to the oracle's.
 */
class JavaCallContractTest {

    static final String AREA = "javacall";

    static final JavaCall JC = new JavaCall() {
        @Override
        public JsonObject call(Connection conn, JsonObject in) {
            JsonObject r = new JsonObject();
            r.add("eco", in);
            return r;
        }
    };
    static final JavaCallExtra JX = (conn, in, out) -> out;
    static final JavaUtil JU = new JavaUtil() {
    };

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        String iface = c.s("iface");
        return switch (fn) {
            case "loadResourceToString" -> () -> switch (iface) {
                case "JavaCall" -> JC.loadResourceToString(c.s("path"));
                case "JavaCallExtra" -> JX.loadResourceToString(c.s("path"));
                case "JavaUtil" -> JU.loadResourceToString(c.s("path"));
                default -> throw new AssertionError(iface);
            };
            case "call" -> () -> JC.call(null, JsonParser.parseString(c.s("in")).getAsJsonObject()).toString();
            case "getMetadata" -> () -> {
                JavaCallMetadata a = (i, o) -> { };
                JavaCallMetadata b = (i, o) -> { };
                return map("length", a.getMetadata().length, "sameInstanceAcrossImplementations", a.getMetadata() == b.getMetadata(),
                        "initial", Arrays.asList(a.getMetadata()));
            };
            case "writeThroughA_readThroughB" -> () -> {
                JavaCallMetadata a = (i, o) -> { };
                JavaCallMetadata b = (i, o) -> { };
                a.getMetadata()[0] = "escrito-por-a";
                String visto = b.getMetadata()[0];
                a.getMetadata()[0] = null;
                return visto;
            };
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-40/41 equivalence: JavaCall family default methods and JavaCallMetadata == legacy")
    Stream<DynamicTest> util40y41_equivalencia() {
        return Golden.casos("any", AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(), () -> Golden.igual(AREA, c, moderno(c))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-40: the contract keeps call(java.sql.Connection, JsonObject) throws Exception, and JavaCallExtra adds the out object")
    void util40_firmas() throws Exception {
        Method m = JavaCall.class.getMethod("call", Connection.class, JsonObject.class);
        assertThat(m.getReturnType()).isEqualTo(JsonObject.class);
        assertThat(m.getExceptionTypes()).containsExactly(Exception.class);
        Method x = JavaCallExtra.class.getMethod("call", Connection.class, JsonObject.class, JsonObject.class);
        assertThat(x.getReturnType()).isEqualTo(JsonObject.class);
        assertThat(JavaCall.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("UTIL-40: getGsonBuilder writes dates as yyyy-MM-dd HH:mm:ss.SSS, keeps nulls and NaN, and is a fresh instance per call")
    void util40_gson() throws Exception {
        Golden.enZona("UTC", () -> {
            assertThat(JC.getGsonBuilder().toJson(map("f", new java.util.Date(0), "n", null, "x", Double.NaN)))
                    .isEqualTo("{\"f\":\"1970-01-01 00:00:00.000\",\"n\":null,\"x\":NaN}");
            assertThat(JC.getGsonBuilder()).isNotSameAs(JC.getGsonBuilder());
        });
    }

    @Test
    @DisplayName("UTIL-41 legacy defect pinned: JavaCallMetadata.metadata is an interface constant, ONE String[4] shared by every implementation")
    void util41_arregloCompartido() {
        JavaCallMetadata a = (i, o) -> { };
        JavaCallMetadata b = (i, o) -> { };
        assertThat(a.getMetadata()).isSameAs(b.getMetadata()).isSameAs(JavaCallMetadata.metadata).hasSize(4);
    }
}
