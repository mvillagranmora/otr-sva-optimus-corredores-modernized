SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoCustodiaOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoCustodiaOrden"
AS SELECT "TipoCustodiaOrden_$0"."IdTipoCustodiaOrden" 
, "TipoCustodiaOrden_$0"."Nombre" 
, "TipoCustodiaOrden_$0"."IdEstado" 
FROM "BSCL.Orden.BD::ORD.TipoCustodiaOrden" AS "TipoCustodiaOrden_$0"
WHERE "IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoCustodiaOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
