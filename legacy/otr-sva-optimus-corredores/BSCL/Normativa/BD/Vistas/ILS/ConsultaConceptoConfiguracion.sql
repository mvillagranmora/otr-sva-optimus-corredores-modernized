SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoConfiguracion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoConfiguracion" AS

SELECT 
	ConceptoConfiguracion."IdConceptoGrupo",
	ConceptoConfiguracion."IdConcepto",
	ConceptoConfiguracion."IdItem",
	Item."Nombre" AS "NombreItem",
	ConceptoConfiguracion."Factor",	
	ConceptoConfiguracion."Operacion"	
FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConceptoConfiguracion
INNER JOIN "BSCL.Normativa.BD::ILS.Item" AS Item
	    ON Item."IdItem" = ConceptoConfiguracion."IdItem"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoConfiguracion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
