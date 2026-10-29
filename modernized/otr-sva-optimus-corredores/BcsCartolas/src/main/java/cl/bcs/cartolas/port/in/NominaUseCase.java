package cl.bcs.cartolas.port.in;

import cl.bcs.cartolas.domain.Nomina;
import cl.bcs.cartolas.domain.Parametro;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso relacionados con Nóminas de cartolas.
 */
public interface NominaUseCase {

    Optional<Nomina> getNomina(long idNomina);

    List<Nomina> getNominasProgramadas();

    List<Parametro> getParametros(long idNomina);
}
