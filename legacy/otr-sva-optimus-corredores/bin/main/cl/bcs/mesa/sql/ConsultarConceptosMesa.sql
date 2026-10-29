SELECT
     "Concepto"."IdConcepto",
     --"Concepto"."Tipo" AS "TipoConcepto",
     "Concepto"."Nombre" AS "NombreConcepto",
     "ConceptoMesa"."IdTipoProducto",
     "ConceptoMesa"."IdTipoOperacion",
     "ConceptoMesa"."NombreEntidad" AS "Uso",
     "ConceptoRel"."IdConcepto" AS "IdConceptoRelacionado",
     "ConceptoRel"."Nombre" AS "NombreConceptoRelacionado"
FROM   "BSCL.Mesa.BD::MES.ConceptoMesa"  AS "ConceptoMesa" 
    LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" ON "Concepto"."IdConcepto" = "ConceptoMesa"."IdConcepto" 
    LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "ConceptoRel" ON "ConceptoRel"."IdConcepto" = "ConceptoMesa"."IdConceptoRelacionado"
WHERE "IdTipoProducto" = :IdTipoProducto;