package cl.bcs.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

/**
 * UTIL-23: lenient Gson deserializer for {@link Timestamp} (legacy {@code GsonTimestampDeserializer.java:20-32}); see
 * {@link FechaJsonLenient}. Use:
 * {@code new GsonBuilder().registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer())}.
 */
public class GsonTimestampDeserializer implements JsonDeserializer<Timestamp> {

    /**
     * @return the timestamp (millisecond precision), or {@code null} when the text cannot be parsed; a
     * {@link com.google.gson.JsonNull} passed directly throws {@link UnsupportedOperationException}
     */
    @Override
    public Timestamp deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        Date d = FechaJsonLenient.leer(element.getAsString());
        return d == null ? null : new Timestamp(d.getTime());
    }
}
