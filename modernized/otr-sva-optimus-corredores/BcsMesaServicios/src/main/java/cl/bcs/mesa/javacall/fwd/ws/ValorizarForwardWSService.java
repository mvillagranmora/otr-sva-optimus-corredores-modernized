package cl.bcs.mesa.javacall.fwd.ws;

import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValorizarForwardWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValorizarForwardWSService.class);
    private static final Gson GSON = new Gson();

    private final PlataformaBridge plataforma;

    public ValorizarForwardWSService(PlataformaBridge plataforma) {
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.ws.ValorizarForwardWS"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try {
            Entrada in = GSON.fromJson(params, Entrada.class);

            // Delegate to plataforma — no local SQL files for this WS variant
            JsonObject opParams = new JsonObject();
            opParams.addProperty("idEmpresa", in.p_IdEmpresa);
            opParams.addProperty("idUsuario", in.p_IdUsuario);
            opParams.addProperty("idModulo", in.p_IdModulo);
            opParams.addProperty("fecha", in.p_Fecha);
            JsonObject result = plataforma.call("cl.bcs.plataforma.ValorizarForwardWS", opParams);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ValorizarForwardWS fecha=" + in.p_Fecha);
            return result.has("P_OUT_ERRORESCONTADOR") ? result : new Gson().fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("ValorizarForwardWS failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo; public String p_Fecha;
    }
}
