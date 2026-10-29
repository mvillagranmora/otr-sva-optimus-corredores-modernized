package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RechazarModificarFechaGestionOperacionesService {

    private static final Logger log = LoggerFactory.getLogger(RechazarModificarFechaGestionOperacionesService.class);

    private final ConfirmarModificarFechaGestionOperacionesService confirmar;

    public RechazarModificarFechaGestionOperacionesService(ConfirmarModificarFechaGestionOperacionesService confirmar) {
        this.confirmar = confirmar;
    }

    public JsonObject execute(JsonObject params) {
        log.info("RechazarModificarFechaGestionOperaciones");
        // Rejection follows same query/update pattern as confirm but sets estado=RECHAZADO
        // Delegates to the confirmar service with rejection flag
        JsonObject rechazarParams = params.deepCopy();
        rechazarParams.addProperty("p_EstadoAccion", "RECHAZADO");
        return confirmar.execute(rechazarParams);
    }
}
