SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS 

SELECT
	"DIRELE"."IdDireccionElectronica" AS "IdDireccionElectronica" ,
	"DIRELE"."Direccion" AS "Direccion" ,
	"DIRELE"."IdPersona" ,
	"DIRELE"."CodigoProposito",
	COALESCE("PROPMC"."Descripcion",'') AS  "DescripcionProposito" , 
	"DIRELE"."Comentario",
	--"DIRELE"."IdEstado" AS "IdEstado" ,
	"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DIRECCIONELECTRONICA',"DIRELE"."IdEstado","DIRELE"."IdEstadoAutorizacion") AS "IdEstado",
	"EST"."Nombre" as "NombreEstado",
	"DIRELE"."IdEstadoAutorizacion",
	"DIRELE"."FechaIngreso" AS "FechaIngreso" ,
	"DIRELE"."FechaCreacion" AS "FechaCreacion" ,
	"DIRELE"."FechaModificacion" AS "FechaModificacion",
	CASE WHEN "DIRELE"."Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado"
FROM 
	"BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS "DIRELE"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" as "PROPMC" on "PROPMC"."IdPropositoMedioContacto" = "DIRELE"."CodigoProposito"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DIRECCIONELECTRONICA',"DIRELE"."IdEstado","DIRELE"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'DIRECCIONELECTRONICA'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "ESTSOC"."IdEstado" = "EST"."IdEstado"
	WHERE "DIRELE"."Borrado"='N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
