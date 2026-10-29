package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Parametro;

import java.util.List;

/**
 * Puerto de salida para parámetros de nómina (CAR.Parametro).
 */
public interface ParametroPort {

    List<Parametro> findByIdNomina(long idNomina);

    /** Guarda un parámetro (insert/update vía SP HANA). Requiere HANA. */
    long save(Parametro parametro, long idEmpresa, long idUsuario);
}
