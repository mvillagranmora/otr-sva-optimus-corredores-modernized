package cl.bcs.oracle;

import java.sql.Connection;
import java.sql.Statement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Deliberately WRONG implementations used only by the harness self check (self_check.py). Each mutant is a plausible mistake of a rewrite.
 * Select with -Doracle.subject=cl.bcs.oracle.MutantSubject -Doracle.mutant=NAME. The self check requires that the comparison against the
 * recorded goldens reports a difference for every mutant; a mutant that goes unnoticed means the case set is too weak.
 *
 * <ul>
 *   <li>counter-0: P_OUT_ERRORESCONTADOR forced to 0 (the servlet would COMMIT a rejected request).</li>
 *   <li>counter-missing: the counter is removed (CallServlet rolls back when it is absent).</li>
 *   <li>drop-message: loses the first P_OUT_MENSAJES entry.</li>
 *   <li>resultado-monto-plus-1: the first P_OUT_RESULTADO Monto + 1.</li>
 *   <li>delete-solicitudes: the INSERT of FMM.SolicitudAporteRescate is lost (rows deleted after the call).</li>
 *   <li>update-estado: the stored solicitud gets Estado ANU.</li>
 *   <li>lock-left-behind: an extra row stays in the anti duplicate lock table.</li>
 *   <li>drop-collaborator-call: the last collaborator call is not made (or not recorded).</li>
 *   <li>sequence-skipped: an extra number is taken from the SolicitudAporteRescate sequence.</li>
 *   <li>emit-nulls: serialises the output with a null extra property.</li>
 * </ul>
 */
public class MutantSubject implements Subject {
    private final Subject real = new LegacySubject();

    @Override
    public JsonObject call(String servicio, Connection con, JsonObject jin) throws Exception {
        String m = System.getProperty("oracle.mutant", "");
        JsonObject out = real.call(servicio, con, jin);
        switch (m) {
            case "counter-0": out.addProperty("P_OUT_ERRORESCONTADOR", 0); break;
            case "counter-missing": out.remove("P_OUT_ERRORESCONTADOR"); break;
            case "drop-message": {
                JsonArray a = out.getAsJsonArray("P_OUT_MENSAJES");
                if (a != null && a.size() > 0) a.remove(0);
                break;
            }
            case "resultado-monto-plus-1": {
                JsonArray a = out.getAsJsonArray("P_OUT_RESULTADO");
                if (a != null && a.size() > 0) {
                    JsonObject o = a.get(0).getAsJsonObject();
                    if (o.has("Monto")) o.addProperty("Monto", o.get("Monto").getAsBigDecimal().add(java.math.BigDecimal.ONE));
                }
                break;
            }
            case "delete-solicitudes": exec(con, "DELETE FROM \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\""); break;
            case "update-estado": exec(con, "UPDATE \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\" SET \"Estado\" = 'ANU'"); break;
            case "lock-left-behind":
                exec(con, "INSERT INTO \"" + SessionConnection.LOCK_TABLE + "\" (\"IdBloqueoSolicitudAporteRescate\",\"IdPortafolio\",\"IdSerieFondo\","
                        + "\"IdRegimenTributario\",\"TipoSolicitud\",\"Monto\",\"NumeroCuotas\",\"FechaSistema\") VALUES ('mutant',1,'X','Y','RES',1,1,DATE '2026-01-01')");
                break;
            case "drop-collaborator-call": {
                JsonArray c = Doubles.calls();
                if (c.size() > 0) c.remove(c.size() - 1);
                break;
            }
            case "sequence-skipped": cl.bcs.utils.Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", 1L); break;
            case "emit-nulls": out.add("P_OUT_EXTRA", com.google.gson.JsonNull.INSTANCE); break;
            default: throw new IllegalStateException("unknown mutant '" + m + "'");
        }
        return out;
    }

    private static void exec(Connection con, String sql) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    @SuppressWarnings("unused")
    private static JsonElement unused() { return null; }
}
