SELECT
	 CAR."IdCartera"
	,CAR."FolioCartera"
    ,CAR."IdOperacion"
    ,CAR."IdTipoOperacion"
	,CAR."PlazoEnDiasOperacion"
	,CAR."FechaOperacion"
	,CAR."MontoMonedaPrincipal"					AS "MontoOperacion"
	,CAR."ValorTipoCambioCierre"				AS "TipoCambioCierre"
	,CAR."ValorParidadCierre"					AS "ParidadCierre"
	,COALESCE(PARIDAD."TipoCambioMercado",0)	AS "TipoCambioMercado"
	,COALESCE(PARIDAD."PrecioSpotCompra",0)		AS "PrecioSpotCompra"
	,COALESCE(PARIDAD."PrecioSpotVenta",0)		AS "PrecioSpotVenta"
	,COALESCE(PARIDAD."PrecioSpotPromedio",0)	AS "PrecioSpotPromedio"
	,COALESCE(PARDO."TipoCambioMercado",0)		AS "TCDolarObservado"
	,COALESCE(PARUF."TipoCambioMercado",0)		AS "TipoCambioMercadoUF"
	,COALESCE(TIPMON."MultiplicaODivide",'M')	AS "MultiplicaODivide"
	,"BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas".NEXTVAL AS "IdRegistro"
	,CAR."IdClasificacionTipoInstrumento"		AS "IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"					AS "IdTipoInstrumento"
	,CAR."IdInstrumento"						AS "IdInstrumento"
	,CAR."Nemotecnico"							AS "Nemotecnico"
	,CAR."IdMonedaPrincipal"					AS "IdMonedaPrincipal"
	,CAR."IdMonedaSecundaria"					AS "IdMonedaSecundaria"
	,CAR."FechaEfectiva"
	,MODCUM."ValorParametro"					AS "ValorParametroModalidadLiquidacion"
	,CAR."FechaVencimientoOperacion"
	,CASE WHEN :OcupaFechaEfectiva = 'S' AND CAR."Nemotecnico" = 'SEINFL' THEN 
		TO_BIGINT(DAYS_BETWEEN(CONFIG_PLA."Valor", CAR."FechaEfectiva"))
	ELSE 
		TO_BIGINT(DAYS_BETWEEN(CONFIG_PLA."Valor", CAR."FechaVencimientoOperacion")) 
	END AS "PlazoResidual"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS MODCUM
	ON (MODCUM."IdParametro" = CAR."IdModalidadCumplimiento" AND
		MODCUM."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		MODCUM."IdEmpresa" = CAR."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PARIDAD
	ON (PARIDAD."IdMoneda" = CAR."IdMonedaPrincipal" AND
		TO_CHAR(PARIDAD."Fecha",'YYYY-MM-DD') = :FechaProceso AND
		PARIDAD."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
	ON (PARDO."IdMoneda" = 'DO' AND PARDO."Fecha" = :FechaProceso)
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUF
	ON (PARUF."IdMoneda" = 'UF' AND PARUF."Fecha" = :FechaProceso)
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	ON (MON."IdMoneda" = CAR."IdMonedaPrincipal")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
	ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONFIG_PLA
	ON (CAR."IdEmpresa" = CONFIG_PLA."IdEmpresa" AND "CodigoParametro" = 'PLA.FechaSistema')
WHERE CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" = 'VIG'
  AND CAR."FechaOperacion" <= :FechaProceso
  AND CAR."IdEmpresa" = :IdEmpresa;
