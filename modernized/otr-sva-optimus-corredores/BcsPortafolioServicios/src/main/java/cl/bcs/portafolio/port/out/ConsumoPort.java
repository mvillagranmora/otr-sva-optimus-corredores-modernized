package cl.bcs.portafolio.port.out;

import cl.bcs.portafolio.domain.consumo.ConsumoEnLineaEntrada;
import cl.bcs.portafolio.domain.consumo.ConsumoEnLineaResultado;
import cl.bcs.portafolio.domain.consumo.ConsumoLimiteEntrada;
import cl.bcs.portafolio.domain.consumo.ConsumoLimiteResultado;

public interface ConsumoPort {

    ConsumoEnLineaResultado consultarConsumoEnLinea(ConsumoEnLineaEntrada entrada);
    ConsumoLimiteResultado consultarLimiteConsumo(ConsumoLimiteEntrada entrada);
}
