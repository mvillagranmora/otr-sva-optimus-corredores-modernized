SELECT
	 DISTINCT(CST."IdCriterioSeleccionTasas") AS "IdCriterioSeleccionTasas"
	,CST."Prioridad"
	--,CST."TipoCartera"
	--,CST."IdUso"
	--,CST."IdTipoCriterioTasa"
	--,CST."IdClasificacionTipoInstrumento"
	--,CST."IdTipoInstrumento"
	--,CST."DiasAntiguedad"
	,CST."CodigoFuente"
	,CST."IdTipoTasa"
	--,CST."IdTipoPrecio"
	--,CST."Interpolar"
	--,CST."IdEmpresa"
FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
INNER JOIN "BSCL.Plataforma.BD::TAS.Fuente" FUE
	ON (CST."CodigoFuente" = FUE."Codigo" AND
		CST."IdEmpresa" = FUE."IdEmpresa" AND
		FUE."NombreEntidad" = 'MANTENEDOR')
WHERE CST."IdEmpresa" = :IdEmpresa
  AND CST."Borrado" = 'N'
  AND CST."TipoCartera" = '0'
  AND CST."IdUso" = 'CARTERA'
  AND CST."IdClasificacionTipoInstrumento" = 'DV'
  AND CST."IdTipoInstrumento" = 'FWD';