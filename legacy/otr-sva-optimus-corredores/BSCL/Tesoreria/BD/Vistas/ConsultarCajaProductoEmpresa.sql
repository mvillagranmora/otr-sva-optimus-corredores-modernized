SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaProductoEmpresa";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaProductoEmpresa" AS

SELECT 
	"CajaProductoEmpresa"."IdCajaProductoEmpresa"
	,"CajaProductoEmpresa"."IdEmpresa"
	,"CajaProductoEmpresa"."IdCaja"
	,"Caja"."Codigo" AS "CodigoCaja" 
	,"Caja"."Nombre" AS "NombreCaja" 
	,"CajaProductoEmpresa"."IdProductoEmpresa"
	,"ProductoEmpresa"."IdTipoProducto"
	,"TipoProducto"."Nombre" AS "NombreTipoProducto"
	,"CajaProductoEmpresa"."IdEstado"
	,E."Nombre" AS "NombreCajaProductoEmpresa"
FROM "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" AS "CajaProductoEmpresa"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = "CajaProductoEmpresa"."IdEstado"
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS "Caja" ON "CajaProductoEmpresa"."IdCaja" = "Caja"."IdCaja"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ProductoEmpresa" AS "ProductoEmpresa" ON "CajaProductoEmpresa"."IdProductoEmpresa" = "ProductoEmpresa"."IdProductoEmpresa"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto" ON "TipoProducto"."IdTipoProducto" = "ProductoEmpresa"."IdTipoProducto"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCajaProductoEmpresa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;