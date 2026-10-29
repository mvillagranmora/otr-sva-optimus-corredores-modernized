package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobTrxCFileManager.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobTrxCFileManager
 * Calls TrxCFileManager service.
 */
@Service
public class TrxCFileManagerJob {

    private static final Logger log = LoggerFactory.getLogger(TrxCFileManagerJob.class);
    public static final String ID_TIPO_PROGRAMA = "TrxCFileManager";

    private final FondoMutuoServicioPort servicioPort;

    public TrxCFileManagerJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        Map<String, Object> resultado = servicioPort.procesarTrxCFileManager(idEmpresa, idUsuario);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
