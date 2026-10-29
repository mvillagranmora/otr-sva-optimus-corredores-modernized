SELECT TOP 1
	"RutaLog"
FROM "BSCL.Plataforma.BD::PLA.LogValorizadorPauta"
WHERE "IdEjecucion" = :IdEjecucion
AND "Proceso" = :CodigoProceso
AND "RutaLog" IS NOT NULL
ORDER BY "FechaInicio" DESC