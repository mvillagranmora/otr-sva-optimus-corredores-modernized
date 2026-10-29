SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarProductoPortafolioMesa";
-- 
--  Proposito: Validar Producto Bloqueado de Portafolio de Mesa.
--  Fecha Creacion: 13/10/2017
--
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarProductoPortafolioMesa"(
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_IdModulo			NVARCHAR(3),
	IN p_IdPortafolio		BIGINT,
	IN p_IdTipoOperacion	NVARCHAR(6),
	
	OUT p_out_Salida TABLE ("IdResultado" INT, "Codigo" NVARCHAR(100))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	v_ProductoPortafolio =
		SELECT
			 POR."IdPortafolio"
			,POR."CodigoPortafolio"
			,POR."DescripcionPortafolio"
			,POR."IdCliente"
			,POR."IdPerfilProductos"
			,POR."IdPerfilProductosExtra"
			,CASE
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND 
					 IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND 
					(IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR 
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = :p_IdTipoOperacion OR 
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 1
				ELSE 4 END									AS "IdResultado"
			,CASE
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'				THEN 'PRODUCTO_INACTIVO'
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO' THEN 'PERFIL_PRODUCTO_BLOQUEADO'
				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'				THEN 'PRODUCTO_BLOQUEADO'
				WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',:p_IdTipoOperacion) THEN 'TIPO_OPERACION_SIN_VALOR'
				ELSE 'ERROR_PRODUCTO' END						AS "Codigo"
		FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfExtra
			ON (por."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
				perfExtra."IdTipoProducto" = 'MEME' AND 
				perfExtra."IdEmpresa" = :p_IdEmpresa AND 
				(perfExtra."IdTipoOperacion" = :p_IdTipoOperacion OR perfExtra."IdTipoOperacion" = '0'))
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfBase
			ON (por."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
				perfBase."IdTipoProducto" = 'MEME' AND 
				perfBase."IdEmpresa" = :p_IdEmpresa AND 
				(perfBase."IdTipoOperacion" = :p_IdTipoOperacion OR perfBase."IdTipoOperacion" = '0'))
		WHERE POR."IdPortafolio" = :p_IdPortafolio
		;
	
	
	
	p_out_Salida =
		SELECT "IdResultado", CASE WHEN "IdResultado" = 4 THEN "Codigo" ELSE 'PRODUCTO_OK' END AS "Codigo"
		FROM :v_ProductoPortafolio;
	
END;
