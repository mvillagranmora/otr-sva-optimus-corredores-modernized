package cl.bcs.filters.port.out;

import java.util.Optional;

/** Port — service registry and access-control queries. */
public interface ServicioRepository {

    /**
     * Returns 'S' if access to the service is validated per user, 'N' otherwise.
     * Empty means the service ID is not registered.
     */
    Optional<String> getValidaAcceso(String idServicio);

    /**
     * Counts how many access grants the session token holder has for the service.
     * Returns 0 → no access; > 0 → access granted.
     */
    int countAccesoServicio(String token, String idServicio);

    /**
     * Reads a platform configuration value.
     * Key examples: PLA.TipoValidaServicio, PLA.ListaValidaServicio, COR_SESSION_TIMEOUT_MINUTES.
     */
    Optional<String> getConfiguracion(String codigoParametro);

    /**
     * Returns the CodigoUsuario associated with the session token.
     * Throws NoResultException (DataAccessException) if token unknown.
     */
    String getCodigoUsuario(String token);
}
