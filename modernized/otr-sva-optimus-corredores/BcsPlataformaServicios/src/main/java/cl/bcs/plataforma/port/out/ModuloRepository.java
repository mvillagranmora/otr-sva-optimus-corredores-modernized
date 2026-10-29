package cl.bcs.plataforma.port.out;

import java.util.List;

import cl.bcs.plataforma.cre.ConsultarEstadoModuloService.Modulo;

/**
 * Read port over {@code PAU.Modulo} (the only table {@code ConsultarEstadoModulo} touches). One method, one SELECT, no ORDER BY:
 * the order of the rows is whatever the database returns (PLA-ESM-08 / D-ESM-3).
 */
public interface ModuloRepository {

    /** All the modules of the company (PLA-ESM-03); an empty list when there are none or the id is {@code null} (PLA-ESM-04). */
    List<Modulo> listarPorEmpresa(Long idEmpresa);
}
