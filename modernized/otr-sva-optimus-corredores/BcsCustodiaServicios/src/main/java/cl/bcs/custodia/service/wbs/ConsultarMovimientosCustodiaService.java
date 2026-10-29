package cl.bcs.custodia.service.wbs;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.wbs.ConsultarMovimientosCustodia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaConsultarClientePorDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.SalidaConsultarClientePorDocumentoIdentidad.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarMovimientosCustodiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientosCustodiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.wbs.ConsultarMovimientosCustodia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarMovimientosCustodiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.wbs.ConsultarMovimientosCustodia is deferred: cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaConsultarClientePorDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.EntradaDocumentoIdentidad, cl.bcs.socionegocio.cli.ws.ConsultarClientePorDocumentoIdentidad.SalidaConsultarClientePorDocumentoIdentidad");
    }
}
