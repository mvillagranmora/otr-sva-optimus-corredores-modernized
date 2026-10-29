SELECT
CASE WHEN CAR."IdClasificacionTipoInstrumento"='IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END AS "CantidadDisponible",
REG."IdRegistro",
CAR."IdPortafolio"
FROM "BSCL.Custodia.BD::GBL.ValidaCantidadRetiroCustodia_RegistrosAValidar" REG
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR 
ON (CAR."IdCliente" = REG."IdCliente" 
AND CAR."IdPortafolio" = REG."IdPortafolio" 
AND CAR."IdClasificacionTipoInstrumento" = REG."IdClasificacionTipoInstrumento"
AND CAR."IdInstrumento" =  REG."IdInstrumento"
AND CAR."FechaOperacion" <= :FechaSistema 
AND CAR."IdEstado" = 'VIG'
AND CAR."IdBoveda" = REG."IdBoveda"
) WHERE CAR."IdPortafolio" IS NOT NULL
