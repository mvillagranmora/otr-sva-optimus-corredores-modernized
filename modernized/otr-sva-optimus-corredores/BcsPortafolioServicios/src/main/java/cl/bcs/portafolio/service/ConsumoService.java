package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.consumo.*;
import cl.bcs.portafolio.port.out.ConsumoPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ConsumoService {

    private final ConsumoPort port;

    public ConsumoService(ConsumoPort port) {
        this.port = port;
    }

    public ConsumoEnLineaResultado consultarConsumoEnLinea(ConsumoEnLineaEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.consultarConsumoEnLinea(entrada);
    }

    public ConsumoLimiteResultado consultarLimite(ConsumoLimiteEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.consultarLimiteConsumo(entrada);
    }
}
