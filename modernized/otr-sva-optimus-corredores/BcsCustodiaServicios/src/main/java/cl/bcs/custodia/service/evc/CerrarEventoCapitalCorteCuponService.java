package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalCorteCuponService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalCorteCuponService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalCorteCuponService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon is deferred: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar, cl.bcs.plataforma.pla.ObtenerDiaSiguiente, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida");
    }
}
