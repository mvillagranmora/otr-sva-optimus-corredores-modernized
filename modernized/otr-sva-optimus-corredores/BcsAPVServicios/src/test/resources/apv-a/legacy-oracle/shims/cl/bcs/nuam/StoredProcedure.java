package cl.bcs.nuam;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * ORACLE SHIM of cl.bcs.nuam.StoredProcedure. The legacy class is NOT in the repository (it comes from a platform library): only its use is known,
 * {@code new StoredProcedure("<catalog procedure>").call(conn, JsonObject)} (ObtenerFolioSuscripcion.java, ActualizarFolioSuscripcion). A HANA
 * procedure is not reproducible on H2, so the call is RECORDED as the collaborator "StoredProcedure" ({procedimiento, entrada}, the same shape the
 * SQL.call shim records) and answers the scripted output of case.json "colaboradores.SalidaProcedimiento.<procedure>" (default: an empty object).
 * The effect of the procedure on its tables is whatever the case seed left there: the procedure itself is outside the proof.
 */
public class StoredProcedure {
    private final String nombre;

    public StoredProcedure(String nombre) {
        this.nombre = nombre;
    }

    public JsonObject call(Connection conn, JsonObject entrada) throws Exception {
        java.util.Map<String, Object> reg = new java.util.LinkedHashMap<>();
        reg.put("procedimiento", nombre);
        reg.put("entrada", entrada);
        cl.bcs.oracle.Hooks.registrar("StoredProcedure", reg);
        JsonObject salida = new JsonObject();
        com.google.gson.JsonObject cols = cl.bcs.oracle.Hooks.colaboradores;
        if (cols != null && cols.has("SalidaProcedimiento") && cols.getAsJsonObject("SalidaProcedimiento").has(nombre)) {
            cols.getAsJsonObject("SalidaProcedimiento").getAsJsonObject(nombre).entrySet().forEach(e -> salida.add(e.getKey(), e.getValue().deepCopy()));
        }
        return salida;
    }
}
