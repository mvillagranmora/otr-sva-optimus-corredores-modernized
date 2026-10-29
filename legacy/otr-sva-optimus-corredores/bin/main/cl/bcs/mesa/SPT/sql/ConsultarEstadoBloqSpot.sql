SELECT "IdOperacionSpot" ,"EstadoConfirmacion"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" 
WHERE "EstadoConfirmacion" = 'PEN'
AND "IdOperacionSpot" IN (:NumeroOperacion) FOR UPDATE; 