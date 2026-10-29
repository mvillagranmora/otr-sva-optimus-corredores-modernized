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
public class ReversarAnticipoOperacionMesaRFService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ReversarAnticipoOperacionMesaRFService.class);
    private static final Gson GSON = new Gson();

    private final PlataformaBridge plataforma;

    public ReversarAnticipoOperacionMesaRFService(PlataformaBridge plataforma) {
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ReversarAnticipoOperacionMesaRF"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            JsonObject opParams = new JsonObject();
            opParams.addProperty("idEmpresa", in.p_IdEmpresa);
            opParams.addProperty("idUsuario", in.p_IdUsuario);
            opParams.addProperty("idModulo", in.p_IdModulo);
            opParams.addProperty("idOperacion", in.p_IdOperacion);
            JsonObject result = plataforma.call("cl.bcs.plataforma.ReversarAnticipoOperacionMesaRF", opParams);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "ReversarAnticipoOperacionMesaRF op=" + in.p_IdOperacion);
            return result.has("P_OUT_ERRORESCONTADOR") ? result : new Gson().fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("ReversarAnticipoOperacionMesaRF failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion;
    }
}
