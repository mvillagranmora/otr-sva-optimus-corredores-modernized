SELECT DISTINCT
	 TO_BIGINT(:IdTipoCentralizacion)										AS "IdTipoCentralizacion"
	,ConsultarOperacionSpot."IdOperacionUI" 									AS "IdMovimiento"
	,CASE ConsultarOperacionSpot."IdTipoOperacion"
		WHEN 'COMSPT'	THEN 'COMSPT'			
		WHEN 'VENSPT' 	THEN 'VENSPT'
		ELSE '??????????'			
	 END																	AS "IdTipoMovimiento"
	,ConsultarOperacionSpot."IdEmpresa"         							AS "IdEmpresa"									    
	,TO_BIGINT(ConsultarOperacionSpot."IdOperacionUI")						AS "Numero"
	,TO_BIGINT(ConsultarOperacionSpot."FolioOperacion")						AS "Linea"
	,YEAR(ConsultarOperacionSpot."FechaOperacion") ||'-'|| 
	 RIGHT('0' || MONTH(ConsultarOperacionSpot."FechaOperacion"),2) ||'-'|| 
	 RIGHT('0' || DAYOFMONTH(ConsultarOperacionSpot."FechaOperacion"),2) 	AS "Fecha"
	,ConsultarOperacionSpot."RutCliente"									AS "DocumentoIdentidad"  
	,ConsultarOperacionSpot."IdPortafolio"									AS "IdPortafolio"
	,TO_BIGINT(YEAR(ConsultarOperacionSpot."FechaOperacion"))				AS "Ejercicio"
	,RIGHT('0' || MONTH(ConsultarOperacionSpot."FechaOperacion"),2) ||'-'|| 
	 YEAR(ConsultarOperacionSpot."FechaOperacion") 							AS "Periodo"
	,ABS(ConsultarOperacionSpot."MontoMonedaPrincipal")						AS "Monto" 
	,ConsultarOperacionSpot."IdMonedaPrincipal"								AS "IdMoneda"                                               
	,ConsultarOperacionSpot."Estado"										AS "Estado"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ConsultarOperacionSpot
WHERE ConsultarOperacionSpot."IdEmpresa" = :IdEmpresa 
AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) = :FechaContable
AND ConsultarOperacionSpot."Estado" = 'VIG';
