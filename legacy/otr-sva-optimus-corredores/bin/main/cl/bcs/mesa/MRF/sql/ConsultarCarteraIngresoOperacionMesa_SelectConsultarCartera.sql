SELECT 
	CAR."IdEmpresa" 
	,CAR."IdPortafolio" 
	,CAR."IdCliente" 
	,CAR."IdMesa" 
	,CAR."FolioCartera" 
	,CAR."IdCartera" 
	,CAR."IdTipoInstrumento" 
	,CAR."IdInstrumento" 
	,CAR."Nemotecnico" 
	,CAR."ValorClasificacionRiesgo" 
	,CAR."IdClasificacionTipoInstrumento" 
	,CAR."IdEmisor" 
	,CAR."IdBoveda" 
	,CAR."IdBolsa" 
	,CAR."EsFungible" 
	,COALESCE(CatIFRS."CodigoCarteraIFRS" ,'')  as "CodigoIFRS" 
	,COALESCE(CatIFRS."CarteraIFRS",'')         as "CodigoDescIFRS" 
	,CAR."IdMonedaSistema" 
	,CAR."IdTipoReajuste" 
	,CAR."IdMonedaEmision"         AS "Moneda" 
	,CAR."IdMonedaInstrumento"     AS "MonedaInstrumento" 
	,CAR."INGMontoCompraActualMonedaInstrumento" 
	,CAR."IdMonedaInstrumento" ,CAR."INGMontoATasaCompraActual" 
	,CAR."Cantidad" 
	,CAR."INGCantidad" 
	,CASE WHEN CAR."IdClasificacionTipoInstrumento"='IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad_UI" 
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'N' THEN CAR."CorteMinimo" ELSE INS."CorteMinimo" END AS "CorteMinimo" 
	,CAR."INGTasaCompra" 
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' THEN 0 ELSE CAR."INGValorRescate" END AS "INGValorRescate" 
	,CAR."INGTasaMercadoActual" 
	,CAR."FechaVencimientoInstrumento" 
	,CAR."INGPrecioMercadoActual" 
	,CAR."INGPrecioCompra" AS "INGPrecioCompraActual" 
	,CASE WHEN CAR."TipoCarteraCorredor" = 'CC' THEN CAR."FechaVencimientoOperacion" ELSE CAR."FechaLiquidacion" END AS "FechaLiquidacion" 
	,0.0 as "CantidadSimulacion" 
	,0.0 as "PrecioSimulacion" 
	,0.0 as "TasaSimulacion" 
	,0.0 as "MontoSimulacion" 
	,0.0 as "ValorRescateSimulacion" 
	,CAR."FechaVencimientoInstrumento" as "FechaVencimientoSimulacion" 
	,CASE WHEN CAR."TipoCarteraCorredor" = 'CC' THEN 'S' ELSE 'N' END AS "CarteraPacto" 
	,CAR."ClasificacionTipoInstrumento" 
	,CAR."TipoValorizacion" 
	,CAR."NombreBoveda" 
	,INS."FechaEmision" AS "FechaEmisionInstrumento" 
	,CAR."INGMontoATasaCompraActual" AS "MontoCLP_Org" 
	,CAR."IdUsuario" 
	,CAR."TipoCarteraCorredor" 
FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" INS 
	ON (CAR."IdInstrumento" = INS."IdInstrumento" AND CAR."IdTipoReajuste" = INS."IdTipoReajuste") 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS CatIFRS 
	ON (CAR."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS") 
WHERE CAR."IdEmpresa" = :IdEmpresa
	AND (
		:FiltrarPorFolioCartera = 0
		AND (CAR."CodigoIFRS" = 'T' OR CAR."CodigoIFRS" = '')
		AND CAR."IdPortafolio" = :IdPortafolio
		AND CAR."IdCliente" = :IdCliente
		AND (CAR."IdMesa" = :IdMesa OR :IdMesa =0) 
		AND CAR."FechaVencimientoInstrumento" >= :FechaVencimiento
		AND ( (CAR."TipoCarteraCorredor" <> 'CC' AND CAR."FechaLiquidacion" <=:FechaLiquidacionMenor ) 
			  OR (CAR."TipoCarteraCorredor" = 'CC' AND CAR."FechaVencimientoOperacion" >=:FechaLiquidacionMayor AND :FechaLiquidacionMayor > '0001-01-01' ))
		AND CAR."IdTipoInstrumento" != 'PAC'
		AND (CASE WHEN CAR."EsFungible" = 'S' AND CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END) >= 0
		AND (CAR."IdEmisor" = :IdEmisor OR :IdEmisor ='')
		AND ((CAR."IdTipoReajuste" IN (:IdTipoReajuste)) OR :IdTipoReajuste ='')
		AND (CAR."Nemotecnico" =:Nemotecnico OR :Nemotecnico = '')
		AND (CAR."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento OR :IdClasificacionTipoInstrumento = '')
	) OR (
		:FiltrarPorFolioCartera = 1
         AND CAR."FolioCartera" IN (:FolioCartera)
	)
ORDER BY CAR."IdClasificacionTipoInstrumento" ASC, CAR."INGMontoATasaCompraActual" DESC, CAR."Cantidad" DESC, CAR."INGValorRescate" DESC;