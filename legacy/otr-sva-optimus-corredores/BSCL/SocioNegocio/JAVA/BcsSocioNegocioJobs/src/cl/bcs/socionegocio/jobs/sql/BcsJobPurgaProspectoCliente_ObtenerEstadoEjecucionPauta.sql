SELECT 
	COALESCE("Estado", 'PEN') AS "EstadoPauta"
FROM "BSCL.Plataforma.BD::PAU.Ejecucion"
WHERE "FechaSistema" = :FechaEjecucion AND "IdEmpresa" = :IdEmpresa;