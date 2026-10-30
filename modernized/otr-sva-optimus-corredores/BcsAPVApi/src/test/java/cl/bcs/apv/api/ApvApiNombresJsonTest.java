package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.apv.api.LegacyApv.Campo;
import cl.bcs.apv.api.comun.Mensaje;

/**
 * Pins how the legacy JSON names relate to the records. The records carry the TYPED (camelCase) names; the legacy JSON names ({@code p_IdEmpresa},
 * {@code P_OUT_MENSAJES}, ...) live ONLY in the adapter mapper of each operation (record javadoc: "The legacy JSON names live only in the adapter
 * mapper"). So there are two pins:
 * <ul>
 * <li>B-APVAPI-11: Gson round trip of a populated record of every one of the 234 classes ({@code fromJson(toJson(r)).equals(r)}, keys = component names in
 * order) and of the legacy JSON that the legacy POJO would produce (built from the legacy field names, in legacy order), renamed position by position:
 * the rename is bijective, every legacy key is accounted for and no legacy prefix leaks into a record.</li>
 * <li>B-APVAPI-12: every legacy field name, prefix included ({@code p_}, {@code P_}, {@code P_OUT_}), appears as the JSON key literal in the generated mapper
 * of its operation in {@code BcsApvJavaCallAdapter} (the place where the legacy JSON is really read and written).</li>
 * </ul>
 */
