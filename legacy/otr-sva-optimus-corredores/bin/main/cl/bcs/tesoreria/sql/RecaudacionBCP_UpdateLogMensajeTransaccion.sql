UPDATE "BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP"
SET "MensajeSalida" = :MensajeSalida     -- Mensaje de respuesta(json) enviado desde Optimus a BCP  
WHERE "IdMensajeTransaccion" = :IdMensajeTransaccion;