SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarConceptosGarantia";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarConceptosGarantia" AS 

SELECT  
		UPPER(CCU."IdConcepto") as "IdConcepto",
		CON."Nombre",
		CCU."ConCertificadoAutomatico",
		CCU."NombreEntidad",
		CON."IdEstado",
		CCU."IdClasificacionTipoInstrumento",
		CCU."IdTipoInstrumento",
		CCU."IdInstrumento",
		CCU."IdContraparte",
		CCU."IdTipoOperacion",
		CCU."ConfiguradoEnModulo",
		CCU."IdEntidadDestinoGarantia",
		CCU."FolioObligatorio",
		CCU."VisibleOptimus",
		CCU."VisibleFiori",
		CCU."VisibleWS",
		CCU."EsGarantiaPrendaria"
FROM  "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS CCU
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON UPPER(CON."IdConcepto") = UPPER(CCU."IdConcepto") 
WHERE UPPER(CCU."NombreEntidad") = UPPER('CUS.Garantia')

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarConceptosGarantia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
