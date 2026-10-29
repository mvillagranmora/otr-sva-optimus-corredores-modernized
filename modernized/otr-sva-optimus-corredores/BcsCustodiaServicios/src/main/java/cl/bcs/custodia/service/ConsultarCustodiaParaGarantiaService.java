package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ConsultarCustodiaParaGarantia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pla.ObtenerPlazo, cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut, cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo, cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.plataforma.tas.EntregarPreciosTasasUI, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada_DatosInstrumentos.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarCustodiaParaGarantiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaParaGarantiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ConsultarCustodiaParaGarantia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarCustodiaParaGarantiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ConsultarCustodiaParaGarantia is deferred: cl.bcs.plataforma.pla.ObtenerPlazo, cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut, cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo, cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.plataforma.tas.EntregarPreciosTasasUI, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada_DatosInstrumentos");
    }
}
