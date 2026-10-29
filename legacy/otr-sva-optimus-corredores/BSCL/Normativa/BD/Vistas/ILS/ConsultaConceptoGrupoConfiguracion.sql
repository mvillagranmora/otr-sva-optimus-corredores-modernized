SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoGrupoConfiguracion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoGrupoConfiguracion" AS

SELECT 
	Concepto."IdConcepto",
	ConceptoGrupoConfiguracion."IdConceptoGrupo",
	ConceptoGrupoConfiguracion."Nombre",
	ConceptoGrupoConfiguracion."NombreCorto",
	ConceptoGrupoConfiguracion."Factor"
FROM "BSCL.Normativa.BD::ILS.Concepto" AS Concepto
INNER JOIN "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConceptoGrupoConfiguracion
	    ON ConceptoGrupoConfiguracion."IdConcepto" = Concepto."IdConcepto"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoGrupoConfiguracion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
