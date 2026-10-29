/********************************************************** 
Proposito	: Script para obtener configuración de movimientos de custodia
autor		: NRM
fecha		: 18/04/2017
***********************************************************/


SELECT 
	CMC."IdTipoOperacion",
	
	CMC."IdTipoOperacionRetornoCartera",  -- Tipo de Operacion que se usara para la cartera devuelta en un vencimiento, anticipo, etc
		
		
	CMC."IdAccionOperacion",
	CMC."UtilizaPlantillaMovimientoOriginal",
	CMC."InvierteClientesPlantillaMovimientoOriginal",
	CMC."BuscaMovimientosGeneradosPorCustodia",
	CMC."PoseeInstrumentoSubyacente",
	CMC."EsGarantia",
	CMC."PermiteGarantiaAutomatica",
	CMC."GeneraCarteraNueva",
	CMC."RetornaCarteraAlPrecioTasa",
	CMC."GrabaClienteRelacionado",
	CMC."EsClienteIngresoClienteRelacionado",
	CMC."PoseeInstrumentoRelacionado",
	CMC."CertificaEn",
	CMC."UtilizaFechaLiquidacionBusqueda",
	CMC."PermiteAnulacion",
	CMC."PermiteAnularOperacionesCertificadas",
	CMC."InstrumentoPorIdTipoOperacion",
	CMC."IdConceptoVentaCorta",
	CMC."IdTipoOperacionVentaCorta",
	CMC."IdTipoPrestamoVentaCorta",
	CMC."IdEstadoOperacion",
	CMC."AgrupaPorIdGrupoOperacionInterno",
	CMC."OrdenEjecucionMovimientos",
	CMC."CliIngGeneraMvtoEnTransitoSinCustodia",
	CMC."CliIngMueveCarteraEnTransitoING",
	CMC."CliIngMueveCarteraING",
	CMC."CliIngMueveSubyacenteING",
	CMC."CliIngMueveGarantiaING",
	CMC."CliIngMueveCarteraEnTransitoEGR",
	CMC."CliIngMueveCarteraEGR",
	CMC."CliIngMueveSubyacenteEGR",
	CMC."CliIngMueveGarantiaEGR",
	CMC."CliIngMueveCarteraEnTransitoAutomaticaING",
	CMC."CliIngMueveCarteraAutomaticaING",
	CMC."CliIngMueveSubyacenteAutomaticaING",
	CMC."CliIngMueveGarantiaAutomaticaING",
	CMC."CliIngMueveCarteraEnTransitoAutomaticaEGR",
	CMC."CliIngMueveCarteraAutomaticaEGR",
	CMC."CliIngMueveSubyacenteAutomaticaEGR",
	CMC."CliIngMueveGarantiaAutomaticaEGR",
	CMC."CliIngIdCondicionLiquidacionTransitoAutoING",
	CMC."CliIngIdCondicionLiquidacionCarteraAutoING",
	CMC."CliIngIdCondicionLiquidacionSubyacenteAutoING",
	CMC."CliIngIdCondicionLiquidacionGarantiaAutoING",
	CMC."CliIngIdCondicionLiquidacionTransitoAutoEGR",
	CMC."CliIngIdCondicionLiquidacionCarteraAutoEGR",
	CMC."CliIngIdCondicionLiquidacionSubyacenteAutoEGR",
	CMC."CliIngIdCondicionLiquidacionGarantiaAutoEGR",
	CMC."CliEgrGeneraMvtoEnTransitoSinCustodia",
	CMC."CliEgrMueveCarteraEnTransitoING",
	CMC."CliEgrMueveCarteraING",
	CMC."CliEgrMueveSubyacenteING",
	CMC."CliEgrMueveGarantiaING",
	CMC."CliEgrMueveCarteraEnTransitoEGR",
	CMC."CliEgrMueveCarteraEGR",
	CMC."CliEgrMueveSubyacenteEGR",
	CMC."CliEgrMueveGarantiaEGR",
	CMC."CliEgrMueveCarteraEnTransitoAutomaticaING",
	CMC."CliEgrMueveCarteraAutomaticaING",
	CMC."CliEgrMueveSubyacenteAutomaticaING",
	CMC."CliEgrMueveGarantiaAutomaticaING",
	CMC."CliEgrMueveCarteraEnTransitoAutomaticaEGR",
	CMC."CliEgrMueveCarteraAutomaticaEGR",
	CMC."CliEgrMueveSubyacenteAutomaticaEGR",
	CMC."CliEgrMueveGarantiaAutomaticaEGR",
	CMC."CliEgrIdCondicionLiquidacionTransitoAutoING",
	CMC."CliEgrIdCondicionLiquidacionCarteraAutoING",
	CMC."CliEgrIdCondicionLiquidacionSubyacenteAutoING",
	CMC."CliEgrIdCondicionLiquidacionGarantiaAutoING",
	CMC."CliEgrIdCondicionLiquidacionTransitoAutoEGR",
	CMC."CliEgrIdCondicionLiquidacionCarteraAutoEGR",
	CMC."CliEgrIdCondicionLiquidacionSubyacenteAutoEGR",
	CMC."CliEgrIdCondicionLiquidacionGarantiaAutoEGR",
	CMC."PermiteRetiroParcialSubyacente",
	CFS."IngresaAGarantiaSimultaneaDeCompra",
	CMC."OperacionMigrada",
	CMC."IdConceptoFusionCartera",
	CMC2."IdTipoOperacion" AS "IdTipoOperacionRelacionada",
	CMC2."IdAccionOperacion" AS "IdAccionOperacionRelacionada"
FROM "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TiPo ON TIPO."IdTipoOperacion" = CMC."IdTipoOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" CMC2 ON CMC2."IdTipoOperacion" = TIPO."IdTipoOperacionRelacionada",
	(
		SELECT
			CAST(COALESCE(MAX("IngresaAGarantiaSimultaneaDeCompra"),'N') AS NVARCHAR(1)) AS "IngresaAGarantiaSimultaneaDeCompra"
		FROM
		(
			SELECT
				CASE WHEN "CodigoParametro"='IngresaAGarantiaSimultaneaDeCompra' THEN "Valor" ELSE NULL END AS "IngresaAGarantiaSimultaneaDeCompra"
			FROM (SELECT "CodigoParametro","Valor"
				  	FROM "BSCL.Custodia.BD::CUS.Configuracion"
				   WHERE "IdEmpresa" = :IdEmpresa
				     AND "CodigoParametro" IN ('IngresaAGarantiaSimultaneaDeCompra')
				 )
		)
	) AS CFS
WHERE CMC."IdEstado" 		= 'ACT'
  --AND CMC."IdTipoOperacion"	= :IdTipoOperacion
  AND CMC."IdEmpresa" 		= :IdEmpresa
ORDER BY CMC."IdTipoOperacion";