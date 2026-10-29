SELECT
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,CAR."FolioCartera"													AS "IdMovimiento"
	    ,'MONTO_PRESTAMO'													AS "IdEvento" 	
	    ,'Monto Préstamo'													AS "Nombre"
 		,CAR."MontoCompra"													AS "MontoEvento"
		,SUB."IdMonedaInstrumento"											AS "CodigoMoneda"
 FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
 LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
 WHERE
    	CAR."IdEmpresa" 					= :IdEmpresa		AND 
    	CAR."FechaLiquidacion"	 			= :FechaContable 	AND
    	CAR."FechaRespaldo"					= :FechaContable	AND
    	SUB."FechaRespaldo"					= :FechaContable	AND
    	CAR."IdEstado"						= 'VIG'				AND
    	CAR."IdTipoInstrumento"				IN ('PRT','PRF')	AND
    	CAR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento   	
 GROUP BY 	    
    	CAR."FolioCartera"
 		,CAR."MontoCompra"
		,SUB."IdMonedaInstrumento"
		
UNION ALL

SELECT
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,CAR."FolioCartera"													AS "IdMovimiento"
	    ,'PRIMA'															AS "IdEvento" 	
	    ,'Prima'															AS "Nombre"
 		,CAR."PrecioAPlazo"													AS "MontoEvento"
		,SUB."IdMonedaInstrumento"											AS "CodigoMoneda"
 FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
 LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
 WHERE
    	CAR."IdEmpresa" 					= :IdEmpresa		AND 
    	CAR."FechaLiquidacion"	 			= :FechaContable 	AND
    	CAR."FechaRespaldo"					= :FechaContable	AND
    	SUB."FechaRespaldo"					= :FechaContable	AND
    	CAR."IdEstado"						= 'VIG'				AND
    	CAR."IdTipoInstrumento"				IN ('PRT','PRF')	AND
    	CAR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento   	
 GROUP BY 	    
    	CAR."FolioCartera"
 		,CAR."PrecioAPlazo"
		,SUB."IdMonedaInstrumento";