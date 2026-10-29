package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalEmisionLiberada (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalEmisionLiberadaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalEmisionLiberadaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalEmisionLiberada";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalEmisionLiberadaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalEmisionLiberada is deferred: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas");
    }
}
