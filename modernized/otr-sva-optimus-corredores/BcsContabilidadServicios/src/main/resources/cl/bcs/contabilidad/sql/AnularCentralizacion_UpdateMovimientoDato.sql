UPDATE CTB_ItzMovimientoDato
SET "Estado" = 'A'
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_ItzMovimientoDato
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_ItzMovimiento ON 
CTB_ItzMovimientoDato."IdMovimiento" = CTB_ItzMovimiento."IdMovimiento" 
AND CTB_ItzMovimiento."IdCentralizacion" = :IdCentralizacion;