package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultarDetalleSolicitudService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDetalleSolicitudService.class);

    private final ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUI;

    public ConsultarDetalleSolicitudService(ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUI) {
        this.consultarSolicitudAporteRescateUI = consultarSolicitudAporteRescateUI;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarDetalleSolicitud");
        // Delegates to ConsultarSolicitudAporteRescateUI — same logic
        JsonObject delegated = new JsonObject();
        delegated.addProperty("p_IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L);
        delegated.addProperty("p_IdUsuario", params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L);
        delegated.addProperty("p_IdModulo", params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "");
        if (params.has("p_SolicitudesAporteRescate")) {
            delegated.add("p_IdSolicitudAporteRescate", params.get("p_SolicitudesAporteRescate"));
        }
        return consultarSolicitudAporteRescateUI.execute(delegated);
    }
}
