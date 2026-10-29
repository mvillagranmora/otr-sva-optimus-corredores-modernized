UPDATE "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle"
SET "ValorCuota" = :ValorCuota,
"UltimoValorCuota" = :UltimoValorCuota,
"DiferenciaValorCuota" = :DiferenciaValorCuota,
"DiferenciaValorCuotaAGF" = :DiferenciaValorCuotaAGF,
"IdEstado" = :IdEstado
WHERE "IdOmnibusDetalle" = :IdOmnibusDetalle