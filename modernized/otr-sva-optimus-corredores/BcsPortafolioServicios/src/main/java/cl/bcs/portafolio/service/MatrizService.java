package cl.bcs.portafolio.service;

import cl.bcs.portafolio.domain.matriz.*;
import cl.bcs.portafolio.port.out.PortafolioCRUDPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MatrizService {

    private final PortafolioCRUDPort port;

    public MatrizService(PortafolioCRUDPort port) {
        this.port = port;
    }

    public List<MatrizAgrupadorResultado> consultarMatriz(MatrizAgrupadorEntrada entrada) {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(entrada.idPortafolio(), "idPortafolio");
        return port.consultarMatrizAgrupador(entrada);
    }
}
