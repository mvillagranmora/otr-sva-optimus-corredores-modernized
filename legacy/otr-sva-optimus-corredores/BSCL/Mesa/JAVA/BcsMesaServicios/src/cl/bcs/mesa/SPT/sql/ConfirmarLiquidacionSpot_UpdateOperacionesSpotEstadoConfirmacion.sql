UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot" 
SET "EstadoConfirmacion" = 'CNF'
WHERE "IdOperacionSpotOrigen" IN (:IdOperacionSpot);