SELECT
	MESA."FolioOperacion" 											AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN MESA."IdTipoOperacion" = 'COMPAC' THEN 'CRV '
			WHEN MESA."IdTipoOperacion" = 'VENPAC' THEN 'VRC '
			ELSE '    ' END  										AS "Producto"  
	,'  ' 															AS "Mercado"
	,TO_VARCHAR(MESA."FechaOperacion",'YYYYMMDD')					AS "FechaInicio"
	,TO_VARCHAR(MESA."FechaVencimiento", 'YYYYMMDD') 				AS "FechaVencimiento"
	,MESA."PlazoEnDias" 											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(DNICLI."DocumentoIdentidad",1,LENGTH(DNICLI."DocumentoIdentidad")-2),SUBSTRING(DNICLI."DocumentoIdentidad",LENGTH(DNICLI."DocumentoIdentidad"),1)),10,'0')												AS "RutCliente"
	,'0000000000'													AS "RutEmisor"
	,LPAD(MON."CodigoNumerico",3,'0') 								AS "MonedaEmisor"
	,'999' 															AS "MonedaPacto"
	, 0.0															AS "TasaTransaccion"
	,MESA."Tasa" 													AS "TasaOperacion"
	,CASE WHEN MESA."IdTipoReajuste" = 'CLP' THEN '030'
		ELSE '360' END 												AS "BASTAS"
	,'001' 															AS "Sucursal"
	,MESA."MontoOperacionInicial" 									AS "MontoCompraVenta"
	,CAR."ValorRescate" 											AS "ValorPresente"
	,CAR."INGMontoInteresCompraProximo" 							AS "Interes"
	,CAR."INGMontoReajusteCompraProximo"							AS "Reajuste"
	,0.0 															AS "MontoTransaccion"
	,0.0 															AS "Comision"
	,0.0 															AS "UtilidaPerdidaDiferenciaPrecio"
	,CAR."INGPrecioMercadoActual"									AS "PrecioCompraVenta"
	,0.0 															AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
    ,TO_VARCHAR (TO_date(MESA."FechaOperacion"))					as "FechaOperacion"
	,TO_VARCHAR (TO_date(MESA."FechaVencimiento"))				 	as "FechaVencimientoOperacion"
	,TO_VARCHAR (TO_date(INS."FechaVencimiento"))				 	as "InsFechaVencimiento"
	,TO_VARCHAR (TO_date(INS."FechaEmision"))					 	as "InsFechaEmision"
	,MESA."IdCondicionLiquidacion"									as "IdCondicionDeLiquidacion"
	,TO_VARCHAR (TO_date(MESA."FechaLiquidacion"))					as "FechaDeLiquidacion"
	,MESA."MontoOperacionMoneda"									AS "MontoMoneda" 
	,COALESCE (TasaAnterior."TipoCambioMercado",0)					AS "TasaAnterior"
	,COALESCE (Tasa."TipoCambioMercado",0)							AS "TasaActual"
	,0.0															AS "TasaCompra"
	FROM "BSCL.Mesa.BD::MES.Operacion" 							AS MESA
	LEFT  JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS CAR 		ON MESA."IdOperacion"  	= CAR."IdOperacion" 
	INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" 				AS MON 		ON MON."IdMoneda" 		= MESA."IdTipoReajuste"
	LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" 			AS Tasa 			ON ( Tasa."IdMoneda"  = CASE WHEN MESA."IdMonedaPrincipal" = 'USD' THEN 'DO' WHEN MESA."IdMonedaPrincipal" = 'CLP' THEN 'DO' else MESA."IdMonedaPrincipal" END  AND Tasa."Fecha" = :Fecha)
	LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" 			AS TasaAnterior 	ON ( TasaAnterior ."IdMoneda"  = CASE WHEN MESA."IdMonedaPrincipal" = 'USD' THEN 'DO' else MESA."IdMonedaPrincipal" END  AND TasaAnterior."Fecha" = :FechaAnterior)
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 				AS CLI 		ON MESA."IdCliente" 	= CLI."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 				AS PER 		ON CLI."IdPersona" 		= PER."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 	AS DNICLI 	ON DNICLI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	INNER JOIN  "BSCL.Plataforma.BD::INS.Instrumento" 		AS INS 		ON (INS."IdInstrumento"	=MESA."IdInstrumento")
WHERE 	MESA."IdEmpresa" 					= 	:IdEmpresa
AND MESA."IdTipoOperacion" 					IN	('COMPAC','VENPAC')
AND CAR."IdClasificacionTipoInstrumento" 	IN 	( 'RF' , 'IF' )
AND MESA."IdEstado" 						<> 	'ANU'
AND MESA."FechaOperacion" 					<= 	:Fecha 
AND CAR."IdTipoInstrumento" = 'PAC' 