package cl.bcs.apv.application.comun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.apv.api.comun.Mensaje;

/**
 * JSON helpers for the contract with the collaborators OUTSIDE APV ({@code Colaboradores} port): the payload is the legacy input
 * object of that service, written with the legacy settings of {@code Utils.getGsonBuilder()} (nulls written as JSON null, dates
 * {@code yyyy-MM-dd HH:mm:ss.SSS}, numbers exactly as given: a {@link BigDecimal} keeps its scale), and the answer is read tolerantly the way Gson filled the
 * legacy Salida: a key that is absent keeps the legacy default (the reader decides it: {@link #lista} answers an empty list), a key that is present with
 * JSON null is null.
 */
public final class Json {

    /** The legacy platform Gson ({@code Utils.getGsonBuilder()} without its Timestamp/Date deserializers: only used to write). */
    public static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeSpecialFloatingPointValues().create();

    private Json() {
    }

    // ---------------------------------------------------------------- writing

    /** Fluent builder of a legacy payload; {@code poner(k, null)} writes JSON null (serializeNulls). */
    public static Objeto objeto() {
        return new Objeto();
    }

    public static final class Objeto {
        private final JsonObject o = new JsonObject();

        public Objeto poner(String clave, Object valor) {
            o.add(clave, aElemento(valor));
            return this;
        }

        public JsonObject construir() {
            return o;
        }
    }

    /** Java value to its JSON element (null, Number, Boolean, String, Character, JsonElement, collections, anything else through the legacy Gson). */
    public static JsonElement aElemento(Object valor) {
        if (valor == null) {
            return JsonNull.INSTANCE;
        }
        if (valor instanceof JsonElement e) {
            return e;
        }
        if (valor instanceof Number n) {
            return new JsonPrimitive(n);
        }
        if (valor instanceof Boolean b) {
            return new JsonPrimitive(b);
        }
        if (valor instanceof String || valor instanceof Character) {
            return new JsonPrimitive(valor.toString());
        }
        if (valor instanceof Collection<?> c) {
            JsonArray a = new JsonArray();
            for (Object x : c) {
                a.add(aElemento(x));
            }
            return a;
        }
        return GSON.toJsonTree(valor);
    }

    public static JsonArray aArreglo(Collection<?> valores) {
        return aElemento(valores).getAsJsonArray();
    }

    // ---------------------------------------------------------------- reading

    /** Text of a member; null when absent or JSON null (numbers and booleans give their literal). */
    public static String texto(JsonObject o, String clave) {
        JsonElement e = o == null ? null : o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    public static Long largo(JsonObject o, String clave) {
        JsonElement e = o == null ? null : o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsBigDecimal().longValue();
    }

    public static BigDecimal decimal(JsonObject o, String clave) {
        JsonElement e = o == null ? null : o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsBigDecimal();
    }

    public static JsonObject objeto(JsonObject o, String clave) {
        JsonElement e = o == null ? null : o.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsJsonObject();
    }

    /**
     * The objects of an array member. Absent: an empty list (the legacy Salida initialised its lists in the constructor); present as JSON null: null
     * (Gson assigns null over the default); an element that is JSON null stays a null element.
     */
    public static List<JsonObject> lista(JsonObject o, String clave) {
        if (o == null || !o.has(clave)) {
            return new ArrayList<>();
        }
        JsonElement e = o.get(clave);
        if (e.isJsonNull()) {
            return null;
        }
        List<JsonObject> filas = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) {
            filas.add(x.isJsonNull() ? null : x.getAsJsonObject());
        }
        return filas;
    }

    /** Messages of a Salida ({@code P_OUT_MENSAJES}, {@code p_out_Mensajes}...): legacy {@code MensajeSalida} {id, tipo, codigo, descripcion}. */
    public static List<Mensaje> mensajes(JsonObject o, String clave) {
        List<JsonObject> filas = lista(o, clave);
        if (filas == null) {
            return null;
        }
        List<Mensaje> mensajes = new ArrayList<>();
        for (JsonObject f : filas) {
            mensajes.add(f == null ? null : new Mensaje(largo(f, "id"), texto(f, "tipo"), texto(f, "codigo"), texto(f, "descripcion")));
        }
        return mensajes;
    }
}
