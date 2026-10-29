------- C40 CBI HISTORICA -------

-- OBTIENE PACTOS
SELECT 
	car."IdOperacion"   												AS "IdOperacion",						-- * ORIGEN DE PACTOS  
	carr."NemoSVS"														AS "NemoSVS",							-- * ORIGEN DE PACTOS
	car."IdTipoOperacion"												AS "IdTipoOperacion",					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 10(Tipo operación): "V"
	CASE WHEN res."Nemotecnico" IS NULL THEN '          ' 
		ELSE res."Nemotecnico" 
	END																	AS "Nemotecnico",						-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 2 (nemo) se debe reportar el nemo asociado a la simultanea (ejemplo: COPEC, CMPC, ETC).
	carr."IdTipoReajuste" 												AS "IdTipoReajuste",					-- * ORIGEN DE PACTOS
	car."FechaCreacion" 												AS "FechaOperacion",					-- * ORIGEN DE PACTOS
	CASE WHEN carr."FechaComprobante" IS NULL 
		THEN '0001-01-01 00:00:00' 
		ELSE CAST(carr."FechaComprobante" AS Timestamp)
	END 																AS "FechaInicio",						-- ORIGEN SIMULTANEA: para calculo de Valor Presente
	CASE WHEN carr."FechaVencimientoInstrumento" IS NULL THEN '0001-01-01 00:00:00' 
		ELSE CAST(carr."FechaVencimientoInstrumento" AS Timestamp )
	END 																AS "FechaVencimiento",					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 5 (Banda): utilizar lógica de pactos Y Campo 6 (fecha vencimiento).
	COALESCE (carr."INGMontoATasaMercadoActual", 0) 					AS "IngMontoValorizado",				-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 3 y Campo 7 (valor presente): reportar monto actual (a la fecha de la interfaz)
	COALESCE (Cambio."TipoCambioMercado"	, 1)						AS "CambioMercado",						-- * ORIGEN DE PACTOS
	car."PlazoEnDias"													AS "PlazoEnDias",						-- * ORIGEN DE PACTOS
	res."Tasa" 															AS "Tasa",								-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 9(tasa operación): mismo valor que el campo 8
	COALESCE (carr."INGTasaMercadoActual", 0) 							AS "TasaMercado",						-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 8(tasa mercado): tasa asociada a la operación y Campo 9(tasa operación): mismo valor que el campo 8
	car."IdTipoReajuste" 												AS "MonedaPago",						-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 4 (tipo moneda): "1"
	carr."INGValorRescate"												AS "ValorRescate",						-- * ORIGEN DE PACTOS
	carr."INGCantidad"													AS "Cantidad"							-- * ORIGEN DE PACTOS
FROM "BSCL.Mesa.BD::MES.Operacion" car
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" res 	ON car."IdOperacion" 	= res."IdOperacion"
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" carr 								ON carr."IdOperacion" 	= res."IdReserva" 		AND car."IdTipoOperacion" = carr."IdTipoOperacion"
	LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio 					ON ( Cambio."IdMoneda"  = carr."IdTipoReajuste" AND Cambio."Fecha"  = :Fecha )
WHERE car."IdEstado" 			IN ('VIG','LIQ')
	AND car."IdTipoOperacion" 		NOT IN ('VENFWD')
	AND car."FechaOperacion" 		<= :Fecha
	AND (car."FechaLiquidacion" 	>= :Fecha OR car."FechaLiquidacion" = '0001-01-01')
	AND car."IdTipoOperacion" 		IN ('VENPAC','COMPAC')
	AND carr."IdClasificacionTipoInstrumento" IN ('RF','IF') --Omite Simultaneas
	AND carr."FechaRespaldo" = :Fecha


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
	END 												AS "FechaVencimiento",				 					-- Campo 5 (Banda): utilizar lógica de pactos Y Campo 6 (fecha vencimiento): fecha de vencimiento de la simultanea.
    COALESCE (Car."PrecioCompra", 0)				 	AS "IngMontoValorizado",								-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 3 y Campo 7 (valor presente): reportar monto actual (a la fecha de la interfaz)
	COALESCE (Cambio."TipoCambioMercado"	, 1)		AS "CambioMercado",					 					-- * ORIGEN DE PACTOS
	COALESCE (op."PlazoEnDias", 0)						AS "PlazoEnDias",					 					-- * ORIGEN DE PACTOS
	COALESCE (Car."INGTasaCompra", 0) 					AS "Tasa",							 					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 9(tasa operación): mismo valor que el campo 8
	COALESCE (Car."INGTasaCompra", 0) 					AS "TasaMercado",					 					-- Campo 8(tasa mercado): tasa asociada a la operación y Campo 9(tasa operación): mismo valor que el campo 8 
	Car."IdMonedaInstrumento" 							AS "MonedaPago",					 					-- MISMO DATO PARA PACTOS Y SIMULTANEA: Campo 4 (tipo moneda): "1"     
	Car."INGValorRescate"								AS "ValorRescate",										-- * ORIGEN DE PACTOS
	Car."INGCantidad"									AS "Cantidad"											-- * ORIGEN DE PACTOS
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
GROUP BY Car."IdOperacion", Car."NemoSVS", Car."IdTipoOperacion", Sub."Nemotecnico", Car."IdMonedaInstrumento", Car."FechaOperacion", Car."FechaComprobante", Car."FechaVencimientoOperacion", Car."PrecioCompra", 
Cambio."TipoCambioMercado", op."PlazoEnDias", Car."INGTasaCompra", Car."IdMonedaInstrumento", Car."INGValorRescate", Car."INGCantidad"