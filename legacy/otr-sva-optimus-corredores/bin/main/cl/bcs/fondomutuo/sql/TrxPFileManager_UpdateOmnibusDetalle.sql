UPDATE "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle"
SET "CantidadCuotasAGF" = :CantidadCuotasAGF,
"MontoAGF" = :MontoAGF,
"ValorCuotaAGF" = :ValorCuotaAGF
WHERE "IdOmnibusDetalle" = :IdOmnibusDetalle