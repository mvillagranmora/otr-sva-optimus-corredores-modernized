package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * Legacy {@code cl.bcs.utils.Utils.getGsonBuilder()}: the Gson of {@code ConsultarCustodiaPortafolioFFMM.call} (date format {@code yyyy-MM-dd HH:mm:ss.SSS}, length-based
 * date deserializers, special floating values, NULLS WRITTEN). A NEW Gson PER CALL (the date format takes the zone of the JVM when it is created). No request state.
 */
final class ConsultarCustodiaPortafolioFfmmJson {

    private ConsultarCustodiaPortafolioFfmmJson() {
    }

    static Gson gson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .registerTypeAdapter(java.sql.Timestamp.class, (JsonDeserializer<java.sql.Timestamp>) (e, t, c) -> {
                java.util.Date d = parse(e);
                return d == null ? null : new java.sql.Timestamp(d.getTime());
            })
            .registerTypeAdapter(java.sql.Date.class, (JsonDeserializer<java.sql.Date>) ConsultarCustodiaPortafolioFfmmJson::fecha)
            .serializeSpecialFloatingPointValues().serializeNulls().create();
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
            return null;
        }
    }
}
