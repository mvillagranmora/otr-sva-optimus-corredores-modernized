UPDATE "BSCL.Plataforma.BD::PLA.LogValorizadorPauta"
SET "FechaTermino" = CURRENT_TIMESTAMP
WHERE "IdLogValorizadorPauta" = :IdLogValorizadorPauta
AND "FechaTermino" IS NULL