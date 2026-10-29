package cl.bcs.cartolas.port.out;

import cl.bcs.cartolas.domain.Traza;

/**
 * Puerto de salida para trazas de proceso (CAR.Traza).
 */
public interface TrazaPort {

    void save(Traza traza);
}
