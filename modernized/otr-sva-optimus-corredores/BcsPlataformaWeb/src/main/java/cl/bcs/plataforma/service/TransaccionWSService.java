package cl.bcs.plataforma.service;

import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Business rules for transaction WS idempotency locking.
 * Pure-logic methods are public static for characterization tests.
 * Source: ProcesarIdTransaccion
 */
@Service
public class TransaccionWSService {

    public static final int    MAX_LENGTH_ID     = 36;
    public static final String ACTION_INSERT      = "INSERT";
    public static final String ACTION_DELETE      = "DELETE";
    public static final int    EXPECTED_PARAMS    = 4;

    /**
     * Validates the raw body params split by "@@@".
     * Source: ProcesarIdTransaccion.performTask — params.length != 4 guard
     */
    public static boolean isValidParams(String[] params) {
        Objects.requireNonNull(params, "params must not be null");
        return params.length == EXPECTED_PARAMS;
    }

    /**
     * Validates that the transaction ID does not exceed the max length.
     * Source: ProcesarIdTransaccion.insert — idTransaccionWS.length() > MAX_LENGTH_ID
     */
    public static boolean isValidIdLength(String idTransaccionWS) {
        Objects.requireNonNull(idTransaccionWS, "idTransaccionWS must not be null");
        return idTransaccionWS.length() <= MAX_LENGTH_ID;
    }

    /**
     * Resolves the action from the first param.
     * Source: ProcesarIdTransaccion.performTask — switch(action)
     */
    public static boolean isValidAction(String action) {
        Objects.requireNonNull(action, "action must not be null");
        return ACTION_INSERT.equals(action) || ACTION_DELETE.equals(action);
    }
}
