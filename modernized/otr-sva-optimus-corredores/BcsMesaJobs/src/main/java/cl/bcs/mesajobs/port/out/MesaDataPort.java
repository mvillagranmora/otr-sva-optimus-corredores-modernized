package cl.bcs.mesajobs.port.out;

import cl.bcs.mesajobs.domain.JobParams;
import cl.bcs.mesajobs.domain.OperacionBloqueada;

import java.util.List;
import java.util.Map;

/**
 * Puerto de acceso a datos HANA para BcsMesaJobs.
 * Un método por tipo de operación de datos.
 */
public interface MesaDataPort {

    /** Ejecuta un SP genérico con los parámetros del job y retorna el ResultSet como filas */
    List<List<String>> ejecutarSpJob(String nombreSp, JobParams params);

    /** Ejecuta un SP y retorna filas con metadatos de columna (para formato archivo) */
    List<Map<String, String>> ejecutarSpConMetadata(String nombreSp, JobParams params);

    /** B-MES-07: Busca IdUsuario por CodigoUsuario (LiberaBloqueoEdicionPactos) */
    long buscarIdUsuarioPorCodigo(long idEmpresa, String codigoUsuario);

    /** LiberaBloqueoEdicionPactos: consulta operaciones bloqueadas */
    List<OperacionBloqueada> buscarOperacionesBloqueadas(long idEmpresa);

    /** LiberaBloqueoEdicionPactos: libera el bloqueo de operaciones */
    int liberarOperaciones(long idEmpresa, List<Long> idOperaciones);
}
