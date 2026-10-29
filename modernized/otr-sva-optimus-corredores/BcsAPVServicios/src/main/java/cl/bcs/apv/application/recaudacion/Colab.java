package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Helpers of family C for the collaborators OUTSIDE APV (reached through the {@link Colaboradores} port with the legacy JSON contract) and for the
 * legacy utilities that are ports here (sequences, stored procedures).
 */
final class Colab {

    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final Pattern NOMBRE_PROCEDIMIENTO = Pattern.compile("(?i)call\\s+\"([^\"]+)\"");

    /** Encoder of what is handed to a collaborator (nulls serialised, dates {@code yyyy-MM-dd HH:mm:ss.SSS}). */
    private static final Gson ENTRADA = new GsonBuilder().serializeNulls().setDateFormat(FORMATO_FECHA).create();
    /** Decoder of collaborator answers: legacy {@code Utils.getGsonBuilder()} (date format, special floats). */
    private static final Gson SALIDA = new GsonBuilder().setDateFormat(FORMATO_FECHA).serializeSpecialFloatingPointValues().serializeNulls().create();

    private Colab() {
    }

    static JsonObject aJson(Object o) {
        return ENTRADA.toJsonTree(o).getAsJsonObject();
    }

    static <T> T de(JsonObject json, Class<T> tipo) {
        return SALIDA.fromJson(json, tipo);
    }

    /** Legacy {@code Utils.getSecuencia(con, name, n)}: n values (empty when n is null or not positive); the port draws one value per call. */
    static List<Long> secuencia(Secuencias secuencias, String nombre, Long cantidad) {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        for (long i = 0; i < cantidad; i++) {
            salida.add(secuencias.siguiente(nombre));
        }
        return salida;
    }

    /**
     * Legacy {@code SQL.call}: HANA stored procedures cannot run on the test database; they go through the port as service {@code StoredProcedure}
     * with {@code {procedimiento, entrada}} (the entrada keys sorted). The answer is the OUT parameters by name.
     */
    static JsonObject procedimiento(Colaboradores colaboradores, String sqlCall, Map<String, Object> entrada) {
        Matcher m = NOMBRE_PROCEDIMIENTO.matcher(sqlCall);
        Map<String, Object> reg = new LinkedHashMap<>();
        reg.put("procedimiento", m.find() ? m.group(1) : sqlCall);
        reg.put("entrada", new TreeMap<>(entrada));
        return colaboradores.invocar("StoredProcedure", aJson(reg));
    }
}
