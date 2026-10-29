package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Deliberately WRONG implementations used only by the harness self check (self_check.py). Each mutant is a plausible mistake of a
 * rewrite. Select with -Doracle.subject=cl.bcs.oracle.MutantSubject -Doracle.mutant=NAME. The self check requires that the
 * comparison against the recorded goldens reports differences for every mutant; a mutant that goes unnoticed means the case set is
 * too weak.
 *
 * <ul>
 *   <li>reverse-order: returns the modules in reverse order (HANA gives no ORDER BY, but the goldens pin insertion order).</li>
 *   <li>filter-by-module: "fixes" D-ESM-1 by honouring p_IdModulo (modules different from the requested one disappear).</li>
 *   <li>drop-last: loses the last module.</li>
 *   <li>counter-1: P_OUT_ERRORESCONTADOR = 1 (the servlet would roll back).</li>
 *   <li>emit-nulls: serialises with serializeNulls (adds p_out_AdvertenciasDetalle / p_out_ErroresDetalle = null).</li>
 *   <li>lower-estado: lower-cases IdEstadoModulo.</li>
 *   <li>swap-fields: IdEstadoModulo and NombreModulo swapped.</li>
 *   <li>trim-name: trims NombreModulo.</li>
 * </ul>
 */
public class MutantSubject implements Subject {
    private final Subject real = new LegacySubject();

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        String m = System.getProperty("oracle.mutant", "");
        JsonObject out = real.call(con, jin);
        JsonArray lista = out.getAsJsonArray("p_out_ListaModulos");
        switch (m) {
            case "reverse-order": {
                JsonArray r = new JsonArray();
                for (int i = lista.size() - 1; i >= 0; i--) r.add(lista.get(i));
                out.add("p_out_ListaModulos", r);
                break;
            }
            case "filter-by-module": {
                JsonElement want = jin.get("p_IdModulo");
                if (want != null && !want.isJsonNull() && !want.getAsString().isEmpty()) {
                    JsonArray r = new JsonArray();
                    for (JsonElement e : lista) {
                        if (e.getAsJsonObject().get("NombreModulo").getAsString().equals(want.getAsString())) r.add(e);
                    }
                    out.add("p_out_ListaModulos", r);
                }
                break;
            }
            case "drop-last":
                if (lista.size() > 0) lista.remove(lista.size() - 1);
                break;
            case "counter-1":
                out.addProperty("P_OUT_ERRORESCONTADOR", 1);
                break;
            case "emit-nulls":
                out.add("p_out_AdvertenciasDetalle", com.google.gson.JsonNull.INSTANCE);
                out.add("p_out_ErroresDetalle", com.google.gson.JsonNull.INSTANCE);
                break;
            case "lower-estado":
                for (JsonElement e : lista) {
                    JsonObject o = e.getAsJsonObject();
                    o.addProperty("IdEstadoModulo", o.get("IdEstadoModulo").getAsString().toLowerCase());
                }
                break;
            case "swap-fields":
                for (JsonElement e : lista) {
                    JsonObject o = e.getAsJsonObject();
                    String a = o.get("IdEstadoModulo").getAsString();
                    o.addProperty("IdEstadoModulo", o.get("NombreModulo").getAsString());
                    o.addProperty("NombreModulo", a);
                }
                break;
            case "trim-name":
                for (JsonElement e : lista) {
                    JsonObject o = e.getAsJsonObject();
                    o.addProperty("NombreModulo", o.get("NombreModulo").getAsString().trim());
                }
                break;
            default:
                throw new IllegalStateException("unknown mutant '" + m + "'");
        }
        return out;
    }
}
