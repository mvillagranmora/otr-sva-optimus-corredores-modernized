package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Config;

import java.util.List;

/**
 * Puerto de salida para configuración por empresa (CAR.Config).
 */
public interface ConfigPort {

    List<Config> findByIdEmpresa(long idEmpresa);
}
