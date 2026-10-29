UPDATE "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
SET 
	"FechaInicio" = COALESCE(:FechaInicio,"FechaInicio"),
	"FechaFin" = COALESCE(:FechaFin,"FechaFin"),
	"Estado" = COALESCE(:Estado,"Estado"),
	"Mensaje" = COALESCE(:Mensaje,"Mensaje")
WHERE "FechaSistema" = :FechaSistema AND "CodigoProceso" = :CodigoProceso AND "IdEmpresa" = :IdEmpresa
