SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"  
AS
SELECT "TipoProducto_$0"."IdTipoProducto" , "TipoProducto_$0"."Codigo" , "TipoProducto_$0"."Nombre" , "TipoProducto_$0"."IdEstado" 
FROM "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto_$0";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 