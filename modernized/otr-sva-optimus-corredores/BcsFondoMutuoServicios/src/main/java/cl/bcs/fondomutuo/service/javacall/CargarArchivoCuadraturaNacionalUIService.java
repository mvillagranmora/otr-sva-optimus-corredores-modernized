package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CargarArchivoCuadraturaNacionalUIService {

    private static final Logger log = LoggerFactory.getLogger(CargarArchivoCuadraturaNacionalUIService.class);

    private final ProcesarArchivoCuadraturaNacionalService procesarService;

    public CargarArchivoCuadraturaNacionalUIService(ProcesarArchivoCuadraturaNacionalService procesarService) {
        this.procesarService = procesarService;
    }

    public JsonObject execute(JsonObject params) {
        log.info("CargarArchivoCuadraturaNacionalUI");
        // Delegates to ProcesarArchivoCuadraturaNacional after mapping file fields
        JsonObject delegated = new JsonObject();
        if (params.has("p_IdEmpresa")) delegated.add("p_IdEmpresa", params.get("p_IdEmpresa"));
        if (params.has("p_IdUsuario")) delegated.add("p_IdUsuario", params.get("p_IdUsuario"));
        if (params.has("p_IdModulo")) delegated.add("p_IdModulo", params.get("p_IdModulo"));
        if (params.has("p_FechaCuadratura")) delegated.add("p_FechaCuadratura", params.get("p_FechaCuadratura"));
        if (params.has("p_Cuadratura")) delegated.add("p_Cuadratura", params.get("p_Cuadratura"));
        return procesarService.execute(delegated);
    }
}
