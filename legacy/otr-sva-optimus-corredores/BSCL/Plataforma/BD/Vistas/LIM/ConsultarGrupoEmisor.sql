SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisor"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisor" 
AS
	SELECT 
		GE."IdGrupoEmisor",
		GE."IdEmpresa",
		GE."Descripcion",
		--GE."IdEstado",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',GE."IdEstado",GE."IdEstadoAutorizacion") AS "IdEstado",
		EST."Nombre" as "EstadoGrupoEmisor",
		GE."FechaIngreso",
		GE."FechaCreacion",
		GE."FechaModificacion",
		GE."IdEstadoAutorizacion",
		GE."Borrado",
		GE."IdUsuarioCreador",
		(SELECT COUNT(1) 
		 FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle" AS GED
		 WHERE GE."IdGrupoEmisor" = GED."IdGrupoEmisor" AND GED."IdEstado" = 'ACT') AS "CantidadEmisores"
	FROM "BSCL.Plataforma.BD::LIM.GrupoEmisor" GE
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
		ON (EST."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',GE."IdEstado",GE."IdEstadoAutorizacion"))
	WHERE GE."Borrado"='N';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisor" TO BCS_DESARROLLADOR WITH GRANT OPTION;
