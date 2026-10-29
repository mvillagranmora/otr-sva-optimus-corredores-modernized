SELECT  
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,"FolioEmprestito"													AS "IdMovimiento"
	    ,'MONTO_EMPRESTITO'													AS "IdEvento" 	
	    ,'Monto Empréstito'													AS "Nombre"
 		,"MontoInstrumento"													AS "MontoEvento"
		,"IdMonedaSubyacente"												AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" 
WHERE
    	"IdEmpresa" 		= :IdEmpresa		AND 
    	"FechaLiquidacion"	= :FechaContable 	AND
    	"IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
GROUP BY 
		"FolioEmprestito"
    	,"IdEmpresa"
    	,"MontoInstrumento"
    	,"IdMonedaSubyacente"
	    
UNION ALL

SELECT   
		TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	    ,"FolioEmprestito"													AS "IdMovimiento"
	    ,'PREMIO'															AS "IdEvento" 	
	    ,'Premio'															AS "Nombre"
 		,"INGMontoPremio"													AS "MontoEvento"
		,"IdMonedaSubyacente"												AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" 
WHERE
    	"IdEmpresa" 		= :IdEmpresa		AND 
    	"FechaLiquidacion"	= :FechaContable 	AND
    	"IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
GROUP BY 
		"FolioEmprestito"
    	,"IdEmpresa"
    	,"INGMontoPremio"
    	,"IdMonedaSubyacente";