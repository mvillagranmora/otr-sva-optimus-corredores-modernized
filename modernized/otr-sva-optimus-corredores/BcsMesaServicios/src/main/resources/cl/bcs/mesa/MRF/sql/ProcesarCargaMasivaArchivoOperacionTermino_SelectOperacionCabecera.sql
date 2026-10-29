SELECT
"IdCargaMasivaDetalle"
,"IdPortafolio"
,"TipoOperacion"
,"TipoReajuste"
,"ValorInicial"
,"CondicionLiquidacion"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle"
WHERE "IdCargaMasivaDetalle" IN (:IdCargaMasivaDetalle) AND "TipoRegistro" = 'C' AND "IdEstado" = 'ING';