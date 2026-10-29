SELECT
"IdSerieFondo",
"ValorCuota"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota"
WHERE "IdSerieFondo" IN (:IdSerieFondo) AND "IdEmpresa" = :IdEmpresa;