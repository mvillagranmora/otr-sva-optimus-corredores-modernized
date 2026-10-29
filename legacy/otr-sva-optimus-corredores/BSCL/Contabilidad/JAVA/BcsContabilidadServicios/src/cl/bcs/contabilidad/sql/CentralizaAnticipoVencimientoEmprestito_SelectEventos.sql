	SELECT   
			 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	     	,EMP."IdCartera"	 												AS "IdMovimiento"
	     	,CASE "IdEstado"
				WHEN 'VCD'			THEN 'MONTO_INICIO_VENCIMIENTO_EMPRESTITO'			
				WHEN 'ANT'			THEN 'MONTO_INICIO_ANTICIPO_EMPRESTITO'			
				ELSE 'ERROR'			
			END 																AS "IdEvento" 
	     	
	     	,CASE "IdEstado"
				WHEN 'VCD'			THEN 'Monto Vencimiento Emprestito al inicio'			
				WHEN 'ANT'			THEN 'Monto Anticipo Emprestito al inicio'			
				ELSE 'ERROR'			
			END 																AS "Nombre"
			,EMP."EGRMonto" 													AS "MontoEvento"
			,EMP."IdMonedaSubyacente"											AS "CodigoMoneda"
	FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico" EMP
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C on C."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema' 
	WHERE
	    	EMP."IdEmpresa" 		= :IdEmpresa		AND 
	    	EMP."EGRFechaOperacion" = :FechaContable 	AND
	    	EMP."IdEstado"			IN ('VCD','ANT')	AND
	    	EMP."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
	
	GROUP BY EMP."FolioEmprestito"
			,EMP."IdCartera"
	    	,EMP."IdEmpresa"
	    	,EMP."DocumentoIdentidad"
	    	,EMP."IdPortafolio"
	    	,EMP."EGRFechaOperacion"
	    	,EMP."EGRMonto"
	    	,EMP."IdMonedaSubyacente"
	    	,EMP."IdEstado"
	    	
	UNION ALL
	
	
	SELECT    
			 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
	     	,EMP."IdCartera" 													AS "IdMovimiento"
	     	,CASE "IdEstado"
				WHEN 'VCD'			THEN 'MONTO_FINAL_VENCIMIENTO_EMPRESTITO'			
				WHEN 'ANT'			THEN 'MONTO_FINAL_ANTICIPO_EMPRESTITO'			
				ELSE 'ERROR'			
			END 																AS "IdEvento" 
	     	
	     	,CASE "IdEstado"
				WHEN 'VCD'			THEN 'Monto Vencido (incluye prima)'			
				WHEN 'ANT'			THEN 'Monto Anticipado (incluye prima)'			
				ELSE 'Error'			
			END 																AS "Nombre"
			,EMP."EGRMontoAPlazo" 												AS "MontoEvento"
			,EMP."IdMonedaSubyacente"											AS "CodigoMoneda"
	FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico" EMP
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C on C."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema' 
	WHERE
	    	EMP."IdEmpresa" 		= :IdEmpresa		AND 
	    	EMP."EGRFechaOperacion" = :FechaContable 	AND
	    	EMP."IdEstado"			IN ('VCD','ANT')	AND
	    	EMP."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
	
	GROUP BY 
			EMP."FolioEmprestito"
			,EMP."IdCartera"
	    	,EMP."EGRMontoAPlazo"
	    	,EMP."EGRFechaOperacion"
	    	,EMP."IdMonedaSubyacente"
	    	,EMP."IdEstado"
	
	UNION ALL
	
	SELECT   
				 TO_BIGINT(:IdTipoCentralizacion)                            		AS "IdTipoCentralizacion"	    
		     	,EMP."IdCartera"													AS "IdMovimiento"
		     	,'PREMIO'															AS "IdEvento" 	
		     	,'Premio'															AS "Nombre"
	 			,EMP."EGRMontoAPlazo" - EMP."EGRMonto"								AS "MontoEvento"
				,EMP."IdMonedaSubyacente"											AS "CodigoMoneda"
	
	 FROM  "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico"  EMP
	 LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C on C."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema' 
	 WHERE
	    	EMP."IdEmpresa" 		= :IdEmpresa		AND 
	    	EMP."EGRFechaOperacion" = :FechaContable 	AND
	    	EMP."IdEstado"			IN ('VCD','ANT')	AND
	    	EMP."IdClasificacionTipoInstrumento"= :ClasificacionTipoInstrumento
	
	 GROUP BY 
			EMP."FolioEmprestito"
			,EMP."IdCartera"
	    	,EMP."EGRMontoAPlazo"
	    	,EMP."EGRMonto"
	    	,EMP."EGRFechaOperacion"
	    	,EMP."IdMonedaSubyacente";