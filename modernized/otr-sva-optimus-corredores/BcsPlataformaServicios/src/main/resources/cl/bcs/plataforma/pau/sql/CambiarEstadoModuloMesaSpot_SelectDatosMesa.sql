SELECT
	"IdMesa",
	"Nombre" AS "NombreMesa",
	'A' AS "IdEstadoMesa"
FROM "BSCL.Mesa.BD::MES.Mesa"
WHERE "IdTipoProducto" = :IdTipoProducto;