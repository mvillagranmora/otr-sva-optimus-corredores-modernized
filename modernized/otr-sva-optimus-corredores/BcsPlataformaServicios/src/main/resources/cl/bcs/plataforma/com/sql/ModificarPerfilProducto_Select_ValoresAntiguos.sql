SELECT 
	IFNULL(CAST("DescripcionPerfilProductos" AS NVARCHAR(255)), '') AS "DescripcionPerfilProductos",
	IFNULL(CAST("IdCategoriaInversionista" AS NVARCHAR(255)), '') AS "IdCategoriaInversionista",
	IFNULL(CAST("EsPerfilPersonalizado" AS NVARCHAR(255)), '') AS "EsPerfilPersonalizado",
	IFNULL(CAST("TipoEntidadReferencia" AS NVARCHAR(255)), '') AS "TipoEntidadReferencia",
	IFNULL(CAST("IdReferencia" AS NVARCHAR(255)), '') AS "IdReferencia",
	IFNULL(CAST("IdEstado" AS NVARCHAR(255)), '') AS "IdEstado",
	IFNULL(CAST("PorDefecto" AS NVARCHAR(255)), '') AS "PorDefecto"
FROM "BSCL.Plataforma.BD::COM.PerfilProductos"
WHERE "IdPerfilProductos" = :IdPerfilProductos
