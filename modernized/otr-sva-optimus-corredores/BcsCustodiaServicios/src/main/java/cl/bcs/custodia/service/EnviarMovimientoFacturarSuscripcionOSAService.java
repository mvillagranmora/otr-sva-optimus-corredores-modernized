package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class EnviarMovimientoFacturarSuscripcionOSAService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(EnviarMovimientoFacturarSuscripcionOSAService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("EnviarMovimientoFacturarSuscripcionOSAService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA is deferred: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida");
    }
}
