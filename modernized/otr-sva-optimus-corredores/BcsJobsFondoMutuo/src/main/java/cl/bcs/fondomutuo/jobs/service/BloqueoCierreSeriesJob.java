package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobBloqueoCierreSeries.
 *
 * Legacy: cl.bcs.fondomutuo.BcsJobBloqueoCierreSeries
 * Calls HANA SP: CALL "BSCL.FondoMutuo.BD.Servicios::CierreBloqueoAutomaticoFondos"(...)
 *
 * D-FMJ-05: HANA SP wrapped behind FondoMutuoServicioPort; Phase 3 implementation required.
 */
@Service
public class BloqueoCierreSeriesJob {

    private static final Logger log = LoggerFactory.getLogger(BloqueoCierreSeriesJob.class);
    public static final String ID_TIPO_PROGRAMA = "BLOQUEOCIERRESERIESFFMM";

    private final FondoMutuoServicioPort servicioPort;

    public BloqueoCierreSeriesJob(FondoMutuoServicioPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    /**
     * Executes the CierreBloqueoAutomaticoFondos HANA stored procedure.
     *
     * @param idEmpresa company ID (required)
     * @param idUsuario user ID (required)
     * @return service result map
     */
    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);
        // D-FMJ-05: delegates to port; Phase 2 stub throws UnsupportedOperationException
        Map<String, Object> resultado = servicioPort.ejecutarBloqueoCierreSeries(idEmpresa, idUsuario);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
