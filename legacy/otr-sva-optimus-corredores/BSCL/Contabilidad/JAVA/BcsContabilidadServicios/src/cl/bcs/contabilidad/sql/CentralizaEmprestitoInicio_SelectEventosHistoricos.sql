SELECT  
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,CAR."FolioCartera"													AS "IdMovimiento"
	    ,'MONTO_EMPRESTITO'													AS "IdEvento" 	
	    ,'Monto Empréstito'													AS "Nombre"
 		,CAR."MontoCompra"													AS "MontoEvento"
		,SUB."IdMonedaInstrumento"											AS "CodigoMoneda"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente"= CAR."IdCliente" 
WHERE
    	CAR."IdEmpresa" 					= :IdEmpresa		AND 
    	CAR."FechaLiquidacion"	 			= :FechaContable 	AND
    	CAR."FechaRespaldo"					= :FechaContable	AND
    	SUB."FechaRespaldo"					= :FechaContable	AND
    	CAR."IdEstado"						= 'VIG'				AND
    	CAR."IdTipoInstrumento" 			IN ('EMP','ERF')	AND
    	CAR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
GROUP BY 
		CAR."FolioCartera"
    	,CAR."MontoCompra"
    	,SUB."IdMonedaInstrumento"
	    
UNION ALL

SELECT   
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,CAR."FolioCartera"													AS "IdMovimiento"
	    ,'PREMIO'															AS "IdEvento" 	
	    ,'Premio'															AS "Nombre"
 		,CAR."INGPrecioAPlazo"												AS "MontoEvento"
		,SUB."IdMonedaInstrumento"											AS "CodigoMoneda"
 FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
 LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente"= CAR."IdCliente" 
 WHERE
    	CAR."IdEmpresa" 					= :IdEmpresa		AND 
    	CAR."FechaLiquidacion"	 			= :FechaContable 	AND
    	CAR."FechaRespaldo"					= :FechaContable	AND
    	SUB."FechaRespaldo"					= :FechaContable	AND
    	CAR."IdEstado"						= 'VIG'				AND
    	CAR."IdTipoInstrumento" 			IN ('EMP','ERF')	AND
    	CAR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
 GROUP BY 
    	CAR."FolioCartera"
    	,CAR."INGPrecioAPlazo"
    	,SUB."IdMonedaInstrumento";