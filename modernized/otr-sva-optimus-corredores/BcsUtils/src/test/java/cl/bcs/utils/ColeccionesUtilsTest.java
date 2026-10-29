package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import cl.bcs.utils.equivalencia.Pojos;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.d;
import static cl.bcs.utils.equivalencia.Golden.map;
import static cl.bcs.utils.equivalencia.Golden.ts;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure reflection/collection helpers of Utils, per zone (dates inside are rendered in the default zone):
 * UTIL-24 List2Hash (Utils.java:768-783), UTIL-25 getColumn (:792-813), UTIL-26 List2ListHashGenericObject
 * (:1385-1398), UTIL-27 copyPojo (:827-915). Not listed in the binding scope but pure and heavily used
 * (List2Hash 314 files, copyPojo 149, List2ListHashGenericObject 31): included and reported.
 */
class ColeccionesUtilsTest {

    static final String AREA = "collections";

    static Object dump(Object x) throws Exception {
        if (x == null) return null;
        if (x instanceof java.util.Date dt) return d(dt);
        if (x instanceof List<?> l) {
            List<Object> r = new ArrayList<>();
            for (Object e : l) r.add(dump(e));
            return r;
        }
        if (x instanceof String || x instanceof Number || x instanceof Boolean) return x.getClass().getSimpleName() + ":" + x;
        Map<String, Object> m = new LinkedHashMap<>();
        for (Field f : x.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            m.put(f.getName(), dump(f.get(x)));
        }
        return m;
    }

    static Object render(HashMap<?, Object> h) throws Exception {
        TreeMap<String, Object> t = new TreeMap<>();
        for (Map.Entry<?, Object> e : h.entrySet())
            t.put(e.getKey() + (e.getKey() == null ? "" : ":" + e.getKey().getClass().getSimpleName()), dump(e.getValue()));
        return t;
    }

    static Pojos.Origen origen() {
        Pojos.Origen s = new Pojos.Origen();
        s.Id = 10L;
        s.Num = 20;
        s.Texto = "texto";
        s.Monto = new BigDecimal("12.3400");
        s.FechaHora = ts(1725764400000L, 5);
        s.Fecha = new java.sql.Date(1725764400000L);
        s.Hora = new java.sql.Time(45030000L);
        s.Detalles = Arrays.asList(new Pojos.Detalle("A", 1L), new Pojos.Detalle("B", null));
        s.Tasa = 0.5;
        s.Activo = Boolean.TRUE;
        s.Primitivo = 42;
        s.FechaTexto = "2024-09-08";
        s.FechaHoraTexto = "2024-09-08 00:30:00";
        s.FechaHoraATexto = ts(1710512430123L, 123456789);
        s.FechaATexto = new java.sql.Date(1725764400000L);
        s.SoloOrigen = "solo";
        s.TipoDistinto = 99L;
        return s;
    }

    static final List<Pojos.Fila> FILAS = Arrays.asList(new Pojos.Fila("A", 1L, "x"), new Pojos.Fila("B", 2L, "y"), new Pojos.Fila("C", 3L, "x"));

