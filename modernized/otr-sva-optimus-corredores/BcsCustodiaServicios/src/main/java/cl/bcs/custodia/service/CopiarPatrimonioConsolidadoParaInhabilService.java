package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.GuardarPatrimonioCaja, cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja, cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja, cl.bcs.replica.ReplicaUtils, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CopiarPatrimonioConsolidadoParaInhabilService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CopiarPatrimonioConsolidadoParaInhabilService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CopiarPatrimonioConsolidadoParaInhabilService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.CopiarPatrimonioConsolidadoParaInhabil is deferred: cl.bcs.cuentaInversion.GuardarPatrimonioCaja, cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja, cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja, cl.bcs.replica.ReplicaUtils, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn");
    }
}
