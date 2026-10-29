SELECT DISTINCT
	TO_BIGINT(:IdTipoCentralizacion)                    						AS "IdTipoCentralizacion",
	ConsultarCartera."IdCartera"												AS "IdMovimiento",
	CASE ConsultarCartera."IdTipoInstrumento"
		WHEN 'PRT'	THEN 'PRESTAMO'			
		WHEN 'EMP'  THEN 'EMPRESTITO'
		ELSE '????????????'			
	END																			AS "IdTipoMovimiento",
	ConsultarCartera."IdEmpresa"         										AS "IdEmpresa",									    
	ConsultarCartera."FolioCartera"												AS "Numero",
	ConsultarCartera."FolioCartera" 											AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 									AS "Fecha",
	ConsultarCartera."DocumentoIdentidad"										AS "DocumentoIdentidad",  
	ConsultarCartera."IdPortafolio"												AS "IdPortafolio",
	TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))						AS "Ejercicio",
	RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| 
	TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))						AS "Periodo",
	ABS(ConsultarCartera."INGMontoATasaCompraActual")							AS "Monto",             
	ConsultarCartera."IdMonedaSistema"											AS "IdMoneda",                        
	ConsultarCartera."IdEstado"													AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" 				= :IdEmpresa 
	AND ("FechaVencimientoOperacion" 			> :FechaContable AND "EGRCantidad" = 0) 
    AND "IdClasificacionTipoInstrumento" 		= 'RV' 
    AND "IdTipoInstrumento" 					IN ('PRT','EMP')
    AND "IdEstado"								= 'VIG';
--    AND ConsultarCartera."DocumentoIdentidad"	= :DocumentoIdentidad;