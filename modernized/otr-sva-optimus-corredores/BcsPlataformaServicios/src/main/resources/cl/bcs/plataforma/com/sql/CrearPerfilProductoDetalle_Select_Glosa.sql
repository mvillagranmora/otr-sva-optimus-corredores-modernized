SELECT 
	"DescripcionPerfilProductos"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"
WHERE "IdPerfilProductos" = :IdPerfilProductos
	AND "IdTipoProducto" = :IdTipoProducto
