SELECT "FechaCierre" AS "Valor1","IdSerieFondo" AS "Valor2" 
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
WHERE "FechaCierre" = :p_FechaSistema 
AND "ValorCuota" = 0;