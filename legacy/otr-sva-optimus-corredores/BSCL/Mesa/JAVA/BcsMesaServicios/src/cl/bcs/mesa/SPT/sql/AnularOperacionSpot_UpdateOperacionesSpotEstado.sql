UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot" 
SET "Estado" = 'ANU'
WHERE "IdOperacionSpot" IN (:IdOperacionSpot);