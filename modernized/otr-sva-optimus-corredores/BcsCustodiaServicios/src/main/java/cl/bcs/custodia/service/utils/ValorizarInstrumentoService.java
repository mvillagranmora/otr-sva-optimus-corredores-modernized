package cl.bcs.custodia.service.utils;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.utils.ValorizarInstrumento (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerNemoSVS, cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada, cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Salida, cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada, cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas, cl.bcs.plataforma.val.CalcularIntereses, cl.bcs.plataforma.val.Valorizar, cl.bcs.plataforma.val.Valorizar.OutValorizar, cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ, cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS, cl.bcs.plataforma.val.ValorizarSubyacentePacto, cl.bcs.plataforma.val.ValorizarSubyacentePacto.EntradaValorizarSubyacentePacto, cl.bcs.plataforma.val.ValorizarSubyacentePacto.SalidaValorizarSubyacentePacto.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ValorizarInstrumentoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ValorizarInstrumentoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.utils.ValorizarInstrumento";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ValorizarInstrumentoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.utils.ValorizarInstrumento is deferred: cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerNemoSVS, cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada, cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Salida, cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada, cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida, cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas, cl.bcs.plataforma.val.CalcularIntereses, cl.bcs.plataforma.val.Valorizar, cl.bcs.plataforma.val.Valorizar.OutValorizar, cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ, cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS, cl.bcs.plataforma.val.ValorizarSubyacentePacto, cl.bcs.plataforma.val.ValorizarSubyacentePacto.EntradaValorizarSubyacentePacto, cl.bcs.plataforma.val.ValorizarSubyacentePacto.SalidaValorizarSubyacentePacto");
    }
}
