SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia"
AS
  SELECT
		 "CCustodia"."IdConceptoCustodia",
		 "CCustodia"."IdConcepto",
--		 "C"."IdModulo",
		 "C"."Nombre" AS "NombreConcepto",
		 '[' || "C"."IdConcepto" || '] - ' || "C"."Nombre" AS "NombreConceptoFull",
		 "CCustodia"."NombreEntidad",
		 "CCustodia"."IdClasificacionTipoInstrumento",
		 "CCustodia"."IdTipoOperacion",
		 "CCustodia"."ConCertificadoAutomatico",
		 "CCustodia"."GeneraTraspaso",
		 "CCustodia"."EsGarantiaVendible",
		 "CCustodia"."GeneraTransferenciaBoveda",
		 "CCustodia"."EsTransferenciaPortafolio",
		 "CCustodia"."EsTransferenciaCliente",
		 "C"."IdEstado",
		 "CCustodia"."IdContraparte",
		 "CCustodia"."ConfiguradoEnModulo",
		 "CCustodia"."VisibleOptimus",
		 "CCustodia"."VisibleFiori",
		 "CCustodia"."VisibleWS" 
		 
  FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS "CCustodia" 
  INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "C" ON "C"."IdConcepto" = "CCustodia"."IdConcepto" 
  WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO