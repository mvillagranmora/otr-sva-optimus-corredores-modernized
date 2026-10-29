package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultarSolicitudesFFMMAgenteService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSolicitudesFFMMAgenteService.class);

    private final ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUI;

    public ConsultarSolicitudesFFMMAgenteService(ConsultarSolicitudAporteRescateUIService consultarSolicitudAporteRescateUI) {
        this.consultarSolicitudAporteRescateUI = consultarSolicitudAporteRescateUI;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarSolicitudesFFMMAgente");
        // Delegates to ConsultarSolicitudAporteRescateUI with agent filters
        JsonObject delegated = new JsonObject();
        delegated.addProperty("p_IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L);
        delegated.addProperty("p_IdUsuario", params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L);
        delegated.addProperty("p_IdModulo", params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "");
        if (params.has("p_IdCliente")) delegated.add("p_IdCliente", params.get("p_IdCliente"));
        if (params.has("p_IdPortafolio")) delegated.add("p_IdPortafolio", params.get("p_IdPortafolio"));
        if (params.has("p_IdSerieFondo")) delegated.add("p_IdSerieFondo", params.get("p_IdSerieFondo"));
        if (params.has("p_IdEmisor")) delegated.add("p_IdEmisor", params.get("p_IdEmisor"));
        if (params.has("p_Estado")) delegated.add("p_Estado", params.get("p_Estado"));
        if (params.has("p_FechaAceptacionDesde")) delegated.add("p_FechaAceptacionDesde", params.get("p_FechaAceptacionDesde"));
        if (params.has("p_FechaAceptacionHasta")) delegated.add("p_FechaAceptacionHasta", params.get("p_FechaAceptacionHasta"));
        if (params.has("p_IdTipoMovimientoFondoMutuo")) delegated.add("p_IdTipoMovimientoFondoMutuo", params.get("p_IdTipoMovimientoFondoMutuo"));
        return consultarSolicitudAporteRescateUI.execute(delegated);
    }
}
