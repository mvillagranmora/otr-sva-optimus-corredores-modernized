SELECT
perfilComercial."IdCategoriaInversionista" AS "CategoriaInvComercial",
perfilProductos."IdCategoriaInversionista" AS "CategoriaInvProducto",
perfilCliente."IdCategoriaInversionista" AS "CategoriaInvCliente"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" AS perfilComercial
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" AS perfilProductos 
ON perfilProductos."IdPerfilProductos" = :IdPlantillaProductos
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS perfilCliente
ON perfilCliente."IdCliente" = :IdCliente
WHERE perfilComercial."IdPerfilComercialBase" = :IdPerfilComercialBase
AND perfilComercial."IdEmpresa" = :IdEmpresa
AND perfilComercial."IdEstado" = 'ACT';


 