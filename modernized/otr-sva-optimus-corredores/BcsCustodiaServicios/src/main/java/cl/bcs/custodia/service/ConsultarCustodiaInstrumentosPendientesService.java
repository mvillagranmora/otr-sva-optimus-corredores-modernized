package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ConsultarCustodiaInstrumentosPendientes (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ConsultarCustodiaInstrumentosPendientesService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaInstrumentosPendientesService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ConsultarCustodiaInstrumentosPendientes";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ConsultarCustodiaInstrumentosPendientesService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ConsultarCustodiaInstrumentosPendientes is deferred: cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas");
    }
}
