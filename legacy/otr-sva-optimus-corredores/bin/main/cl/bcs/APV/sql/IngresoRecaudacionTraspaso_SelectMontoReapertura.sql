SELECT 
SUM("MontoPeso") AS "MontoReapertura"
FROM "BSCL.APV.BD::TRA.DetalleTraspaso"
WHERE "IdTraspaso" IN (:IdTraspaso) AND "EsReapertura" = 'S' AND "IdEstado" = 'VIG' AND "FechaIngreso" = (:FechaIngreso);