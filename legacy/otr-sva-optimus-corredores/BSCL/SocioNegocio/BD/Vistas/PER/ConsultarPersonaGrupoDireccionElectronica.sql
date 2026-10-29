SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronica" AS 

SELECT
	"GDIREL"."IdGrupoDireccion" AS "IdGrupoDireccion" ,
	"GDIREL"."Grupo" AS "Grupo" ,
	"GDIREL"."IdPersona" ,
	"GDIREL"."Comentario",
	--"GDIREL"."IdEstado" AS "IdEstado" ,
	"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('GRUPODIRECCIONELECTRONICA',"GDIREL"."IdEstado","GDIREL"."IdEstadoAutorizacion") AS "IdEstado",
	"EST"."Nombre" as "NombreEstado",
	"GDIREL"."IdEstadoAutorizacion",
	"GDIREL"."FechaIngreso" AS "FechaIngreso" ,
	"GDIREL"."FechaCreacion" AS "FechaCreacion" ,
	"GDIREL"."FechaModificacion" AS "FechaModificacion", 
	"GDIREL"."IdUsuarioCreador" AS "IdUsuarioCreador"
FROM 
	"BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS "GDIREL"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('GRUPODIRECCIONELECTRONICA',"GDIREL"."IdEstado","GDIREL"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'GRUPODIRECCIONELECTRONICA'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "ESTSOC"."IdEstado" = "EST"."IdEstado"
	WHERE "GDIREL"."Borrado"='N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
