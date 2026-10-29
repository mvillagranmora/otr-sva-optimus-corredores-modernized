SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes"; 
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes" AS 
	SELECT
		"LIB"."IdLibroOrdenes",
		"LIB"."IdEmpresa",
		"LIB"."NombreLibroOrdenes",
		"LIB"."IdTipoLibroOrden",
		"TLIB"."NombreTipoLibroOrden",
		"LIB"."IdEstado",
		"EST"."Nombre" AS "NombreEstado",
		"LIB"."AsignacionAutomatica",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("LIB"."AsignacionAutomatica") AS "AsignacionAutomaticaTranslate",
		"LIB"."IdTipoAsignacion",
		IFNULL("TASIG"."Nombre",' -') AS "NombreTipoAsignacion",
		"LIB"."Prioridad",
		"LIB"."LibroPorDefecto",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("LIB"."LibroPorDefecto") AS "LibroPorDefectoTranslate",
		"LIB"."IdInstrumento",
		ins."Nemotecnico",
		"IdClienteOfertante",
		cli."NombreCompleto" AS "NombreClienteOfertante",
		"Cantidad",
		"IdPlantillaCobros",
		"FechaApertura",
		"FechaCierre",
		"TLIB"."EsLibroOfertaPublica",
		ope."AccionOrden",
		ope."IdTipoOperacion",
		"LIB"."Precio",
		"LIB"."IdBolsa",
		"LIB"."TipoSubasta"
		
		
	FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LIB"
	LEFT JOIN "BSCL.Orden.BD::ORD.TipoLibroOrden" AS "TLIB" ON "LIB"."IdTipoLibroOrden" = "TLIB"."IdTipoLibroOrden"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "LIB"."IdEstado" = "EST"."IdEstado"
	LEFT JOIN "BSCL.Orden.BD::ORD.TipoAsignacion" AS "TASIG" ON ("LIB"."IdTipoAsignacion" = "TASIG"."IdTipoAsignacion" AND "TASIG"."SoportaAsignacionAutomatica" = 'S')
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = "LIB"."IdInstrumento"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = "LIB"."IdClienteOfertante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" ope on ope."IdTipoOperacion" = "TLIB"."IdTipoOperacionOfertaPublica"
	WHERE "LIB"."Borrado" = 'N';
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes" TO BCS_DESARROLLADOR WITH GRANT OPTION;
