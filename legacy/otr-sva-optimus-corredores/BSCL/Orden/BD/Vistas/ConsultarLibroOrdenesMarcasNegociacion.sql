SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesMarcasNegociacion"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesMarcasNegociacion" AS 
	SELECT
		"MN"."IdMarcaNegociacion",
		"MN"."IdEmpresa",
		"MN"."IdLibroOrdenes",
		"MN"."MarcaNegociacion",
		"MN"."IdEstado"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" AS "MN"
--	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "MARNEG"."IdEstado" = "EST"."IdEstado"
	WHERE "MN"."IdEstado" = 'ACT';


GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesMarcasNegociacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;