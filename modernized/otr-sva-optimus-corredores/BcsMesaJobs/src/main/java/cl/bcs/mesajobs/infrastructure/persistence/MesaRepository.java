package cl.bcs.mesajobs.infrastructure.persistence;

import cl.bcs.mesajobs.domain.JobParams;
import cl.bcs.mesajobs.domain.OperacionBloqueada;
import cl.bcs.mesajobs.port.out.MesaDataPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Stub — todas las operaciones requieren HANA QA.
 */
@Repository
public class MesaRepository implements MesaDataPort {

    @Override
    public List<List<String>> ejecutarSpJob(String nombreSp, JobParams params) {
        throw new UnsupportedOperationException("requires HANA: " + nombreSp);
    }

    @Override
    public List<Map<String, String>> ejecutarSpConMetadata(String nombreSp, JobParams params) {
        throw new UnsupportedOperationException("requires HANA: " + nombreSp);
    }

    @Override
    public long buscarIdUsuarioPorCodigo(long idEmpresa, String codigoUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Plataforma.BD::PLA.Usuario (LiberaBloqueoEdicionPactos_SelectIdUsuarioPorCodigo.sql)");
    }

    @Override
    public List<OperacionBloqueada> buscarOperacionesBloqueadas(long idEmpresa) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Mesa.BD::MRF.Operacion (LiberaBloqueoEdicionPactos_SelectOperacionesBloqueadas.sql)");
    }

    @Override
    public int liberarOperaciones(long idEmpresa, List<Long> idOperaciones) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Mesa.BD::MRF.Operacion (LiberaBloqueoEdicionPactos_UpdateLiberarOperaciones.sql)");
    }
}
