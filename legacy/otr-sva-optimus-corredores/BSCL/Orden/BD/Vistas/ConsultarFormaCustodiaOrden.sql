SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden"
AS SELECT "FormaCustodiaOrden_$0"."IdTipoFormaCustodiaOrden"  as "IdFormaCustodiaOrden"
, "FormaCustodiaOrden_$0"."Nombre" 
, "FormaCustodiaOrden_$0"."IdEstado" 
FROM "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS "FormaCustodiaOrden_$0"
WHERE "IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
