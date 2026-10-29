package cl.bcs.fondomutuo.jobs.port.out;

import cl.bcs.fondomutuo.jobs.domain.ParametrosPrograma;

import java.util.List;

/**
 * Reads job parameters from BSCL.Plataforma.BD::BAT.ParametrosPrograma.
 * Implementation requires HANA connection.
 */
public interface ParametrosProgramaPort {
    List<ParametrosPrograma> consultarParametros(Long jobId);
}
