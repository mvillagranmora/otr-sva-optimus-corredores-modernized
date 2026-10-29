UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdTransaccion"= :IdTransaccion
  , "IdOrden"= :IdOrden
  , "IdAsignacion"= :IdAsignacion
  , "IdComprobante" = :IdComprobante
  , "IdMovimientoComprobante"= :IdMovimientoComprobante
  , "IdEstadoInstruccion" = :IdEstadoInstruccion
  , "FolioPoliza" = :IdComprobante
WHERE "IdInstruccion" = :IdInstruccion;