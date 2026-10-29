package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.EnviarFacturarPrepagoBono (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class EnviarFacturarPrepagoBonoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(EnviarFacturarPrepagoBonoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.EnviarFacturarPrepagoBono";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("EnviarFacturarPrepagoBonoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.EnviarFacturarPrepagoBono is deferred: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
