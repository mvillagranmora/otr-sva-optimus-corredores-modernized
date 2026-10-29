UPDATE Cartera
SET "IdEstado" = 'ANU'
FROM "BSCL.Custodia.BD::CUS.Cartera" Cartera
WHERE "IdCartera" = (:IdCartera)
    AND "IdEstado" = 'ANT';    