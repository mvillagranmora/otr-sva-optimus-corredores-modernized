SELECT
"IdSerieFondo"
,"IdMoneda"
,"FechaCierre" AS "FechaVigencia"
,"ValorCuota"
,"Observacion"
,"FechaCarga" AS "FechaCalculo"
,"IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" WHERE "IdSerieFondo" IN (:IdSerieFondo);