class ApvApiNombresJsonTest {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>) (s, t, c) -> new JsonPrimitive(s.toString()))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>) (j, t, c) -> LocalDate.parse(j.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (s, t, c) -> new JsonPrimitive(s.toString()))
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>) (j, t, c) -> LocalDateTime.parse(j.getAsString()))
            .create();

    private int semilla;

    // B-APVAPI-11
    @Test
    void b_apvapi_11_ida_y_vuelta_Gson_de_cada_record_contra_el_JSON_legacy_renombrado() throws Exception {
        GrafoApv g = GrafoApv.get();
        assertEquals(234, g.pares.size());
        int comprobados = 0;
        for (GrafoApv.Par p : g.pares) {
            semilla = 1;
            Object r = muestra(p.record(), 0);
            String json = GSON.toJson(r);
            Object vuelta = GSON.fromJson(json, p.record());
            assertEquals(r, vuelta, p.record().getName() + ": Gson round trip");
            JsonObject tipado = JsonParserCompat.objeto(json);
            RecordComponent[] cs = p.record().getRecordComponents();
            List<String> claves = new ArrayList<>(tipado.keySet());
            List<String> nombres = new ArrayList<>();
            for (RecordComponent c : cs) {
                nombres.add(c.getName());
            }
            assertEquals(nombres, claves, p.record().getName() + ": JSON keys of the record are its components, in order");

            JsonObject legacy = aLegacy(r, p.legacy(), p.record());
            List<String> clavesLegacy = new ArrayList<>(legacy.keySet());
            List<String> esperadasLegacy = p.campos().stream().map(Campo::nombre).toList();
            assertEquals(esperadasLegacy, clavesLegacy, p.record().getName() + ": legacy JSON keys, legacy order");
            // legacy -> typed: rename position by position, recursively; must give exactly the Gson tree of the record
            assertEquals(tipado, renombrarATipado(legacy, p.legacy(), p.record()), p.record().getName() + ": legacy JSON renamed = record JSON");
            // and typed -> legacy gives the legacy JSON back
            assertEquals(legacy, aLegacy(GSON.fromJson(tipado, p.record()), p.legacy(), p.record()), p.record().getName() + ": typed -> legacy");
            // legacy names with prefix never become the name of a component
            for (Campo c : p.campos()) {
                if (c.nombre().matches("^(?:p_|P_).*")) {
                    assertFalse(nombres.contains(c.nombre()), p.record().getName() + ": prefix leaked: " + c.nombre());
                }
            }
            comprobados++;
        }
        assertEquals(234, comprobados);
    }

    // B-APVAPI-12
    @Test
    void b_apvapi_12_los_nombres_legacy_con_prefijo_se_preservan_en_el_mapeador_JSON_del_adaptador() throws Exception {
        GrafoApv g = GrafoApv.get();
        Path dir = LegacyApv.modulo().resolve("../BcsApvJavaCallAdapter/src/main/java/cl/bcs/apv/adapter/javacall/mapeo").normalize();
        assertTrue(Files.isDirectory(dir), "adapter mapper directory not found (the test cannot be skipped): " + dir);
        List<String> faltan = new ArrayList<>();
        int conPrefijo = 0;
        int total = 0;
        for (GrafoApv.Par p : g.pares) {
            Path mapeo = dir.resolve(p.caso() + "Mapeo.java");
            assertTrue(Files.isRegularFile(mapeo), "mapper of " + p.caso() + " not found: " + mapeo);
            String texto = LegacyApv.leer(mapeo);
            for (Campo c : p.campos()) {
                total++;
                if (c.nombre().matches("^(?:p_|P_).*")) {
                    conPrefijo++;
                }
                if (!texto.contains("\"" + c.nombre() + "\"")) {
                    faltan.add(p.caso() + ": legacy JSON key \"" + c.nombre() + "\" of " + p.legacy().ruta() + " is not in " + mapeo.getFileName());
                }
            }
        }
        System.out.println("[B-APVAPI-12] legacy keys checked in the adapter mappers: " + total + " (with p_/P_ prefix: " + conPrefijo + ")");
        assertTrue(conPrefijo > 100, "expected many prefixed legacy names, found " + conPrefijo);
        assertTrue(faltan.isEmpty(), faltan.size() + " legacy keys missing:\n  " + String.join("\n  ", faltan.stream().limit(30).toList()));
    }

    // ------------------------------------------------------------------------------------------------ samples

    private Object muestra(Class<?> t, int prof) throws Exception {
        if (t.isRecord()) {
            RecordComponent[] cs = t.getRecordComponents();
            Class<?>[] tipos = new Class<?>[cs.length];
            Object[] vals = new Object[cs.length];
            for (int i = 0; i < cs.length; i++) {
                tipos[i] = cs[i].getType();
                vals[i] = valor(cs[i].getGenericType(), prof + 1);
            }
            Constructor<?> k = t.getDeclaredConstructor(tipos);
            return k.newInstance(vals);
        }
        throw new AssertionError("not a record " + t);
    }

    private Object valor(Type gt, int prof) throws Exception {
        Class<?> e = GrafoApv.elemento(gt);
        if (gt instanceof java.lang.reflect.ParameterizedType) {
            List<Object> l = new ArrayList<>();
            if (prof <= 3) {
                l.add(escalarOReg(e, prof));
                l.add(escalarOReg(e, prof));
            }
            return l;
        }
        return escalarOReg(e, prof);
    }

    private Object escalarOReg(Class<?> e, int prof) throws Exception {
        int n = semilla++;
        if (e == Long.class || e == long.class) {
            return (long) n;
        } else if (e == Integer.class || e == int.class) {
            return n;
        } else if (e == Double.class || e == double.class) {
            return n + 0.5;
        } else if (e == Float.class || e == float.class) {
            return n + 0.5f;
        } else if (e == Short.class || e == short.class) {
            return (short) n;
        } else if (e == Byte.class || e == byte.class) {
            return (byte) n;
        } else if (e == Boolean.class || e == boolean.class) {
            return n % 2 == 0;
        } else if (e == String.class) {
            return "v" + n;
        } else if (e == BigDecimal.class) {
            return new BigDecimal(n + ".5");
        } else if (e == LocalDate.class) {
            return LocalDate.of(2026, 1, 1).plusDays(n);
        } else if (e == LocalDateTime.class) {
            return LocalDateTime.of(2026, 1, 1, 10, 0).plusMinutes(n);
        } else if (e == JsonObject.class) {
            JsonObject o = new JsonObject();
            o.addProperty("k" + n, n);
            return o;
        } else if (e == Mensaje.class) {
            return new Mensaje((long) n, "t" + n, "c" + n, "d" + n);
        } else if (e.isRecord()) {
            return muestra(e, prof);
        }
        throw new AssertionError("no sample for " + e);
    }

    // ------------------------------------------------------------------------------------------------ legacy JSON <-> typed JSON

    /** The JSON the legacy POJO would produce for the same values: legacy field names in legacy order. */
    private JsonObject aLegacy(Object registro, LegacyApv.Clase lc, Class<?> rc) throws Exception {
        JsonObject o = new JsonObject();
        List<Campo> campos = LegacyApv.camposPlanos(lc);
        RecordComponent[] cs = rc.getRecordComponents();
        for (int i = 0; i < cs.length; i++) {
            Object v = cs[i].getAccessor().invoke(registro);
            Class<?> elem = GrafoApv.elemento(cs[i].getGenericType());
            LegacyApv.Clase hijo = elem != null && elem.isRecord() && elem != Mensaje.class ? hijo(lc, campos.get(i)) : null;
            if (v instanceof List<?> l) {
                JsonArray a = new JsonArray();
                for (Object x : l) {
                    a.add(hijo != null ? aLegacy(x, hijo, elem) : GSON.toJsonTree(x));
                }
                o.add(campos.get(i).nombre(), a);
            } else if (hijo != null) {
                o.add(campos.get(i).nombre(), aLegacy(v, hijo, elem));
            } else {
                o.add(campos.get(i).nombre(), GSON.toJsonTree(v));
            }
        }
        return o;
    }

    private JsonObject renombrarATipado(JsonObject legacy, LegacyApv.Clase lc, Class<?> rc) throws Exception {
        JsonObject o = new JsonObject();
        List<Campo> campos = LegacyApv.camposPlanos(lc);
        RecordComponent[] cs = rc.getRecordComponents();
        assertEquals(cs.length, legacy.size(), rc.getName());
        for (int i = 0; i < cs.length; i++) {
            JsonElement v = legacy.get(campos.get(i).nombre());
            assertTrue(v != null, "legacy key missing " + campos.get(i).nombre());
            Class<?> elem = GrafoApv.elemento(cs[i].getGenericType());
            LegacyApv.Clase hijo = elem != null && elem.isRecord() && elem != Mensaje.class ? hijo(lc, campos.get(i)) : null;
            if (hijo != null && v.isJsonArray()) {
                JsonArray a = new JsonArray();
                for (JsonElement x : v.getAsJsonArray()) {
                    a.add(renombrarATipado(x.getAsJsonObject(), hijo, elem));
                }
                o.add(cs[i].getName(), a);
            } else if (hijo != null) {
                o.add(cs[i].getName(), renombrarATipado(v.getAsJsonObject(), hijo, elem));
            } else {
                o.add(cs[i].getName(), v);
            }
        }
        return o;
    }

    private static LegacyApv.Clase hijo(LegacyApv.Clase lc, Campo c) {
        String t = c.tipo().replace("java.util.", "");
        t = t.replaceAll("^(?:List|ArrayList|Collection|LinkedList|Set|HashSet)<(.+)>$", "$1").replaceAll("\\[\\]$", "");
        return LegacyApv.resolver(t, lc);
    }

    /** tiny helper so the test does not depend on a particular JsonParser API shape. */
    static final class JsonParserCompat {
        static JsonObject objeto(String json) {
            return GSON.fromJson(json, JsonObject.class);
        }
    }
}
