SELECT "IdCargaMasivaDetalle" 
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle"
WHERE "IdCargaMasivaDetalleCabecera" IN (:IdCargaMasivaDetalle) AND "TipoRegistro" = 'D';