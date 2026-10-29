package cl.bcs.portalconfirmacion.adapter.in;

import cl.bcs.portalconfirmacion.port.in.ValidateNotificationUseCase;
import cl.bcs.portalconfirmacion.service.NotificationService.ValidateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the notification-confirmation portal.
 *
 * Handles both GET and POST on /validate — preserving the legacy servlet behaviour
 * where both HTTP methods were routed to the same logic.
 */
@RestController
public class ValidateController {

    private final ValidateNotificationUseCase useCase;

    public ValidateController(ValidateNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Validates a notification token and optionally processes a confirmation.
     *
     * @param token       Required. Notification token.
     * @param rut         Optional. RUT of the confirming person.
     * @param accion      Optional. "1" = accept, "2" = reject.
     * @param observacion Optional. Free-text observation.
     * @return HTML or plain-text body with appropriate Content-Type.
     */
    @RequestMapping(
            value  = "/validate",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = {"text/html;charset=UTF-8", "text/plain;charset=UTF-8"}
    )
    public ResponseEntity<String> validate(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String observacion) {

        ValidateResponse result = useCase.processValidation(token, rut, accion, observacion);
        return ResponseEntity.ok()
                .header("Content-Type", result.contentType() + ";charset=UTF-8")
                .body(result.body());
    }
}
