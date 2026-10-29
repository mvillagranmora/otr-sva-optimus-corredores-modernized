SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarConceptosContratoComision"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarConceptosContratoComision"  
AS
	SELECT
		"Concepto"."IdConcepto",
		"Concepto"."Nombre" AS "NombreConcepto",
		"ConceptoOrden"."IdTipoOperacion",
		"ConceptoOrden"."Uso",
		"ConceptoOrden"."IdBolsa",
		"TipoInstrumento"."IdClasificacionTipoInstrumento"
	FROM   "BSCL.Orden.BD::ORD.ConceptoOrden"  AS "ConceptoOrden" 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" ON "Concepto"."IdConcepto" = "ConceptoOrden"."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacion" ON "TipoOperacion"."IdTipoOperacion" = "ConceptoOrden"."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON "TipoInstrumento"."IdTipoInstrumento" = "TipoOperacion"."IdTipoInstrumento" 
	WITH READ ONLY;
    
GRANT SELECT 	ON "BSCL.Orden.BD.Vistas::ConsultarConceptosContratoComision" TO  BCS_DESARROLLADOR WITH GRANT OPTION;