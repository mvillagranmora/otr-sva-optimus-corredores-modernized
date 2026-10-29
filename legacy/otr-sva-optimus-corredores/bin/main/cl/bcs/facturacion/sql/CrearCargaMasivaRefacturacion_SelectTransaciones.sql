SELECT "Folio","FechaSistemaCreacion","IdTransaccion" 
FROM "BSCL.Orden.BD::ORD.Transaccion"
WHERE "Folio" IN (:FolioTransaccion)