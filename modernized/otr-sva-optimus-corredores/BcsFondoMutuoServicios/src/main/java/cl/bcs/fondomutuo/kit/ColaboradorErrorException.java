package cl.bcs.fondomutuo.kit;

/**
 * An HTTP collaborator failed in a way that does NOT prove it did nothing (read timeout after the request was sent, HTTP 500/4xx, a body that is not a
 * JSON object). It must NOT become a 503: the router would repeat the whole request in TomEE and a write collaborator might already have committed
 * (R-04, duplicated money). The unit of work rolls back and the controller answers the generic 500 with a correlationId.
 */
public class ColaboradorErrorException extends RuntimeException {

    public ColaboradorErrorException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
