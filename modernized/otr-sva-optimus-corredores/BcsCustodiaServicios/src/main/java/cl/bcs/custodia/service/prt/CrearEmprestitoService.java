package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.CrearEmprestito (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain, cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Entrada, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Salida, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida, cl.bcs.socionegocio.pojo.ConstantesBCK.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CrearEmprestitoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CrearEmprestitoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.CrearEmprestito";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CrearEmprestitoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.CrearEmprestito is deferred: cl.bcs.plataforma.bat.AgregarPrograma, cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.pla.RevisarRequisitosBasicos, cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain, cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Entrada, cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Salida, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada, cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida, cl.bcs.socionegocio.pojo.ConstantesBCK");
    }
}
