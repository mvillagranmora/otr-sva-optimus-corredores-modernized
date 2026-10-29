package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearOperacionMesaCustodiaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearOperacionMesaCustodiaService.class);
    private static final Gson GSON = new Gson();

    private final PlataformaBridge plataforma;

    public CrearOperacionMesaCustodiaService(PlataformaBridge plataforma) {
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.CrearOperacionMesaCustodia"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            // Delegate to plataforma — no local SQL files exist for custodia variant
            JsonObject opParams = new JsonObject();
            opParams.addProperty("idEmpresa", in.p_IdEmpresa);
            opParams.addProperty("idUsuario", in.p_IdUsuario);
            opParams.addProperty("idModulo", in.p_IdModulo);
            opParams.addProperty("idPortafolio", in.p_IdPortafolio);
            opParams.addProperty("idInstrumento", in.p_IdInstrumento);
            JsonObject result = plataforma.call("cl.bcs.plataforma.CrearOperacionMesaCustodia", opParams);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "CrearOperacionMesaCustodia ejecutado");
            return result;
        } catch (Exception e) {
            log.error("CrearOperacionMesaCustodia failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdPortafolio; public String p_IdInstrumento; public String p_FechaEmision;
        public String p_FechaVencimiento; public Long p_IdSocioNegocio;
    }
}
