package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.ValorizarCustodiaUIbatch (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.pau.ValorizarCarteraCupones, cl.bcs.plataforma.pau.ValorizarCarteraFuturos, cl.bcs.plataforma.pau.ValorizarCarteraPactos, cl.bcs.plataforma.pau.ValorizarCarteraPrestEmpres, cl.bcs.plataforma.pau.ValorizarCarteraIF, cl.bcs.plataforma.pau.ValorizarCarteraOpciones, cl.bcs.plataforma.pau.ValorizarCarteraRF, cl.bcs.plataforma.pau.ValorizarCarteraRV, cl.bcs.plataforma.pau.ValorizarCarteraSimultaneas, cl.bcs.plataforma.pau.ValorizarGarantiasRVRFIF, cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class ValorizarCustodiaUIbatchService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(ValorizarCustodiaUIbatchService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.ValorizarCustodiaUIbatch";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("ValorizarCustodiaUIbatchService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.ValorizarCustodiaUIbatch is deferred: cl.bcs.plataforma.pau.ValorizarCarteraCupones, cl.bcs.plataforma.pau.ValorizarCarteraFuturos, cl.bcs.plataforma.pau.ValorizarCarteraPactos, cl.bcs.plataforma.pau.ValorizarCarteraPrestEmpres, cl.bcs.plataforma.pau.ValorizarCarteraIF, cl.bcs.plataforma.pau.ValorizarCarteraOpciones, cl.bcs.plataforma.pau.ValorizarCarteraRF, cl.bcs.plataforma.pau.ValorizarCarteraRV, cl.bcs.plataforma.pau.ValorizarCarteraSimultaneas, cl.bcs.plataforma.pau.ValorizarGarantiasRVRFIF, cl.bcs.fondomutuo.ActualizarCarterasFondosMutuos");
    }
}
