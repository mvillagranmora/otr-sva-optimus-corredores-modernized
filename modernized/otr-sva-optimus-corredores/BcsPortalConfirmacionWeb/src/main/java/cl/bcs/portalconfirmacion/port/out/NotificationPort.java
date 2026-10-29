package cl.bcs.portalconfirmacion.port.out;

/**
 * Output port for notification persistence and HANA-backed operations.
 *
 * HANA stored-procedure methods throw UnsupportedOperationException in the H2 adapter.
 * SQL-SELECT / SQL-UPDATE methods are H2-compatible.
 */
public interface NotificationPort {

    /**
     * Calls HANA SP EVT.ValidarTokenNotificacion.
     * Returns the empresa ID associated with the token, or throws if invalid.
     *
     * @throws UnsupportedOperationException when HANA is not available.
     */
    long validateToken(String token);

    /**
     * Retrieves the platform URL for the given empresa from PLA.Configuracion.
     * H2-compatible.
     */
    String getUrlHana(long idEmpresa);

    /**
     * Calls HANA SP EVT.ValidarConfirmarNotificacion, processes the result sets
     * through EvalTemplateService and returns the rendered template string,
     * or null / "-1" when invalid.
     *
     * @throws UnsupportedOperationException when HANA is not available.
     */
    String validateTokenAndId(String token, String rut);

    /**
     * Updates EVT.Notificacion with the accept/reject decision.
     * accionCode must be "ACP" or "RZD".
     * Returns 1 on success, -1 on failure.
     * H2-compatible.
     */
    int acceptOrReject(String token, String observacion, String accionCode);

    /**
     * Fetches the confirmation macro template body from EVT.TagGenerales.
     * H2-compatible.
     */
    String getConfirmationMacro();
}
