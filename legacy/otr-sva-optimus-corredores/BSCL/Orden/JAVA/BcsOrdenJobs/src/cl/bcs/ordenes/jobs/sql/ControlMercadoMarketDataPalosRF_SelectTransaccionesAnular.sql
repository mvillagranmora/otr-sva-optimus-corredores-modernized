SELECT 	tx."IdTransaccion",
		tx."Folio",
		tx."FechaSistemaCreacion"
FROM "BSCL.Orden.BD::ORD.Transaccion" tx
WHERE tx."Folio" || tx."FechaSistemaCreacion" IN (:Llave)
AND tx."IdEstado" <> 'ANU';