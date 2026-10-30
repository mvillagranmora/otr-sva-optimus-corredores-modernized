package cl.bcs.canalagente.kit;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The Gson of the platform (legacy {@code Utils.getGsonBuilder}): nulls kept, dates {@code yyyy-MM-dd HH:mm:ss.SSS}, and the lenient date readers of BcsUtils
 * ({@code GsonDateDeserializer}, {@code GsonTimestampDeserializer}: length 10, 19 or other picks the pattern; an unparseable text becomes null).
 */
public final class GsonPlataforma {

    private static final Logger log = LoggerFactory.getLogger(GsonPlataforma.class);

    public static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .registerTypeAdapter(java.sql.Timestamp.class, (JsonDeserializer<java.sql.Timestamp>) GsonPlataforma::timestamp)
            .registerTypeAdapter(java.sql.Date.class, (JsonDeserializer<java.sql.Date>) GsonPlataforma::fecha)
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private GsonPlataforma() {
    }

    private static java.util.Date leer(JsonElement e) {
        String texto = e.getAsString();
        SimpleDateFormat formato;
        if (texto.length() == 10) formato = new SimpleDateFormat("yyyy-MM-dd");
        else if (texto.length() == 19) formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        else formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            return formato.parse(texto);
        } catch (ParseException ex) {
            log.warn("Failed to parse Date: {}", ex.getMessage());
            return null;
        }
    }

    private static java.sql.Date fecha(JsonElement e, Type tipo, JsonDeserializationContext ctx) throws JsonParseException {
        java.util.Date d = leer(e);
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    private static java.sql.Timestamp timestamp(JsonElement e, Type tipo, JsonDeserializationContext ctx) throws JsonParseException {
        java.util.Date d = leer(e);
        return d == null ? null : new java.sql.Timestamp(d.getTime());
    }
}
