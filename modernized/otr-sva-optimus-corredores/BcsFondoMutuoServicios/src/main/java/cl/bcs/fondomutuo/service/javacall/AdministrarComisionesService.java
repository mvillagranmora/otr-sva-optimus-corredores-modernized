package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdministrarComisionesService {

    private static final Logger log = LoggerFactory.getLogger(AdministrarComisionesService.class);

    private final CrearComisionService crearComision;
    private final ModificarComisionService modificarComision;

    public AdministrarComisionesService(CrearComisionService crearComision, ModificarComisionService modificarComision) {
        this.crearComision = crearComision;
        this.modificarComision = modificarComision;
    }

    public JsonObject execute(JsonObject params) {
        log.info("AdministrarComisiones");

        JsonArray comisiones = params.has("p_Comisiones") ? params.getAsJsonArray("p_Comisiones") : new JsonArray();
        int errores = 0;
        JsonArray resultados = new JsonArray();

        for (JsonElement e : comisiones) {
            JsonObject c = e.getAsJsonObject();
            JsonObject req = new JsonObject();
            if (params.has("p_IdEmpresa")) req.add("p_IdEmpresa", params.get("p_IdEmpresa"));
            req.add("p_Comision", c);

            JsonObject res;
            boolean esNueva = !c.has("IdComision") || c.get("IdComision").isJsonNull()
                || c.get("IdComision").getAsLong() == 0L;
            if (esNueva) {
                res = crearComision.execute(req);
            } else {
                res = modificarComision.execute(req);
            }
            if (res.has("P_OUT_ERRORESCONTADOR")) errores += res.get("P_OUT_ERRORESCONTADOR").getAsInt();
            resultados.add(res);
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        out.add("P_OUT_RESULTADOS", resultados);
        return out;
    }
}
