SELECT 
perfilProducto."IdPerfilProductos"
FROM "BSCL.Plataforma.BD::COM.PerfilProductos" AS perfilProducto
WHERE perfilProducto."IdEmpresa" = :IdEmpresa 
AND perfilProducto."IdEstado" = 'ACT'
AND perfilProducto."IdCategoriaInversionista" <> '0'
AND perfilProducto."IdPerfilProductos" = :IdPerfilProducto;