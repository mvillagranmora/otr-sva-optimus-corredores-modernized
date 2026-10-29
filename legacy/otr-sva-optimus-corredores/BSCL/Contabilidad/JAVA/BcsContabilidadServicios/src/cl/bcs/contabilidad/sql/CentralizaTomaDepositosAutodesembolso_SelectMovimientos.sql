
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
	ConsultarCartera."IdCartera"						AS "IdMovimiento",
	CASE ConsultarCartera."IdTipoOperacion"					
		WHEN 'TDEPLA'	THEN 'TOMDE' 
		WHEN 'TAUTDI'	then 'AUTOD'
	END  												AS "IdTipoMovimiento",
	ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
	ConsultarCartera."FolioCartera"						AS "Numero",
	ConsultarCartera."FolioCartera" 					AS "Linea",
	YEAR(ConsultarCartera."FechaOperacion") ||'-'|| RIGHT('0' || MONTH(ConsultarCartera."FechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarCartera."FechaOperacion"),2) AS "Fecha",
	ConsultarCartera."DocumentoIdentidad"				AS "DocumentoIdentidad",  
	ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
	TO_BIGINT(YEAR(ConsultarCartera."FechaOperacion"))				AS "Ejercicio",
	RIGHT('0' || MONTH(ConsultarCartera."FechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."FechaOperacion") 	AS "Periodo",
	ABS(ConsultarCartera."INGMontoAPlazo")					AS "Monto",
	ConsultarCartera."IdMonedaSistema"					AS "IdMoneda",                                 
	ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE 
		ConsultarCartera."IdEmpresa" 					=   :IdEmpresa
	AND	ConsultarCartera."IdEstado"						=	'VIG'
	AND ConsultarCartera."FechaOperacion" 	   			= 	:FechaContable
	AND ConsultarCartera."IdTipoOperacion"				IN ('TDEPLA','TAUTDI')
   	AND ConsultarCartera."IdClasificacionTipoInstrumento" 	= 'IF'; 