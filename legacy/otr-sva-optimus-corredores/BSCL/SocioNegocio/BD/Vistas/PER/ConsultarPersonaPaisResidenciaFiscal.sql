SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaPaisResidenciaFiscal";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaPaisResidenciaFiscal" AS
SELECT
	"PRF"."IdPaisResidenciaFiscal" AS "IdPaisResidenciaFiscal" ,
	"PRF"."IdPersona" ,
	"PRF"."IdPais" AS "IdPais" ,
	"PAI"."NombrePais",
	"PRF"."IdEstado" AS "IdEstado",
	"PRF"."FechaInicioTributacionPais",
	"PRF"."FechaCreacion" AS "FechaCreacion" ,
	"PRF"."FechaModificacion" AS "FechaModificacion"
FROM "BSCL.SocioNegocio.BD::PER.PaisResidenciaFiscal" AS "PRF"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as "PAI" on "PRF"."IdPais" = "PAI"."IdPais"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "PRF"."IdEstado" = "EST"."IdEstado"
WHERE "PRF"."IdEstado"='ACT';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaPaisResidenciaFiscal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 