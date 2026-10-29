
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionChequeras";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionChequeras" AS

SELECT 
	"ConfChequera"."IdConfiguracionChequera",
	"ConfChequera"."NombreConfChequera",
	"ConfChequera"."AnchoCheque",
	"ConfChequera"."AltoCheque",
	"ConfChequera"."SigloImpreso",
	"ConfChequera"."MontoFila",
	"ConfChequera"."MontoColumna",
	"ConfChequera"."ProvinciaFila",
	"ConfChequera"."ProvinciaColumna",
	"ConfChequera"."MesFila",
	"ConfChequera"."MesColumna",
	"ConfChequera"."AnioFila",
	"ConfChequera"."AnioColumna",
	"ConfChequera"."ExtendidoAFila",
	"ConfChequera"."ExtendidoAColumna",
	"ConfChequera"."PrimeraCantidadFila",
	"ConfChequera"."PrimeraCantidadColumna",
	"ConfChequera"."SegundaCantidadFila",
	"ConfChequera"."SegundaCantidadColumna"
FROM "BSCL.Tesoreria.BD::TES.ConfiguracionChequera"  AS "ConfChequera"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionChequeras" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
