SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarInstrumentoAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarInstrumentoAsignacionMasiva"  
AS SELECT  DISTINCT 
	"IdInstrumentoUI" 
	, "InstrumentoUI"
	, "FechaSistemaCreacion"
	, "IdMonedaInstrumento"
	, "FolioTransaccion"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" 
WHERE "CantidadDisponibleTransaccion" > 0 AND "IdClasificacionTipoInstrumento" = 'RV' AND "IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarInstrumentoAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;