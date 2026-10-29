SELECT
	CLI."IdCategoriaInversionista"
	,PERF."IdFormaCobro"
	,PERF."IdPerfilComercialBase" AS "IdPerfilComercial"
	,PERF."IdPerfilCobros"
	,PERF."IdPerfilProductos"
	,CLI."IdCliente"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" PERF
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = :IdCliente
WHERE PERF."IdCategoriaInversionista" = CLI."IdCategoriaInversionista"
AND "IdFormaCobro" = :IdFormaCobro;