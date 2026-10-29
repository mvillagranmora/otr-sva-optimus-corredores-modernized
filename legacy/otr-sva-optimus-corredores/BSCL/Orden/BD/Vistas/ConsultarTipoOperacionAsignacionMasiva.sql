SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoOperacionAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoOperacionAsignacionMasiva"  
AS SELECT  DISTINCT 
	"AccionTipoOperacion" 
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" 
WHERE "CantidadDisponibleTransaccion" > 0 AND "IdClasificacionTipoInstrumento" = 'RV' AND "IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoOperacionAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;