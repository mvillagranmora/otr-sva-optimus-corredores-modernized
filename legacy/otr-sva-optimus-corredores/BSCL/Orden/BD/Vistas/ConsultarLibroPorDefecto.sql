SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroPorDefecto"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroPorDefecto" AS 
	SELECT
		"LIB"."IdLibroOrdenes",
		"LIB"."IdEmpresa"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LIB"
	WHERE "LIB"."LibroPorDefecto" = 'S';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroPorDefecto" TO BCS_DESARROLLADOR WITH GRANT OPTION;
