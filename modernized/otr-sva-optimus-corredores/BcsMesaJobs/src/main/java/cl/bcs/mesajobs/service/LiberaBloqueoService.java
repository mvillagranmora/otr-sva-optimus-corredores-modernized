package cl.bcs.mesajobs.service;

import org.springframework.stereotype.Service;

/**
 * B-MES-07: Lógica pura de resolución de usuario para LiberaBloqueoEdicionPactos.
 * Réplica de LiberaBloqueoEdicionPactos.resolverIdUsuarioPorCodigo():guard.
 */
@Service
public class LiberaBloqueoService {

    /**
     * B-MES-07: Determina si el código de usuario requiere resolución en HANA.
     * Legacy: if (codigoUsuario == null || codigoUsuario.trim().isEmpty()) return 0L
     */
    public static boolean esCodigoUsuarioVacio(String codigoUsuario) {
        return codigoUsuario == null || codigoUsuario.trim().isEmpty();
    }

    /**
     * B-MES-07: Retorna 0L para código vacío sin consultar HANA.
     * Para código no vacío, delega a la capa de datos (requiere HANA).
     */
    public static long resolverIdUsuario(String codigoUsuario) {
        if (esCodigoUsuarioVacio(codigoUsuario)) return 0L;
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Plataforma.BD::PLA.Usuario (LiberaBloqueoEdicionPactos_SelectIdUsuarioPorCodigo.sql)");
    }
}
