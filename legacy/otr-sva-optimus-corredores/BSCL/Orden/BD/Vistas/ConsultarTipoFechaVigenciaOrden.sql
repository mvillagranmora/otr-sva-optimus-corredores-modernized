SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoFechaVigenciaOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoFechaVigenciaOrden"  
AS SELECT "TipoVigenciaOrden_$0"."IdTipoVigenciaOrden" 
, "TipoVigenciaOrden_$0"."Codigo" 
, "TipoVigenciaOrden_$0"."Nombre" 
, "TipoVigenciaOrden_$0"."IdEstado" 
FROM "BSCL.Orden.BD::ORD.TipoVigenciaOrden" AS "TipoVigenciaOrden_$0";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoFechaVigenciaOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
