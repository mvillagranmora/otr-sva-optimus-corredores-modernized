package cl.bcs.normativa.service;

import cl.bcs.normativa.domain.Empresa;
import cl.bcs.normativa.port.in.FindEmpresaUseCase;
import cl.bcs.normativa.port.out.EmpresaPort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de empresas.
 * Migrado desde legacy cl.bcs.normativa.dal.EmpresaDAO / CodigoCorredoraDAO.
 */
@Service
public class EmpresaService implements FindEmpresaUseCase {

    private final EmpresaPort empresaPort;

    public EmpresaService(EmpresaPort empresaPort) {
        this.empresaPort = empresaPort;
    }

    @Override
    public Empresa findById(long idEmpresa) {
        return empresaPort.buscarPorId(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada id: " + idEmpresa));
    }

    @Override
    public Optional<String> findCodigoCorredora(long idEmpresa, String idBolsa) {
        Objects.requireNonNull(idBolsa, "idBolsa no puede ser null");
        return empresaPort.buscarCodigoCorredora(idEmpresa, idBolsa);
    }
}
