SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria" AS
 
SELECT
	 CT."IdConcepto"
	,CON."Nombre" AS "NombreConcepto"
	,CON."IdEstado"	
	,CT."NombreEntidad"	
	,CT."Tipo" AS "TipoConceptoTesoreria"  -- IN/EG	
    ,CT."Origen"	
    ,CT."MultipleConcepto"
    ,CT."DesplegarEnComboBox"
    ,CT."DisponibleIngresoManual"
    ,CT."VisiblePorEjecutivo"
    ,CT."VisibleOptimus"
    ,CT."VisibleFiori"
    ,CT."VisibleWS"
    ,CT."GeneraComprobanteAutomaticamente"
FROM "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" AS CT
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (CON."IdConcepto" = CT."IdConcepto")

ORDER BY CON."Nombre" ASC
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
