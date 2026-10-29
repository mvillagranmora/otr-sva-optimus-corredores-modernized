package cl.bcs.portafolio.port.out;

import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioEntrada;
import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioResultado;

public interface AutorizacionPort {

    AutorizacionPortafolioResultado autorizarEventoPortafolio(AutorizacionPortafolioEntrada entrada);
    AutorizacionPortafolioResultado rechazarEventoPortafolio(AutorizacionPortafolioEntrada entrada);
}
