SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarBolsaAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarBolsaAsignacionMasiva"  
AS SELECT  DISTINCT 
	"IdBolsa"
	,"NombreBolsa" 
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" 
WHERE "CantidadDisponibleTransaccion" > 0 AND "IdClasificacionTipoInstrumento" = 'RV' AND "IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarBolsaAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;