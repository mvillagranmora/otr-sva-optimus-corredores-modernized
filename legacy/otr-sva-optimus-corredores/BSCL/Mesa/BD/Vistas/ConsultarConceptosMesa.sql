SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarConceptosMesa"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarConceptosMesa"  
AS
	SELECT
		 "Concepto"."IdConcepto",
		 --"Concepto"."Tipo" AS "TipoConcepto",
		 "Concepto"."Nombre" AS "NombreConcepto",
		 "ConceptoMesa"."IdTipoProducto",
		 "ConceptoMesa"."IdTipoOperacion",
		 "ConceptoMesa"."NombreEntidad",
		 "ConceptoRel"."IdConcepto" AS "IdConceptoRelacionado",
		 "ConceptoRel"."Nombre" AS "NombreConceptoRelacionado"
	FROM   "BSCL.Mesa.BD::MES.ConceptoMesa"  AS "ConceptoMesa" 
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" ON "Concepto"."IdConcepto" = "ConceptoMesa"."IdConcepto" 
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "ConceptoRel" ON "ConceptoRel"."IdConcepto" = "ConceptoMesa"."IdConceptoRelacionado" 
	
	WITH READ ONLY;
    
GRANT SELECT 	ON "BSCL.Mesa.BD.Vistas::ConsultarConceptosMesa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
