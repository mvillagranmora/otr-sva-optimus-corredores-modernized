 SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,'MONTO_VENTA_RFIF' 								AS "IdEvento"
	,'Monto Venta'										AS "Nombre"
	,CASE WHEN ConsultarCartera."IdClasificacionTipoInstrumento" = 'IF' THEN ConsultarCartera."EGRMonto"
	ELSE Reserva."MontoValorizado" 	END AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 						ConsultarCartera
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" Reserva on Reserva."FolioCarteraOrigen" = ConsultarCartera."FolioCartera" AND Reserva."IdEstado" != 'ANU' AND ConsultarCartera."IdClasificacionTipoInstrumento"IN('RF','IF') AND Reserva."IdCarteraOrigen" = ConsultarCartera."IdCartera"
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" ope ON ope."IdOperacion" = RESERVA."IdOperacion" 
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad
	AND ope."IdTipoOperacion"= 'VENTER' 

UNION ALL 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,CASE WHEN ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaCompraActual" ) ) >= 0 THEN 'SPREAD_POSITIVO_TASA_COMPRA_RFIF' ELSE 'SPREAD_NEGATIVO_TASA_COMPRA_RFIF' 		END			AS "IdEvento"
	,CASE WHEN ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaCompraActual" ) ) >= 0 THEN 'Spread Positivo a Tasa Compra RF IF' ELSE 'Spread Negativo a Tasa Compra RF IF' END			AS "Nombre"
	,ABS(ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaCompraActual" ) ) )																											AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 						ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion" 				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad

UNION ALL 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,CASE WHEN ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaMercadoActual" ) )>= 0 THEN 'SPREAD_POSITIVO_TASA_MERCADO_RFIF' ELSE 'SPREAD_NEGATIVO_TASA_MERCADO_RFIF' 		END	AS "IdEvento"
	,CASE WHEN ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaMercadoActual" ) )>= 0 THEN 'Spread Positivo a Tasa Mercado RF IF' ELSE 'Spread Negativo a Tasa Mercado RF IF' 	END AS "Nombre"
	,ABS(ROUND( ConsultarCartera."EGRMonto" - ( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaMercadoActual" ) )) 																									AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 						ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"					= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad

UNION ALL 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,'VALOR_VENTA_RFIF'									AS "IdEvento"
	,'Valor Venta'										AS "Nombre"
	,ConsultarCartera."EGRMontoMonedaSecundaria"		AS "MontoEvento"
	,ConsultarCartera."IdMonedaSecundaria"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 	ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad

UNION ALL 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,'MONTO_VENTA_TASA_COMPRA'							AS "IdEvento"
	,'Monto Venta Tasa Compra'							AS "Nombre"
	,ROUND( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaCompraActual"  )				AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 						ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad

UNION ALL 

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,'MONTO_VENTA_TASA_MERCADO'							AS "IdEvento"
	,'Monto Venta Tasa Mercado' 						AS "Nombre"
	,ROUND( ( CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."EGRValorRescate" ELSE ConsultarCartera."EGRCantidad" END / CASE ConsultarCartera."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ConsultarCartera."INGValorRescate" ELSE ConsultarCartera."INGCantidad" END ) * ConsultarCartera."INGMontoATasaMercadoActual" ) 			AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 						ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad

UNION ALL

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion",
	"IdCartera"											AS "IdMovimiento"
	,'MONTO_FINAL_RF_IF'								AS "IdEvento"
	,'Monto Final RF IF' 								AS "Nombre"
	,Operacion."MontoOperacionFinal" 				    AS "MontoEvento"
	,ConsultarCartera."IdMonedaInstrumento"				AS "CodigoMoneda"
FROM "BSCL.Mesa.BD::MES.Operacion" 						Operacion
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"	ConsultarCartera
	ON Operacion."IdOperacion" = ConsultarCartera."EGRFolioComprobante"
WHERE ConsultarCartera."IdEmpresa" 							= :IdEmpresa
	AND ConsultarCartera."EGRFechaOperacion" 				= :FechaContable 
	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	IN ('RF', 'IF')
	AND ConsultarCartera."EGRIdTipoOperacion"				= 'VENTER'	
	AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad;


