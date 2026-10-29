package cl.bcs.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * UTIL-23: lenient Gson deserializer for {@link java.sql.Date} (legacy {@code GsonDateDeserializer.java:21-33}); see
 * {@link FechaJsonLenient}. The time of day it was given is kept. Use:
 * {@code new GsonBuilder().registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer())}.
 */
public class GsonDateDeserializer implements JsonDeserializer<java.sql.Date> {

    /**
     * @return the date, or {@code null} when the text cannot be parsed; a {@link com.google.gson.JsonNull} passed
     * directly throws {@link UnsupportedOperationException} (through Gson a JSON null never reaches here)
     */
    @Override
    public java.sql.Date deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        Date d = FechaJsonLenient.leer(element.getAsString());
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
