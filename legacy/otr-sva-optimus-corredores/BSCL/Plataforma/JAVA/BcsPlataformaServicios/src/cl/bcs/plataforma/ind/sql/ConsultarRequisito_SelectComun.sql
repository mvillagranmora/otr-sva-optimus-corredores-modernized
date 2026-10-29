SELECT
	IND."IdIndicador",
	IND."IdTipoIndicador",
	IND."NombreTipoIndicador",
	IND."ClaseDato",
	IND."CodigoTipoIndicador",
	IND."RelacionDocumento",
	IND."ValorIndicador",
	IND."IdEstadoAutorizacion",
	IND."GrupoIndicador",
	IND."IdUso",
	IND."CantidadDecimales",
	IND."LongitudMinima",
	IND."LongitudMaxima",
	IND."IdTipoEntidad",
	IND."IdEntidad",
	IND."ModeloCliente",
	IND."IdTipoPersona",
	IND."IdTipoDocumento",
	IND."IdClase"
FROM
	#ConsultarRequisito_Indicadores IND 
WHERE
	NOT EXISTS
	(
		SELECT
			1
		FROM
			#ConsultarRequisito_IndicadoresTipoCliente TCLI
		WHERE
			IND."CodigoTipoIndicador" = TCLI."CodigoTipoIndicador"
		AND IND."GrupoIndicador" = TCLI."GrupoIndicador"
	) 
UNION ALL
SELECT
	ITC."IdIndicador",
	ITC."IdTipoIndicador",
	ITC."NombreTipoIndicador",
	ITC."ClaseDato",
	ITC."CodigoTipoIndicador",
	ITC."RelacionDocumento",
	ITC."ValorIndicador",
	ITC."IdEstadoAutorizacion",
	ITC."GrupoIndicador",
	ITC."IdUso",
	ITC."CantidadDecimales",
	ITC."LongitudMinima",
	ITC."LongitudMaxima",
	ITC."IdTipoEntidad",
	ITC."IdEntidad",
	ITC."ModeloCliente",
	ITC."IdTipoPersona",
	ITC."IdTipoDocumento",
	ITC."IdClase"
FROM
	#ConsultarRequisito_IndicadoresTipoCliente ITC;