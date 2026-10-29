SELECT DISTINCT  
	 TO_BIGINT(:IdTipoCentralizacion) 						AS "IdTipoCentralizacion"
	,"IdOperacionUI"											AS "IdMovimiento" 
	,CASE ConsultarOperacionSpot."IdTipoOperacion"	
	WHEN 'COMSPT' 	THEN CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'MONEDA_PRINCIPAL_SPOT_COMPRA_CLP' ELSE 'MONEDA_PRINCIPAL_SPOT_COMPRA' END
						ELSE CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'MONEDA_PRINCIPAL_SPOT_VENTA_CLP' ELSE 'MONEDA_PRINCIPAL_SPOT_VENTA' END							
	END						 								AS "IdEvento"
	,CASE ConsultarOperacionSpot."IdTipoOperacion"
	 	WHEN 'COMSPT'   THEN CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'Monto Moneda Principal Spot Compra' ELSE 'Valor Moneda Principal Spot Compra' END
		 				ELSE CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'Monto Moneda Principal Spot Venta' ELSE 'Valor Moneda Principal Spot Venta' END
		END 												AS "Nombre" 
	,"MontoMonedaPrincipal" 								AS "MontoEvento" 
	,"IdMonedaPrincipal"									AS "CodigoMoneda"	 
FROM 
	"BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
WHERE 
		ConsultarOperacionSpot."IdEmpresa" 					= :IdEmpresa 
	AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) 	= :FechaContable 
    AND ConsultarOperacionSpot."Estado" 					= 'VIG'
    
UNION ALL

SELECT DISTINCT
	 TO_BIGINT(:IdTipoCentralizacion) 						AS "IdTipoCentralizacion"
	,"IdOperacionUI"											AS "IdMovimiento" 
	,CASE ConsultarOperacionSpot."IdTipoOperacion"			
		WHEN 'COMSPT' 	THEN CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'MONEDA_SECUNDARIA_SPOT_COMPRA_CLP' ELSE 'MONEDA_SECUNDARIA_SPOT_COMPRA' END
						ELSE CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'MONEDA_SECUNDARIA_SPOT_VENTA_CLP' ELSE 'MONEDA_SECUNDARIA_SPOT_VENTA' END			
	END						 								AS "IdEvento"
	,CASE ConsultarOperacionSpot."IdTipoOperacion"
	 	WHEN 'COMSPT'   THEN CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'Monto Moneda Secundaria Spot Compra' ELSE 'Valor Moneda Secundaria Spot Compra' END
		 				ELSE CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'Monto Moneda Secundaria Spot Venta' ELSE 'Valor Moneda Secundaria Spot Venta' END
		END 												AS "Nombre" 
	,"MontoMonedaSecundaria" 								AS "MontoEvento"	 
	,"IdMonedaSecundaria"									AS "CodigoMoneda"	 
FROM 
	"BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
WHERE 
		ConsultarOperacionSpot."IdEmpresa" 					= :IdEmpresa 
	AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) 	= :FechaContable 
    AND ConsultarOperacionSpot."Estado" 					= 'VIG'
    
UNION ALL

SELECT DISTINCT
	 TO_BIGINT(:IdTipoCentralizacion) 						AS "IdTipoCentralizacion"
	,"IdOperacionUI"											AS "IdMovimiento" 
	,CASE ConsultarOperacionSpot."IdTipoOperacion"		
		WHEN 'COMSPT' 	THEN CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'MONEDA_PRINCIPAL_EQUIVAL_SPOT_COMPRA_CLP' ELSE 'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_COMPRA' END
						ELSE CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'MONEDA_PRINCIPAL_EQUIVAL_SPOT_VENTA_CLP' ELSE 'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_VENTA' END					
	END						 								AS "IdEvento"
	,CASE ConsultarOperacionSpot."IdTipoOperacion"
	 	WHEN 'COMSPT'   THEN CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'Monto Moneda Principal Equivalente Spot Compra' ELSE 'Valor Moneda Principal Equivalente Spot Compra' END
		 				ELSE CASE ConsultarOperacionSpot."IdMonedaPrincipal" WHEN 'CLP' THEN 'Monto Moneda Principal Equivalente Spot Venta' ELSE 'Valor Moneda Principal Equivalente Spot Venta' END
		END 												AS "Nombre"
	,"MontoMonedaPrincipalEquivalente" 						AS "MontoEvento"	 
	,"IdMonedaPrincipal"									AS "CodigoMoneda"	 
FROM 
	"BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
WHERE 
		ConsultarOperacionSpot."IdEmpresa" 					= :IdEmpresa 
	AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) 	= :FechaContable 
    AND ConsultarOperacionSpot."Estado" 					= 'VIG'

UNION ALL

SELECT DISTINCT
	 TO_BIGINT(:IdTipoCentralizacion) 											AS "IdTipoCentralizacion"
	,"IdOperacionUI"																AS "IdMovimiento" 
	,CASE ConsultarOperacionSpot."IdTipoOperacion"		
	WHEN 'COMSPT' 	THEN CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'MONEDA_SECUNDARIA_EQUIVAL_SPOTCOMPRA_CLP' ELSE 'MONEDA_SECUNDARIA_EQUIVALENTE_SPOTCOMPRA' END
						ELSE CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'MONEDA_SECUNDARIA_EQUIVAL_SPOT_VENTA_CLP' ELSE 'MONEDA_SECUNDARIA_EQUIVALENTE_SPOT_VENTA' END	
	END						 													AS "IdEvento"
	,CASE ConsultarOperacionSpot."IdTipoOperacion"
	 	WHEN 'COMSPT'   THEN CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'Monto Moneda Secundaria Equivalente Spot Compra' ELSE 'Valor Moneda Secundaria Equivalente Spot Compra' END
		 				ELSE CASE ConsultarOperacionSpot."IdMonedaSecundaria" WHEN 'CLP' THEN 'Monto Moneda Secundaria Equivalente Spot Venta' ELSE 'Valor Moneda Secundaria Equivalente Spot Venta' END
		END 																	AS "Nombre"
	,"MontoEquivalente" 														AS "MontoEvento"	 
	,"IdMonedaSecundaria"														AS "CodigoMoneda"	 
FROM 
	"BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
WHERE 
		ConsultarOperacionSpot."IdEmpresa" 					= :IdEmpresa 
	AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) 	= :FechaContable 
    AND ConsultarOperacionSpot."Estado" 					= 'VIG';