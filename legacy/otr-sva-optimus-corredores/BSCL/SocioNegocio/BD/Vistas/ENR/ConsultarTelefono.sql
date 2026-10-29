SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTelefono";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTelefono" AS
SELECT
	"TEL"."IdTelefono" AS "IdTelefono" ,
	"TEL"."IdEnrolamiento" ,
	"TEL"."IdPais" AS "IdPais" ,
	"TEL"."IdTipoTelefono" AS "IdTipoTelefono" ,
	"TIPTEL"."Nombre" AS "NombreTipoTelefono" ,
	"TEL"."CodigoProposito" AS "CodigoProposito" ,
	COALESCE("PROPMC"."Descripcion",'') AS  "DescripcionProposito" ,
	"TEL"."Comentario" AS "Comentario" ,
	"PAI"."NombrePais",
	"TEL"."Numero" AS "Numero" ,
	"TEL"."Anexo" AS "Anexo" ,
	CASE WHEN "TEL"."Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado"
FROM "BSCL.SocioNegocio.BD::ENR.Telefono" AS "TEL"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAI" on "TEL"."IdPais" = "PAI"."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoTelefono" AS "TIPTEL" on "TIPTEL"."IdTipoTelefono" = "TEL"."IdTipoTelefono"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" as "PROPMC" on "PROPMC"."IdPropositoMedioContacto" = "TEL"."CodigoProposito";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTelefono" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 