package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.Empresa;

import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Empresa.
 */
public interface EmpresaPort {

    /** Busca empresa por id. */
    Optional<Empresa> buscarPorId(long idEmpresa);

    /** Busca codigo de corredora por empresa y bolsa. */
    Optional<String> buscarCodigoCorredora(long idEmpresa, String idBolsa);
}
