SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarPorcentajeVisibilidadOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarPorcentajeVisibilidadOrden"
AS  SELECT "PorcentajeVisibilidad_$0"."IdPorcentajeVisibilidad" 
, "PorcentajeVisibilidad_$0"."Codigo" 
, "PorcentajeVisibilidad_$0"."Nombre" 
, "PorcentajeVisibilidad_$0"."IdEstado" 
FROM "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" AS "PorcentajeVisibilidad_$0";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarPorcentajeVisibilidadOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
