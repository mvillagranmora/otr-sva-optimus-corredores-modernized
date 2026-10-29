package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Plantilla;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Puerto de salida para plantillas (CAR.Plantilla + CAR.Recurso).
 */
public interface PlantillaPort {

    Optional<Plantilla> findById(long idPlantilla);

    /**
     * Busca la plantilla vigente más reciente para una empresa y nombre,
     * cuya fecha de vigencia sea anterior o igual a {@code fecha}.
     */
    Optional<Plantilla> findVigentePorNombreYFecha(long idEmpresa, String nombre, LocalDate fecha);
}
