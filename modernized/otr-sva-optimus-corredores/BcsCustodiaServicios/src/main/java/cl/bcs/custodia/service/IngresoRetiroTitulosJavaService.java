package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.IngresoRetiroTitulosJava (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo, cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceEntrada, cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.cre.ConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class IngresoRetiroTitulosJavaService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(IngresoRetiroTitulosJavaService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.IngresoRetiroTitulosJava";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("IngresoRetiroTitulosJavaService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.IngresoRetiroTitulosJava is deferred: cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo, cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceEntrada, cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.cre.ConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo, cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida");
    }
}
