package cl.bcs.portalconfirmacion.port.in;

import cl.bcs.portalconfirmacion.service.NotificationService.ValidateResponse;

/**
 * Input port for notification validation and confirmation.
 */
public interface ValidateNotificationUseCase {

    /**
     * Processes a validate request, routing through:
     * <ul>
     *   <li>No rut  → returns the confirmation-entry HTML page (fetched from BCS platform).</li>
     *   <li>Rut, no accion → returns the rendered notification template with confirmation controls.</li>
     *   <li>Rut + accion  → records accept/reject decision; returns "1" on success.</li>
     * </ul>
     *
     * @param token       Required. Token identifying the notification.
     * @param rut         Optional. RUT of the person being asked to confirm.
     * @param accion      Optional. "1" = accept, "2" = reject.
     * @param observacion Optional. Free-text observation for the confirmation.
     * @return ValidateResponse containing the body string and content-type.
     * @throws NullPointerException     when token is null.
     * @throws IllegalArgumentException when token is invalid (no empresa found) or accion invalid.
     */
    ValidateResponse processValidation(String token, String rut, String accion, String observacion);
}
