SELECT "IdTransaccion","Folio","FechaSistemaCreacion"
FROM "BSCL.Orden.BD::ORD.Transaccion" tx
where "Folio" || "FechaSistemaCreacion" IN (:Llave)
AND "IdEstado" <> 'ANU';