SELECT
	 "IdUnicoDatatec"
	,"NumeroRegistro"
	,"IdOperacionSpot"
	,"IdDatatec"
	,"IdEstadoRegistro"
	,:Origen||' - '||"GlosaProceso" AS "GlosaProceso"
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec"
WHERE "IdDatatec" IN (:IdDatatec)
  AND "IdEmpresa" = :IdEmpresa
  AND "IdEstadoRegistro" IN ('ING','ERR','PEN','ANU');
