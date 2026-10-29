SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden"
AS SELECT 
ord."IdOrden",
CASE WHEN ord."CantidadAsignada" = ord."Cantidad" THEN 'ASC' WHEN ord."CantidadAsignada" > 0 THEN 'ASP' WHEN ord."CantidadAsignada" = 0 THEN 'SAS' END AS "IdEstadoAsignacion"

FROM "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" AS ord;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
