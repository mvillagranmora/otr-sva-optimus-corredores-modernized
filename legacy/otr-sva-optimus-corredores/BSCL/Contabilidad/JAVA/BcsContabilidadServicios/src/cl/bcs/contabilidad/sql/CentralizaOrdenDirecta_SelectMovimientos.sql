SELECT
			TO_BIGINT(:IdTipoCentralizacion)											AS "IdTipoCentralizacion",
			Tran."IdTransaccion"														AS "IdMovimiento",
			IFNULL(CONCAT(Tran."IdClasificacionTipoInstrumento",Tran."IdTipoOperacion"),'')		AS "IdTipoMovimiento",  
			Tran."IdEmpresa"         													AS "IdEmpresa",									    
--			Tran."IdTransaccion"														AS "Numero",
			TO_BIGINT("FolioTransaccion")												AS "Numero",
			Tran."IdTransaccion" 														AS "Linea",
			YEAR(Tran."FechaOperacion") ||'-'|| 
			RIGHT('0' || MONTH(Tran."FechaOperacion"),2) ||'-'|| 
			RIGHT('0' || DAYOFMONTH(Tran."FechaOperacion"),2) 							AS "Fecha",
			''																			AS "DocumentoIdentidad",  
			TO_BIGINT(0)																AS "IdPortafolio",			
			TO_BIGINT(YEAR(Tran."FechaOperacion"))										AS "Ejercicio",
			RIGHT('0' || MONTH(Tran."FechaOperacion"),2) ||'-'|| 
			YEAR(Tran."FechaOperacion") 												AS "Periodo",
			ABS(Tran."MontoTransaccion")												AS "Monto",
			Tran."IdMonedaTransaccion"													AS "IdMoneda",                                 
			Tran."IdEstadoTransaccion"													AS "Estado"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		Tran 
		WHERE 
				Tran."IdEmpresa" 			= :IdEmpresa
			AND Tran."FechaOperacion" 		= :FechaContable
			AND Tran."IndicadorOrdenDirecta" IN ('OD','OE')
			AND Tran."IdEstadoTransaccion"	= 'ING'
		
UNION ALL
		SELECT
			TO_BIGINT(:IdTipoCentralizacion)											AS "IdTipoCentralizacion",
			Tran."IdTransaccion"														AS "IdMovimiento",
			IFNULL(CONCAT(Tran."IdClasificacionTipoInstrumento",Tran."IdTipoOperacion"),'')		AS "IdTipoMovimiento",  
			Tran."IdEmpresa"         													AS "IdEmpresa",									    
--			Tran."IdTransaccion"														AS "Numero",
			TO_BIGINT("FolioTransaccion")												AS "Numero",
			Tran."IdTransaccion" 														AS "Linea",
			YEAR(Tran."FechaOperacion") ||'-'|| 
			RIGHT('0' || MONTH(Tran."FechaOperacion"),2) ||'-'|| 
			RIGHT('0' || DAYOFMONTH(Tran."FechaOperacion"),2) 							AS "Fecha",
			''																			AS "DocumentoIdentidad",  
			TO_BIGINT(0)																AS "IdPortafolio",			
			YEAR(Tran."FechaOperacion")													AS "Ejercicio",
			RIGHT('0' || MONTH(Tran."FechaOperacion"),2) ||'-'|| 
			TO_BIGINT(YEAR(Tran."FechaOperacion"))										AS "Periodo",
			ABS(Tran."MontoTransaccion")												AS "Monto",
			Tran."IdMonedaTransaccion"													AS "IdMoneda",                                 
			Tran."IdEstadoTransaccion"													AS "Estado"
		FROM  "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"		Tran 
		WHERE 
				Tran."IdEmpresa" 			= :IdEmpresa
			AND Tran."FechaOperacion" 		= :FechaContable
			AND Tran."IndicadorOrdenDirecta" IN ('OD','OE')
			AND Tran."IdEstadoTransaccion"	= 'ING'	;
