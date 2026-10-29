package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Persistence port of {@code CambiarEstadoNotificacionContratos} (legacy {@code CambiarEstadoEnvioNotificacionContratoSAAPV}). */
public interface CambiarEstadoNotificacionContratosRepositorio {

    /** One UPDATE per folio (a null folio binds NULL and matches nothing); an empty list updates nothing. */
    void actualizarEstadoEnvio(String idEstadoEnvioNotificacion, List<String> folios);
}
