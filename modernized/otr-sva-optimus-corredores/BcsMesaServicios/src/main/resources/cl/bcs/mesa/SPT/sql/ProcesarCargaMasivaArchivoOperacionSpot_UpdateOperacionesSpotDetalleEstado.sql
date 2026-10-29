UPDATE "BSCL.Mesa.BD::CAR.CargaMasivaSpotDetalle" 
SET "DetalleValidacion" = (:DetalleValidacion)
WHERE "IdCargaMasivaDetalle" IN (:IdCargaMasivaDetalle);