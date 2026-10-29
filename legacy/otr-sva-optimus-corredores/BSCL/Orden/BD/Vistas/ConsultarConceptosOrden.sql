SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden"  
AS
	SELECT
		"Concepto"."IdConcepto",
		"Concepto"."Nombre" AS "NombreConcepto",
		"ConceptoOrden"."IdTipoOperacion",
		"ConceptoOrden"."Uso",
		"ConceptoOrden"."IdBolsa"
	FROM   "BSCL.Orden.BD::ORD.ConceptoOrden"  AS "ConceptoOrden" 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" ON "Concepto"."IdConcepto" = "ConceptoOrden"."IdConcepto" 
	
	WITH READ ONLY;
    
GRANT SELECT 	ON "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
