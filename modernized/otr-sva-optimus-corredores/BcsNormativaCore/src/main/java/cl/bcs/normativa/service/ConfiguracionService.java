package cl.bcs.normativa.service;

import cl.bcs.normativa.port.in.FindConfiguracionUseCase;
import cl.bcs.normativa.port.out.ConfiguracionPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Servicio de configuracion normativa por empresa.
 * Migrado desde legacy cl.bcs.normativa.dal.ConfiguracionDAO.
 */
@Service
public class ConfiguracionService implements FindConfiguracionUseCase {

    private final ConfiguracionPort configuracionPort;

    public ConfiguracionService(ConfiguracionPort configuracionPort) {
        this.configuracionPort = configuracionPort;
    }

    @Override
    public String findValor(long idEmpresa, String codigoParametro) {
        Objects.requireNonNull(codigoParametro, "codigoParametro no puede ser null");
        return configuracionPort.buscarValor(idEmpresa, codigoParametro)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Configuracion no encontrada: empresa=" + idEmpresa + ", codigo=" + codigoParametro));
    }
}
