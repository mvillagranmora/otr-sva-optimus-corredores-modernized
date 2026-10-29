SELECT
			 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	     	,"FolioPrestamo"													AS "IdMovimiento"
	     	,'MONTO_PRESTAMO'													AS "IdEvento" 	
	     	,'Monto Préstamo'													AS "Nombre"
 			,"MontoInstrumento" 	 											AS "MontoEvento"
			,"IdMonedaSubyacente"												AS "CodigoMoneda"
FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE
    		"IdEmpresa" 		= :IdEmpresa		AND 
    		"FechaLiquidacion"	= :FechaContable	AND
    		"IdClasificacionTipoInstrumento"	= :IdClasificacionTipoInstrumento
GROUP BY
		"FolioPrestamo"
 		,"MontoInstrumento"
		,"IdMonedaSubyacente"
	    
UNION ALL

SELECT
			 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	     	,"FolioPrestamo"													AS "IdMovimiento"
	     	,'PRIMA'															AS "IdEvento" 	
	     	,'Prima'															AS "Nombre"	     	
	     	,"MontoPrimaOriginal"												AS "MontoEvento"
			,"IdMonedaSubyacente"												AS "CodigoMoneda"
FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE
    		"IdEmpresa" 		= :IdEmpresa		AND 
    		"FechaLiquidacion"	= :FechaContable 	AND
    		"IdClasificacionTipoInstrumento"	= :IdClasificacionTipoInstrumento
GROUP BY
			"FolioPrestamo"
	    	,"MontoPrimaOriginal"
			,"IdMonedaSubyacente";
