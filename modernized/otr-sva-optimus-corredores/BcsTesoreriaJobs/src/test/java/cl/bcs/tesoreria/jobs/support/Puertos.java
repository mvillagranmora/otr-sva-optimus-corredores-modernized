package cl.bcs.tesoreria.jobs.support;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.jobs.job.port.CabecerasCarga;
import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.tesoreria.port.out.Secuencias;

/** The scripted outbound ports of the jobs, built from a {@link Caso}; each one answers what the oracle shim of the same legacy call answers. */
public final class Puertos {

    private Puertos() {
    }

    /** Legacy ConfiguracionSistema: "MODULO/Parametro" from the case; null when not configured (the legacy threw; the job must). */
    public static ConfiguracionSistema configuracion(Caso caso) {
        Map<String, String> cfg = caso.config();
        return (idEmpresa, modulo, nombre) -> cfg.get(modulo + "/" + nombre);
    }

    /** The database UTC clock of the case. */
    public static Reloj reloj(Caso caso) {
        Instant ahora = Timestamp.valueOf(caso.reloj()).toLocalDateTime().toInstant(java.time.ZoneOffset.UTC);
        return () -> ahora;
    }

    /** The oracle Diccionario shim: {@code "[CODIGO]{k1=v1, k2=v2}"}, keys sorted, null value as empty. */
    public static DiccionarioMensajes diccionario() {
        return (idEmpresa, codigo, valores) -> {
            Map<String, String> sorted = new TreeMap<>();
            valores.forEach((k, v) -> sorted.put(k, v == null ? "" : v));
            return "[" + codigo + "]" + sorted;
        };
    }

    /** Sequences: the next value of each name starts at case "secuencias" (default 1); every request is traced. */
    public static Secuencias secuencias(Caso caso, Traza traza) {
        Map<String, Long> siguiente = new HashMap<>(caso.secuencias());
        return nombre -> {
            traza.secuencia(nombre);
            long v = siguiente.getOrDefault(nombre, 1L);
            siguiente.put(nombre, v + 1);
            return v;
        };
    }

    /** Legacy getCabeceras: "colaboradores.cabeceras.&lt;idEstado&gt;" headers (default 0); traced as in the oracle. */
    public static CabecerasCarga cabeceras(Traza traza) {
        return (tipo, fecha, estado) -> {
            JsonObject o = new JsonObject();
            o.addProperty("tipoArchivoCarga", tipo);
            o.addProperty("fechaEjecucion", fecha);
            o.addProperty("idEstado", estado);
            traza.registrar("CargaMasivaIngresoEgresoDAPFFMM.getCabeceras", o);
            traza.lanzaSiCorresponde("getCabeceras.lanza");
            JsonObject c = traza.objeto("cabeceras");
            return c.has(estado) ? c.get(estado).getAsInt() : 0;
        };
    }

    /** The scripted stored procedure: "colaboradores.procedimiento.resultados"; one traced "StoredProcedure" call per execution. */
    public static ProcedimientoArchivo procedimiento(Traza traza) {
        return (llamada, parametros) -> {
            JsonObject o = new JsonObject();
            o.addProperty("llamada", llamada);
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
            traza.registrar("StoredProcedure", o);
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
