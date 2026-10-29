SELECT 
	TOP 1 CASE WHEN 	COALESCE(DETALLE$1."IdTipoProducto",'') = '' THEN 
		  	CASE WHEN COALESCE(DETALLE$2."IdTipoProducto",'') = '' THEN 'N'
		 	ELSE 'S' 
		 END
		 	ELSE 'S'
		 END AS "resultado"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORTAFOLIO 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$1 ON (PORTAFOLIO."IdPerfilProductosExtra" = DETALLE$1."IdPerfilProductos" AND DETALLE$1."IdTipoProducto" = :p_IdTipoProducto AND DETALLE$1."IdEmpresa" = :p_IdEmpresa AND DETALLE$1."EstadoProducto" = 'ACT')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$2 ON (PORTAFOLIO."IdPerfilProductos" = DETALLE$2."IdPerfilProductos" AND DETALLE$2."IdTipoProducto" = :p_IdTipoProducto AND DETALLE$2."IdEmpresa" = :p_IdEmpresa AND COALESCE(DETALLE$1."IdTipoProducto",'') = '' AND DETALLE$2."EstadoProducto" = 'ACT')
WHERE PORTAFOLIO."IdPortafolio" = :p_IdPortafolio;
	