package cl.bcs.normativa.port.in;

import cl.bcs.normativa.domain.Empresa;

import java.util.Optional;

/**
 * Caso de uso: obtener datos de empresa.
 */
public interface FindEmpresaUseCase {

    /**
     * Busca empresa por id.
     * @throws IllegalArgumentException si no existe.
     */
    Empresa findById(long idEmpresa);

    /**
     * Busca codigo de corredora de la empresa para una bolsa.
     */
    Optional<String> findCodigoCorredora(long idEmpresa, String idBolsa);
}
