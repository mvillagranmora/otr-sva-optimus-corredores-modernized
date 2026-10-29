SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarBovedaAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarBovedaAsignacionMasiva"   AS
SELECT  DISTINCT 
	CASE WHEN IFNULL(TRIM(TRAN."IdBoveda"),'') = '' THEN '' ELSE TRAN."IdBoveda" END AS "IdBoveda"
	, CASE WHEN TRIM(IFNULL(BOV."Nombre",'')) = '' THEN 'SIN BOVEDA' ELSE BOV."Nombre" END AS "NombreBoveda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" AS TRAN
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOV ON BOV."IdBoveda" = TRAN."IdBoveda" 
WHERE TRAN."CantidadDisponibleTransaccion" > 0 AND TRAN."IdClasificacionTipoInstrumento" = 'RV' AND TRAN."IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarBovedaAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;