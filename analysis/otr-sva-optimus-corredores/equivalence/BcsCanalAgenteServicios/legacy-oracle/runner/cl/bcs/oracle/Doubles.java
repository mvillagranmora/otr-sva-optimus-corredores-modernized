package cl.bcs.oracle;

import java.lang.reflect.Type;
import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.canalagente.oraculo.GuionColaboradores;

/**
 * Runtime of the REGISTERED DOUBLES of the BcsReplica oracle (ObtenerDiaSiguiente, ConsultarEmpresa generated from the real sources by tools/gen_doubles.py; ConfiguracionSistema,
 * Feriados and the Utils shim written by hand with the real signatures): each call is delegated to the shared {@link GuionColaboradores}, which records it and answers from the case.
 */
public final class Doubles {
    private static GuionColaboradores guion = new GuionColaboradores(new JsonObject());
    private static final Gson OUT = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().disableHtmlEscaping().create();
    private static final Gson IN = cl.bcs.utils.Utils.getGsonBuilder();

    private Doubles() { }

    public static void reset(JsonObject cfg) { guion = new GuionColaboradores(cfg); }

    public static JsonArray calls() { return guion.llamadas(); }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E { throw (E) t; }

    public static JsonElement answer(String colab, String metodo, Object[] args) {
        JsonArray argJson = new JsonArray();
        for (Object a : args) {
            if (a instanceof Connection) continue;
            argJson.add(OUT.toJsonTree(a));
        }
        try {
            return guion.responder(colab, metodo, argJson);
        } catch (Exception e) {
            Doubles.<RuntimeException>sneaky(e);
            return null;
        }
    }

    public static <T> T answer(String colab, String metodo, Object[] args, Class<T> ret) {
        JsonElement e = answer(colab, metodo, args);
        if (ret == Void.class || e == null || e.isJsonNull()) return null;
        return IN.fromJson(e, (Type) ret);
    }
}
