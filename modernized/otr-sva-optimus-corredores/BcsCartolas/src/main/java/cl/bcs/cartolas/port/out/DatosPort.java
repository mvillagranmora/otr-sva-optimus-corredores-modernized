package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Datos;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para datos CSV de cartola (CAR.Datos).
 */
public interface DatosPort {

    Optional<Datos> findByIdCartolaAndNombre(long idCartola, String nombre);

    List<Datos> findByIdCartola(long idCartola);

    void save(Datos datos);

    void deleteByIdCartola(long idCartola);
}
