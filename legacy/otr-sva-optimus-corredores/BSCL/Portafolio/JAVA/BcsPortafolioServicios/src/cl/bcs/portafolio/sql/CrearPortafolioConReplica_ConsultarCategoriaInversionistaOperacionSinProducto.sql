SELECT
perfilComercial."IdCategoriaInversionista" AS "CategoriaInvComercial",
perfilCliente."IdCategoriaInversionista" AS "CategoriaInvCliente"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" AS perfilComercial
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS perfilCliente
ON perfilCliente."IdCliente" = :IdCliente
WHERE perfilComercial."IdPerfilComercialBase" = :IdPerfilComercialBase
AND perfilComercial."IdEmpresa" = :IdEmpresa
AND perfilComercial."IdEstado" = 'ACT';

