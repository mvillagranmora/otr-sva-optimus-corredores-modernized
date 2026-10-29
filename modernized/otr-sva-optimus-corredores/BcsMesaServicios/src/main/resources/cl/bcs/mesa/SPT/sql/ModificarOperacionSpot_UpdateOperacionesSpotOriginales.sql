UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot" 
SET "Estado" = 'MOD', "IdOperacionSpotVigente" = :IdOperacionSpot
WHERE "IdOperacionSpot" IN (:NumeroOperacion);