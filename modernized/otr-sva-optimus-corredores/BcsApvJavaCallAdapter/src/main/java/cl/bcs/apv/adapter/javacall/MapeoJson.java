package cl.bcs.apv.adapter.javacall;

import java.lang.reflect.Type;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Runtime helpers of the generated mappers. Scalars are converted by the SAME Gson the legacy used ({@link Sabor}), so number/string coercions
 * and error behaviour are the legacy ones. Read rules: key missing = default, key null = null (primitives: default), key present = Gson conversion.
 * Write rules: null is omitted by {@link Sabor#PLANO} and written as JSON null by {@link Sabor#CONSTRUCTOR}; null ELEMENTS of arrays are always written.
 */
public final class MapeoJson {

    private MapeoJson() {
    }

    private static boolean ausente(JsonObject o, String k) {
        return !o.has(k);
    }

    private static boolean nulo(JsonElement e) {
        return e == null || e.isJsonNull();
    }

    /**
     * The legacy parsed the TEXT of the whole input ({@code gson.fromJson(in.toString(), X.class)}); reading an element through its text keeps the
     * text-reader semantics (for example {@code 1.5} into a Long fails, while a tree reader would truncate it).
     */
    private static <T> T leer(Sabor s, JsonElement e, Type tipo) {
        return s.gson().fromJson(e.toString(), tipo);
    }

    // ------------------------------------------------------------------ read
    public static <T> T escalar(Sabor s, JsonObject o, String k, Class<T> tipo, T defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        return nulo(e) ? null : leer(s, e, tipo);
    }

    /** Java primitive component: a JSON null leaves the default (Gson does not assign null to a primitive field). */
    public static <T> T primitivo(Sabor s, JsonObject o, String k, Class<T> tipoCaja, T defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        return nulo(e) ? defecto : leer(s, e, tipoCaja);
    }

    public static <T> List<T> listaEscalar(Sabor s, JsonObject o, String k, Class<T> tipo, List<T> defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        if (nulo(e)) {
            return null;
        }
        if (tipo == JsonObject.class && e.isJsonArray()) {
            // Gson reads a JSON null element of a List<JsonObject> as a JsonNull the record cannot hold: it is kept as a null element (written back as null)
            List<T> r = new ArrayList<>();
            for (JsonElement x : e.getAsJsonArray()) {
                r.add(nulo(x) ? null : leer(s, x, tipo));
            }
            return r;
        }
        Type t = TypeToken.getParameterized(List.class, tipo).getType();
        return leer(s, e, t);
    }

    public static <T> T objeto(Sabor s, JsonObject o, String k, Function<JsonObject, T> conv, T defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        if (nulo(e)) {
            return null;
        }
        return conv.apply(comoObjeto(k, e));
    }

    public static <T> List<T> listaObjeto(Sabor s, JsonObject o, String k, Function<JsonObject, T> conv, List<T> defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        if (nulo(e)) {
            return null;
        }
        if (!e.isJsonArray()) {
            throw new JsonSyntaxException("Expected a JSON array for '" + k + "'");
        }
        List<T> r = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) {
            r.add(nulo(x) ? null : conv.apply(comoObjeto(k, x)));
        }
        return r;
    }

    private static JsonObject comoObjeto(String k, JsonElement e) {
        if (!e.isJsonObject()) {
            throw new JsonSyntaxException("Expected a JSON object for '" + k + "'");
        }
        return e.getAsJsonObject();
    }

    /** legacy {@code java.sql.Date} field to {@link LocalDate}. */
    public static LocalDate fechaSql(Sabor s, JsonObject o, String k, LocalDate defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        if (nulo(e)) {
            return null;
        }
        Date d = leer(s, e, Date.class);
        return d == null ? null : d.toLocalDate();
    }

    /** legacy {@code java.util.Date} field to {@link LocalDateTime} (system zone, as the legacy JVM). */
    public static LocalDateTime fechaUtil(Sabor s, JsonObject o, String k, LocalDateTime defecto) {
        if (ausente(o, k)) {
            return defecto;
        }
        JsonElement e = o.get(k);
        if (nulo(e)) {
            return null;
        }
        java.util.Date d = leer(s, e, java.util.Date.class);
        return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    // ------------------------------------------------------------------ write
    public static void poner(Sabor s, JsonObject j, String k, Object valor) {
        if (valor == null) {
            if (s.serializaNulos()) {
                j.add(k, JsonNull.INSTANCE);
            }
            return;
        }
        j.add(k, s.gson().toJsonTree(valor));
    }

    public static void ponerLista(Sabor s, JsonObject j, String k, List<?> lista) {
        if (lista == null) {
            poner(s, j, k, null);
            return;
        }
        JsonArray a = new JsonArray();
        for (Object x : lista) {
            a.add(x == null ? JsonNull.INSTANCE : s.gson().toJsonTree(x));
        }
        j.add(k, a);
    }

    public static <T> void ponerObjeto(Sabor s, JsonObject j, String k, T valor, Function<T, JsonObject> conv) {
        if (valor == null) {
            poner(s, j, k, null);
            return;
        }
        j.add(k, conv.apply(valor));
    }

    public static <T> void ponerListaObjeto(Sabor s, JsonObject j, String k, List<T> lista, Function<T, JsonObject> conv) {
        if (lista == null) {
            poner(s, j, k, null);
            return;
        }
        JsonArray a = new JsonArray();
        for (T x : lista) {
            a.add(x == null ? JsonNull.INSTANCE : conv.apply(x));
        }
        j.add(k, a);
    }

    public static void ponerFechaSql(Sabor s, JsonObject j, String k, LocalDate valor) {
        poner(s, j, k, valor == null ? null : Date.valueOf(valor));
    }

    public static void ponerFechaUtil(Sabor s, JsonObject j, String k, LocalDateTime valor) {
        poner(s, j, k, valor == null ? null : java.util.Date.from(valor.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