    static List<?> lista(String code) {
        return switch (code) {
            case "A1x,B2y,C3x" -> FILAS;
            case "[]" -> new ArrayList<>();
            case "null1x,B2y" -> Arrays.asList(new Pojos.Fila(null, 1L, "x"), new Pojos.Fila("B", 2L, "y"));
            case "Fila(A),OtraFila(null)" -> Arrays.asList(new Pojos.Fila("A", 1L, "x"), new Pojos.OtraFila());
            case "null,null,z" -> Arrays.asList(new Pojos.Fila(null, 1L, null), new Pojos.Fila(null, 2L, null), new Pojos.Fila("Z", 3L, "z"));
            default -> throw new AssertionError(code);
        };
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        return switch (fn) {
            case "List2Hash" -> () -> render(Utils.List2Hash(c.s("list") == null ? null : lista(c.s("list")), c.s("keyfield")));
            case "getColumn" -> () -> Utils.getColumn(c.s("list") == null ? null : lista(c.s("list")), c.s("keyfield"), c.input().get("duplicate").getAsBoolean());
            case "List2ListHashGenericObject" -> () -> {
                if (c.s("list") == null) return Utils.List2ListHashGenericObject(null);
                if (c.es("list", "[]")) return Utils.List2ListHashGenericObject(new ArrayList<>());
                List<Object> r = new ArrayList<>();
                for (HashMap<String, Object> h : Utils.List2ListHashGenericObject(FILAS)) {
                    TreeMap<String, Object> t = new TreeMap<>();
                    for (Map.Entry<String, Object> e : h.entrySet()) t.put(e.getKey(), dump(e.getValue()));
                    r.add(t);
                }
                return r;
            };
            case "copyPojo" -> copy(c);
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    Callable<Object> copy(Caso c) {
        if (c.es("check", "identity")) return () -> {
            Pojos.Origen s = origen();
            Pojos.Destino t = Utils.copyPojo(s, new Pojos.Destino());
            return map("FechaHoraSameInstance", t.FechaHora == s.FechaHora, "DetallesSameInstance", t.Detalles == s.Detalles,
                    "DetalleItemSameInstance", t.Detalles.get(0) == s.Detalles.get(0), "MontoSameInstance", t.Monto == s.Monto,
                    "TextoSameInstance", t.Texto == s.Texto, "DetallesClass", t.Detalles.getClass().getName());
        };
        if (c.tiene("src") && c.s("src") == null) return () -> dump(Utils.copyPojo(null, new Pojos.Destino()));
        if (c.tiene("target") && c.s("target") == null) return () -> dump(Utils.copyPojo(origen(), null));
        if (c.es("src", "Fila(A,1,v)")) return () -> dump(Utils.copyPojo(new Pojos.Fila("A", 1L, "v"), new Pojos.Detalle()));
        if (c.es("restrictDate", "default(true)")) return () -> dump(Utils.copyPojo(origen(), new Pojos.Destino()));
        boolean rd = c.input().get("restrictDate").getAsBoolean();
        if (c.es("FechaTexto", "no-es-fecha")) return () -> {
            Pojos.Origen s = origen();
            s.FechaTexto = "no-es-fecha";
            return dump(Utils.copyPojo(s, new Pojos.Destino(), false));
        };
        if (c.tiene("nulls")) return () -> {
            Pojos.Origen s = origen();
            s.FechaTexto = null;
            s.FechaHoraTexto = null;
            s.FechaHoraATexto = null;
            s.FechaATexto = null;
            s.Texto = null;
            s.Detalles = null;
            return dump(Utils.copyPojo(s, new Pojos.Destino(), false));
        };
        return () -> dump(Utils.copyPojo(origen(), new Pojos.Destino(), rd));
    }

    @TestFactory
    @DisplayName("UTIL-24..27 equivalence per zone: List2Hash / getColumn / List2ListHashGenericObject / copyPojo == legacy")
    Stream<DynamicContainer> util24a27_equivalencia() {
        return Golden.ZONAS.entrySet().stream().map(z -> DynamicContainer.dynamicContainer(z.getValue(),
                Golden.casos(z.getKey(), AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(),
                        () -> Golden.enZona(z.getValue(), () -> Golden.igual(AREA, c, moderno(c)))))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-24: List2Hash throws java.lang.Exception on duplicate key and on null list; NoSuchFieldException on unknown field")
    void util24_errores() {
        assertThatThrownBy(() -> Utils.List2Hash(FILAS, "Valor")).isExactlyInstanceOf(Exception.class);
        assertThatThrownBy(() -> Utils.List2Hash(null, "Codigo")).isExactlyInstanceOf(Exception.class);
        assertThatThrownBy(() -> Utils.List2Hash(FILAS, "NoExiste")).isInstanceOf(NoSuchFieldException.class);
    }

    @Test
    @DisplayName("UTIL-27 legacy defect pinned: copyPojo silently skips Double, Boolean and primitive fields (no branch for them)")
    void util27_tiposNoCopiados() throws Exception {
        Pojos.Destino t = Utils.copyPojo(origen(), new Pojos.Destino());
        assertThat(t.Tasa).isNull();
        assertThat(t.Activo).isNull();
        assertThat(t.Primitivo).isZero();
        assertThat(t.Id).isEqualTo(10L);
        assertThat(t.SoloDestino).isEqualTo("intacto");
        assertThat(t.TipoDistinto).isEqualTo("intacto");
    }

    @Test
    @DisplayName("UTIL-27: copyPojo(restrictDate=false) converts String<->Timestamp/Date through Fecha; bad date text propagates DateTimeParseException")
    void util27_conversionFechas() throws Exception {
        Golden.enZona("UTC", () -> {
            Pojos.Destino t = Utils.copyPojo(origen(), new Pojos.Destino(), false);
            assertThat(t.FechaTexto).hasToString("2024-09-08");
            assertThat(t.FechaHoraTexto).hasToString("2024-09-08 00:30:00.0");
            assertThat(t.FechaHoraATexto).isEqualTo("2024-03-15 14:20:30.123");
            Pojos.Origen s = origen();
            s.FechaTexto = "no-es-fecha";
            assertThatThrownBy(() -> Utils.copyPojo(s, new Pojos.Destino(), false)).isInstanceOf(java.time.format.DateTimeParseException.class);
        });
    }
}
