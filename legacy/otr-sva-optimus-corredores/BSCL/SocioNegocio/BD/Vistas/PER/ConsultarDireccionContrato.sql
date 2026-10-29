SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDireccionContrato";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDireccionContrato" AS
SELECT 
	DIRREL."IdPersonaOrigen"					AS "IdPersona",
	DIRREL."NombreLargo"						AS "DireccionCompleta",
	DIRREL."IdRelacionPersona"					AS "IdRelacion"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS DIRREL
WHERE DIRREL."IdTipoRelacion" IN ('APO','RELEEN','REPAD','REPLEG');

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDireccionContrato" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
