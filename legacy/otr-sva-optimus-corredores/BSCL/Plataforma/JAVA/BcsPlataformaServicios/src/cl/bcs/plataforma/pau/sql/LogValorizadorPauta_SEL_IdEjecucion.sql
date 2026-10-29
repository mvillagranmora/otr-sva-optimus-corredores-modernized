SELECT TOP 1
	"IdEjecucion"
FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
WHERE "IdEmpresa" = :IdEmpresa
AND "FechaSistema" <= :FechaSistema
AND "CodigoProceso" = :CodigoProceso
AND "Estado" = 'PRO'
ORDER BY "FechaInicio" DESC