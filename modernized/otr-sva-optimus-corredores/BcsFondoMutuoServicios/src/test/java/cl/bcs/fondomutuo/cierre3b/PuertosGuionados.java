package cl.bcs.fondomutuo.cierre3b;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.FechaContrato;

/**
 * The typed ports of step 3B toward the 3A classes, answered by the case script ({@link Guion}): the request is recorded as the legacy JSON of the
 * collaborator (so it is compared with what the legacy class sent to its registered double) and the scripted answer is read back with the legacy
 * Gson rules (plain Gson plus the legacy sql.Date reader, as the oracle's {@code Utils.getGsonBuilder()}).
 */
public final class PuertosGuionados {

    private static final com.google.gson.Gson LECTOR = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.sql.Date.class, (com.google.gson.JsonDeserializer<java.sql.Date>) (el, t, c) -> {
                String s = el.getAsString();
                java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(s.length() == 10 ? "yyyy-MM-dd"
                        : s.length() == 19 ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS");
                try {
                    return new java.sql.Date(f.parse(s).getTime());
                } catch (java.text.ParseException e) {
                    return null;
                }
            }).create();

    private PuertosGuionados() {
    }

    public static CuotaContrato.Puerto cuotas(Guion guion) {
        return entrada -> {
            JsonObject req = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
            JsonObject resp = guion.responder("CalcularMontoNumeroCuota", req);
            return LECTOR.fromJson(resp, CuotaContrato.Salida.class);
        };
    }

    public static FechaContrato.Puerto fechas(Guion guion) {
        return entrada -> {
            JsonObject req = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
            JsonObject resp = guion.responder("CalcularFechaSolicitudAporteRescate", req);
            return LECTOR.fromJson(resp, FechaContrato.Salida.class);
        };
    }
}
