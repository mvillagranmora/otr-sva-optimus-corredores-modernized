SET SCHEMA "BSCL";

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarMarcaNegociacionAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarMarcaNegociacionAsignacionMasiva"  
AS SELECT  DISTINCT 
	CASE WHEN TRIM(CT."MarcaNegociacion") = '' THEN 'VACÍO / SIN MARCA' ELSE CT."MarcaNegociacion" END AS "NombreMarcaNegociacion"
	, CASE WHEN CT."MarcaNegociacion" = '' THEN 'UI_VACIO_SIN_MARCA' ELSE CT."MarcaNegociacion" END AS "MarcaNegociacion" 
	, CT."FechaSistemaCreacion"
	, CT."FolioTransaccion"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" AS CT
WHERE CT."CantidadDisponibleTransaccion" > 0 AND CT."IdClasificacionTipoInstrumento" = 'RV' AND CT."IdEstadoTransaccion" = 'ING'
ORDER BY CT."MarcaNegociacion";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarMarcaNegociacionAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;