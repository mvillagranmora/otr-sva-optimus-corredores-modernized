SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS
SELECT
		 CCI."IdConcepto",
		 CCI."TipoConcepto" ,
		 C."Nombre" AS "NombreConcepto",
		 C."NombreConceptoCartola" AS "NombreConceptoCartola",
		 C."ExclusivoCartola" AS "ExclusivoCartola",
		 CCI."DesplegarEnCombobox",
		 CCI."DisponibleIngresoManual",
		 CCI."VisiblePorEjecutivo",
		 CCI."NombreEntidad" ,		 
		 CCI."Origen" ,		 
		 C."IdEstado",
		 CCI."VisibleOptimus" 
  FROM "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" AS CCI 
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS C ON C."IdConcepto" = CCI."IdConcepto" 
  WITH READ ONLY;
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
