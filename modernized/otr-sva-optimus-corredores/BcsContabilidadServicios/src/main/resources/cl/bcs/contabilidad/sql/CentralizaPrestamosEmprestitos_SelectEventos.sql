	SELECT	DISTINCT    
			 TO_BIGINT(:IdTipoCentralizacion)                            													AS "IdTipoCentralizacion"	    
	     	,"IdCartera"																									AS "IdMovimiento"
	     	,'MONTO_PRESTAMO'																								AS "IdEvento" 	
	     	,'Monto Prestamo'																								AS "Nombre"
 			,ConsultarCartera."MontoCompra"																					AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																			AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" 		= :IdEmpresa 
			AND ("FechaVencimientoOperacion" 	> :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('EMP','PRT')
	        AND "IdEstado" = 'VIG'
--	        AND "DocumentoIdentidad"	= :DocumentoIdentidad
		UNION
		SELECT DISTINCT
			 TO_BIGINT(:IdTipoCentralizacion)                            													AS "IdTipoCentralizacion"		
	     	,"IdCartera"																									AS "IdMovimiento"
	     	,'INTERES_PREMIO'																								AS "IdEvento" 
	     	,'Interés Premio'																								AS "Nombre"
 			,ROUND( DAYS_BETWEEN(:FechaAnterior, CASE WHEN ConsultarCartera."FechaVencimientoOperacion" <= :FechaContable 	
													THEN ConsultarCartera."FechaVencimientoOperacion"
													ELSE :FechaContable							
										END
							   ) * (((ConsultarCartera."TasaCompra"/30)/100)* ConsultarCartera."MontoCompra"), 0 )			AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																			AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" > :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('EMP')
	        AND "IdEstado" = 'VIG'
--	        AND "DocumentoIdentidad"	= :DocumentoIdentidad
		UNION
	    SELECT DISTINCT
	    	TO_BIGINT(:IdTipoCentralizacion)                            													AS "IdTipoCentralizacion"
	     	,"IdCartera"																									AS "IdMovimiento"
	     	,'INTERES_PRIMA'																								AS "IdEvento" 
	     	,'Interés Prima'																								AS "Nombre"
 			,ROUND( DAYS_BETWEEN(:FechaAnterior, CASE WHEN ConsultarCartera."FechaVencimientoOperacion" <= :FechaContable 	
													THEN ConsultarCartera."FechaVencimientoOperacion"
													ELSE :FechaContable							
										END
							   ) * (((ConsultarCartera."TasaCompra"/30)/100)* ConsultarCartera."MontoCompra"), 0 )			AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																			AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" > :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('PRT')
	        AND "IdEstado" = 'VIG'
--	        AND "DocumentoIdentidad"	= :DocumentoIdentidad
	    UNION
	    SELECT DISTINCT
			TO_BIGINT(:IdTipoCentralizacion)                            														AS "IdTipoCentralizacion"	    	
	     	,"IdCartera"																										AS "IdMovimiento"
	     	,'UTILIDAD'																											AS "IdEvento" 
	     	,'Utilidad'																											AS "Nombre"
 			,ABS((ConsultarCartera."PrecioCompra" - ConsultarCartera."INGPrecioMercadoActual") * ConsultarCartera."Cantidad")	AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																				AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" > :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('PRT')
	        AND (ConsultarCartera."PrecioCompra" - ConsultarCartera."INGPrecioMercadoActual") * ConsultarCartera."Cantidad" > 0
	        AND "IdEstado" = 'VIG'
--	        AND "DocumentoIdentidad"	= :DocumentoIdentidad
	    UNION
	    	SELECT DISTINCT
			TO_BIGINT(:IdTipoCentralizacion)                            															AS "IdTipoCentralizacion"	    	
	     	,"IdCartera"																											AS "IdMovimiento"
	     	,'PERDIDA'																												AS "IdEvento" 
	     	,'Perdida'																												AS "Nombre"
 			,ABS((ConsultarCartera."PrecioCompra" - ConsultarCartera."INGPrecioMercadoActual") * ConsultarCartera."Cantidad")		AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																					AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" > :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('PRT')
	        AND (ConsultarCartera."PrecioCompra" - ConsultarCartera."INGPrecioMercadoActual") * ConsultarCartera."Cantidad" < 0
	        AND "IdEstado" = 'VIG'
--	        AND "DocumentoIdentidad"	= :DocumentoIdentidad
		UNION
	    	SELECT DISTINCT
			TO_BIGINT(:IdTipoCentralizacion)                            															AS "IdTipoCentralizacion"	    	
	     	,"IdCartera"																											AS "IdMovimiento"
	     	,'INTERES_DIARIO'																										AS "IdEvento" 
	     	,'Interés Diario'																										AS "Nombre"
 			,ConsultarCartera."INGMontoInteresCompraProximo"																		AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																					AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" 	> :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('EMP','PRT')
	        AND "IdEstado" = 'VIG'
	     UNION
	    	SELECT DISTINCT
			TO_BIGINT(:IdTipoCentralizacion)                            															AS "IdTipoCentralizacion"	    	
	     	,"IdCartera"																											AS "IdMovimiento"
	     	,'PRIMA'																												AS "IdEvento" 
	     	,'Prima'																												AS "Nombre"
 			,ConsultarCartera."INGPrecioAPlazo"																						AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																					AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" 	> :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('EMP','PRT')
	        AND "IdEstado" = 'VIG'
	     UNION
	    	SELECT DISTINCT
			TO_BIGINT(:IdTipoCentralizacion)                            															AS "IdTipoCentralizacion"	    	
	     	,"IdCartera"																											AS "IdMovimiento"	
	     	,'INTERES_ACUMULADO'																									AS "IdEvento" 
	     	,'Interés Acumulado'																									AS "Nombre"
 			,ConsultarCartera."INGMontoInteresCorridoCompraActual"																	AS "MontoEvento"
			,ConsultarCartera."IdMonedaInstrumento"																					AS "CodigoMoneda"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
		WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
			AND ("FechaVencimientoOperacion" 	> :FechaContable AND "EGRCantidad" = 0) 
	        AND "IdClasificacionTipoInstrumento" = 'RV' 
	        AND "IdTipoInstrumento" IN ('EMP','PRT')
	        AND "IdEstado" = 'VIG'