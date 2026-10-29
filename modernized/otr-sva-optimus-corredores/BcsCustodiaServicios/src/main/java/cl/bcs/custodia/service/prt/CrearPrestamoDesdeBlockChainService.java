package cl.bcs.custodia.service.prt;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida, cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain, cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.GrupoIndicador, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Indicador, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento, cl.bcs.socionegocio.pojo.ConstantesBCK.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CrearPrestamoDesdeBlockChainService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CrearPrestamoDesdeBlockChainService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CrearPrestamoDesdeBlockChainService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain is deferred: cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE, cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada, cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida, cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain, cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.GrupoIndicador, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Indicador, cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento, cl.bcs.socionegocio.pojo.ConstantesBCK");
    }
}
