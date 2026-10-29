SELECT 	 		
	 TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'CANTIDAD'							AS "IdEvento"	
	,'Cantidad'							AS "Nombre"	
	,"CantidadTransaccion"				AS "MontoEvento"
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
			
UNION ALL
	    
SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"		
	,"IdTransaccion" 					AS "IdMovimiento"
	,'MONTO'							AS "IdEvento"	
	,'Monto'							AS "Nombre"	
	,"MontoTransaccion"					AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
	AND "IdMonedaTransaccion" = :MonedaSistema

UNION ALL
	    
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'VALOR'							AS "IdEvento"	
	,'Valor'							AS "Nombre"	
	,"MontoTransaccion"					AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
	AND "IdMonedaTransaccion" <> :MonedaSistema

UNION ALL
	    
SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'PRECIO'							AS "IdEvento"	
	,'Precio'							AS "Nombre"	
	,"PrecioTransaccion"				AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
			
UNION ALL
	    
SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'TASA'								AS "IdEvento"	
	,'Tasa'								AS "Nombre"	
	,"Tasa"								AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'

UNION ALL

SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'CANTIDAD'							AS "IdEvento"	
	,'Cantidad'							AS "Nombre"	
	,"CantidadTransaccion"				AS "MontoEvento"
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
	FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"		 
	WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
			
UNION ALL
	    
SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'MONTO'							AS "IdEvento"	
	,'Monto'							AS "Nombre"	
	,"MontoTransaccion"					AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"	 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'

UNION ALL
	    
 SELECT 	 
	 TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'PRECIO'							AS "IdEvento"	
	,'Precio'							AS "Nombre"	
	,"PrecioTransaccion"				AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"	 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'
			
UNION ALL
	    
SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion"
	,"IdTransaccion" 					AS "IdMovimiento"
	,'TASA'								AS "IdEvento"	
	,'Tasa'								AS "Nombre"	
	,"Tasa"								AS "MontoEvento" 
	,"IdMonedaTransaccion" 				AS "CodigoMoneda"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"	 
WHERE 
		"IdEmpresa" 			= :IdEmpresa
	AND "FechaOperacion" 		= :FechaContable
	AND "IndicadorOrdenDirecta" IN ('OD','OE')
	AND "IdEstadoTransaccion"	= 'ING'	;