package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.RetirarGarantiaJava (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo, cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo, cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class RetirarGarantiaJavaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(RetirarGarantiaJavaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.RetirarGarantiaJava";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("RetirarGarantiaJavaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.RetirarGarantiaJava is deferred: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo, cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo, cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
