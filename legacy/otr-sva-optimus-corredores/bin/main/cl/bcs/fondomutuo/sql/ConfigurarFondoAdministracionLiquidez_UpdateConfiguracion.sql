UPDATE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"
SET "IdSerieFondo" = :IdSerieFondo,
"MontoMinimo" = :MontoMinimo,
"HoraEjecucion" = :HoraEjecucion
WHERE "IdEmpresa" = :IdEmpresa AND "IdMoneda" = :IdMoneda