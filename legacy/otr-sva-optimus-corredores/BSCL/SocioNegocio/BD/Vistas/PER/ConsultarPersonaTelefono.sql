SET SCHEMA "BSCL";

---BSCL.SocioNegocio.BD::PER.Servicios.ConsultarTelefono
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS
SELECT
	"TEL"."IdTelefono" AS "IdTelefono" ,
	"TEL"."IdPersona" ,
	"TEL"."IdPais" AS "IdPais" ,
	"TEL"."IdTipoTelefono" AS "IdTipoTelefono" ,
	"TIPTEL"."Nombre" AS "NombreTipoTelefono" ,
	"TEL"."CodigoProposito" AS "CodigoProposito" ,
	COALESCE("PROPMC"."Descripcion",'') AS  "DescripcionProposito" ,
	"TEL"."Comentario" AS "Comentario" ,
	"PAI"."NombrePais",
	"TEL"."Numero" AS "Numero" ,
	"TEL"."Anexo" AS "Anexo" ,
	--"TEL"."IdEstado" AS "IdEstado" ,
	"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('TELEFONO',"TEL"."IdEstado","TEL"."IdEstadoAutorizacion") AS "IdEstado",
	"EST"."Nombre" as "NombreEstado",
	"TEL"."IdEstadoAutorizacion",
	"TEL"."FechaIngreso" AS "FechaIngreso" ,
	"TEL"."FechaCreacion" AS "FechaCreacion" ,
	"TEL"."FechaModificacion" AS "FechaModificacion",
	CASE WHEN "TEL"."Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS "TEL"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAI" on "TEL"."IdPais" = "PAI"."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoTelefono" AS "TIPTEL" on "TIPTEL"."IdTipoTelefono" = "TEL"."IdTipoTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" as "PROPMC" on "PROPMC"."IdPropositoMedioContacto" = "TEL"."CodigoProposito"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('TELEFONO',"TEL"."IdEstado","TEL"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'TELEFONO'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "ESTSOC"."IdEstado" = "EST"."IdEstado"
WHERE "TEL"."Borrado"='N';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 