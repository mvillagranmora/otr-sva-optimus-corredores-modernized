
UPDATE Subyacente
SET "IdEstado" = 'ANU'
FROM "BSCL.Custodia.BD::CUS.Subyacente" Subyacente
WHERE "IdSubyacente" = (:IdSubyacente)
    AND "IdEstado" = 'ANT';