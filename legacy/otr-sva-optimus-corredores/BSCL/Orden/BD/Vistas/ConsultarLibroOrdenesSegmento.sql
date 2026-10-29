SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesSegmento"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesSegmento" AS 
	SELECT
		"LOSEG"."IdLibroOrdenesSegmento",
		"LOSEG"."IdEmpresa",
		"LOSEG"."IdLibroOrdenes",
		lib."NombreLibroOrdenes",
		lib."IdTipoLibroOrden",
		"LOSEG"."IdSegmentoCliente",
		"TSEG"."NombreSegmentoCliente",
		"TSEG"."DescripcionSegmento",
		"LOSEG"."IdEstado"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenesSegmento" AS "LOSEG"
	INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on lib."IdLibroOrdenes" = "LOSEG"."IdLibroOrdenes" and lib."IdEstado" = 'ACT'
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS "TSEG" ON "LOSEG"."IdSegmentoCliente" = "TSEG"."IdSegmentoCliente"
	--LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "LIB"."IdEstado" = "EST"."IdEstado"
	WHERE "LOSEG"."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesSegmento" TO BCS_DESARROLLADOR WITH GRANT OPTION;
