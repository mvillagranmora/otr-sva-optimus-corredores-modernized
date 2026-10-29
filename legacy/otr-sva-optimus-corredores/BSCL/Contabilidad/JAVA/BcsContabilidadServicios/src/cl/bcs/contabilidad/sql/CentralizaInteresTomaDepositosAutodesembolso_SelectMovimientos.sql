SELECT 
	TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
	ConsultarCartera."IdCartera"						AS "IdMovimiento",
	CASE ConsultarCartera."IdTipoOperacion"					
		WHEN 'TDEPLA'	THEN 'ITOMD' 
		WHEN 'TAUTDI'	then 'IAUTO'
	END  												AS "IdTipoMovimiento",
	ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
	ConsultarCartera."FolioCartera"						AS "Numero",
	ConsultarCartera."FolioCartera" 					AS "Linea",
	YEAR(ConsultarCartera."INGFechaOperacion") ||'-'|| RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarCartera."INGFechaOperacion"),2) AS "Fecha",
	ConsultarCartera."DocumentoIdentidad"				AS "DocumentoIdentidad",  
	ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
	TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))			AS "Ejercicio",
	RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."INGFechaOperacion") 	AS "Periodo",
	ABS(ConsultarCartera."INGMontoATasaMercadoActual")	AS "Monto",     
	ConsultarCartera."IdMonedaSistema"					AS "IdMoneda",                                
	ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
	AND (
    	("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" = 0) OR("INGFechaOperacion" <= :FechaContable AND "EGRCantidad" <> 0 AND "INGFechaOperacion" > :FechaContable)
    	)
	AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
   	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF' 	        
--    AND ConsultarCartera."DocumentoIdentidad" 				= :DocumentoIdentidad
    AND ConsultarCartera."IdEstado"							= 'VIG'
    AND ConsultarCartera."INGMontoInteresCompraAnterior" 	<> 0;
       	