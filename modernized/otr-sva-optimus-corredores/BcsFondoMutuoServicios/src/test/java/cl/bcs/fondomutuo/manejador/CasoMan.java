package cl.bcs.fondomutuo.manejador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.normal.NormalPort;

/**
 * One case of the oracle ({@code fondomutuo-man/cases/<id>/{case.json,seed.sql,expected.json}}, golden recorded from the UNMODIFIED legacy) and the
 * way the NEW Manejador runs it: the scripted destinations answer exactly like the oracle's registered doubles ("colaboradores": "respuestas" with
 * "cuando" paths {@code 0.<field>} over the request, "error", "defecto") and record the call in the dump format. The dump has the keys of the golden
 * that this module can produce without a database: {@code salida}, {@code salidaCruda}, {@code excepcion}, {@code decisionServlet}, {@code colaboradores}.
 */
public final class CasoMan {
    static final Gson OUT = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    public final String id;
    public final JsonObject caso;
    public final JsonObject esperado;
    final JsonArray llamadas = new JsonArray();

    private CasoMan(String id, JsonObject caso, JsonObject esperado) {
        this.id = id;
        this.caso = caso;
        this.esperado = esperado;
    }

    public static List<CasoMan> todos() throws IOException {
        List<CasoMan> r = new ArrayList<>();
        for (Resource rc : new PathMatchingResourcePatternResolver().getResources("classpath*:fondomutuo-man/cases/*/case.json")) {
            String dir = rc.getURL().toString().replaceAll("/case\\.json$", "");
            String id = dir.substring(dir.lastIndexOf('/') + 1);
            Resource gold = rc.createRelative("expected.json");
            if (!gold.exists()) throw new IllegalStateException("case " + id + " has no golden (expected.json): record it from the legacy");
            r.add(new CasoMan(id, leer(rc), leer(gold)));
        }
        return r;
    }

    private static JsonObject leer(Resource rc) throws IOException {
        return JsonParser.parseString(new String(rc.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    public boolean traeNormal() {
        JsonElement l = caso.getAsJsonObject("entrada").get("p_SolicitudesAporteRescateNormal");
        return l != null && l.isJsonArray() && l.getAsJsonArray().size() > 0;
    }

    /** Answer of the scripted double: the oracle's matching rules (first matching entry; "error" throws; else "defecto"; else fail loudly). */
    private JsonElement responder(String colab, JsonObject arg) throws Exception {
        JsonObject c = caso.has("colaboradores") && caso.getAsJsonObject("colaboradores").has(colab) ? caso.getAsJsonObject("colaboradores").getAsJsonObject(colab) : null;
        if (c != null && c.has("respuestas")) {
            for (JsonElement e : c.getAsJsonArray("respuestas")) {
                JsonObject r = e.getAsJsonObject();
                boolean ok = true;
                if (r.has("cuando")) {
                    for (String path : r.getAsJsonObject("cuando").keySet()) {
                        JsonElement got = arg.get(path.substring(2));   // "0.<field>": argument 0 is the request
                        JsonElement want = r.getAsJsonObject("cuando").get(path);
                        ok &= got != null && !got.isJsonNull() && got.getAsString().equals(want.getAsString());
                    }
                }
                if (!ok) continue;
                if (r.has("error")) throw new Exception(r.get("error").getAsString());
                return r.get("respuesta");
            }
        }
        if (c != null && c.has("defecto")) return c.get("defecto");
        throw new IllegalStateException("case " + id + ": no scripted answer for " + colab);
    }

    /** Answer of a scripted destination (may throw like the oracle's double). */
    public interface Responder {
        JsonElement apply(String colaborador, JsonObject argumento) throws Exception;
    }

    /** The seven destinations of the Manejador answered by {@code responder(collaborator, request as JSON)} (the oracle's registered doubles). */
    public static Map<Lista, TipoSolicitudPort> destinos(Responder responder) {
        Map<Lista, TipoSolicitudPort> destinos = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) {
            if (l == Lista.NORMAL) continue;
            String colab = switch (l) {
                case PROGRAMADO -> "CrearSolicitudAporteRescateProgramado";
                case OVERNIGHT -> "CrearSolicitudAporteOvernight";
                case REINVERSION_APORTE -> "CrearSolicitudAporteReinversion";
                case CAMBIO_SERIE -> "CrearSolicitudCambioSerie";
                case RESCATE_REINVERSION -> "CrearSolicitudRescateReinversion";
                case TRANSFERENCIA -> "CrearSolicitudRescateTransferencia";
                default -> "CrearSolicitudRescateTraspaso";
            };
            destinos.put(l, p -> new Gson().fromJson(responder.apply(colab, OUT.toJsonTree(p).getAsJsonObject()), TipoSolicitudPort.Respuesta.class));
        }
        return destinos;
    }

    JsonObject ejecutar(NormalPort normal) {
        Map<Lista, TipoSolicitudPort> destinos = destinos((colab, arg) -> {
            JsonObject rec = new JsonObject();
            rec.addProperty("colaborador", colab);
            rec.addProperty("metodo", "logica");
            JsonArray args = new JsonArray();
            args.add(arg);
            rec.add("argumentos", args);
            llamadas.add(rec);
            JsonElement resp = responder(colab, arg);
            rec.add("respuesta", resp);
            return resp;
        });
        Compuertas g = new Compuertas("NORMAL,PROGRAMADO,OVERNIGHT,REINVERSION_APORTE,CAMBIO_SERIE,RESCATE_REINVERSION,TRANSFERENCIA,TRASPASO", false, false,
                false, false, false);
        ManejadorJavaCallHandler handler = new ManejadorJavaCallHandler(new ManejadorSolicitudAporteRescateService(normal, destinos, g::g0ListasConFilas));
        JsonObject dump = new JsonObject();
        try {
            JsonObject out = handler.call(null, caso.getAsJsonObject("entrada").deepCopy());
            dump.add("salida", out);
            dump.addProperty("salidaCruda", out.toString());
            JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
            dump.addProperty("decisionServlet", cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0 ? "COMMIT" : "ROLLBACK");
        } catch (Throwable t) {
            dump.addProperty("excepcion", t.getClass().getSimpleName() + ": " + t.getMessage());
            dump.addProperty("decisionServlet", "ROLLBACK_HTTP500");
        }
        dump.add("colaboradores", llamadas);
        return dump;
    }
}
