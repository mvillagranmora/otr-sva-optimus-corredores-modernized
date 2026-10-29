package cl.bcs.cuentainversion.jobs.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.job.port.LibroInventario;
import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo;

/** The scripted outbound ports of the jobs, built from a {@link Caso}; each one answers what the oracle shim of the same legacy call answers. */
public final class Puertos {

    /** The call text the legacy LibroInventarioJob prepares (LibroInventarioJob.java:22, with its trailing ';'). */
    public static final String LLAMADA_LIBRO_INVENTARIO =
            "CALL \"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario\"(?,?,?);";

    private Puertos() {
    }

    /** Legacy ConfiguracionSistema / Utils.getConfiguracion: "MODULO/Parametro" from the case; null when not configured (the legacy threw). */
    public static ConfiguracionJobs configuracion(Caso caso) {
        Map<String, String> cfg = caso.config();
        return (idEmpresa, modulo, nombre) -> cfg.get(modulo + "/" + nombre);
    }

    private static JsonObject llamada(String texto, List<Object> parametros) {
        JsonObject o = new JsonObject();
        o.addProperty("llamada", texto);
        JsonArray ps = new JsonArray();
        for (Object v : parametros) {
            JsonObject x = new JsonObject();
            x.addProperty("tipo", v instanceof Long ? "long" : "string");
            if (v instanceof Long l) {
                x.addProperty("valor", l);
            } else {
                x.addProperty("valor", (String) v);
            }
            ps.add(x);
        }
        o.add("parametros", ps);
        return o;
    }

    /** The scripted LibroInventario procedure: one traced "StoredProcedure" call; "procedimiento.lanza" fails it; OUT value: procedimiento.cuentasAfectadas (7). */
    public static LibroInventario libroInventario(Traza traza) {
        return (fecha, idEjecucion) -> {
            traza.registrar("StoredProcedure", llamada(LLAMADA_LIBRO_INVENTARIO, java.util.Arrays.asList(fecha, idEjecucion)));
            String lanza = traza.texto("procedimiento.lanza");
            if (lanza != null) {
                throw new java.sql.SQLException(lanza);
            }
            JsonObject p = traza.objeto("procedimiento");
            return p.has("cuentasAfectadas") ? p.get("cuentasAfectadas").getAsLong() : 7L;
        };
    }

    /** The scripted stored procedure of the generic generator: "colaboradores.procedimiento.resultados"; one traced "StoredProcedure" call per execution. */
    public static ProcedimientoArchivo procedimiento(Traza traza) {
        return (llamada, parametros) -> {
            traza.registrar("StoredProcedure", llamada(llamada, parametros));
            String lanza = traza.texto("procedimiento.lanza");
            if (lanza != null) {
                throw new java.sql.SQLException(lanza);
            }
            List<ProcedimientoArchivo.ResultadoProcedimiento> out = new ArrayList<>();
            JsonObject script = traza.objeto("procedimiento");
            if (script.has("resultados")) {
                for (JsonElement r : script.getAsJsonArray("resultados")) {
                    List<String> columnas = new ArrayList<>();
                    r.getAsJsonObject().getAsJsonArray("columnas").forEach(c -> columnas.add(c.getAsString()));
                    List<List<String>> filas = new ArrayList<>();
                    for (JsonElement f : r.getAsJsonObject().getAsJsonArray("filas")) {
                        List<String> fila = new ArrayList<>();
                        f.getAsJsonArray().forEach(v -> fila.add(v.isJsonNull() ? null : v.getAsString()));
                        filas.add(fila);
                    }
                    out.add(new ProcedimientoArchivo.ResultadoProcedimiento(columnas, filas));
                }
            }
            return out;
        };
    }
}
