package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobAdministracionLiquidez.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobAdministracionLiquidez
 * Delegates to AdministracionLiquidez service.
 *
 * D-FMJ-01: BcsJob replaced with @Service.
 * D-FMJ-02: main() removed.
 * D-FMJ-03: HANA connection replaced by FondoMutuoServicioPort (Phase 3 REST client).
 * D-FMJ-04: hardcoded debug values removed; parameters always required.
 */
@Service
public class AdministracionLiquidezJob {

    private static final Logger log = LoggerFactory.getLogger(AdministracionLiquidezJob.class);
    public static final String ID_TIPO_PROGRAMA = "ADMINISTRACIONLIQUIDEZFFMM";

    private final FondoMutuoServicioPort servicioPort;

    public AdministracionLiquidezJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Executes the AdministracionLiquidez job.
     *
     * @param idEmpresa company ID (required)
     * @param idUsuario user ID (required)
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        Map<String, Object> resultado = servicioPort.ejecutarAdministracionLiquidez(
                idEmpresa, idUsuario, "FMM", "N", "S");
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
