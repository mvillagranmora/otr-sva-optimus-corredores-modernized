package cl.bcs.cartolas.port.in;

import cl.bcs.cartolas.domain.Cartola;
import cl.bcs.cartolas.domain.Config;
import cl.bcs.cartolas.domain.Datos;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Casos de uso del dominio de cartolas.
 */
public interface CartolasUseCase {

    Optional<Cartola> getCartola(long idCartola);

    List<Cartola> getCartolasByNomina(long idNomina, boolean soloReproceso);

    List<Config> getConfig(long idEmpresa);

    List<Datos> getDatos(long idCartola);

    /**
     * Resolución de nombre de archivo para una cartola.
     * Implementado como pure-logic en el servicio.
     */
    String resolverNombreArchivoCartola(String patron, Map<String, String> contexto);

    /**
     * Resolución de nombre de archivo para una nómina.
     */
    String resolverNombreArchivoNomina(String patron, Map<String, String> contexto);

    /**
     * Resolución de nombre de archivo ZIP.
     */
    String resolverNombreArchivoZip(String patron, Map<String, String> contexto);
}
