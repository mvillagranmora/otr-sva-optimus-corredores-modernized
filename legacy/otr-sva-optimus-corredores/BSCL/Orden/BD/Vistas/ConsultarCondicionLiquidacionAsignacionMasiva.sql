SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCondicionLiquidacionAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCondicionLiquidacionAsignacionMasiva"  
AS SELECT  DISTINCT 
	"IdCondicionDeLiquidacion"
	, "NombreCondicionDeLiquidacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" 
WHERE "CantidadDisponibleTransaccion" > 0 AND "IdClasificacionTipoInstrumento" = 'RV' AND "IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCondicionLiquidacionAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;