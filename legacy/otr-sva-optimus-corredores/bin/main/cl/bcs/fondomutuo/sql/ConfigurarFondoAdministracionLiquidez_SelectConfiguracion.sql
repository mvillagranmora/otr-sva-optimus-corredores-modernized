SELECT
"IdMoneda"
,"IdSerieFondo"
,"MontoMinimo"
,"IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"
WHERE "IdMoneda" IN (:IdMoneda) AND "IdEmpresa" = :IdEmpresa