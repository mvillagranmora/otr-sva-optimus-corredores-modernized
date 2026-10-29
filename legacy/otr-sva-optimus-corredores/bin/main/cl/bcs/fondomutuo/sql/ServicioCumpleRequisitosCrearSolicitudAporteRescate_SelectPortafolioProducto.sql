
SELECT 
TOP 1 CASE WHEN COALESCE(DETALLE$1."IdTipoProducto",'')  = '' THEN 
 	  CASE WHEN COALESCE(DETALLE$2."IdTipoProducto",'') = '' 
 		THEN 'N'
 	  ELSE 
 	  	CASE WHEN COALESCE(DETALLE$2."EstadoProducto",'') = 'ACT'
 	  		THEN 'S'
 	  	ELSE 
	 	  	'N'
	 	END 	 	
 	  END
 ELSE
 	 CASE WHEN COALESCE(DETALLE$1."EstadoProducto",'') = 'ACT'
 	 	THEN 'S'
 	 ELSE 
 	 	'N'
 	 END
 END as "ValidaProducto"
FROM "BSCL.FondoMutuo.BD::GBL.ServicioCumpleRequisitosCrearSolicitudAporteRescate_Portafolios" PORTAFOLIOS_PASO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORTAFOLIO on (PORTAFOLIO."IdPortafolio" = PORTAFOLIOS_PASO."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$1 ON (PORTAFOLIO."IdPerfilProductosExtra" = DETALLE$1."IdPerfilProductos" AND DETALLE$1."IdTipoProducto" IN (:p_IdTipoProducto) AND DETALLE$1."IdEmpresa" = :p_IdEmpresa)
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DETALLE$2 ON (PORTAFOLIO."IdPerfilProductos" = DETALLE$2."IdPerfilProductos" AND DETALLE$2."IdTipoProducto" IN (:p_IdTipoProducto) AND DETALLE$2."IdEmpresa" = :p_IdEmpresa AND COALESCE(DETALLE$1."IdTipoProducto",'') = '');