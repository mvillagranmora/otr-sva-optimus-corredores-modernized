package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobValorCuotaInternacionalFileManager.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobValorCuotaInternacionalFileManager
 * Calls ValorCuotaInternacionalFileManager service (includes HANA SP).
 *
 * D-FMJ-25: HANA SP with #temp_table syntax deferred to Phase 3.
 */
@Service
public class ValorCuotaInternacionalFileManagerJob {

    private static final Logger log = LoggerFactory.getLogger(ValorCuotaInternacionalFileManagerJob.class);
    public static final String ID_TIPO_PROGRAMA = "ValorCuotaInternacionalFileManager";

    private final FondoMutuoServicioPort servicioPort;

    public ValorCuotaInternacionalFileManagerJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        Map<String, Object> resultado = servicioPort.procesarValorCuotaInternacionalFileManager(idEmpresa, idUsuario);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
