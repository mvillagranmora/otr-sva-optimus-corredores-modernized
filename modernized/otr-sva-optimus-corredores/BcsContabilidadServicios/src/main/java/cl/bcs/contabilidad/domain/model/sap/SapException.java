package cl.bcs.contabilidad.domain.model.sap;

import java.util.Objects;

/**
 * A Service Layer answer that ends the processing of a pre-voucher (exhausted retries, rejected voucher, failed
 * partner check). Legacy threw {@code new Exception(respuesta.toString())}; the message keeps that text.
 */
public class SapException extends RuntimeException {

    private final transient SapRespuesta respuesta;

    public SapException(SapRespuesta respuesta) {
        super(Objects.requireNonNull(respuesta, "respuesta").toString());
        this.respuesta = respuesta;
    }

    public SapRespuesta respuesta() {
        return respuesta;
    }
}
