SELECT 
	 TBD."CodigoTipoBorrado"
	,TBD."Tabla"
	,TBD."Campo"
	,TBD."DescripcionCampo"
	,TBD."CodigoDato"
	,TBD."ValorOfuscado"
	,TBD."Accion"
	,TBD."NombreLlave"
	,TBD."TipoLlave"
	,TBD."ValorLlave"
	,TBD."OrigenLlave"
	,TBD."IdEstado"
	,TBD."OrdenDelete"
FROM "BSCL.SocioNegocio.BD::SOC.ConfiguracionTipoBorrado" TBD
INNER JOIN "BSCL.SocioNegocio.BD::SOC.TipoBorrado" TB ON TB."CodigoTipoBorrado" = TBD."CodigoTipoBorrado"
WHERE TB."IdEstado" = 'ACT'
AND TBD."IdEstado" = 'ACT';