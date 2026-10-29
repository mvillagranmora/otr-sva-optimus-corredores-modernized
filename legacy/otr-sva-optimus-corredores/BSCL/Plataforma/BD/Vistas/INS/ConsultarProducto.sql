SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarProducto"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarProducto" 
AS
SELECT "ProductoEmpresa_$0"."IdProductoEmpresa" ,
"ProductoEmpresa_$0"."IdEstado" , 
"ProductoEmpresa_$0"."IdEmpresa" , 
"ProductoEmpresa_$0"."IdTipoProducto" , 
"TipoProducto_$1"."Codigo" AS "CodigoTipoProducto" , 
"TipoProducto_$1"."Nombre" AS "NombreTipoProducto" 
FROM "BSCL.Plataforma.BD::INS.ProductoEmpresa" AS "ProductoEmpresa_$0" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto_$1" ON  "TipoProducto_$1"."IdTipoProducto" = "ProductoEmpresa_$0"."IdTipoProducto";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarProducto" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
