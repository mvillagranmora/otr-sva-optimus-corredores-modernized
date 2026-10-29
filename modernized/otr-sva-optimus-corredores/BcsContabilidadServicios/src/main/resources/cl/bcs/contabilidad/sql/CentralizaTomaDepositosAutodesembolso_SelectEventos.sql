SELECT 	 
   TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
   "IdCartera" 										AS "IdMovimiento"
		,CASE ConsultarCartera."IdTipoOperacion"
			WHEN 'TDEPLA' THEN 	'TOTAL_A_PLAZO'				
			ELSE 				'TOTAL_AUTODESEMBOLSO'		
		END 										AS "IdEvento"	
		,CASE ConsultarCartera."IdTipoOperacion"
			WHEN 'TDEPLA' THEN 	'Total a Plazo'		
			ELSE 				'Total Desembolso'
		END 										AS "Nombre"	
		,"INGMontoAPlazo"							AS "MontoEvento"
		,"IdMonedaSistema"							AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		ConsultarCartera."IdEmpresa" 					= 	:IdEmpresa
	AND	ConsultarCartera."IdEstado"						=	'VIG'
	AND ConsultarCartera."FechaOperacion" 	   			= 	:FechaContable
	AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
   	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF'; 

