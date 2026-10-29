package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class IngresarDetalleCompraVentaPactoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(IngresarDetalleCompraVentaPactoService.class);
    private static final Gson GSON = new Gson();

    private final PlataformaBridge plataforma;

    public IngresarDetalleCompraVentaPactoService(PlataformaBridge plataforma) {
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.IngresarDetalleCompraVentaPacto"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            JsonObject opParams = GSON.toJsonTree(params).getAsJsonObject();
            JsonObject result = plataforma.call("cl.bcs.plataforma.IngresarDetalleCompraVentaPacto", opParams);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "IngresarDetalleCompraVentaPacto op=" + in.p_IdOperacion);
            return result.has("P_OUT_ERRORESCONTADOR") ? result : new Gson().fromJson("{\"P_OUT_ERRORESCONTADOR\":0}", JsonObject.class);
        } catch (Exception e) {
            log.error("IngresarDetalleCompraVentaPacto failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class DetalleIn { public Long IdCartera; public BigDecimal Cantidad; public BigDecimal Precio; }
    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public List<DetalleIn> p_Detalles;
    }
}
