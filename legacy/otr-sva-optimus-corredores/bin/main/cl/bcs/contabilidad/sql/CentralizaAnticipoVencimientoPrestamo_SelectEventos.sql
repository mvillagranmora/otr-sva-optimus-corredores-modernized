SELECT    
		 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
     	,PRT."IdCartera"	 												AS "IdMovimiento"
     	,CASE PRT."IdEstado"
			WHEN 'VCD'			THEN 'MONTO_INICIO_VENCIMIENTO_PRESTAMO'			
			WHEN 'ANT'			THEN 'MONTO_INICIO_ANTICIPO_PRESTAMO'			
			ELSE 'ERROR'			
		END 																AS "IdEvento" 
     	
     	,CASE "IdEstado"
			WHEN 'VCD'			THEN 'Monto Vencimiento Prestamo al inicio'			
			WHEN 'ANT'			THEN 'Monto Anticipo Prestamo al inicio'			
			ELSE 'ERROR'			
		END 																AS "Nombre"
		,PRT."EGRMonto"			 											AS "MontoEvento"
		,PRT."IdMonedaSubyacente"											AS "CodigoMoneda"
FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" PRT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C on C."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema' 
WHERE
    	PRT."IdEmpresa" 		= :IdEmpresa		AND 
    	PRT."EGRFechaOperacion" = :FechaContable 	AND
    	PRT."IdEstado"			IN ('VCD','ANT')	AND
    	PRT."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
    	
UNION ALL

SELECT    
		 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
     	,PRT."IdCartera"	 												AS "IdMovimiento"
     	,CASE PRT."IdEstado"
			WHEN 'VCD'			THEN 'MONTO_FINAL_VENCIMIENTO_PRESTAMO'			
			WHEN 'ANT'			THEN 'MONTO_FINAL_ANTICIPO_PRESTAMO'			
			ELSE 'ERROR'			
		END 																AS "IdEvento" 
     	
     	,CASE PRT."IdEstado"
			WHEN 'VCD'			THEN 'Monto Vencido (incluye prima)'			
			WHEN 'ANT'			THEN 'Monto Anticipado (incluye prima)'			
			ELSE 'ERROR'			
		END 																AS "Nombre"
		,PRT."EGRMontoAPlazo"												AS "MontoEvento"
		,PRT."IdMonedaSubyacente"											AS "CodigoMoneda"
FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" PRT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C on C."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema' 
WHERE
    	PRT."IdEmpresa" 		= :IdEmpresa		AND 
    	PRT."EGRFechaOperacion" = :FechaContable 	AND
    	PRT."IdEstado"			IN ('VCD','ANT')	AND
    	PRT."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
        	
UNION ALL

SELECT   
	 	TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
     	,PRT."IdCartera"	 												AS "IdMovimiento"
     	,'PRIMA'															AS "IdEvento" 	
     	,'Prima'					  										AS "Nombre"
		,PRT."EGRMontoAPlazo" - PRT."EGRMonto"								AS "MontoEvento"
		,PRT."IdMonedaSubyacente"											AS "CodigoMoneda"
FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico"  PRT
WHERE
    	PRT."IdEmpresa" 		= :IdEmpresa		AND 
    	PRT."EGRFechaOperacion" = :FechaContable 	AND
    	PRT."IdEstado"			IN ('VCD','ANT')	AND
    	PRT."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento;