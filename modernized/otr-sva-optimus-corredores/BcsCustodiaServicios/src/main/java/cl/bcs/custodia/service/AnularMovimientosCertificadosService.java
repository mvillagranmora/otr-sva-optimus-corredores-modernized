package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.AnularMovimientosCertificados (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.AnularComprobanteFinal.CargoAbono_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.EntidadTesoreria_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.MovimientoCarteraEnTransito_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.Subyacente_Proceso, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class AnularMovimientosCertificadosService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(AnularMovimientosCertificadosService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.AnularMovimientosCertificados";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("AnularMovimientosCertificadosService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.AnularMovimientosCertificados is deferred: cl.bcs.facturacion.AnularComprobanteFinal.CargoAbono_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.EntidadTesoreria_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.MovimientoCarteraEnTransito_Proceso, cl.bcs.facturacion.AnularComprobanteFinal.Subyacente_Proceso, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
