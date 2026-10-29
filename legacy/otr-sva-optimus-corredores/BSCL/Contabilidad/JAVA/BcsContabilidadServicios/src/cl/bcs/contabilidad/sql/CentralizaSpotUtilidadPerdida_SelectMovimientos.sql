SELECT DISTINCT 
	 TO_BIGINT(:IdTipoCentralizacion)										AS "IdTipoCentralizacion"
	,ConsultarOperacionSpot."IdOperacionSpotOrigen"								AS "IdMovimiento"
	,CASE TRIM(ConsultarOperacionSpot."IdTipoOperacion")
		WHEN 'COMSPT'	THEN 'UTILIDADSP'			
		WHEN 'VENSPT' 	THEN 'PERDIDASPO'
		ELSE '??????????'
	 END																	AS "IdTipoMovimiento"
	,TO_BIGINT(ConsultarOperacionSpot."IdEmpresa")         					AS "IdEmpresa"
	,TO_BIGINT(ConsultarOperacionSpot."IdOperacionSpotOrigen")					AS "Numero"	
	,TO_BIGINT(0)                                        					AS "Linea"	
	,YEAR(ConsultarOperacionSpot."FechaOperacion") ||'-'|| 
	 RIGHT('0' || MONTH(ConsultarOperacionSpot."FechaOperacion"),2) ||'-'|| 
	 RIGHT('0' || DAYOFMONTH(ConsultarOperacionSpot."FechaOperacion"),2) 	AS "Fecha"	
	,PER."DocumentoIdentidad"												AS "DocumentoIdentidad"	 
	,ConsultarOperacionSpot."IdPortafolio"									AS "IdPortafolio"	
	,TO_BIGINT(YEAR(ConsultarOperacionSpot."FechaOperacion"))				AS "Ejercicio"	
	,RIGHT('0' || MONTH(ConsultarOperacionSpot."FechaOperacion"),2) ||'-'|| 
	 YEAR(ConsultarOperacionSpot."FechaOperacion") 							AS "Periodo"
	,ABS(ConsultarOperacionSpot."MontoMonedaPrincipal")						AS "Monto"
	,ConsultarOperacionSpot."IdMonedaPrincipal"								AS "IdMoneda"
	,ConsultarOperacionSpot."Estado"										AS "Estado"	
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" ConsultarOperacionSpot
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
ON ConsultarOperacionSpot."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
ON CLI."IdPersona" = PER."IdPersona"
WHERE ConsultarOperacionSpot."IdEmpresa" = :IdEmpresa 
AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) = :FechaContable 
AND ConsultarOperacionSpot."Estado" = 'VIG'	
	
UNION ALL

SELECT DISTINCT
	 TO_BIGINT(:IdTipoCentralizacion)										AS "IdTipoCentralizacion"
	,"IdResultadoContableSpot" 												AS "IdMovimiento"
	,'MOPOSI'				  												AS "IdTipoMovimiento"	
	,TO_BIGINT(:IdEmpresa)													AS "IdEmpresa"
	,TO_BIGINT("IdResultadoContableSpot")								 	AS "Numero"	
	,TO_BIGINT("IdResultadoContableSpot")									AS "Linea"
	,YEAR("Fecha") ||'-'|| RIGHT('0' || MONTH("Fecha"),2) ||'-'|| 
	 RIGHT('0' || DAYOFMONTH("Fecha"),2) 									AS "Fecha"
	,''						  												AS "DocumentoIdentidad"
	,0						  												AS "IdPortafolio"
	,TO_BIGINT(YEAR("Fecha"))  												AS "Ejercicio"	
	,RIGHT('0' || MONTH("Fecha"),2) ||'-'|| YEAR("Fecha") 					AS "Periodo"
	,"UtilidadPerdidaDolObs"	  											AS "Monto"	
	,:MonedaSistema			  												AS "IdMoneda"	
	,"IdEstado"				  												AS "Estado"
FROM "BSCL.Mesa.BD::SPT.ResultadoContableSpot"
WHERE "IdEstado" = 'ACT' 
AND "Fecha" = :FechaContable;	
	
	