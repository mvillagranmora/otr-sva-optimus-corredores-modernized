package cl.bcs.custodia.flujo.kit;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The Gson of the legacy JavaCall classes ({@code JavaCall.getGsonBuilder()}): dates as {@code yyyy-MM-dd HH:mm:ss.SSS}, nulls written, special floating
 * values, and the lenient {@code java.sql.Date} / {@code Timestamp} readers of BcsUtils (pattern chosen by the text length: 10 {@code yyyy-MM-dd}, 19
 * {@code yyyy-MM-dd HH:mm:ss}, otherwise {@code yyyy-MM-dd HH:mm:ss.SSS}; unparseable text gives {@code null}). Instances are thread-safe.
 */
public final class JsonFlujo {

    private static final Gson LEGACY = construir();

    private JsonFlujo() {
    }

    public static Gson legacy() {
        return LEGACY;
    }

    private static Gson construir() {
        GsonBuilder gbuild = new GsonBuilder();
        gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gbuild.registerTypeAdapter(java.sql.Timestamp.class, new LectorTimestamp());
        gbuild.registerTypeAdapter(java.sql.Date.class, new LectorFecha());
        return gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
    }

    private static Date leer(String texto) {
        SimpleDateFormat formato;
        if (texto.length() == 10) {
            formato = new SimpleDateFormat("yyyy-MM-dd");
        } else if (texto.length() == 19) {
            formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        try {
            return formato.parse(texto);
        } catch (ParseException e) {
            return null;
        }
    }

    private static final class LectorFecha implements JsonDeserializer<java.sql.Date> {
        @Override
        public java.sql.Date deserialize(JsonElement element, Type tipo, JsonDeserializationContext ctx) throws JsonParseException {
            Date d = leer(element.getAsString());
            return d == null ? null : new java.sql.Date(d.getTime());
        }
    }

    private static final class LectorTimestamp implements JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(JsonElement element, Type tipo, JsonDeserializationContext ctx) throws JsonParseException {
            Date d = leer(element.getAsString());
            return d == null ? null : new Timestamp(d.getTime());
        }
    }
}
