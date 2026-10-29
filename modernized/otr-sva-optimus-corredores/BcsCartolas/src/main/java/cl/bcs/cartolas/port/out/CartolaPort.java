package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Cartola;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de Cartola.
 */
public interface CartolaPort {

    Optional<Cartola> findById(long idCartola);

    List<Cartola> findByIdNomina(long idNomina, boolean soloReproceso);

    /** Guarda (insert/update vía SP HANA). Requiere HANA. */
    long save(Cartola cartola, long idEmpresa, long idUsuario);
}
