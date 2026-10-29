SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionExclusionConcepto";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionExclusionConcepto" AS
SELECT 
	ECCI."IdExclusionConcepto",
	ECCI."IdConcepto",
	C."Nombre" AS "NombreConcepto",
	ECCI."FechaVigenciaDesde"
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" AS ECCI
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS C ON C."IdConcepto" = ECCI."IdConcepto"
WITH READ ONLY;

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionExclusionConcepto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;