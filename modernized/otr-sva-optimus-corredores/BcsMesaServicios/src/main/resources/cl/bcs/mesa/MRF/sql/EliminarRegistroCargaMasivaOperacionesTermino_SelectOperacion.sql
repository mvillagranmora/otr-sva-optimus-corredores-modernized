SELECT "IdCargaMasivaDetalle" 
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle"
WHERE "IdCargaMasivaDetalle" IN (:IdCargaMasivaDetalle) AND "TipoRegistro" = 'C' AND "IdEstado" = 'ING';