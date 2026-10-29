SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion";
 GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" As 
SELECT
		 "Concepto"."IdConcepto",
		 "Concepto"."Nombre" AS "NombreConcepto",
		 "ConceptoFacturacion"."IdTipoOperacion",
		 "ConceptoFacturacion"."Origen",
		 "Concepto"."IdEstado",
		 "ConceptoFacturacion"."EsOtrosConceptos",
		 "ConceptoFacturacion"."ConfiguradoEnModulo",	 
		 "ConceptoFacturacion"."VisibleOptimus",
		 "ConceptoFacturacion"."Contrapartida"

  FROM   "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS "ConceptoFacturacion" 
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" 
    ON "Concepto"."IdConcepto" = "ConceptoFacturacion"."IdConcepto" 
  WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO