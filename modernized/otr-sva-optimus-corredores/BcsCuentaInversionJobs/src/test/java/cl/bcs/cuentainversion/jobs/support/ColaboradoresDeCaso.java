package cl.bcs.cuentainversion.jobs.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.Colaboradores;

/**
 * The scripted {@link Colaboradores} port (the platform services the PAUTA steps call): records every call under the SHORT class name
 * (the Servicios contract passes the legacy fully qualified name, {@code cl.bcs.plataforma.bat.InscribirProcesoBatch}; the oracle fake
 * records {@code InscribirProcesoBatch}) and answers "colaboradores.&lt;Servicio&gt;.salida" (absent: an empty object, as the legacy
 * InscribirProcesoBatchSalida has no field defaults). A scripted "&lt;Servicio&gt;.lanza" failure must be unchecked here (the port cannot
 * throw checked exceptions).
 */
public final class ColaboradoresDeCaso implements Colaboradores {

    private final Traza traza;

    public ColaboradoresDeCaso(Traza traza) {
        this.traza = traza;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        String nombre = servicio.substring(servicio.lastIndexOf('.') + 1);
        traza.registrar(nombre, entrada);
        try {
            traza.lanzaSiCorresponde(nombre + ".lanza");
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            // Step 2 fix: the legacy InscribirProcesoBatch.logica threw a CHECKED Exception (PB-04, PR-04); rethrown as it is (sneaky)
            throw ColaboradoresDeCaso.<RuntimeException>sneaky(e);
        }
        JsonElement salida = traza.objeto(nombre).get("salida");
        return salida == null || salida.isJsonNull() ? new JsonObject() : salida.getAsJsonObject().deepCopy();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E sneaky(Throwable e) throws E {
        throw (E) e;
    }
}
