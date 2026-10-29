SELECT 
	C."IdCobro",
	C."NombreCobro",
	--C."FormaCobro",
	IFNULL(C."ValorCobro",0) AS "ValorCobro"--, --defaults
	--C."ValorTopeMinimoOrden", --defaults
	--C."UnidadDeCobro",
	--C."PermiteCambiarValor",
	--CC."IdCobroClasificacion",
	--CC."CodigoCobroClasificacion",
	--CC."DescripcionCobroClasificacion",
	--C."IdEmpresa",
	--C."EsRecalculable",
	--C."PermiteAsignarCorredorCorresponsal",
	--C."PermiteAsignarPortafolio",
	--C."IdMoneda"
FROM 
"BSCL.Plataforma.BD::COM.Cobro" AS C
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CC ON (CC."IdCobroClasificacion" = C."IdCobroClasificacion")
WHERE C."IdCobro" = :IdCobro;