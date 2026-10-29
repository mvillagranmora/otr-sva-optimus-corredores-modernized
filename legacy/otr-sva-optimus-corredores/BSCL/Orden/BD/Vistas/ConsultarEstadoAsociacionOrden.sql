SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsociacionOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsociacionOrden" 
AS SELECT
ord."IdOrden",
CASE WHEN ord."MontoAsociado" = ord."MontoOrden" THEN 'RAC' WHEN ord."MontoAsociado" > 0 THEN 'RAP' WHEN ord."MontoAsociado" = 0 THEN 'RSA' END AS "IdEstadoAsociacion"

FROM "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrdenReporte" AS ord;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoAsociacionOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 