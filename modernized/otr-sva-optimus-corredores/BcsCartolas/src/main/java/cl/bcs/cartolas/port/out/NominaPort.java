package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Nomina;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de Nómina.
 */
public interface NominaPort {

    Optional<Nomina> findById(long idNomina);

    List<Nomina> findByTipoProceso(String tipoProceso);

    Optional<Nomina> findRecurrente(long idEmpresa, String periodo);

    /** Guarda (insert/update vía SP HANA). Requiere HANA. */
    long save(Nomina nomina, long idEmpresa, long idUsuario);
}
