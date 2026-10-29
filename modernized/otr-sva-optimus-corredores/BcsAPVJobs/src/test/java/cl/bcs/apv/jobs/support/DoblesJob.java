package cl.bcs.apv.jobs.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.adapter.javacall.mapeo.CargarArchivoPreviredMapeo;
import cl.bcs.apv.api.casouso.CargarArchivoPrevired;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredEntrada;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredResultado;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Reloj;

/** The scripted doubles a case gives to a job: the typed use case, the configuration port and the clock. */
public final class DoblesJob {

    /** The clock of every case (the legacy read the wall clock; the file-name stamp is masked in the goldens and checked against this instant). */
    public static final Instant AHORA = Instant.parse("2025-04-01T10:15:30Z");
    /** The stamp the job must put in the file name with {@link #AHORA}, in the JVM zone of the run (UTC). */
    public static final String MARCA = "20250401101530";

    private DoblesJob() {
    }

    public static Reloj reloj() {
        return () -> AHORA;
    }

    /** Port ConfiguracionSistema from the case: null when the parameter is not configured (the legacy raised or signalled; the caller decides). */
    public static ConfiguracionSistema configuracion(CasoJob caso) {
        Map<String, String> m = caso.config();
        return (idEmpresa, modulo, nombre) -> m.get(modulo + "/" + nombre);
    }

    /** The typed use case CargarArchivoPrevired answering the scripted legacy output and tracing its entrada as the legacy JSON of the oracle trace. */
    public static final class CargaPreviredDeCaso implements CargarArchivoPrevired {
        public static final String SERVICIO = "CargaMasivaPrevired";
        private final JsonObject guion;
        private final List<JsonObject> traza = new ArrayList<>();

        public CargaPreviredDeCaso(CasoJob caso) {
            this.guion = caso.colaboradores();
        }

        public List<JsonObject> traza() {
            return traza;
        }

        @Override
        public CargarArchivoPreviredResultado ejecutar(CargarArchivoPreviredEntrada entrada) {
            JsonObject registro = new JsonObject();
            registro.addProperty("servicio", SERVICIO);
            registro.add("entrada", CargarArchivoPreviredMapeo.INSTANCIA.entradaAJson(entrada));
            traza.add(registro);
            JsonElement g = guion.get(SERVICIO);
            ColaboradoresDeCaso.lanzarSiCorresponde(g);
            if (g == null || g.isJsonNull()) {
                return CargarArchivoPreviredResultado.porDefecto();
            }
            JsonObject o = g.getAsJsonObject();
            if (o.has("P_OUT_MENSAJES") && o.get("P_OUT_MENSAJES").isJsonNull()) {
                // an explicit null list cannot come through the mapper (it would default to empty): build the outcome with the null list, as the legacy object had it
                return new CargarArchivoPreviredResultado(null, o.has("P_OUT_ERRORESCONTADOR") ? o.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L);
            }
            return CargarArchivoPreviredMapeo.INSTANCIA.resultadoDesdeJson(o);
        }
    }
}
