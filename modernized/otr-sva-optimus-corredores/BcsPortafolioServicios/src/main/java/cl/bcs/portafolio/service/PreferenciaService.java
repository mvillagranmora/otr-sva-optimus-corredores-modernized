package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.preferencia.*;
import cl.bcs.portafolio.port.out.CustodioPreferenciaPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PreferenciaService {

    private final CustodioPreferenciaPort port;

    public PreferenciaService(CustodioPreferenciaPort port) {
        this.port = port;
    }

    public PreferenciaPagoResultado crear(PreferenciaPagoEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.crearPreferenciaPago(entrada);
    }

    public void actualizar(PreferenciaPagoEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        port.actualizarPreferenciaPago(entrada);
    }
}
