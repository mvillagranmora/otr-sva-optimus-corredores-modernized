SELECT 
	CAST("DescripcionPerfilProductos" AS NVARCHAR(255)) AS "DescripcionPerfilProductos"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"
WHERE "IdPerfilProductos" = :IdPerfilProductos
