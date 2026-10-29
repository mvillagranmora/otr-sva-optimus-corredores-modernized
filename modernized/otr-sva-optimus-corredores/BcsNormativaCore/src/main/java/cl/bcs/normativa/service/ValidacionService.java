package cl.bcs.normativa.service;

import cl.bcs.normativa.domain.Validacion;
import cl.bcs.normativa.port.in.FindValidacionUseCase;
import cl.bcs.normativa.port.out.ValidacionPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de validaciones normativas.
 * Migrado desde legacy cl.bcs.normativa.dal.ValidacionDAO.
 */
@Service
public class ValidacionService implements FindValidacionUseCase {

    private final ValidacionPort validacionPort;

    public ValidacionService(ValidacionPort validacionPort) {
        this.validacionPort = validacionPort;
    }

    @Override
    public List<Validacion> findActivasPorEsquema(String nombreEsquema, String versionEsquema) {
        Objects.requireNonNull(nombreEsquema, "nombreEsquema no puede ser null");
        Objects.requireNonNull(versionEsquema, "versionEsquema no puede ser null");
        return validacionPort.buscarActivasPorEsquema(nombreEsquema, versionEsquema);
    }
}
