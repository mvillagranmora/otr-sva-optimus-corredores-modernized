SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionElectronica" AS
SELECT
	"IdDireccionElectronica",
	"Direccion",
	"CodigoProposito",
	COALESCE("PROPMC"."Descripcion",'') AS  "DescripcionProposito",
	"Comentario",
	"IdEnrolamiento",
	CASE WHEN "Predeterminado"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado"
FROM "BSCL.SocioNegocio.BD::ENR.DireccionElectronica"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" as "PROPMC" on "PROPMC"."IdPropositoMedioContacto" = "CodigoProposito";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 