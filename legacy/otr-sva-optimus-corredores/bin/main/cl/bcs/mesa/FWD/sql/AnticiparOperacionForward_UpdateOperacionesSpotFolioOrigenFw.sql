UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot"
SET "FolioOrigenFW" = :FolioCartera
WHERE "IdOperacionSpot" IN (:IdOperacionSpot);
