package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.CertificarMovimientosEnTransito (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.ProcesarFacturacionCustodia, cl.bcs.facturacion.ProcesarFacturacionCustodia.MovimientoCustodiaSalida, cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Entrada, cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Salida, cl.bcs.facturacion.ReversarFacturacionCustodia, cl.bcs.mesa.MRF.CrearOperacionMesaCustodia, cl.bcs.plataforma.evt.AgregarEvento, cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS, cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CertificarMovimientosEnTransitoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CertificarMovimientosEnTransitoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.CertificarMovimientosEnTransito";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CertificarMovimientosEnTransitoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.CertificarMovimientosEnTransito is deferred: cl.bcs.facturacion.ProcesarFacturacionCustodia, cl.bcs.facturacion.ProcesarFacturacionCustodia.MovimientoCustodiaSalida, cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Entrada, cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Salida, cl.bcs.facturacion.ReversarFacturacionCustodia, cl.bcs.mesa.MRF.CrearOperacionMesaCustodia, cl.bcs.plataforma.evt.AgregarEvento, cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS, cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
