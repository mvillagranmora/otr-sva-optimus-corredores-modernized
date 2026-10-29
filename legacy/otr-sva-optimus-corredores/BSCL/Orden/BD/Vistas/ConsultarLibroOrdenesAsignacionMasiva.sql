SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesAsignacionMasiva"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesAsignacionMasiva"  
AS SELECT  DISTINCT 
	TRAN."IdLibroOrdenes"
	, LIB."NombreLibroOrdenes"
	, LIB."EstadoLibroOrdenes"
	, LIB."IdEmpresa"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" AS TRAN
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI" AS LIB ON LIB."IdLibroOrdenes" = TRAN."IdLibroOrdenes" 
WHERE "CantidadDisponibleTransaccion" > 0 AND "IdClasificacionTipoInstrumento" = 'RV' AND "IdEstadoTransaccion" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesAsignacionMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;