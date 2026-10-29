------- C08 CBI AL DIA -------

-- OBTIENE PACTOS
SELECT 
SUBY."IdSubyacente" 																	AS "IdOperacion",					-- * ORIGEN DE PACTOS
SUBY."NemoSVS" 																			AS "NemoSVS",						-- * ORIGEN DE PACTOS
PAC."IdTipoOperacion" 																	AS "IdTipoOperacion",				-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 10(Tipo operación): "V"
SUBY."Nemotecnico" 																		AS "Nemotecnico",					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 2 (nemo) se debe reportar el nemo asociado a la simultanea (ejemplo: COPEC, CMPC, ETC).
SUBY."IdTipoReajuste" 																	AS "IdTipoReajuste",				-- * ORIGEN DE PACTOS
SUBY."INGFechaOperacion" 																AS "FechaOperacion",				-- * ORIGEN DE PACTOS
'1'																						AS "FechaInicio",					-- ORIGEN SIMULTANEA: para calculo de Valor Presente
SUBY."FechaVencimientoInstrumento" 														AS "FechaVencimiento",				-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 5 (Banda): utilizar lógica de pactos Y Campo 6 (fecha vencimiento).
COALESCE ( NULLIF ( SUBY."INGMontoATasaMercadoActual",0),CAR."INGMontoATasaMercadoActual" , -1) AS "IngMontoValorizadoCLP",			-- CAMPO 3 y 7 CUANDO LOS DIAS A VENCIMIENTOS ES MENOR A 90 DIAS
SUBY."INGMontoATasaSubyacenteActual" 													AS "IngMontoValorizado",			-- SOLO EN EL CAMPO 3 CUANDO ES MAYOR A 90 DIAS A VENCIMIENTOS
'1'																						AS "CambioMercado",					-- * ORIGEN DE PACTOS
'1'																						AS "PlazoEnDias",					-- * ORIGEN DE PACTOS
COALESCE (RES."Tasa" , CAR."TasaCompra", 0  )											AS "Tasa",							-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 9(tasa operación): mismo valor que el campo 8
COALESCE (SUBY."INGTasaMercadoActual",PAC."INGTasaMercadoActual", 0 )					AS "TasaMercado",					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 8(tasa mercado): tasa asociada a la operación y Campo 9(tasa operación): mismo valor que el campo 8
'1'																						AS "MonedaPago",					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 4 (tipo moneda): "1"
'1'																						AS "Cantidad"						-- Valor necesario para calcular Valor Presente de simultaneas
 
FROM    "BSCL.Custodia.BD::CUS.Subyacente" SUBY
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS 	 PAC 	ON 	(PAC."FolioCartera" 	= SUBY."FolioCarteraRelacionada"  )
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS   CAR 	ON 	(CAR."IdCartera" 		= SUBY."IdCarteraRetirada" )
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 					AS 	 RES 	ON 	RES."FolioSubyacente"  	= SUBY."FolioSubyacente"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS 	 CAR2 	ON 	(CAR2."IdCartera" 		= RES."IdCarteraCustodia" )
WHERE  PAC."Nemotecnico" 						IN  ( 'PACTO VENTA' , 'PACTO COMPRA' ) 
AND SUBY."IdEstado" 						IN	 ('VIG' )
AND  PAC."IdEstado"  						<> 	'ANU'

UNION ALL 

