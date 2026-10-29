package cl.bcs.cartolas.jobs.service;

import cl.bcs.cartolas.jobs.port.out.ProgramacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Servicio de procesamiento programado de cartolas.
 *
 * Modernización de: cl.bcs.normativa.cartolas.jobs.BcsScheduledJobCartolas.transaction()
 *                   cl.bcs.normativa.cartolas.procesos.ProcesoProgramacionCartolas
 *
 * B-CAR-07: El proceso programado verifica cada minuto si hay procesos pendientes.
 *
 * NOTA: El legacy usa Thread.sleep(30000) para evitar doble ejecución dentro del mismo
 *       ciclo de un minuto. En Spring Boot se controla con @Scheduled(fixedDelay=30000)
 *       que espera 30s entre el fin de una ejecución y el inicio de la siguiente,
 *       logrando el mismo comportamiento sin bloquear el thread principal.
 */
@Service
public class ProgramacionCartolasService {

    private static final Logger log = LoggerFactory.getLogger(ProgramacionCartolasService.class);

    private final ProgramacionPort programacionPort;

    public ProgramacionCartolasService(ProgramacionPort programacionPort) {
        this.programacionPort = programacionPort;
    }

    /**
     * B-CAR-07: Verifica y ejecuta los procesos programados pendientes.
     *
     * Oracle: BcsScheduledJobCartolas.transaction() →
     *         ProcesoProgramacionCartolas(conn, properties).procesar()
     *
     * @param idEmpresa identificador de la empresa
     */
    public void procesarPendientes(long idEmpresa) {
        Objects.requireNonNull(programacionPort, "programacionPort no puede ser null");
        log.info("INICIO::ProgramacionCartolas idEmpresa={}", idEmpresa);
        programacionPort.procesarPendientes(idEmpresa);
        log.info("FIN::ProgramacionCartolas idEmpresa={}", idEmpresa);
    }
}
