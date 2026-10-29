SET SCHEMA "BSCL"; 
GO
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor" 
AS
	SELECT 
		TLIM."IdLimiteEmisor",
		TLIM."IdEmisor",
		TLIM."IdLimite",
		--TLIM."IdEstado",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',TLIM."IdEstado",TLIM."IdEstadoAutorizacion") AS "IdEstado",
		EST."Nombre" as "NombreEstadoEmisor",
		TLIM."FechaIngreso",
		TLIM."FechaCreacion",
		TLIM."FechaModificacion",
		TLIM."IdEstadoAutorizacion",
		TLIM."Borrado",
		TLIM."IdUsuarioCreador",
		--EMI."Nombre",
		EMI."IdEmisor" AS "Nombre",
		--EMI."RazonSocial"
		EMI."IdEmisor" AS "RazonSocial"
	FROM "BSCL.Plataforma.BD::LIM.LimiteEmisor" TLIM
	INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI 
		ON ( TLIM."IdEmisor"= EMI."IdEmisor")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
		ON (EST."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',TLIM."IdEstado",TLIM."IdEstadoAutorizacion"))
	WHERE TLIM."Borrado"='N';
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor" TO BCS_DESARROLLADOR WITH GRANT OPTION;
