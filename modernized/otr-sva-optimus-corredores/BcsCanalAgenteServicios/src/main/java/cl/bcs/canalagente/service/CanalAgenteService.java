package cl.bcs.canalagente.service;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;
import cl.bcs.canalagente.port.in.ConsultarHistorialMovimientosUseCase;
import cl.bcs.canalagente.port.out.CanalAgenteDataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Service for BcsCanalAgenteServicios.
 *
 * Implements ConsultarHistorialMovimientosUseCase with the same two-step
 * logic as the legacy ConsultarHistorialMovimientosCuentaInversion:
 * 1. Look up IdCliente from IdPortafolio.
 * 2. If found, delegate to the full HANA workflow via CanalAgenteDataPort.
 *    If not found, return a "Cliente no encontrado" error response.
 *
 * clienteNoEncontrado() and buildErrorResponse() are public static methods
 * so they can be tested without a Spring context.
 */
@Service
public class CanalAgenteService implements ConsultarHistorialMovimientosUseCase {

    private static final Logger log = LoggerFactory.getLogger(CanalAgenteService.class);

    private final CanalAgenteDataPort dataPort;

    public CanalAgenteService(CanalAgenteDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @Override
    public HistorialMovimientosResponse consultar(HistorialMovimientosRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.idPortafolio(), "idPortafolio must not be null");
        Objects.requireNonNull(request.idEmpresa(), "idEmpresa must not be null");
        Objects.requireNonNull(request.idMoneda(), "idMoneda must not be null");

        log.info("INICIO ConsultarHistorialMovimientosCuentaInversion idPortafolio={}", request.idPortafolio());
        long t0 = System.currentTimeMillis();

        Optional<Long> idCliente = dataPort.findIdClienteByPortafolio(request.idPortafolio());
        if (idCliente.isEmpty()) {
            log.warn("Cliente no encontrado para idPortafolio={}", request.idPortafolio());
            return HistorialMovimientosResponse.clienteNoEncontrado();
        }

        HistorialMovimientosResponse response =
                dataPort.consultarHistorialCompleto(request, idCliente.get());

        log.info("FIN ConsultarHistorialMovimientosCuentaInversion tiempo={}ms",
                System.currentTimeMillis() - t0);
        return response;
    }

    /**
     * Pure-static factory: builds an error response for the "client not found" case.
     * Kept public static for direct unit-testing without Spring.
     */
    public static HistorialMovimientosResponse buildClienteNoEncontradoResponse() {
        return HistorialMovimientosResponse.clienteNoEncontrado();
    }
}
