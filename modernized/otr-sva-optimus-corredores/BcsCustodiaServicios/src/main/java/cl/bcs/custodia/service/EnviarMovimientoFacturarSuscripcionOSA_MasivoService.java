package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class EnviarMovimientoFacturarSuscripcionOSA_MasivoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(EnviarMovimientoFacturarSuscripcionOSA_MasivoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("EnviarMovimientoFacturarSuscripcionOSA_MasivoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo is deferred: cl.bcs.facturacion.CrearMovimientoComprobante, cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada, cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada");
    }
}
