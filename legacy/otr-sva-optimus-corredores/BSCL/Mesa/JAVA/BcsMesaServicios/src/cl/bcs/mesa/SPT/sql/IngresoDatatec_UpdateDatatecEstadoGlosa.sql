UPDATE "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" 
SET
	 "NumeroIntentos" = :NumeroIntentos
	,"IdEstadoRegistro" = :IdEstadoRegistro
	,"GlosaProceso" = :GlosaProceso
	,"FechaReproceso" = :FechaReproceso
WHERE "IdUnicoDatatec" IN (:IdUnicoDatatec)
  --AND "IdEstadoRegistro" = 'LEI'
;