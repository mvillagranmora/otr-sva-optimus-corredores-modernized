SELECT 
"IdSerieFondo"
,"FechaCierre" AS "FechaVigencia"
,"ValorCuota" AS "ValorCuotaVigente"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
WHERE "IdSerieFondo" IN (:IdSerieFondo)
AND "FechaCierre" IN (:FechaCierre)
AND "EsUltimaCargaDiaria" = 'S';