package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.IngresarGarantiaJava (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class IngresarGarantiaJavaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(IngresarGarantiaJavaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.IngresarGarantiaJava";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("IngresarGarantiaJavaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.IngresarGarantiaJava is deferred: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada");
    }
}
