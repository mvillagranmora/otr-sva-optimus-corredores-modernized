package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ProcesaCargaMasivaCustodia (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ProcesaCargaMasivaCustodiaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ProcesaCargaMasivaCustodiaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ProcesaCargaMasivaCustodia";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ProcesaCargaMasivaCustodiaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ProcesaCargaMasivaCustodia is deferred: cl.bcs.plataforma.ins.ConsultarDatosInstrumentos, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento, cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.Instrumento, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente");
    }
}
