SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle" 
AS
	SELECT 
		GED."IdGrupoEmisorDetalle",
		GED."IdGrupoEmisor",
		GED."IdEmisor",
		--GED."IdEstado",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',GED."IdEstado",GED."IdEstadoAutorizacion") AS "IdEstado",
		EST."Nombre" as "EstadoEmisor",
		GED."FechaIngreso",
		GED."FechaCreacion",
		GED."FechaModificacion",
		GED."IdEstadoAutorizacion",
		GED."Borrado",
		GED."IdUsuarioCreador",
		--EMI."Nombre",
		EMI."IdEmisor" AS "Nombre",
		--EMI."RazonSocial"
		EMI."IdEmisor" AS "RazonSocial"
	FROM "BSCL.Plataforma.BD::LIM.GrupoEmisorDetalle" GED
	INNER JOIN "BSCL.Plataforma.BD::LIM.GrupoEmisor" GRU ON (GRU."IdGrupoEmisor"=GED."IdGrupoEmisor" AND GRU."Borrado"='N')
	INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON (GED."IdEmisor"= EMI."IdEmisor")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
		ON (EST."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',GED."IdEstado",GED."IdEstadoAutorizacion"))
	WHERE GED."Borrado"='N'
	AND GRU."IdGrupoEmisor" <> 'DEFAULT'
	
	UNION ALL 
		SELECT 
		0 AS "IdGrupoEmisorDetalle",
		GRU."IdGrupoEmisor",
		EMI."IdEmisor",
		EMI."IdEstado",
		EST."Nombre" AS "EstadoEmisor",
		GRU."FechaIngreso",
		GRU."FechaCreacion",
		GRU."FechaModificacion",
		GRU."IdEstadoAutorizacion",
		GRU."Borrado",
		GRU."IdUsuarioCreador",
		--EMI."Nombre",
		EMI."IdEmisor" AS "Nombre",
		--EMI."RazonSocial"
		EMI."IdEmisor" AS "RazonSocial"
	FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI
	LEFT JOIN "BSCL.Plataforma.BD::LIM.GrupoEmisor" GRU ON (GRU."Borrado"='N' AND GRU."IdGrupoEmisor"='DEFAULT' AND EMI."IdEstado"='ACT')
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = EMI."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;
