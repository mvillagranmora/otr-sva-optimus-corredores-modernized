SELECT 
	IFNULL(CAST("IdTipoProducto" AS NVARCHAR(255)), '') AS "IdTipoProducto",
	IFNULL(CAST("IdTipoOperacion" AS NVARCHAR(255)), '') AS "IdTipoOperacion",
	IFNULL(CAST("IdCanalIngreso" AS NVARCHAR(255)), '') AS "IdCanalIngreso",
	IFNULL(CAST("IdMoneda" AS NVARCHAR(255)), '') AS "IdMoneda",
	IFNULL(CAST("IdGrupoActivos" AS NVARCHAR(255)), '') AS "IdGrupoActivos",
	IFNULL(CAST("IdEstado" AS NVARCHAR(255)), '') AS "IdEstado",
	IFNULL(CAST("PermiteRuteo" AS NVARCHAR(255)), '') AS "PermiteRuteo"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
WHERE "IdDetallePerfilProductos" = :IdDetallePerfilProductos