-- OBTIENE SIMULTANEAS
SELECT
	COALESCE (Car."IdOperacion", 0)   					AS "IdOperacion",					 					-- * ORIGEN DE PACTOS
	Car."NemoSVS"										AS "NemoSVS",						 					-- * ORIGEN DE PACTOS
	COALESCE (Car."IdTipoOperacion", '')				AS "IdTipoOperacion",				 					-- * ORIGEN DE PACTOS
	Sub."Nemotecnico"									AS "Nemotecnico",  					 					-- Campo 2 (nemo): se debe reportar el nemo asociado a la simultanea (ejemplo: COPEC, CMPC, ETC).
	Car."IdMonedaInstrumento" 							AS "IdTipoReajuste",				 					-- * ORIGEN DE PACTOS
	CASE WHEN Car."FechaOperacion" IS NULL 
		THEN '0001-01-01 00:00:00' 
		ELSE CAST(Car."FechaOperacion" AS Timestamp)
	END 												AS "FechaOperacion",				 					-- * ORIGEN DE PACTOS 		
	CASE WHEN Car."FechaComprobante" IS NULL 
		THEN '0001-01-01 00:00:00' 
		ELSE CAST(Car."FechaComprobante" AS Timestamp)
	END 												AS "FechaInicio",										-- SIMULTANEA: para calculo de Valor Presente
	CASE WHEN Car."FechaVencimientoOperacion" IS NULL 
		THEN '0001-01-01 00:00:00' 
		ELSE CAST(Car."FechaVencimientoOperacion" AS Timestamp)
	END 												AS "FechaVencimiento",				 					-- Campo 5 (Banda): utilizar lógica de pactos Y Campo 6 (fecha vencimiento).
	0 													AS "IngMontoValorizadoCLP",								-- DATO NO AFECTA SIMULTANEAS SOLO PACTOS
	COALESCE (Car."PrecioCompra", 0)				 	AS "IngMontoValorizado",								-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 3 y Campo 7 (valor presente): reportar monto actual (a la fecha de la interfaz)
	COALESCE (Cambio."TipoCambioMercado"	, 1)		AS "CambioMercado",					 					-- * ORIGEN DE PACTOS
	COALESCE (op."PlazoEnDias", 0)						AS "PlazoEnDias",					 					-- * ORIGEN DE PACTOS
	COALESCE (Car."INGTasaCompra", 0) 					AS "Tasa",							 					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 9(tasa operación): mismo valor que el campo 8
	COALESCE (Car."INGTasaCompra", 0) 					AS "TasaMercado",					 					-- Campo 8(tasa mercado): tasa asociada a la operación y Campo 9(tasa operación): mismo valor que el campo 8
	Car."IdMonedaInstrumento" 							AS "MonedaPago",					 					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 4 (tipo moneda): "1"
	Car."INGCantidad"									AS "Cantidad"											-- Valor necesario para calcular Valor Presente de simultaneas
FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
  	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = car."FolioCartera"
  	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
  	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = Car."IdTipoOperacion")
  	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpeContraria ON (TipOpeContraria."IdTipoOperacion" = TipOpe."IdTipoOperacionRelacionada")
  	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CP ON (CP."IdPortafolio" = CAR."IdPortafolio")
  	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on MovNormal."IdEmpresa" = Car."IdEmpresa" and MovNormal."IdComprobante" = Car."FolioComprobante" and MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico"  -- dejar
  	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on (MovPlazo."IdEmpresa" = Car."IdEmpresa" and MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico")  -- dejar
  	-- JOINS DE PACTOS
  	LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" op 										ON op."IdOperacion" = Car."IdOperacion"
  	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res 		ON op."IdOperacion" 	= res."IdOperacion"
  	LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio 						ON ( Cambio."IdMoneda"  = Car."IdTipoReajuste" AND Cambio."Fecha"  = :Fecha )
WHERE Car."IdTipoInstrumento" IN ('SIM', 'RRV', 'RRF')
	AND   Car."IdEstado" IN ('VIG','LIQ','ANT','VCD')
	AND   Car."IdCondicionLiquidacion" NOT IN ('T0')
	AND   Car."FechaVencimientoOperacion" >= :Fecha
	AND   (Car."EGRFechaOperacion" > :Fecha or Car."EGRFechaOperacion" = '0001-01-01')
GROUP BY Car."IdOperacion", Car."NemoSVS", Car."IdTipoOperacion", Sub."Nemotecnico", Car."IdMonedaInstrumento", Car."FechaOperacion",Car."FechaComprobante", Car."FechaVencimientoOperacion", Car."PrecioCompra",
Cambio."TipoCambioMercado", op."PlazoEnDias", Car."INGTasaCompra", Car."IdMonedaInstrumento", Car."INGCantidad"
