package cl.bcs.plataforma.domain;

import java.sql.Timestamp;

/**
 * Web-service transaction for idempotency locking.
 * Source: ProcesarIdTransaccion
 */
public record TransaccionWSData(
    String    idTransaccionWS,
    String    servicio,
    Timestamp fechaInicio
) {}
