package cl.bcs.fondomutuo.rescatereinversion;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * Legacy {@code cl.bcs.utils.Utils.getGsonBuilder()}, the Gson the legacy {@code CrearSolicitudRescateReinversion.call} uses to read its input AND to write
 * its output (FM-REI-01; unlike the plain {@code new Gson()} of Normal and of the Manejador): date format {@code yyyy-MM-dd HH:mm:ss.SSS}, the
 * {@code GsonTimestampDeserializer} / {@code GsonDateDeserializer} of BcsUtils (format chosen by the length 10 / 19 / other, JVM zone, an unparseable
 * text is {@code null}), special floating values and NULLS WRITTEN. Thread-safe, no request state.
 */
final class JsonRre {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .registerTypeAdapter(java.sql.Timestamp.class, (JsonDeserializer<java.sql.Timestamp>) (e, t, c) -> {
                java.util.Date d = parse(e);
                return d == null ? null : new java.sql.Timestamp(d.getTime());
            })
            .registerTypeAdapter(java.sql.Date.class, (JsonDeserializer<java.sql.Date>) JsonRre::fecha)
            .serializeSpecialFloatingPointValues().serializeNulls().create();

    private JsonRre() {
    }

    static Gson gson() {
        return GSON;
    }

    private static java.sql.Date fecha(JsonElement e, Type t, JsonDeserializationContext c) {
        java.util.Date d = parse(e);
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    private static java.util.Date parse(JsonElement element) {
        String date = element.getAsString();
        SimpleDateFormat format;
        if (date.length() == 10) format = new SimpleDateFormat("yyyy-MM-dd");
        else if (date.length() == 19) format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        else format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            return format.parse(date);
        } catch (ParseException exp) {
            return null;   // legacy: System.err line and null (DEV-F7: no log line)
        }
    }
}
