package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ManejadorSolicitudAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(ManejadorSolicitudAporteRescateService.class);

    private final CrearSolicitudAporteRescateNormalService crearNormal;
    private final CrearSolicitudAporteRescateProgramadoService crearProgramado;
    private final CrearSolicitudAporteOvernightService crearOvernight;
    private final CrearSolicitudAporteReinversionService crearReinversion;
    private final CrearSolicitudRescateReinversionService crearRescateReinversion;
    private final CrearSolicitudRescateTransferenciaService crearRescateTransferencia;
    private final CrearSolicitudRescateTraspasoService crearRescateTraspaso;

    public ManejadorSolicitudAporteRescateService(
            CrearSolicitudAporteRescateNormalService crearNormal,
            CrearSolicitudAporteRescateProgramadoService crearProgramado,
            CrearSolicitudAporteOvernightService crearOvernight,
            CrearSolicitudAporteReinversionService crearReinversion,
            CrearSolicitudRescateReinversionService crearRescateReinversion,
            CrearSolicitudRescateTransferenciaService crearRescateTransferencia,
            CrearSolicitudRescateTraspasoService crearRescateTraspaso) {
        this.crearNormal = crearNormal;
        this.crearProgramado = crearProgramado;
        this.crearOvernight = crearOvernight;
        this.crearReinversion = crearReinversion;
        this.crearRescateReinversion = crearRescateReinversion;
        this.crearRescateTransferencia = crearRescateTransferencia;
        this.crearRescateTraspaso = crearRescateTraspaso;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ManejadorSolicitudAporteRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray resultado = new JsonArray();

        if (params.has("p_SolicitudesAporteRescateNormal") && params.getAsJsonArray("p_SolicitudesAporteRescateNormal").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesAporteRescate", params.get("p_SolicitudesAporteRescateNormal"));
            JsonObject r = crearNormal.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudesAporteRescateProgramado") && params.getAsJsonArray("p_SolicitudesAporteRescateProgramado").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesAporte", params.get("p_SolicitudesAporteRescateProgramado"));
            JsonObject r = crearProgramado.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudAporteOvernight") && params.getAsJsonArray("p_SolicitudAporteOvernight").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesAporte", params.get("p_SolicitudAporteOvernight"));
            JsonObject r = crearOvernight.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudAporteReinversion") && params.getAsJsonArray("p_SolicitudAporteReinversion").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesAporte", params.get("p_SolicitudAporteReinversion"));
            JsonObject r = crearReinversion.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudRescateReinversionRescate") && params.getAsJsonArray("p_SolicitudRescateReinversionRescate").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesRescate", params.get("p_SolicitudRescateReinversionRescate"));
            if (params.has("p_SolicitudRescateReinversionAporte")) {
                delegateParams.add("p_SolicitudesAporte", params.get("p_SolicitudRescateReinversionAporte"));
            }
            JsonObject r = crearRescateReinversion.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudRescateTransferencia") && params.getAsJsonArray("p_SolicitudRescateTransferencia").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesTransferencia", params.get("p_SolicitudRescateTransferencia"));
            JsonObject r = crearRescateTransferencia.execute(delegateParams);
            resultado.add(r);
        }
        if (params.has("p_SolicitudRescateTraspaso") && params.getAsJsonArray("p_SolicitudRescateTraspaso").size() > 0) {
            JsonObject delegateParams = params.deepCopy();
            delegateParams.add("p_SolicitudesTraspaso", params.get("p_SolicitudRescateTraspaso"));
            JsonObject r = crearRescateTraspaso.execute(delegateParams);
            resultado.add(r);
        }

        out.add("P_OUT_RESULTADO", resultado);
        return out;
    }
}
