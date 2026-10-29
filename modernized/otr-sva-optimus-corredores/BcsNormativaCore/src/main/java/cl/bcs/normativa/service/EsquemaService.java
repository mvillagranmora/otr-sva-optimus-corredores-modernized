package cl.bcs.normativa.service;

import cl.bcs.normativa.domain.Campo;
import cl.bcs.normativa.domain.Esquema;
import cl.bcs.normativa.port.in.FindEsquemaUseCase;
import cl.bcs.normativa.port.out.CampoPort;
import cl.bcs.normativa.port.out.EsquemaPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de esquemas normativos.
 * Migrado desde legacy cl.bcs.normativa.dal.EsquemaDAO.
 */
@Service
public class EsquemaService implements FindEsquemaUseCase {

    private final EsquemaPort esquemaPort;
    private final CampoPort campoPort;

    public EsquemaService(EsquemaPort esquemaPort, CampoPort campoPort) {
        this.esquemaPort = esquemaPort;
        this.campoPort = campoPort;
    }

    @Override
    public Esquema findByNombreVersion(String nombre, String version) {
        Objects.requireNonNull(nombre, "nombre no puede ser null");
        Objects.requireNonNull(version, "version no puede ser null");

        Esquema esquema = esquemaPort.buscarPorClave(nombre, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Esquema no encontrado: " + nombre + "-" + version));

        List<Campo> campos = campoPort.buscarPorEsquema(nombre, version);
        esquema.setCampos(campos);
        return esquema;
    }
}
