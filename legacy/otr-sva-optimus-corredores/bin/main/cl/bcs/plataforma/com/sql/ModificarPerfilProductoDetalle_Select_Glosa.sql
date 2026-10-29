SELECT 
	CAST("DescripcionPerfilProductos" AS NVARCHAR(255)) AS "DescripcionPerfilProductos"
FROM "BSCL.Plataforma.BD::COM.PerfilProductos"
WHERE "IdPerfilProductos" = :IdPerfilProductos
