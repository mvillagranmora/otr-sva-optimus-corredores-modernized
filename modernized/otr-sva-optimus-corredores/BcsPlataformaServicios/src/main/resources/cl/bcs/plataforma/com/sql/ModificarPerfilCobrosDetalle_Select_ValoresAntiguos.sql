SELECT 
	DOC."IdValorCobro",
	DOC."IdBolsa",
	DOC."IdTipoProducto",
	DOC."IdTipoOperacion",
	DOC."IdCanalIngreso",
	DOC."IdMoneda",
	CAST(DOC."DiasPlazo" AS NVARCHAR(255)) AS "DiasPlazo",
	CAST(DOC."Prioridad" AS NVARCHAR(255)) AS "Prioridad",
	DOC."IdEstado",
	VC."EsPersonalizado",
	VC."IdCobro",
	CAST(VC."ValorCobro" AS NVARCHAR(255)) AS "ValorCobro",
	CAST(VC."ValorTopeMinimoOrden" AS NVARCHAR(255)) AS "ValorTopeMinimoOrden"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros" DOC
JOIN "BSCL.Plataforma.BD::COM.ValorCobro" VC ON DOC."IdValorCobro" = VC."IdValorCobro"
WHERE DOC."IdDetallePerfilCobros" = :IdDetallePerfilCobros
