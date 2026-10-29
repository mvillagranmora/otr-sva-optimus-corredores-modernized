SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTasaReportanteParaReportado";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTasaReportanteParaReportado" AS
SELECT
	ORDEN_ASOCIADA."IdOrdenN" AS "IdOrden",
	MIN(REPORTANTE."TasaReporte") AS "TasaReportante"
FROM "BSCL.Orden.BD::ORD.OrdenAsociada" ORDEN_ASOCIADA
INNER JOIN "BSCL.Orden.BD::ORD.Orden" REPORTADO ON (REPORTADO."IdOrden" = ORDEN_ASOCIADA."IdOrdenN")
INNER JOIN "BSCL.Orden.BD::ORD.Orden" REPORTANTE ON (REPORTANTE."IdOrden" = ORDEN_ASOCIADA."IdOrdenM")
GROUP BY "IdOrdenN";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTasaReportanteParaReportado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
