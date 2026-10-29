package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobValorCuotaFileManager.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobValorCuotaFileManager
 * Calls ValorCuotaFileManager service (includes HANA SP CargarValorCuota).
 *
 * D-FMJ-24: HANA SP CargarValorCuota with #temp_table syntax deferred to Phase 3.
 *   Phase 2 port stub throws UnsupportedOperationException.
 */
@Service
public class ValorCuotaFileManagerJob {

    private static final Logger log = LoggerFactory.getLogger(ValorCuotaFileManagerJob.class);
    public static final String ID_TIPO_PROGRAMA = "ValorCuotaFileManager";

    private final FondoMutuoServicioPort servicioPort;

    public ValorCuotaFileManagerJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");
        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        Map<String, Object> resultado = servicioPort.procesarValorCuotaFileManager(idEmpresa, idUsuario);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
