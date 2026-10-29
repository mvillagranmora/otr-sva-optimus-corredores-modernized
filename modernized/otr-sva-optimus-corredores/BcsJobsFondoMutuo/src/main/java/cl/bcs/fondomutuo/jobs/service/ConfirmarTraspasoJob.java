package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.domain.RegistroTraspaso;
import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Modernized BcsJobConfirmarTraspaso.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.BcsJobConfirmarTraspaso
 * Reads TRASPASO_PAUTA_CONFIRMAR and TRASPASO_DIAS_CONFIRMAR config params,
 * queries pending traspasos from HANA, then calls ConfirmarTraspaso service.
 *
 * Business rules:
 *   B-FMJ-08  TRASPASO_PAUTA_CONFIRMAR = "S" → execute confirmation.
 *   B-FMJ-09  TRASPASO_PAUTA_CONFIRMAR ≠ "S" → skip (no-op).
 *   B-FMJ-10  Zero traspasos → skip service call.
 *
 * D-FMJ-12: HANA queries behind HanaFondoMutuoPort.
 * D-FMJ-13: ConfirmarTraspaso call behind FondoMutuoServicioPort.
 */
@Service
public class ConfirmarTraspasoJob {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarTraspasoJob.class);
    public static final String ID_TIPO_PROGRAMA = "CONFIRMARTRASPASOFFMM";
    private static final String SI = "S";

    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public ConfirmarTraspasoJob(HanaFondoMutuoPort hanaPort, FondoMutuoServicioPort servicioPort) {
        this.hanaPort    = hanaPort;
        this.servicioPort = servicioPort;
    }

    /**
     * Executes the ConfirmarTraspaso job.
     *
     * @param idEmpresa company ID (required)
     * @param idUsuario user ID (required)
     * @return service result map; empty map when no action taken
     */
    public Map<String, Object> ejecutar(Long idEmpresa, Long idUsuario) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        Objects.requireNonNull(idUsuario, "idUsuario is required");

        log.info("*{}* Inicio de Job", ID_TIPO_PROGRAMA);

        // Read configuration parameters
        String traspasoConfirmar;
        long diasTranscurridos;
        try {
            traspasoConfirmar = hanaPort.consultarParametroConfiguracion(
                    idEmpresa, "FONDOMUTUO.TRASPASO_PAUTA_CONFIRMAR");
        } catch (Exception e) {
            traspasoConfirmar = "N";
        }
        try {
            diasTranscurridos = Long.parseLong(hanaPort.consultarParametroConfiguracion(
                    idEmpresa, "FONDOMUTUO.TRASPASO_DIAS_CONFIRMAR"));
        } catch (Exception e) {
            diasTranscurridos = 0L;
        }

        log.info("*{}* TRASPASO_PAUTA_CONFIRMAR = {}", ID_TIPO_PROGRAMA, traspasoConfirmar);

        // B-FMJ-09: skip if not enabled
        if (!SI.equals(traspasoConfirmar)) {
            log.info("*{}* Confirmacion de traspasos no habilitada", ID_TIPO_PROGRAMA);
            return Map.of("accion", "omitida");
        }

        // B-FMJ-08: query pending traspasos
        List<RegistroTraspaso> traspasos = hanaPort.consultarTraspasosPendientes(idEmpresa, diasTranscurridos);

        // B-FMJ-10: nothing to confirm
        if (traspasos.isEmpty()) {
            log.info("*{}* Sin traspasos para confirmar", ID_TIPO_PROGRAMA);
            return Map.of("accion", "sin_traspasos");
        }

        List<Long> idTraspasos = traspasos.stream().map(RegistroTraspaso::idTraspaso).toList();
        log.info("*{}* Confirmando {} traspasos", ID_TIPO_PROGRAMA, idTraspasos.size());

        Map<String, Object> resultado = servicioPort.confirmarTraspaso(idEmpresa, idUsuario, "FMM", idTraspasos);
        log.info("*{}* Fin de Job", ID_TIPO_PROGRAMA);
        return resultado;
    }
}
