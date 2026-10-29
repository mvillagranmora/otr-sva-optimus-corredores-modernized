SELECT 
TO_BIGINT(:IdTipoCentralizacion)										AS "IdTipoCentralizacion",	
ConsultarGarantias."IdGarantia"											AS "IdMovimiento",
CASE WHEN ConsultarGarantias."EGRCantidad" = 0 
		THEN 'INGRESOGTA'
		ELSE 'EGRESOGTA' 
	END																	AS "IdTipoMovimiento",
ConsultarGarantias."IdEmpresa"         									AS "IdEmpresa",									    
ConsultarGarantias."FolioGarantia"										AS "Numero",
TO_BIGINT(0)                                        					AS "Linea",
CASE WHEN ConsultarGarantias."EGRCantidad" = 0
		THEN YEAR(ConsultarGarantias."INGFechaOperacion") 	 ||'-'|| RIGHT('0' || 
			 MONTH(ConsultarGarantias."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || 
			 DAYOFMONTH(ConsultarGarantias."INGFechaOperacion"),2) 						
		ELSE YEAR(ConsultarGarantias."EGRFechaOperacion") 	 ||'-'|| RIGHT('0' || 
			 MONTH(ConsultarGarantias."EGRFechaOperacion"),2) ||'-'|| RIGHT('0' || 
			 DAYOFMONTH(ConsultarGarantias."EGRFechaOperacion"),2) 						
	END																	AS "Fecha",
ConsultarGarantias."DocumentoIdentidad"									AS "DocumentoIdentidad",  
ConsultarGarantias."IdPortafolio"										AS "IdPortafolio",
TO_BIGINT(YEAR(ConsultarGarantias."INGFechaOperacion"))					AS "Ejercicio",
RIGHT('0' || MONTH(ConsultarGarantias."INGFechaOperacion"),2) ||'-'|| 
YEAR(ConsultarGarantias."INGFechaOperacion") 							AS "Periodo",			
"IdMonedaSistema"														AS "IdMoneda",
--ABS(ConsultarGarantias."INGMontoATasaCompraActual") 					AS "Monto",
--ABS(ConsultarGarantias."INGMonto") 										AS "Monto",
ABS(ConsultarGarantias."MontoGarantia") 								AS "Monto",
ConsultarGarantias."IdEstado"											AS "Estado"
				
	
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
WHERE "IdEmpresa" = :IdEmpresa 
AND	(("INGFechaOperacion" = :FechaContable  AND "EGRCantidad" = 0) OR ("EGRFechaOperacion" = :FechaContable  AND "EGRCantidad" <> 0))
--AND (ABS(ConsultarGarantias."INGMontoATasaCompraActual") <> 0 OR ABS(ConsultarGarantias."INGMontoATasaMercadoActual") <> 0)
AND (ABS(ConsultarGarantias."MontoGarantia") <> 0)
--AND ConsultarGarantias."IdMonedaSistema" = ConsultarGarantias."IdMonedaInstrumento"
AND "DocumentoIdentidad" = :DocumentoIdentidad;	
