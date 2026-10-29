SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoPrecioOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoPrecioOrden"  
AS SELECT "TipoPrecioOrden_$0"."IdTipoPrecioOrden" 
, "TipoPrecioOrden_$0"."Codigo" 
, "TipoPrecioOrden_$0"."Nombre" 
, "TipoPrecioOrden_$0"."IdEstado" 
FROM "BSCL.Orden.BD::ORD.TipoPrecioOrden" AS "TipoPrecioOrden_$0";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoPrecioOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
