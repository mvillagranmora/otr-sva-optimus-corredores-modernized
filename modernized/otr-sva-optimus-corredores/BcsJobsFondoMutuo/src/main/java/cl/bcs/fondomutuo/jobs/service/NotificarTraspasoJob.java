package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Modernized BcsJobNotificarTraspaso.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.BcsJobNotificarTraspaso
 * Calls NotificarTraspasoConfirmar service.
 */
@Service
public class NotificarTraspasoJob {

    private static final Logger log = LoggerFactory.getLogger(NotificarTraspasoJob.class);
    public static final String ID_TIPO_PROGRAMA = "NOTIFICARTRASPASOFFMM";

    private final FondoMutuoServicioPort servicioPort;

    public NotificarTraspasoJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public void ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        servicioPort.notificarTraspaso(idEmpresa, idUsuario, "FMM");
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
    }
}
