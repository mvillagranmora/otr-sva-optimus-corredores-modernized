package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.custodio.*;
import cl.bcs.portafolio.port.out.CustodioPreferenciaPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CustodioService {

    private final CustodioPreferenciaPort port;

    public CustodioService(CustodioPreferenciaPort port) {
        this.port = port;
    }

    public CuentaCustodioResultado consultar(CuentaCustodioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.consultarCuentaCustodio(entrada);
    }

    public void actualizar(CuentaCustodioEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        port.actualizarCuentaCustodio(entrada);
    }
}
