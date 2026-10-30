package cl.bcs.replica.kit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class EntradaDiaSiguiente {
    private EntradaDiaSiguiente() {
    }

    /** JSON de {@code InObtenerDiaSiguiente} como lo arma el legacy: un dia, sin exigir dia habil. */
    public static JsonObject unDia(Long idPais, String fechaInicial, Long pais) {
        JsonObject f = new JsonObject();
        f.addProperty("fechaInicial", fechaInicial);
        f.addProperty("numeroDias", 1L);
        f.addProperty("pais", pais);
        f.addProperty("IdInterno", 0L);
        JsonArray fechas = new JsonArray();
        fechas.add(f);
        JsonObject in = new JsonObject();
        in.addProperty("p_idPais", idPais);
        in.addProperty("p_diaHabil", "N");
        in.addProperty("p_soloCaeDiaHabil", "N");
        in.addProperty("p_consideraHabilAnterior", "N");
        in.add("p_fechas", fechas);
        return in;
    }
}
