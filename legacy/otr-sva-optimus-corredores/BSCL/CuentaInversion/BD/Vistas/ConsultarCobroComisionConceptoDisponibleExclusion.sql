SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionConceptoDisponibleExclusion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionConceptoDisponibleExclusion" AS 
SELECT CCCI."IdConcepto", CCCI."NombreConcepto" 
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS CCCI
WHERE NOT EXISTS (
	SELECT 1 FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" AS CCEC WHERE CCCI."IdConcepto" = CCEC."IdConcepto" 
)
WITH READ ONLY;

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCobroComisionConceptoDisponibleExclusion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;