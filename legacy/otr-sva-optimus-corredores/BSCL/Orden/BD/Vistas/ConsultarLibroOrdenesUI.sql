SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI" AS 
	SELECT
		"LIB"."IdEmpresa",
		"LIB"."IdLibroOrdenes",
		"LIB"."NombreLibroOrdenes",
		"LIB"."IdTipoLibroOrden",
		"LIB"."IdEstado" as "EstadoLibroOrdenes",
		COALESCE("LIB"."LibroPorDefecto",'N') as "LibroPorDefecto",
		limn."IdMarcaNegociacion",
		loo."CodigoOperador",
		"LIB"."IdInstrumento",
		TIP."EsLibroOfertaPublica",
		TIP."IdTipoOperacionOfertaPublica"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LIB"
	INNER JOIN "BSCL.Orden.BD::ORD.TipoLibroOrden" TIP on TIP."IdTipoLibroOrden" = "LIB"."IdTipoLibroOrden"
	LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" limn on limn."IdLibroOrdenes" = "LIB"."IdLibroOrdenes" and limn."IdEstado" = 'ACT'
	LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenesOperador" loo on loo."IdLibroOrdenes" = "LIB"."IdLibroOrdenes" and loo."IdEstado" = 'ACT'
	WHERE "Borrado" = 'N';
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesUI" TO BCS_DESARROLLADOR WITH GRANT OPTION;
