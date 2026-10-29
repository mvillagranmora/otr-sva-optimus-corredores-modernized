import java.lang.reflect.Field;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.generarArchivoPago.Archivo;
import cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagoBancoEstado;
import cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagos;
import cl.bcs.tesoreria.jobs.support.Canon;

/**
 * Direct invocation of the two helper classes that are not BcsJobs: case job "Pagos" (GenerarArchivoPagos) or "BancoEstado"
 * (GenerarArchivoPagoBancoEstado) with the typed {@code logica(conn, entrada)}, case "entrada" = {p_IdEmpresa, p_IdModulo, p_Fecha,
 * p_IdNominaTransferencia, p_Archivo: {nombreArchivo, extension, ruta}}. The legacy {@code call(con, in)} decodes that JSON with Gson into
 * the legacy Archivo, which holds FileWriter / CSVWriter fields: Gson of the JDK 25 of this oracle cannot build that adapter (the legacy
 * runtime, Java 8, could), so the Archivo is allocated WITHOUT its constructor and its three data fields are set by reflection, which is
 * what Gson did. The answer is serialised with a plain Gson as in {@code call}. Transaction model of the JavaCall handler: commit when it
 * returns, rollback when it throws.
 */
final class Directo {

    private Directo() {
    }

    static Archivo archivo(JsonObject a) throws Exception {
        Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        u.setAccessible(true);
        Archivo ar = (Archivo) ((sun.misc.Unsafe) u.get(null)).allocateInstance(Archivo.class);
        for (String k : new String[]{"nombreArchivo", "extension", "ruta"}) {
            Field f = Archivo.class.getDeclaredField(k);
            f.setAccessible(true);
            if (a.has(k) && !a.get(k).isJsonNull()) f.set(ar, a.get(k).getAsString());
        }
        return ar;
    }

    static void ejecutar(String job, JsonObject caso, OracleDb db, Path rutaBase, JsonObject expected) throws Exception {
        JsonObject in = caso.getAsJsonObject("entrada");
        try {
            Object out;
            if (job.equals("Pagos")) {
                GenerarArchivoPagos.EntradaGenerarArchivoPagos e = new GenerarArchivoPagos.EntradaGenerarArchivoPagos();
                e.p_IdEmpresa = in.get("p_IdEmpresa").getAsLong();
                e.p_IdModulo = in.get("p_IdModulo").getAsString();
                e.p_Fecha = in.get("p_Fecha").getAsString();
                e.p_IdNominaTransferencia = in.get("p_IdNominaTransferencia").getAsLong();
                e.p_Archivo = archivo(in.getAsJsonObject("p_Archivo"));
                out = new GenerarArchivoPagos().logica(db.conexionJob, e);
            } else {
                GenerarArchivoPagoBancoEstado.EntradaGenerarArchivoPagoBancoEstado e = new GenerarArchivoPagoBancoEstado.EntradaGenerarArchivoPagoBancoEstado();
                e.p_IdEmpresa = in.get("p_IdEmpresa").getAsLong();
                e.p_IdModulo = in.get("p_IdModulo").getAsString();
                e.p_Fecha = in.get("p_Fecha").getAsString();
                e.p_IdNominaTransferencia = in.get("p_IdNominaTransferencia").getAsLong();
                e.p_Archivo = archivo(in.getAsJsonObject("p_Archivo"));
                out = new GenerarArchivoPagoBancoEstado().logica(db.conexionJob, e);
            }
            db.conexionJob.commit();
            expected.addProperty("desenlace", "COMMIT");
            expected.add("salida", com.google.gson.JsonParser.parseString(new Gson().toJson(out)));
        } catch (Throwable t) {
            try {
                db.conexionJob.rollback();
            } catch (Exception ignored) {
            }
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", t.getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(t, rutaBase));
            expected.addProperty("desenlace", "ERROR");
            expected.add("excepcion", ex);
        }
    }
}
