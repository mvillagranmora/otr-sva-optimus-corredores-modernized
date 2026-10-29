SELECT 
	TO_BIGINT(:IdTipoCentralizacion)                    						AS "IdTipoCentralizacion",
	ConsultarCartera."IdCartera"												AS "IdMovimiento",
	CASE ConsultarCartera."EGRIdConcepto"
		WHEN 'VENPRT'	THEN 'VENPREST'			
		WHEN 'VENEMP' 	THEN 'VENEMPRE'
		WHEN 'ANTPRT'	THEN 'ANTPREST'			
		WHEN 'ANTEMP' 	THEN 'ANTEMPRE'
		ELSE '??????????'			
	END																			AS "IdTipoMovimiento",
	ConsultarCartera."IdEmpresa"         										AS "IdEmpresa",									    
	ConsultarCartera."FolioCartera"												AS "Numero",
	ConsultarCartera."FolioCartera" 											AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 									AS "Fecha",
	ConsultarCartera."DocumentoIdentidad"										AS "DocumentoIdentidad",  
	ConsultarCartera."IdPortafolio"												AS "IdPortafolio",
	TO_BIGINT(YEAR(:FechaContable))												AS "Ejercicio",
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	TO_BIGINT(YEAR(:FechaContable))												AS "Periodo",
--	ABS(ConsultarCartera."EGRMonto")											AS "Monto",
	CASE ConsultarCartera."EGRIdConcepto"
		WHEN 'VENPRT' THEN ABS("EGRMonto")
		WHEN 'VENEMP' THEN ABS("EGRMonto")
		WHEN 'ANTPRT' THEN ABS("EGRMonto")--ABS("EGRMontoARecibirAnticipo")
		WHEN 'ANTEMP' THEN ABS("EGRMonto")--ABS("EGRMontoARecibirAnticipo")
		ELSE 0			
	  END																		AS "Monto",
	ConsultarCartera."IdMonedaSistema"											AS "IdMoneda",                        
	ConsultarCartera."IdEstado"													AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 			= :IdEmpresa 
	AND "EGRFechaOperacion" 	   			= :FechaContable
	AND "EGRIdConcepto"						IN ('VENPRT','VENEMP','ANTPRT','ANTEMP')
    AND "IdClasificacionTipoInstrumento" 	= 'RV' 
    AND "IdTipoInstrumento" 				IN ('PRT','EMP');