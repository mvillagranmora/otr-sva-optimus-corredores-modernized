package cl.bcs.normativa.service;

import cl.bcs.normativa.port.in.FindParametroUseCase;
import cl.bcs.normativa.port.out.ParametroPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Servicio de parametros de esquema normativo.
 * Migrado desde legacy cl.bcs.normativa.dal.ParametroDAO.
 */
@Service
public class ParametroService implements FindParametroUseCase {

    private final ParametroPort parametroPort;

    public ParametroService(ParametroPort parametroPort) {
        this.parametroPort = parametroPort;
    }

    @Override
    public String findValor(String nombreEsquema, String versionEsquema, String codigo) {
        Objects.requireNonNull(nombreEsquema, "nombreEsquema no puede ser null");
        Objects.requireNonNull(versionEsquema, "versionEsquema no puede ser null");
        Objects.requireNonNull(codigo, "codigo no puede ser null");
        return parametroPort.buscarValor(nombreEsquema, versionEsquema, codigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parametro no encontrado: " + nombreEsquema + "-" + versionEsquema + "." + codigo));
    }
}
